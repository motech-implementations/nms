package org.motechproject.nms.kilkari.service.impl;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.kilkari.domain.CallRetry;
import org.motechproject.nms.kilkari.domain.CallStage;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.exception.InvalidCallRecordDataException;
import org.motechproject.nms.kilkari.exception.NoSuchSubscriptionException;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.service.CsrService;
import org.motechproject.nms.kilkari.service.CsrVerifierService;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.Math.min;


@Service("csrService")
public class CsrServiceImpl implements CsrService {

    private static final String NMS_IMI_KK_PROCESS_CSR = "nms.imi.kk.process_csr";
    private static final int MAX_CHAR_ALERT = 4900;

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrServiceImpl.class);

    private SubscriptionDataService subscriptionDataService;
    private CallRetryDataService callRetryDataService;
    private AlertService alertService;
    private CsrVerifierService csrVerifierService;

    @Autowired
    public CsrServiceImpl(SubscriptionDataService subscriptionDataService, CallRetryDataService callRetryDataService,
                          AlertService alertService, CsrVerifierService csrVerifierService) {
        this.subscriptionDataService = subscriptionDataService;
        this.callRetryDataService = callRetryDataService;
        this.alertService = alertService;
        this.csrVerifierService = csrVerifierService;
    }


    private void completeSubscriptionIfNeeded(Subscription subscription, String contentFileName) {

        resetWelcomeFlagInSubscription(subscription);

        if (!subscription.isLastPackMessage(contentFileName)) {
            // This subscription has not completed, do nothing
            return;
        }

        // Mark the subscription completed
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscriptionDataService.update(subscription);

    }


    private void deleteCallRetryRecordIfNeeded(CallRetry callRetry) {
        if (callRetry == null) {
            return;
        }
        callRetryDataService.delete(callRetry);
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


     private void doReschedule(Subscription subscription, CallRetry existingCallRetry, CallSummaryRecordDto csrDto) {

        boolean invalidNr = StatusCode.fromInt(csrDto.getStatusCode()).equals(StatusCode.OBD_FAILED_INVALIDNUMBER);

        if (existingCallRetry == null) {
            // We've never retried this call, let's do it
            callRetryDataService.create(new CallRetry(
                    subscription.getSubscriptionId(),
                    subscription.getSubscriber().getCallingNumber(),
                    CallStage.RETRY_1,
                    csrDto.getContentFileName(),
                    csrDto.getWeekId(),
                    csrDto.getLanguageCode(),
                    csrDto.getCircleName(),
                    subscription.getOrigin(),
                    csrDto.getTargetFileTimeStamp(),
                    invalidNr ? 1 : 0)
            );
            return;
        }

        // We're dealing with a re-reschedule

        //From the same targetFile?
        if (existingCallRetry.getTargetFiletimestamp().equals(csrDto.getTargetFileTimeStamp())) {
            // We've already processed the call from this particular targetFile, this is probably a
            // resubmit, let's ignore it.
            return;
        }

        // From a different targetFile

        if ((subscription.getSubscriptionPack().retryCount() == 1) ||
                (existingCallRetry.getCallStage() == CallStage.RETRY_LAST)) {

            // This call should not be retried

            // Deactivate subscription for persistent invalid numbers
            // See https://github.com/motech-implementations/mim/issues/169
            if (existingCallRetry.getInvalidNumberCount() == subscription.getSubscriptionPack().retryCount()) {
                subscription.setStatus(SubscriptionStatus.DEACTIVATED);
                subscription.setDeactivationReason(DeactivationReason.INVALID_NUMBER);
                subscriptionDataService.update(subscription);
            }

            callRetryDataService.delete(existingCallRetry);

            // Does subscription need to be marked complete, even if we failed to send the last message?
            completeSubscriptionIfNeeded(subscription, csrDto.getContentFileName());

            return;
        }


        // This call should indeed be re-rescheduled

        existingCallRetry.setCallStage(existingCallRetry.getCallStage().nextStage());
        existingCallRetry.setInvalidNumberCount(existingCallRetry.getInvalidNumberCount() + (invalidNr ? 1 : 0));
        callRetryDataService.update(existingCallRetry);

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

            CallRetry callRetry = callRetryDataService.findBySubscriptionId(subscriptionId);

            switch (FinalCallStatus.fromInt(csrDto.getFinalStatus())) {
                case SUCCESS:
                    completeSubscriptionIfNeeded(subscription, csrDto.getContentFileName());
                    if (callRetry != null) {
                        callRetryDataService.delete(callRetry);
                    }
                    break;

                case FAILED:
                    doReschedule(subscription, callRetry, csrDto);
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
}
