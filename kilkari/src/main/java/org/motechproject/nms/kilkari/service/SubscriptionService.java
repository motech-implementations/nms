package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.InboxCallDetails;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionMode;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.region.domain.LanguageLocation;

/**
 *
 */
public interface SubscriptionService {

    void createSubscriptionPacks();

    void createSubscription(long callingNumber, LanguageLocation languageLocation, SubscriptionPack subscriptionPack,
                            SubscriptionMode mode);

    Subscription getSubscription(String subscriptionId);

    void deactivateSubscription(Subscription subscription, DeactivationReason reason);

    SubscriptionPack getSubscriptionPack(String name);

    //TODO: we'll probably want to move this to a new InboxService once we do more Inbox work in Sprint 2
    long addInboxCallDetails(InboxCallDetails inboxCallDetails);
}
