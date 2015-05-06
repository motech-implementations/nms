package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.LocalDate;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.InboxCallDetails;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionMode;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.InboxCallDetailsDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.language.domain.Language;
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

    private SubscriberService subscriberService;
    private SubscriptionPackDataService subscriptionPackDataService;
    private SubscriptionDataService subscriptionDataService;
    private InboxCallDetailsDataService inboxCallDetailsDataService;

    @Autowired
    public SubscriptionServiceImpl(SubscriberService subscriberService,
                                   SubscriptionPackDataService subscriptionPackDataService,
                                   SubscriptionDataService subscriptionDataService,
                                   InboxCallDetailsDataService inboxCallDetailsDataService) {
        this.subscriberService = subscriberService;
        this.subscriptionPackDataService = subscriptionPackDataService;
        this.subscriptionDataService = subscriptionDataService;
        this.inboxCallDetailsDataService = inboxCallDetailsDataService;

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
            createSubscriptionPack("childPack", SubscriptionPackType.CHILD, 48, 1);
        }
        if (subscriptionPackDataService.byName("pregnancyPack") == null) {
            createSubscriptionPack("pregnancyPack", SubscriptionPackType.PREGNANCY, 72, 2);
        }
    }


    private void createSubscriptionPack(String name, SubscriptionPackType type, int weeks,
                                                    int messagesPerWeek) {
        List<SubscriptionPackMessage> messages = new ArrayList<>();
        for (int week = 1; week <= weeks; week++) {
            messages.add(new SubscriptionPackMessage(week, String.format("week%s-1.wav", week)));

            if (messagesPerWeek == 2) {
                messages.add(new SubscriptionPackMessage(week, String.format("week%s-2.wav", week)));
            }
        }

        subscriptionPackDataService.create(new SubscriptionPack(name, type, messagesPerWeek, messages));
    }


    @Override
    public void createSubscription(long callingNumber, Language language, SubscriptionPack subscriptionPack,
                                   SubscriptionMode mode) {
        Subscriber subscriber = subscriberService.getSubscriber(callingNumber);
        if (subscriber == null) {
            subscriber = new Subscriber(callingNumber, language);
            subscriberService.add(subscriber);
        }

        if (mode == SubscriptionMode.IVR) {
            createSubscriptionViaIvr(subscriber, subscriptionPack);
        } else { // MCTS_UPLOAD
            createSubscriptionViaMcts(subscriber, subscriptionPack);
        }
    }

    private void createSubscriptionViaIvr(Subscriber subscriber, SubscriptionPack pack) {
        Iterator<Subscription> subscriptionIterator = subscriber.getSubscriptions().iterator();
        Subscription existingSubscription;

        while (subscriptionIterator.hasNext()) {

            existingSubscription = subscriptionIterator.next();

            if (existingSubscription.getSubscriptionPack().equals(pack)) {
                if (existingSubscription.getStatus().equals(SubscriptionStatus.ACTIVE) ||
                        existingSubscription.getStatus().equals(SubscriptionStatus.PENDING_ACTIVATION)) {
                    // subscriber already has an active subscription to this pack, don't create a new one
                    return;
                }
            }
        }

        Subscription subscription = new Subscription(subscriber, pack, SubscriptionMode.IVR);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(LocalDate.now().plusDays(1));

        subscriptionDataService.create(subscription);
    }

    private void createSubscriptionViaMcts(Subscriber subscriber, SubscriptionPack pack) {

        if (subscriber.getDateOfBirth() != null && pack.getType() == SubscriptionPackType.CHILD) {
            if (subscriberHasActivePackType(subscriber, SubscriptionPackType.CHILD)) {
                // TODO: #138 log the rejected subscription
                return;
            }
        } else if (subscriber.getLastMenstrualPeriod() != null && subscriber.getDateOfBirth() == null &&
                pack.getType() == SubscriptionPackType.PREGNANCY) {
            if (subscriberHasActivePackType(subscriber, SubscriptionPackType.PREGNANCY)) {
                // TODO: #138 log the rejected subscription
                return;
            }
        }

        Subscription subscription = new Subscription(subscriber, pack, SubscriptionMode.MCTS_IMPORT);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        // TODO: #157 set the start date based on LMP/DOB from MCTS
        subscriptionDataService.create(subscription);
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

    @Override
    public void deactivateSubscription(Subscription subscription, DeactivationReason reason) {
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE ||
                subscription.getStatus() == SubscriptionStatus.PENDING_ACTIVATION) {
            subscription.setStatus(SubscriptionStatus.DEACTIVATED);
            subscription.setDeactivationReason(reason);
            subscriptionDataService.update(subscription);

            // Eventually more will happen here -- e.g. the user's Inbox will be decommissioned
        }
        // Else no-op
    }

    @Override
    public SubscriptionPack getSubscriptionPack(String name) {
        return subscriptionPackDataService.byName(name);
    }

    // TODO: move to a new InboxService eventually
    @Override
    public long addInboxCallDetails(InboxCallDetails inboxCallDetails) {
        InboxCallDetails newRecord = inboxCallDetailsDataService.create(inboxCallDetails);
        return (long) inboxCallDetailsDataService.getDetachedField(newRecord, "id");
    }
}
