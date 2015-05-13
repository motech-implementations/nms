package org.motechproject.nms.imi.component;

import org.joda.time.DateTime;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.imi.domain.CallDetailRecord;
import org.motechproject.nms.imi.domain.CallRetry;
import org.motechproject.nms.imi.repository.CallRetryDataService;
import org.motechproject.nms.imi.service.RequestId;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Listens to nms.imi.complete_subscription MOTECH message and deactivates the provided subscription which,
 * presumably, was rejected by IMI because the subscriber (from MCTS) is in the do not disturb (DND) database
 */
@Component
public class SubscriptionCompleter {
    private static final String COMPLETE_SUBSCRIPTION = "nms.imi.complete_subscription";

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionCompleter.class);

    private SubscriptionService subscriptionService;
    private CallRetryDataService callRetryDataService;


    @Autowired
    public SubscriptionCompleter(SubscriptionService subscriptionService,
                                 CallRetryDataService callRetryDataService) {
        this.subscriptionService = subscriptionService;
        this.callRetryDataService = callRetryDataService;
    }


    @MotechListener(subjects = { COMPLETE_SUBSCRIPTION })
    public void completeSubscription(MotechEvent event) {
        LOGGER.debug("completeSubscription() is handling {}", event.toString());

        try {

            CallDetailRecord cdr = (CallDetailRecord) event.getParameters().get("CDR");
            RequestId requestId = RequestId.fromString(cdr.getRequestId());
            Subscription subscription = subscriptionService.getSubscription(requestId.getSubscriptionId());

            // We're checking if we just successfully sent the last message for a subscription.
            // Since we're potentially processing today's CDRs for next time let's see if the subscription
            // would end tomorrow. If it does, then we can mark this subscription as completed.
            DateTime tomorrow = DateTime.now().plusDays(1);
            if (!subscription.hasCompleted(tomorrow)) {
                // Nope, this subscription has not completed
                return;
            }

            LOGGER.debug("Subscription completed: {}", subscription.getSubscriptionId());

            subscriptionService.markSubscriptionComplete(subscription);

            //Delete the callRetry entry, if any
            CallRetry callRetry = callRetryDataService.findBySubscriptionId(requestId.getSubscriptionId());
            if (callRetry != null) {
                LOGGER.debug("deleting CallRetry for {}", requestId.getSubscriptionId());
                callRetryDataService.delete(callRetry);
            } else {
                LOGGER.debug("no need to delete CallRetry for {}, no record exists", requestId.getSubscriptionId());
            }

        } catch (Exception e) {
            LOGGER.error("********** Unexpected Exception! **********", e);
            throw e;
        }
    }
}
