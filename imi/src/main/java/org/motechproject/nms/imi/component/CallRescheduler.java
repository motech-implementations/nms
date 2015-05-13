package org.motechproject.nms.imi.component;

import org.joda.time.DateTime;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.imi.domain.CallDetailRecord;
import org.motechproject.nms.imi.domain.CallRetry;
import org.motechproject.nms.imi.domain.CallStage;
import org.motechproject.nms.imi.repository.CallRetryDataService;
import org.motechproject.nms.imi.service.RequestId;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Listens to nms.imi.reschedule_call MOTECH message and reschedules a call for the provided subscription which failed
 */
@Component
public class CallRescheduler {

    private static final String RESCHEDULE_CALL = "nms.imi.reschedule_call";

    private static final Logger LOGGER = LoggerFactory.getLogger(CallRescheduler.class);

    private SubscriptionService subscriptionService;
    private CallRetryDataService callRetryDataService;
    private SubscriberDataService subscriberDataService;


    @Autowired
    public CallRescheduler(SubscriptionService subscriptionService, CallRetryDataService callRetryDataService,
                           SubscriberDataService subscriberDataService) {
        this.subscriptionService = subscriptionService;
        this.callRetryDataService = callRetryDataService;
        this.subscriberDataService = subscriberDataService;
    }


    private void checkSubscriptionCompleted(Subscription subscription) {
        DateTime tomorrow = DateTime.now().plusDays(1);
        if (!subscription.hasCompleted(tomorrow)) {
            // This subscription has not completed
            return;
        }
        LOGGER.debug("Subscription completed: {}", subscription.getSubscriptionId());
        subscriptionService.markSubscriptionComplete(subscription);
    }

    @MotechListener(subjects = { RESCHEDULE_CALL })
    public void rescheduleCall(MotechEvent event) {
        LOGGER.debug("rescheduleCall() is handling {}", event.toString());

        try {
            CallDetailRecord cdr = (CallDetailRecord) event.getParameters().get("CDR");
            RequestId requestId = RequestId.fromString(cdr.getRequestId());
            Subscription subscription = subscriptionService.getSubscription(requestId.getSubscriptionId());
            CallRetry callRetry = callRetryDataService.findBySubscriptionId(requestId.getSubscriptionId());

            if (callRetry == null) {
                //todo: don't understand why subscriber.getLanguage() doesn't work here...
                // it's not working because of https://applab.atlassian.net/browse/MOTECH-1678
                Subscriber subscriber = subscription.getSubscriber();
                LanguageLocation languageLocation;
                languageLocation = (LanguageLocation) subscriberDataService.getDetachedField(subscriber,
                                                                                             "languageLocation");

                Circle circle;
                circle = (Circle) subscriberDataService.getDetachedField(subscriber, "circle");

                //first retry day
                LOGGER.debug("rescheduling msisdn {} subscription {}", cdr.getMsisdn(), requestId.getSubscriptionId());
                callRetry = new CallRetry(
                        requestId.getSubscriptionId(),
                        cdr.getMsisdn(),
                        DayOfTheWeek.fromInt(subscription.getStartDate().dayOfWeek().get()).nextDay(),
                        CallStage.RETRY_1,
                        languageLocation.getCode(),
                        circle.getName(),
                        subscription.getOrigin().getCode()
                );
                LOGGER.debug("Creating CallRetry {}", callRetry.toString());
                callRetryDataService.create(callRetry);
                return;
            }

            //we've already rescheduled this call, let's see if it needs to be re-rescheduled

            SubscriptionPack subscriptionPack = subscription.getSubscriptionPack();
            if (subscriptionPack.retryCount() == 1) {
                //This message should only be retried once, so let's delete it from the CallRetry table
                LOGGER.debug("Not re-rescheduling single-retry msisdn {} subscription {}: max retry exceeded",
                        cdr.getMsisdn(), requestId.getSubscriptionId());
                callRetryDataService.delete(callRetry);

                // Check if this subscription needs to be marked complete (even if we failed to send the last
                // message)
                checkSubscriptionCompleted(subscription);

                return;
            }

            if (callRetry.getCallStage() == CallStage.RETRY_LAST) {
                //This message has been re-scheduled for the last (3rd) time, let's delete it from the CallRetry
                //table
                LOGGER.debug("Not re-rescheduling multiple-retry msisdn {} subscription {}: max retry exceeded",
                        cdr.getMsisdn(), requestId.getSubscriptionId());
                callRetryDataService.delete(callRetry);

                // Check if this subscription needs to be marked complete (even if we failed to send the last
                // message)
                checkSubscriptionCompleted(subscription);

                return;
            }

            //re-reschedule the call
            LOGGER.debug("re-rescheduling msisdn {} subscription {}", cdr.getMsisdn(), requestId.getSubscriptionId());

            //update the callStage
            callRetry.setCallStage((callRetry.getCallStage() == CallStage.RETRY_1) ? CallStage.RETRY_2 :
                    CallStage.RETRY_LAST);
            //increment the day of the week
            callRetry.setDayOfTheWeek(callRetry.getDayOfTheWeek().nextDay());
            //update the CallRetry record
            LOGGER.debug("Updating CallRetry {}", callRetry.toString());
            callRetryDataService.update(callRetry);
        } catch (Exception e) {
            LOGGER.error("********** Unexpected Exception! **********", e);
            throw e;
        }
    }
}
