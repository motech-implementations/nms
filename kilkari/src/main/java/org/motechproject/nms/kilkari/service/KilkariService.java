package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.SubscriptionPack;

import java.util.List;

/**
 *
 */
public interface KilkariService {

    List<SubscriptionPack> getSubscriberPacks(String callingNumber);

}
