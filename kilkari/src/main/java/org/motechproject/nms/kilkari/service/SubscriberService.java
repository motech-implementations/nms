package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.Subscriber;

public interface SubscriberService {
    Subscriber getSubscriber(long callingNumber);

    void add(Subscriber subscriber);

    void update(Subscriber subscriber);

    void delete(Subscriber subscriber);
}
