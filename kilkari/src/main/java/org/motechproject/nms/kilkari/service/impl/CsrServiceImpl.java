package org.motechproject.nms.kilkari.service.impl;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.kilkari.domain.CallRetry;
import org.motechproject.nms.kilkari.domain.CallStage;
import org.motechproject.nms.kilkari.domain.CallSummaryRecord;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.exception.InvalidCallRecordDataException;
import org.motechproject.nms.kilkari.exception.NoSuchSubscriptionException;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.CallSummaryRecordDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.service.CsrService;
import org.motechproject.nms.kilkari.service.CsrVerifierService;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.RequestId;
import org.motechproject.nms.props.domain.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jdo.Query;
import java.util.List;

import static java.lang.Math.min;


@Service("csrService")
public class CsrServiceImpl implements CsrService {

    private static final String NMS_IMI_KK_PROCESS_CSR = "nms.imi.kk.process_csr";
    private static final int MAX_CHAR_ALERT = 4900;

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrServiceImpl.class);

    private CallSummaryRecordDataService csrDataService;
    private SubscriptionDataService subscriptionDataService;
    private CallRetryDataService callRetryDataService;
    private AlertService alertService;
    private CsrVerifierService csrVerifierService;

    @Autowired
    public CsrServiceImpl(CallSummaryRecordDataService csrDataService, SubscriptionDataService subscriptionDataService,
                          CallRetryDataService callRetryDataService, AlertService alertService,
                          CsrVerifierService csrVerifierService) {
        this.csrDataService = csrDataService;
        this.subscriptionDataService = subscriptionDataService;
        this.callRetryDataService = callRetryDataService;
        this.alertService = alertService;
        this.csrVerifierService = csrVerifierService;
    }


    private void completeSubscriptionIfNeeded(Subscription subscription, CallSummaryRecord record) {

        resetWelcomeFlagInSubscription(subscription);

        if (!subscription.isLastPackMessage(record.getContentFileName())) {
            // This subscription has not completed, do nothing
            return;
        }

        // Mark the subscription completed
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscriptionDataService.update(subscription);

    }


    // Check if this call has been failing with OBD_FAILED_INVALIDNUMBER for all the retries.
    // See issue #169: https://github.com/motech-implementations/mim/issues/169
    private boolean isMsisdnInvalid(Subscription subscription, CallSummaryRecord csr) {

        return csr.getInvalidNumberCount() > subscription.getSubscriptionPack().retryCount();

    }


    private void deleteCallRetryRecordIfNeeded(CallRetry callRetry) {
        if (callRetry == null) {
            return;
        }
        callRetryDataService.delete(callRetry);
    }


    private void rescheduleCall(Subscription subscription, CallSummaryRecord csr, CallRetry callRetry) {

        Long msisdn = subscription.getSubscriber().getCallingNumber();

        // This message was never retried before, so this is a first retry
        if (callRetry == null) {

            String action = String.format("Creating retry for subscription: %s", subscription.getSubscriptionId());
            try {
                // This message was never retried before, so this is a first retry
                LOGGER.debug(action);

                CallRetry newCallRetry = new CallRetry(
                        subscription.getSubscriptionId(),
                        msisdn,
                        CallStage.RETRY_1,
                        csr.getContentFileName(),
                        csr.getWeekId(),
                        csr.getLanguageCode(),
                        csr.getCircleName(),
                        subscription.getOrigin()
                );
                callRetryDataService.create(newCallRetry);
            } catch (DataIntegrityViolationException e) {
                String msg = String.format("Exception while trying to reschedule call for subscription %s: %s",
                        subscription.getSubscriptionId(), e);
                LOGGER.error(msg);
                alertService.create("rescheduleCall()", action, msg, AlertType.HIGH, AlertStatus.NEW, 0, null);
            }
            return;
        }


        // This call was retried for a different week but never removed from the CallRetry table which means we never
        // got a CDR from IMI, so let's warn about this and still try to reschedule as a first try it for this week.
        if (!callRetry.getWeekId().equals(csr.getWeekId())) {

            String message = String.format("CallRetry record (id %d) for subscription %s for weekId %s was never " +
                            "deleted, which means we never received a CDR about it. I'm cleaning it up.",
                    callRetry.getId(), callRetry.getSubscriptionId(), callRetry.getWeekId());
            LOGGER.info(message);
            callRetry.setCallStage(CallStage.RETRY_1);
            callRetry.setWeekId(csr.getWeekId());
            callRetryDataService.update(callRetry);
            return;
        }


        // We've already rescheduled this call, let's see if it needs to be re-rescheduled
        if ((subscription.getSubscriptionPack().retryCount() == 1) ||
                (callRetry.getCallStage() == CallStage.RETRY_LAST)) {

            // Nope, this call should not be retried

            // Deactivate subscription for persistent invalid numbers
            // See https://github.com/motech-implementations/mim/issues/169
            if (isMsisdnInvalid(subscription, csr)) {
                subscription.setStatus(SubscriptionStatus.DEACTIVATED);
                subscription.setDeactivationReason(DeactivationReason.INVALID_NUMBER);
                subscriptionDataService.update(subscription);
            }

            deleteCallRetryRecordIfNeeded(callRetry);

            // Does subscription need to be marked complete, even if we failed to send the last message?
            completeSubscriptionIfNeeded(subscription, csr);
            return;
        }

        // Re-reschedule the call
        LOGGER.debug(String.format("Updating retry entry for subscription: %s", subscription.getSubscriptionId()));
        callRetry.setCallStage(callRetry.getCallStage().nextStage());
        callRetryDataService.update(callRetry);
    }


    private void deactivateSubscription(Subscription subscription, CallRetry callRetry) {

        if (subscription.getOrigin() == SubscriptionOrigin.IVR) {
            String error = String.format("Subscription %s was rejected (DND) but its origin is IVR, not MCTS!",
                    subscription.getSubscriptionId());
            LOGGER.error(error);
            alertService.create(subscription.getSubscriptionId(), "subscription", error, AlertType.CRITICAL,
                    AlertStatus.NEW, 0, null);
            return;
        }

        deleteCallRetryRecordIfNeeded(callRetry);

        //Deactivate the subscription
        subscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscription.setDeactivationReason(DeactivationReason.DO_NOT_DISTURB);
        subscriptionDataService.update(subscription);
    }


    private List<CallSummaryRecord> findOldCallSummaryRecords(final String subscriptionId, final String weekId) {
        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<CallSummaryRecord>> queryExecution = new SqlQueryExecution<List<CallSummaryRecord>>() {

            @Override
            public String getSqlQuery() {
                String query = String.format(
                        "SELECT * FROM nms_kk_summary_records " +
                                "WHERE subscriptionId like '%%%s' " +
                                "ORDER BY weekId, subscriptionId DESC",
                        subscriptionId, weekId);
                return query;
            }

            @Override
            public List<CallSummaryRecord> execute(Query query) {

                query.setClass(CallSummaryRecord.class);

                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();

                return (List<CallSummaryRecord>) fqr;
            }
        };

        return csrDataService.executeSQLQuery(queryExecution);
    }


    /**
     * Try to find any old CSRs that would exist in the table wih no subcriptionId
     * If there are more than one CSRs for that weekId and subscription, delete all but the last one and set its
     * subscriptionId and return it. findOldCallSummaryRecords returns the list sorted on weekId and subscriptionId in
     * descending order so that if we find a subscriptionId that matches for that week we'll pick the very first one
     *
     * @param subscriptionId
     * @return an old and now fixed up CSR, or null
     */
    private CallSummaryRecord lookupAndFixOldCsr(String subscriptionId, String weekId) {
        List<CallSummaryRecord> csrs = findOldCallSummaryRecords(subscriptionId, weekId);
        if (csrs == null || csrs.size() == 0) {
            return null;
        }

        CallSummaryRecord found = null;
        for (CallSummaryRecord csr : csrs) {
            if (found == null && weekId.equals(csr.getWeekId())) {
                //
                // Funky code!
                // This old CSR used to have a RequestId in the subscriptionId field, and now we really want the
                // subscriptionId field to be a subscriptionId.
                //
                RequestId oldRequestId = RequestId.fromString(csr.getSubscriptionId());
                String newSubscriptionId = oldRequestId.getSubscriptionId();
                csr.setSubscriptionId(newSubscriptionId);
                found = csrDataService.update(csr);
                LOGGER.debug("Fixed up {}", newSubscriptionId);
            } else {
                csrDataService.delete(csr);
            }
        }

        return found;
    }


    @MotechListener(subjects = { NMS_IMI_KK_PROCESS_CSR }) //NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void processCallSummaryRecord(MotechEvent event) { //NOPMD NcssMethodCount

        Timer timer = new Timer();

        String subscriptionId = "###INVALID###";
        try {
            CallSummaryRecordDto csrDto = CallSummaryRecordDto.fromParams(event.getParameters());
            subscriptionId = csrDto.getSubscriptionId();
            csrVerifierService.verify(csrDto);

            Subscription subscription = subscriptionDataService.findBySubscriptionId(subscriptionId);
            if (subscription == null) {
                throw new NoSuchSubscriptionException(subscriptionId);
            }

            CallSummaryRecord existingCsr = csrDataService.findBySubscriptionId(subscriptionId);

            if (existingCsr == null) {
                // This may be an old style CSR, let's try to fix it up
                existingCsr = lookupAndFixOldCsr(subscriptionId, csrDto.getWeekId());
            }

            CallSummaryRecord csr;
            boolean invalidNr = StatusCode.fromInt(csrDto.getStatusCode()).equals(StatusCode.OBD_FAILED_INVALIDNUMBER);
            if (existingCsr == null) {
                csr = csrDataService.create(new CallSummaryRecord(
                        subscriptionId,
                        csrDto.getContentFileName(),
                        csrDto.getLanguageCode(),
                        csrDto.getCircleName(),
                        csrDto.getWeekId(),
                        StatusCode.fromInt(csrDto.getStatusCode()),
                        FinalCallStatus.fromInt(csrDto.getFinalStatus()),
                        invalidNr ? 1 : 0));

            } else {
                existingCsr.setFinalStatus(FinalCallStatus.fromInt(csrDto.getFinalStatus()));
                existingCsr.setContentFileName(csrDto.getContentFileName());
                existingCsr.setStatusCode(StatusCode.fromInt(csrDto.getStatusCode()));
                existingCsr.setWeekId(csrDto.getWeekId());
                if (invalidNr) {
                    existingCsr.setInvalidNumberCount(existingCsr.getInvalidNumberCount() + 1);
                }
                csr = csrDataService.update(existingCsr);
            }

            CallRetry callRetry = callRetryDataService.findBySubscriptionId(subscriptionId);

            /**
             * If we have a null subscription (it was deleted for some reason), but still have a retry record
             * for it, then we need to erase the call retry record.
             */
            if (subscription == null) {
                String msg = String.format("Subscription %s doesn't exist in the database anymore.", subscriptionId);
                LOGGER.warn(msg);
                alertService.create(subscriptionId, NMS_IMI_KK_PROCESS_CSR, msg, AlertType.MEDIUM,
                        AlertStatus.NEW, 0, null);

                if (callRetry != null) {
                    msg = String.format("Deleting callRetry record for deleted subscription %s", subscriptionId);
                    LOGGER.warn(msg);
                    alertService.create(subscriptionId, NMS_IMI_KK_PROCESS_CSR, msg, AlertType.MEDIUM,
                            AlertStatus.NEW, 0, null);
                    deleteCallRetryRecordIfNeeded(callRetry);
                }

                return;
            }

            switch (FinalCallStatus.fromInt(csrDto.getFinalStatus())) {
                case SUCCESS:
                    completeSubscriptionIfNeeded(subscription, csr);
                    deleteCallRetryRecordIfNeeded(callRetry);
                    break;

                case FAILED:
                    rescheduleCall(subscription, csr, callRetry);
                    break;

                case REJECTED:
                    deactivateSubscription(subscription, callRetry);
                    break;

                default:
                    String error = String.format("Invalid FinalCallStatus: %s", csrDto.getFinalStatus());
                    LOGGER.error(error);
                    alertService.create(subscriptionId, NMS_IMI_KK_PROCESS_CSR, error, AlertType.CRITICAL,
                            AlertStatus.NEW, 0, null);
            }

            if (existingCsr == null) {
                csrDataService.create(csr);
            } else {

            }

        } catch (NoSuchSubscriptionException e) {
            String msg = String.format("No such subscription %s", e.getMessage());
            LOGGER.error(msg);
            alertService.create(subscriptionId, "Invalid CSR Data", msg, AlertType.HIGH, AlertStatus.NEW, 0, null);
        } catch (InvalidCallRecordDataException e) {
            String msg = String.format("Invalid CDR data for subscription %s: %s", subscriptionId, e.getMessage());
            LOGGER.error(msg);
            alertService.create(subscriptionId, "Invalid CSR Data", msg, AlertType.HIGH, AlertStatus.NEW, 0, null);
        } catch (Exception e) {
            String msg = String.format("MOTECH BUG *** Unexpected exception in processCallSummaryRecord() for " +
                    "subscription %s: %s", subscriptionId, ExceptionUtils.getFullStackTrace(e));
            LOGGER.error(msg);
            alertService.create(subscriptionId, NMS_IMI_KK_PROCESS_CSR,
                    msg.substring(0, min(msg.length(), MAX_CHAR_ALERT)), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
        }

        LOGGER.debug("processCallSummaryRecord {} : {}", subscriptionId, timer.time());

    }


    private void resetWelcomeFlagInSubscription(Subscription subscription) {

        if (subscription.getNeedsWelcomeMessageViaObd()) {
            subscription.setNeedsWelcomeMessageViaObd(false);
            subscriptionDataService.update(subscription);
        }
    }


    @Override
    public void deleteOldCallSummaryRecords(final int retentionInDays) {

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = String.format(
                        "DELETE FROM nms_kk_summary_records where creationDate < now() - INTERVAL %d DAY",
                        retentionInDays);
                LOGGER.debug("SQL QUERY: {}", query);
                return query;
            }

            @Override
            public Long execute(Query query) {

                return (Long) query.execute();
            }
        };

        LOGGER.debug("Deleting nms_kk_summary_records older than {} days", retentionInDays);
        Timer timer = new Timer();
        long rowCount = csrDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("Deleted {} rows from nms_kk_summary_records in {}", rowCount, timer.time());
    }
}
