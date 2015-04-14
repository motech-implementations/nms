package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.Subscription;

import java.util.List;
import java.util.Set;

/**
 *
 */
public interface KilkariService {

    List<SubscriptionPack> getSubscriberPacks(String callingNumber);

    Set<Subscription> getSubscriptions(String callingNumber);

}
