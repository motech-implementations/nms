package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
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

    @Autowired
    private SubscriberDataService subscriberDataService;

    @Autowired
    private SubscriptionPackDataService subscriptionPackDataService;

    @Autowired
    private SubscriptionDataService subscriptionDataService;

    @Autowired
    private LanguageDataService languageDataService;

    @Override
    public Subscriber getSubscriber(String callingNumber) {
        return subscriberDataService.findByCallingNumber(callingNumber);
    }

    @Override
    public void createSubscription(String callingNumber, int languageLocationCode, String subscriptionPack) {
        Subscriber subscriber = subscriberDataService.findByCallingNumber(callingNumber);
        if (subscriber == null) {
            subscriberDataService.create(new Subscriber(callingNumber));
            subscriber = getSubscriber(callingNumber);
        }

        SubscriptionPack pack = subscriptionPackDataService.findByName(subscriptionPack);
        Language language = languageDataService.findByCode(languageLocationCode);

        subscriptionDataService.create(new Subscription(subscriber, pack, language));
    }
}
