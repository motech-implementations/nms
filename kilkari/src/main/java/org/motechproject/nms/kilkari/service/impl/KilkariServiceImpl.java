package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.nms.kilkari.domain.InboxCallDetails;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.InboxCallDetailsDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.KilkariService;
import org.motechproject.nms.language.domain.Language;
import org.motechproject.nms.language.repository.LanguageDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link KilkariService} interface.
 */
@Service("kilkariService")
public class KilkariServiceImpl implements KilkariService {

    private SubscriberDataService subscriberDataService;
    private SubscriptionPackDataService subscriptionPackDataService;
    private SubscriptionDataService subscriptionDataService;
    private LanguageDataService languageDataService;
    private InboxCallDetailsDataService inboxCallDetailsDataService;

    @Autowired
    public KilkariServiceImpl(SubscriberDataService subscriberDataService,
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
    public void createSubscription(long callingNumber, String languageLocationCode, String subscriptionPack) {
        Subscriber subscriber = subscriberDataService.findByCallingNumber(callingNumber);
        if (subscriber == null) {
            subscriberDataService.create(new Subscriber(callingNumber));
            subscriber = getSubscriber(callingNumber);
        }

        SubscriptionPack pack = subscriptionPackDataService.byName(subscriptionPack);
        Language language = languageDataService.findByCode(languageLocationCode);

        subscriptionDataService.create(new Subscription(subscriber, pack, language));
    }

    @Override
    public Subscription getSubscription(String subscriptionId) {
        return subscriptionDataService.findBySubscriptionId(subscriptionId);
    }

    @Override
    public void deactivateSubscription(Subscription subscription) {
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE ||
                subscription.getStatus() == SubscriptionStatus.PENDING_ACTIVATION) {
            subscription.setStatus(SubscriptionStatus.DEACTIVATED);
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

    @Override
    public long addInboxCallDetails(InboxCallDetails inboxCallDetails) {
        InboxCallDetails newRecord = inboxCallDetailsDataService.create(inboxCallDetails);
        return (long) inboxCallDetailsDataService.getDetachedField(newRecord, "id");
    }
}
