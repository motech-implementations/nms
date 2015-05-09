package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.LocalDate;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionMode;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void add(Subscriber subscriber) {
        subscriberDataService.create(subscriber);
    }

    @Override
    public void update(Subscriber subscriber) {

        // cache the previous version of the subscriber in order to compare before/after
        Subscriber retrievedSubscriber = subscriberDataService.findByCallingNumber(subscriber.getCallingNumber());

        subscriberDataService.update(subscriber);

        // update start dates for subscriptions if reference date (LMP/DOB) has changed
        Set<Subscription> subscriptions = retrievedSubscriber.getActiveSubscriptions();
        Iterator<Subscription> subscriptionIterator = subscriptions.iterator();
        Subscription subscription;

        while (subscriptionIterator.hasNext()) {
            subscription = subscriptionIterator.next();

            if ((subscription.getSubscriptionPack().getType() == SubscriptionPackType.PREGNANCY) &&
                    (retrievedSubscriber.getLastMenstrualPeriod() != subscriber.getLastMenstrualPeriod()))  {

                updateOrSubscribe(subscriber, subscription, subscriber.getLastMenstrualPeriod(),
                        subscriptionService.getSubscriptionPack("pregnancyPack"));

            } else if ((subscription.getSubscriptionPack().getType() == SubscriptionPackType.CHILD) &&
                    (retrievedSubscriber.getDateOfBirth() != subscriber.getDateOfBirth()))  {

                    updateOrSubscribe(subscriber, subscription, subscriber.getDateOfBirth(),
                            subscriptionService.getSubscriptionPack("childPack"));

            }
        }
    }

    private void updateOrSubscribe(Subscriber subscriber, Subscription subscription, LocalDate referenceDate,
                                   SubscriptionPack pack) {
        if (referenceDate == null) {
            return; // TODO: log?
        }

        if (subscription == null) {
            // if not subscribed for pack, subscribe
            subscriptionService.createSubscription(subscriber.getCallingNumber(), subscriber.getLanguage(),
                    pack, SubscriptionMode.MCTS_IMPORT);
        } else {
            // update start date for pack if ACTIVE subscription exists
            subscriptionService.updateStartDate(subscription, referenceDate);
        }
    }

    @Override
    public void delete(Subscriber subscriber) {
        subscriberDataService.delete(subscriber);
    }

}
