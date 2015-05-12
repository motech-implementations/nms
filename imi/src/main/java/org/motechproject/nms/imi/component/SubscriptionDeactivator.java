package org.motechproject.nms.imi.component;

import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.imi.domain.CallDetailRecord;
import org.motechproject.nms.imi.domain.CallRetry;
import org.motechproject.nms.imi.repository.CallRetryDataService;
import org.motechproject.nms.imi.service.RequestId;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Listens to nms.imi.deactivate_subscription MOTECH message and deactivates the provided subscription which,
 * presumably, was rejected by IMI because the subscriber (from MCTS) is in the do not disturb (DND) database
 */
@Component
public class SubscriptionDeactivator {
    private static final String DEACTIVATE_SUBSCRIPTION = "nms.imi.deactivate_subscription";

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionDeactivator.class);

    private SubscriptionService subscriptionService;
    private CallRetryDataService callRetryDataService;
    private AlertService alertService;


    @Autowired
    public SubscriptionDeactivator(SubscriptionService subscriptionService, CallRetryDataService callRetryDataService,
                                   AlertService alertService) {
        this.subscriptionService = subscriptionService;
        this.callRetryDataService = callRetryDataService;
        this.alertService = alertService;
    }


    @MotechListener(subjects = { DEACTIVATE_SUBSCRIPTION })
    public void deactivateSubscription(MotechEvent event) {
        LOGGER.debug("deactivateSubscription() is handling {}", event.toString());

        CallDetailRecord cdr = (CallDetailRecord) event.getParameters().get("CDR");
        RequestId requestId = RequestId.fromString(cdr.getRequestId());
        Subscription subscription = subscriptionService.getSubscription(requestId.getSubscriptionId());
        if (subscription.getOrigin() == SubscriptionOrigin.IVR) {
            String error = String.format("Subscription {} was rejected (DND) but its origin is IVR, not MCTS!",
                    subscription.getSubscriptionId());
            LOGGER.error(error);
            alertService.create(subscription.getSubscriptionId(), "subscription", error, AlertType.CRITICAL,
                    AlertStatus.NEW, 0, null);
            return;
        }

        //Delete the callRetry entry, if any
        CallRetry callRetry = callRetryDataService.findBySubscriptionId(requestId.getSubscriptionId());
        if (callRetry != null) {
            LOGGER.debug("deleting CallRetry for {}", requestId.getSubscriptionId());
            callRetryDataService.delete(callRetry);
        } else {
            LOGGER.debug("no need to delete CallRetry for {}, no record exists", requestId.getSubscriptionId());
        }

        //Deactivate the subscription
        LOGGER.debug("deactivating subscription {}", requestId.getSubscriptionId());
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.DO_NOT_DISTURB);
    }
}
