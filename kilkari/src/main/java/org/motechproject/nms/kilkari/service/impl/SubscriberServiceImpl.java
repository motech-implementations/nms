package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.Set;


/**
 * Implementation of the {@link SubscriberService} interface.
 */
@Service("subscriberService")
public class SubscriberServiceImpl implements SubscriberService {

    private SubscriberDataService subscriberDataService;
    private SubscriptionService subscriptionService;

    @Autowired
    public SubscriberServiceImpl(SubscriberDataService subscriberDataService, SubscriptionService subscriptionService) {
        this.subscriberDataService = subscriberDataService;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public Subscriber getSubscriber(long callingNumber) {
        return subscriberDataService.findByCallingNumber(callingNumber);
    }

    @Override
    public void create(Subscriber subscriber) {
        subscriberDataService.create(subscriber);
    }

    @Override
    @Transactional
    public void update(Subscriber subscriber) {

        Subscriber retrievedSubscriber = subscriberDataService.findByCallingNumber(subscriber.getCallingNumber());

        subscriberDataService.update(subscriber);

        // update start dates for subscriptions if reference date (LMP/DOB) has changed
        Set<Subscription> subscriptions = retrievedSubscriber.getActiveSubscriptions();
        Iterator<Subscription> subscriptionIterator = subscriptions.iterator();
        Subscription subscription;

        while (subscriptionIterator.hasNext()) {
            subscription = subscriptionIterator.next();

            if (subscription.getSubscriptionPack().getType() == SubscriptionPackType.PREGNANCY)  {

                subscriptionService.updateStartDate(subscription, subscriber.getLastMenstrualPeriod());

            } else if (subscription.getSubscriptionPack().getType() == SubscriptionPackType.CHILD) {

                subscriptionService.updateStartDate(subscription, subscriber.getDateOfBirth());

            }
        }
    }

    public void deleteAllowed(Subscriber subscriber) {
        for (Subscription subscription: subscriber.getSubscriptions()) {
            subscriptionService.deletePreconditionCheck(subscription);
        }
    }
}
