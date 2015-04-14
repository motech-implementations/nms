package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.KilkariService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * Implementation of the {@link KilkariService} interface.
 */
@Service("kilkariService")
public class KilkariServiceImpl implements KilkariService {

    @Autowired
    private SubscriberDataService subscriberDataService;

    @Override
    public List<SubscriptionPack> getSubscriberPacks(String callingNumber) {
        Set<Subscription> subscriptions = getSubscriptions(callingNumber);
        List<SubscriptionPack> subscriptionPacksForSubscriber = new ArrayList<>();

        for (Subscription subscription : subscriptions) {
            subscriptionPacksForSubscriber.add(subscription.getSubscriptionPack());
        }
        return subscriptionPacksForSubscriber;
    }

    @Override
    public Set<Subscription> getSubscriptions(String callingNumber) {
        Subscriber subscriber = subscriberDataService.findByCallingNumber(callingNumber);
        if (null == subscriber) {
            //todo: handle that better (ie: proper exception?) such that the proper bad response is returned
            throw new IllegalStateException("NULL subscriber");
        }
        return subscriber.getSubscriptions();
    }
}
