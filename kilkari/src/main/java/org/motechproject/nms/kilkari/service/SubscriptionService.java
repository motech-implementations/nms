package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.region.language.domain.Language;

import java.util.List;

/**
 *
 */
public interface SubscriptionService {

    void createSubscriptionPacks();

    void createSubscription(long callingNumber, Language language, SubscriptionPack subscriptionPack,
                            SubscriptionOrigin mode);

    Subscription getSubscription(String subscriptionId);

    void deactivateSubscription(Subscription subscription, DeactivationReason reason);

    SubscriptionPack getSubscriptionPack(String name);

    void deleteAll();

    Subscription create(Subscription subscription);

    List<Subscription> findActiveSubscriptions(int page, int pageSize);
}
