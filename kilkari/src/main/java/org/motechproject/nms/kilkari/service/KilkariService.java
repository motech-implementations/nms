package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;

/**
 *
 */
public interface KilkariService {

    Subscriber getSubscriber(String callingNumber);

    void createSubscription(String callingNumber, String languageLocationCode, String subscriptionPack);

}
