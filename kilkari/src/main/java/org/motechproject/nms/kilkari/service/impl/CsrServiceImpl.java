package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.kilkari.domain.CallRetry;
import org.motechproject.nms.kilkari.domain.CallStage;
import org.motechproject.nms.kilkari.domain.CallSummaryRecord;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.CallSummaryRecordDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackMessageDataService;
import org.motechproject.nms.kilkari.service.CsrService;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.props.domain.StatusCode;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service("csrService")
public class CsrServiceImpl implements CsrService {

    private static final String PROCESS_SUMMARY_RECORD_SUBJECT = "nms.imi.kk.process_summary_record";
    private static final String CSR_PARAM_KEY = "csr";

    private static final int ONE_HUNDRED = 100;

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrServiceImpl.class);

    private CallSummaryRecordDataService csrDataService;
    private SubscriptionDataService subscriptionDataService;
    private CallRetryDataService callRetryDataService;
    private SubscriberDataService subscriberDataService;
    private AlertService alertService;
    private SubscriptionPackMessageDataService subscriptionPackMessageDataService;

    private Map<String, Integer> messageDurationCache;


    @Autowired
    public CsrServiceImpl(CallSummaryRecordDataService csrDataService,
                          SubscriptionDataService subscriptionDataService,
                          CallRetryDataService callRetryDataService,
                          SubscriberDataService subscriberDataService,
                          AlertService alertService,
                          SubscriptionPackMessageDataService subscriptionPackMessageDataService) {
        this.csrDataService = csrDataService;
        this.subscriptionDataService = subscriptionDataService;
        this.callRetryDataService = callRetryDataService;
        this.subscriberDataService = subscriberDataService;
        this.alertService = alertService;
        this.subscriptionPackMessageDataService = subscriptionPackMessageDataService;

        buildMessageDurationCache();
    }


    public final void buildMessageDurationCache() {
        messageDurationCache = new HashMap<>();
        for (SubscriptionPackMessage msg : subscriptionPackMessageDataService.retrieveAll()) {
            messageDurationCache.put(msg.getMessageFileName(), msg.getDuration());
        }

        if (messageDurationCache.size() == 0) {
            alertService.create("MessageDuration Cache", "Subscription Message duration cache empty",
                    "Subscription pack messages not found", AlertType.CRITICAL, AlertStatus.NEW, 0, null);
        }
    }


    //todo: IT
    private int calculatePercentPlayed(String contentFileName, Integer duration) {

        if (duration == null) {
            return 0;
        }

        //refresh cache if empty
        if (messageDurationCache.size() == 0) {
            buildMessageDurationCache();
        }

        if (messageDurationCache.containsKey(contentFileName)) {
            int totalDuration = messageDurationCache.get(contentFileName);
            return duration / totalDuration * ONE_HUNDRED;
        }
        throw new IllegalArgumentException(String.format("Invalid contentFileName: %s", contentFileName));
    }


    private String getLanguageCode(Subscription subscription) {
        //todo: don't understand why subscriber.getLanguage() doesn't work here...
        // it's not working because of https://applab.atlassian.net/browse/MOTECH-1678
        Subscriber subscriber = subscription.getSubscriber();
        Language language;
        language = (Language) subscriberDataService.getDetachedField(subscriber, "language");
        return language.getCode();
    }


    private String getCircleName(Subscription subscription) {
        Subscriber subscriber = subscription.getSubscriber();
        Circle circle = (Circle) subscriberDataService.getDetachedField(subscriber, "circle");
        return circle.getName();
    }


    private void completeSubscriptionIfNeeded(Subscription subscription, CallSummaryRecord record,
                                              CallRetry callRetry) {

        resetWelcomeFlagInSubscription(subscription);

        if (!subscription.isLastPackMessage(record.getContentFileName())) {
            // This subscription has not completed, do nothing
            return;
        }

        // Mark the subscription completed
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscriptionDataService.update(subscription);

        // Delete the callRetry entry, if any
        if (callRetry != null) {
            callRetryDataService.delete(callRetry);
        }

    }


    // Check if this call has been failing with OBD_FAILED_INVALIDNUMBER for all the retries.
    // See issue #169: https://github.com/motech-implementations/mim/issues/169

    private boolean isMsisdnInvalid(CallSummaryRecord record) {
        if (record.getStatusStats().keySet().size() > 1) {
            return false;
        }

        return record.getStatusStats().containsKey(StatusCode.OBD_FAILED_INVALIDNUMBER.getValue());
    }


    private void rescheduleCall(Subscription subscription, CallSummaryRecord record, CallRetry callRetry) {

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
                        DayOfTheWeek.fromInt(subscription.getStartDate().dayOfWeek().get()).nextDay(),
                        CallStage.RETRY_1,
                        record.getContentFileName(),
                        record.getWeekId(),
                        getLanguageCode(subscription),
                        getCircleName(subscription),
                        subscription.getOrigin()
                );
                callRetryDataService.create(newCallRetry);
            } catch (Exception e) {
                String msg = String.format("Exception while trying to reschedule call for subscription %s: %s",
                        subscription.getSubscriptionId(), e);
                LOGGER.error(msg);
                alertService.create("rescheduleCall()", action, msg, AlertType.HIGH, AlertStatus.NEW, 0, null);
            }
            return;
        }

        // This call was retried for a different week but never removed from the CallRetry table which means we never
        // got a CDR from IMI, so let's warn about this and still try to reschedule as a first try it for this week.
        if (!callRetry.getWeekId().equals(record.getWeekId())) {

            String message = String.format("CallRetry record (id %d) for subscription %s for weekId %s was never " +
                            "deleted, which means we never received a CDR about it. Someone should look into this.",
                    callRetry.getId(), callRetry.getSubscriptionId(), callRetry.getWeekId());
            LOGGER.warn(message);
            alertService.create("CSR Processing", message, null, AlertType.HIGH, AlertStatus.NEW, 0, null);
            callRetry.setCallStage(CallStage.RETRY_1);
            callRetry.setWeekId(record.getWeekId());
            callRetryDataService.update(callRetry);
            return;
        }


        // We've already rescheduled this call, let's see if it needs to be re-rescheduled
        if ((subscription.getSubscriptionPack().retryCount() == 1) ||
                (callRetry.getCallStage() == CallStage.RETRY_LAST)) {

            // Nope, this call should not be retried

            // Deactivate subscription for persistent invalid numbers
            // See https://github.com/motech-implementations/mim/issues/169
            if (isMsisdnInvalid(record)) {
                subscription.setStatus(SubscriptionStatus.DEACTIVATED);
                subscription.setDeactivationReason(DeactivationReason.INVALID_NUMBER);
                subscriptionDataService.update(subscription);
            }

            LOGGER.debug(String.format("Deleting retry entry for subscription: %s", subscription.getSubscriptionId()));
            callRetryDataService.delete(callRetry);

            // Does subscription need to be marked complete, even if we failed to send the last message?
            completeSubscriptionIfNeeded(subscription, record, callRetry);
            return;
        }

        // Re-reschedule the call
        LOGGER.debug(String.format("Updating retry entry for subscription: %s", subscription.getSubscriptionId()));
        callRetry.setCallStage(callRetry.getCallStage().nextStage());
        callRetry.setDayOfTheWeek(callRetry.getDayOfTheWeek().nextDay());
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

        // Delete the callRetry entry, if any
        if (callRetry != null) {
            callRetryDataService.delete(callRetry);
        }

        //Deactivate the subscription
        subscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscription.setDeactivationReason(DeactivationReason.DO_NOT_DISTURB);
        subscriptionDataService.update(subscription);
    }


    private void aggregateStats(Map<Integer, Integer> src, Map<Integer, Integer> dst) {
        for (Map.Entry<Integer, Integer> entry : src.entrySet()) {
            if (dst.containsKey(entry.getKey())) {
                dst.put(entry.getKey(), dst.get(entry.getKey()) + entry.getValue());
            } else {
                dst.put(entry.getKey(), entry.getValue());
            }
        }
    }


    private CallSummaryRecord aggregateSummaryRecord(CallSummaryRecordDto csr) {

        LOGGER.debug("aggregateSummaryRecord({})", csr.getRequestId().toString());

        CallSummaryRecord record = csrDataService.findByRequestId(csr.getRequestId().toString());
        if (record == null) {
            record = csrDataService.create(new CallSummaryRecord(
                    csr.getRequestId().toString(),
                    csr.getMsisdn(),
                    csr.getContentFileName(),
                    csr.getWeekId(),
                    csr.getLanguageLocationCode(),
                    csr.getCircle(),
                    csr.getFinalStatus(),
                    csr.getStatusStats(),
                    calculatePercentPlayed(csr.getContentFileName(), csr.getSecondsPlayed()),
                    csr.getCallAttempts(),
                    1));
        } else {
            aggregateStats(csr.getStatusStats(), record.getStatusStats());
            record.setCallAttempts(record.getCallAttempts() + csr.getCallAttempts());
            record.setAttemptedDayCount(record.getAttemptedDayCount() + 1);
            record.setFinalStatus(csr.getFinalStatus());
            int percentPlayed = calculatePercentPlayed(csr.getContentFileName(), csr.getSecondsPlayed());
            if (percentPlayed > record.getPercentPlayed()) {
                record.setPercentPlayed(percentPlayed);
            }
            csrDataService.update(record);
        }

        return record;
    }


    @MotechListener(subjects = { PROCESS_SUMMARY_RECORD_SUBJECT })
    public void processCallSummaryRecord(MotechEvent event) {

        try {
            CallSummaryRecordDto csrDto = (CallSummaryRecordDto) event.getParameters().get(CSR_PARAM_KEY);
            String subscriptionId = csrDto.getRequestId().getSubscriptionId();
            CallSummaryRecord csr = aggregateSummaryRecord(csrDto);

            CallRetry callRetry = callRetryDataService.findBySubscriptionId(subscriptionId);
            Subscription subscription = subscriptionDataService.findBySubscriptionId(subscriptionId);


            switch (csrDto.getFinalStatus()) {
                case SUCCESS:
                    completeSubscriptionIfNeeded(subscription, csr, callRetry);
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
                    alertService.create(PROCESS_SUMMARY_RECORD_SUBJECT, error, error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            }
        } catch (Exception e) {
            String msg = String.format("Exception in processCallSummaryRecord(): %s", e);
            LOGGER.error(msg);
            alertService.create(PROCESS_SUMMARY_RECORD_SUBJECT, msg, null, AlertType.HIGH, AlertStatus.NEW, 0, null);
        }
    }


    private void resetWelcomeFlagInSubscription(Subscription subscription) {

        if (subscription.getNeedsWelcomeMessageViaObd() == true) {
            subscription.setNeedsWelcomeMessageViaObd(false);
            subscriptionDataService.update(subscription);
        }
    }
}
