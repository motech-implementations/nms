package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the {@link SubscriptionService} interface.
 */
@Service("subscriptionService")
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final int PREGNANCY_PACK_WEEKS = 72;
    private static final int CHILD_PACK_WEEKS = 48;
    private static final int THREE_MONTHS = 90;

    private SubscriberDataService subscriberDataService;
    private SubscriptionPackDataService subscriptionPackDataService;
    private SubscriptionDataService subscriptionDataService;

    @Autowired
    public SubscriptionServiceImpl(SubscriberDataService subscriberDataService,
                                   SubscriptionPackDataService subscriptionPackDataService,
                                   SubscriptionDataService subscriptionDataService) {
        this.subscriberDataService = subscriberDataService;
        this.subscriptionPackDataService = subscriptionPackDataService;
        this.subscriptionDataService = subscriptionDataService;

        createSubscriptionPacks();
    }


    /*
     * Create the subscription packs for Kilkari -- a 48-week child pack and a 72-week pregnancy pack. This service
     * method is effectively internal, but made publicly-accessible so that it can be tested in our ITs.
     */
    @Override
    public final void createSubscriptionPacks() {
        // TODO: make this less hard-coded and hacky once we get spec clarification re: how to populate the pack data
        if (subscriptionPackDataService.byName("childPack") == null) {
            createSubscriptionPack("childPack", SubscriptionPackType.CHILD, CHILD_PACK_WEEKS, 1);
        }
        if (subscriptionPackDataService.byName("pregnancyPack") == null) {
            createSubscriptionPack("pregnancyPack", SubscriptionPackType.PREGNANCY, PREGNANCY_PACK_WEEKS, 2);
        }
    }


    private void createSubscriptionPack(String name, SubscriptionPackType type, int weeks,
                                                    int messagesPerWeek) {
        List<SubscriptionPackMessage> messages = new ArrayList<>();
        for (int week = 1; week <= weeks; week++) {
            messages.add(new SubscriptionPackMessage(week, String.format("w%s_1", week),
                    String.format("w%s_1.wav", week)));

            if (messagesPerWeek == 2) {
                messages.add(new SubscriptionPackMessage(week, String.format("w%s_2", week),
                        String.format("w%s_2.wav", week)));
            }
        }

        subscriptionPackDataService.create(new SubscriptionPack(name, type, weeks, messagesPerWeek, messages));
    }


    @Override
    public Subscription createSubscription(long callingNumber, LanguageLocation languagelocation, SubscriptionPack subscriptionPack,
                                   SubscriptionOrigin mode) {
        Subscriber subscriber = subscriberDataService.findByCallingNumber(callingNumber);
        if (subscriber == null) {
            subscriber = new Subscriber(callingNumber, languagelocation);
            subscriberDataService.create(subscriber);
        }

        if (mode == SubscriptionOrigin.IVR) {
            return createSubscriptionViaIvr(subscriber, subscriptionPack);
        } else { // MCTS_UPLOAD
            return createSubscriptionViaMcts(subscriber, subscriptionPack);
        }
    }

    private Subscription createSubscriptionViaIvr(Subscriber subscriber, SubscriptionPack pack) {
        Iterator<Subscription> subscriptionIterator = subscriber.getSubscriptions().iterator();
        Subscription existingSubscription;

        while (subscriptionIterator.hasNext()) {

            existingSubscription = subscriptionIterator.next();

            if (existingSubscription.getSubscriptionPack().equals(pack)) {
                if (existingSubscription.getStatus().equals(SubscriptionStatus.ACTIVE) ||
                        existingSubscription.getStatus().equals(SubscriptionStatus.PENDING_ACTIVATION)) {
                    // subscriber already has an active subscription to this pack, don't create a new one
                    return null;
                }
            }
        }

        Subscription subscription = new Subscription(subscriber, pack, SubscriptionOrigin.IVR);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(DateTime.now().plusDays(1));

        return subscriptionDataService.create(subscription);
    }

    private Subscription createSubscriptionViaMcts(Subscriber subscriber, SubscriptionPack pack) {
        Subscription subscription;

        if (subscriber.getDateOfBirth() != null && pack.getType() == SubscriptionPackType.CHILD) {
            if (subscriberHasActivePackType(subscriber, SubscriptionPackType.CHILD) ||
                    (Subscription.hasCompletedForStartDate(subscriber.getDateOfBirth(), DateTime.now(), pack))) {

                // TODO: #138 log the rejected subscription
                return null;
            } else {
                subscription = new Subscription(subscriber, pack, SubscriptionOrigin.MCTS_IMPORT);
                subscription.setStartDate(subscriber.getDateOfBirth());
                subscription.setStatus(SubscriptionStatus.ACTIVE);
            }
        } else if (subscriber.getLastMenstrualPeriod() != null && subscriber.getDateOfBirth() == null &&
                pack.getType() == SubscriptionPackType.PREGNANCY) {
            if (subscriberHasActivePackType(subscriber, SubscriptionPackType.PREGNANCY) ||
                    Subscription.hasCompletedForStartDate(subscriber.getLastMenstrualPeriod().plusDays(THREE_MONTHS),
                            DateTime.now(), pack)) {
                // TODO: #138 log the rejected subscription
                return null;
            } else {
                // TODO: #160 deal with early subscription
                subscription = new Subscription(subscriber, pack, SubscriptionOrigin.MCTS_IMPORT);

                // the pregnancy pack starts 3 months after LMP
                subscription.setStartDate(subscriber.getLastMenstrualPeriod().plusDays(THREE_MONTHS));
                subscription.setStatus(SubscriptionStatus.ACTIVE);
            }
        } else {
            // TODO: #138 need to log other error cases?
            return null;
        }

        return subscriptionDataService.create(subscription);
    }

    private boolean subscriberHasActivePackType(Subscriber subscriber, SubscriptionPackType type) {
        Iterator<Subscription> subscriptionIterator = subscriber.getSubscriptions().iterator();
        Subscription existingSubscription;

        while (subscriptionIterator.hasNext()) {
            existingSubscription = subscriptionIterator.next();
            if (existingSubscription.getSubscriptionPack().getType() == type) {
                if (type == SubscriptionPackType.PREGNANCY &&
                        (existingSubscription.getStatus() == SubscriptionStatus.ACTIVE ||
                         existingSubscription.getStatus() == SubscriptionStatus.PENDING_ACTIVATION)) {
                    return true;
                }
                if (type == SubscriptionPackType.CHILD && existingSubscription.getStatus() == SubscriptionStatus.ACTIVE) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Subscription getSubscription(String subscriptionId) {
        return subscriptionDataService.findBySubscriptionId(subscriptionId);
    }

    /**
     * Called by Subscriber.update if the subscriber's LMP or DOB changes -- as a result, the subscription's start date
     * and/or status may change
     * @param subscription The subscription to update
     * @param newReferenceDate The new LMP or DOB from which to base the new subscription start date
     */
    @Override
    public void updateStartDate(Subscription subscription, DateTime newReferenceDate) {
        if (subscription.getSubscriptionPack().getType() == SubscriptionPackType.PREGNANCY) {
            subscription.setStartDate(newReferenceDate.plusDays(THREE_MONTHS));
        } else { // CHILD pack
            subscription.setStartDate(newReferenceDate);
        }

        if (Subscription.hasCompletedForStartDate(subscription.getStartDate(), DateTime.now(),
                subscription.getSubscriptionPack())) {
            subscription.setStatus(SubscriptionStatus.COMPLETED);
        }
        subscriptionDataService.update(subscription);
    }

    @Override
    public void deactivateSubscription(Subscription subscription, DeactivationReason reason) {
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE ||
                subscription.getStatus() == SubscriptionStatus.PENDING_ACTIVATION) {
            subscription.setStatus(SubscriptionStatus.DEACTIVATED);
            subscription.setDeactivationReason(reason);
            subscriptionDataService.update(subscription);
        }
        // Else no-op
    }

    @Override
    public SubscriptionPack getSubscriptionPack(String name) {
        return subscriptionPackDataService.byName(name);
    }


    /**
     * To be used by ITs only!
     */
    public void deleteAll() {
        subscriptionDataService.deleteAll();
    }


    public Subscription create(Subscription subscription) {
        return subscriptionDataService.create(subscription);
    }


    public List<Subscription> findActiveSubscriptions(int page, int pageSize) {
        return subscriptionDataService.findByStatus(SubscriptionStatus.ACTIVE, new QueryParams(page, pageSize));
    }


    public List<Subscription> findActiveSubscriptionsForDay(DayOfTheWeek dayOfTheWeek, int page, int pageSize) {
        return subscriptionDataService.findByStatusAndDay(SubscriptionStatus.ACTIVE, dayOfTheWeek,
                new QueryParams(page, pageSize));
    }
}
