package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.kilkari.domain.CallDetailRecord;
import org.motechproject.nms.kilkari.domain.CallRetry;
import org.motechproject.nms.kilkari.domain.CallStage;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.CallDetailRecordDataService;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.service.CallDetailRecordService;
import org.motechproject.nms.props.domain.CallStatus;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.props.domain.RequestId;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("cdrService")
public class CallDetailRecordServiceImpl implements CallDetailRecordService {

    private static final String PROCESS_CDR = "nms.imi.kk.process_cdr";

    private static final Logger LOGGER = LoggerFactory.getLogger(CallDetailRecordServiceImpl.class);

    private CallDetailRecordDataService cdrDataService;
    private SubscriptionDataService subscriptionDataService;
    private CallRetryDataService callRetryDataService;
    private SubscriberDataService subscriberDataService;
    private AlertService alertService;


    @Autowired
    public CallDetailRecordServiceImpl(CallDetailRecordDataService cdrDataService,
                                       SubscriptionDataService subscriptionDataService,
                                       CallRetryDataService callRetryDataService,
                                       SubscriberDataService subscriberDataService,
                                       AlertService alertService) {
        this.cdrDataService = cdrDataService;
        this.subscriptionDataService = subscriptionDataService;
        this.callRetryDataService = callRetryDataService;
        this.subscriberDataService = subscriberDataService;
        this.alertService = alertService;
    }


    private String getLanguageLocationCode(Subscription subscription) {
        //todo: don't understand why subscriber.getLanguage() doesn't work here...
        // it's not working because of https://applab.atlassian.net/browse/MOTECH-1678
        Subscriber subscriber = subscription.getSubscriber();
        LanguageLocation ll;
        ll = (LanguageLocation) subscriberDataService.getDetachedField(subscriber, "languageLocation");
        return ll.getCode();
    }


    private String getCircleName(Subscription subscription) {
        Subscriber subscriber = subscription.getSubscriber();
        Circle circle = (Circle) subscriberDataService.getDetachedField(subscriber, "circle");
        return circle.getName();
    }


    private void rescheduleCall(String contentFileName, int week, Subscription subscription,
                                CallRetry callRetry) {

        Long msisdn = subscription.getSubscriber().getCallingNumber();

        if (callRetry == null) {

            // This message was never retried before, so this is a first retry

            DayOfTheWeek nextDay = DayOfTheWeek.fromInt(subscription.getStartDate().dayOfWeek().get()).nextDay();
            CallRetry newCallRetry = new CallRetry(
                    subscription.getSubscriptionId(),
                    msisdn,
                    nextDay,
                    CallStage.RETRY_1,
                    contentFileName,
                    week,
                    getLanguageLocationCode(subscription),
                    getCircleName(subscription),
                    subscription.getOrigin().getCode()
            );
            callRetryDataService.create(newCallRetry);
            return;
        }

        // We've already rescheduled this call, let's see if it needs to be re-rescheduled

        if ((subscription.getSubscriptionPack().retryCount() == 1) ||
                (callRetry.getCallStage() == CallStage.RETRY_LAST)) {

            // Nope, this call should not be retried

            callRetryDataService.delete(callRetry);

            // Does subscription need to be marked complete, even if we failed to send the last message?
            completeSubscriptionIfNeeded(contentFileName, subscription, callRetry);
            return;
        }


        // Re-reschedule the call

        callRetry.setCallStage(callRetry.getCallStage().nextStage());
        callRetry.setDayOfTheWeek(callRetry.getDayOfTheWeek().nextDay());
        callRetryDataService.update(callRetry);
    }


    private void deactivateSubscription(Subscription subscription, CallRetry callRetry) {

        if (subscription.getOrigin() == SubscriptionOrigin.IVR) {
            String error = String.format("Subscription {} was rejected (DND) but its origin is IVR, not MCTS!",
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


    private void completeSubscriptionIfNeeded(String contentFileName, Subscription subscription,
                                              CallRetry callRetry) {

        if (!subscription.isLastPackMessage(contentFileName)) {
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


    @MotechListener(subjects = { PROCESS_CDR })
    public void processCallDetailRecord(MotechEvent event) {

        CallDetailRecord cdr = (CallDetailRecord) event.getParameters().get("CDR");
        cdrDataService.create(cdr);

        RequestId requestId = RequestId.fromString(cdr.getRequestId());
        Subscription subscription = subscriptionDataService.findBySubscriptionId(requestId.getSubscriptionId());
        CallRetry callRetry = callRetryDataService.findBySubscriptionId(subscription.getSubscriptionId());

        if (cdr.getFinalStatus() == CallStatus.SUCCESS) {
            completeSubscriptionIfNeeded(cdr.getContentFileName(), subscription, callRetry);
        } else if (cdr.getFinalStatus() == CallStatus.FAILED) {
            rescheduleCall(cdr.getContentFileName(), cdr.getWeek(), subscription, callRetry);
        } else  if (cdr.getFinalStatus() == CallStatus.REJECTED) {
            deactivateSubscription(subscription, callRetry);
        }
    }
}
