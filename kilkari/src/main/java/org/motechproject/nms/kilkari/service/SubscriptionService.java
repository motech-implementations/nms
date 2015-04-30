package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.InboxCallDetails;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionMode;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;

/**
 *
 */
public interface SubscriptionService {

    Subscriber getSubscriber(long callingNumber);

    void createSubscription(long callingNumber, String languageLocationCode, String subscriptionPack,
        SubscriptionMode mode);

    Subscription getSubscription(String subscriptionId);

    void deactivateSubscription(Subscription subscription, DeactivationReason reason);

    SubscriptionPack getSubscriptionPack(String name);

    long getCountSubscriptionPack(String name);

    //TODO: we'll probably want to move this to a new InboxService once we do more Inbox work in Sprint 2
    long addInboxCallDetails(InboxCallDetails inboxCallDetails);
}
