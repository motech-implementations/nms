package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.Subscriber;

/**
 * Service interface for managing Kilkari subscribers
 */
public interface SubscriberService {

    /**
     *
     * @param callingNumber
     * @return
     */
    Subscriber getSubscriber(long callingNumber);

    /**
     *
     * @param subscriber
     */
    void create(Subscriber subscriber);

    /**
     *
     * @param subscriber
     */
    void update(Subscriber subscriber);

    /**
     *
     * @param subscriber
     */
    void delete(Subscriber subscriber);
}
