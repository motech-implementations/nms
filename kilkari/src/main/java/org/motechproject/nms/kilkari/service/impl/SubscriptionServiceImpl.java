package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.nms.kilkari.domain.InboxCallDetails;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.domain.SubscriptionMode;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.repository.InboxCallDetailsDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.language.domain.Language;
import org.motechproject.nms.language.repository.LanguageDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.joda.time.LocalDate;
import java.util.Iterator;

/**
 * Implementation of the {@link SubscriptionService} interface.
 */
@Service("subscriptionService")
public class SubscriptionServiceImpl implements SubscriptionService {

    private SubscriberDataService subscriberDataService;
    private SubscriptionPackDataService subscriptionPackDataService;
    private SubscriptionDataService subscriptionDataService;
    private LanguageDataService languageDataService;
    private InboxCallDetailsDataService inboxCallDetailsDataService;

    @Autowired
    public SubscriptionServiceImpl(SubscriberDataService subscriberDataService,
                                   SubscriptionPackDataService subscriptionPackDataService,
                                   SubscriptionDataService subscriptionDataService,
                                   LanguageDataService languageDataService,
                                   InboxCallDetailsDataService inboxCallDetailsDataService) {
        this.subscriberDataService = subscriberDataService;
        this.subscriptionPackDataService = subscriptionPackDataService;
        this.subscriptionDataService = subscriptionDataService;
        this.languageDataService = languageDataService;
        this.inboxCallDetailsDataService = inboxCallDetailsDataService;
    }

    @Override
    public Subscriber getSubscriber(long callingNumber) {
        return subscriberDataService.findByCallingNumber(callingNumber);
    }

    @Override
    public void createSubscription(long callingNumber, String languageLocationCode, String subscriptionPack,
                                   SubscriptionMode mode) {
        Subscriber subscriber = subscriberDataService.findByCallingNumber(callingNumber);
        if (subscriber == null) {
            subscriberDataService.create(new Subscriber(callingNumber));
            subscriber = getSubscriber(callingNumber);
        }

        Language language = languageDataService.findByCode(languageLocationCode);

        SubscriptionPack pack = subscriptionPackDataService.byName(subscriptionPack);

        if (mode == SubscriptionMode.IVR) {
            createSubscriptionViaIvr(subscriber, language, pack);
        } else { // MCTS_UPLOAD
            createSubscriptionViaMcts(subscriber, language, pack);
        }
    }

    private void createSubscriptionViaIvr(Subscriber subscriber, Language language, SubscriptionPack pack) {
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

        Subscription subscription = new Subscription(subscriber, pack, language, SubscriptionMode.IVR);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(LocalDate.now().plusDays(1));

        subscriptionDataService.create(subscription);
    }

    private void createSubscriptionViaMcts(Subscriber subscriber, Language language, SubscriptionPack pack) {

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

        Subscription subscription = new Subscription(subscriber, pack, language, SubscriptionMode.MCTS_IMPORT);
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

    @Override
    public long getCountSubscriptionPack(String name) {
        return subscriptionPackDataService.countByName(name);
    }

    // TODO: move to a new InboxService eventually
    @Override
    public long addInboxCallDetails(InboxCallDetails inboxCallDetails) {
        InboxCallDetails newRecord = inboxCallDetailsDataService.create(inboxCallDetails);
        return (long) inboxCallDetailsDataService.getDetachedField(newRecord, "id");
    }
}
