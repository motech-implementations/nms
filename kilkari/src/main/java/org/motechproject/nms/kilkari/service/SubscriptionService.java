package org.motechproject.nms.kilkari.service;

import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.region.domain.LanguageLocation;

import java.util.List;

/**
 *
 */
public interface SubscriptionService {

    void createSubscriptionPacks();

    Subscription createSubscription(long callingNumber, LanguageLocation languageLocation, SubscriptionPack subscriptionPack,
                            SubscriptionOrigin mode);

    Subscription getSubscription(String subscriptionId);

    void updateStartDate(Subscription subscription, DateTime newReferenceDate);

    void deactivateSubscription(Subscription subscription, DeactivationReason reason);

    SubscriptionPack getSubscriptionPack(String name);

    void deleteAll();

    Subscription create(Subscription subscription);

    List<Subscription> findActiveSubscriptions(int page, int pageSize);

    List<Subscription> findActiveSubscriptionsForDay(DayOfTheWeek dayOfTheWeek, int page, int pageSize);
}
