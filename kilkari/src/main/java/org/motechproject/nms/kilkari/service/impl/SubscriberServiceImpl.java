package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Implementation of the {@link SubscriptionService} interface.
 */
@Service("subscriberService")
public class SubscriberServiceImpl implements SubscriberService {

    private SubscriberDataService subscriberDataService;

    @Autowired
    public SubscriberServiceImpl(SubscriberDataService subscriberDataService) {
        this.subscriberDataService = subscriberDataService;
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
        subscriberDataService.update(subscriber);
    }

    @Override
    public void delete(Subscriber subscriber) {
        subscriberDataService.delete(subscriber);
    }

}
