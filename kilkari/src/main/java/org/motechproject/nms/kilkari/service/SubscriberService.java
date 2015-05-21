package org.motechproject.nms.kilkari.service;

import org.motechproject.mds.annotations.InstanceLifecycleListener;
import org.motechproject.mds.domain.InstanceLifecycleListenerType;
import org.motechproject.nms.kilkari.domain.Subscriber;

/**
 * Service interface for managing Kilkari subscribers
 */
public interface SubscriberService {

    /**
     * Get the Kilkari subscriber with the specified MSISDN.
     * @param callingNumber MSISDN of the subscriber to create
     * @return The created subscriber.
     */
    Subscriber getSubscriber(long callingNumber);

    /**
     * Create a new Kilkari subscriber in the database.
     * @param subscriber The subscriber to create
     */
    void create(Subscriber subscriber);

    /**
     * Update subscriber. If subscriber has any subscriptions and the update changes her LMP or DOB, then subscription
     * start date (and potentially status) will also be updated.
     * @param subscriber The subscriber to update
     */
    void update(Subscriber subscriber);

    /**
     * Lifecycle listener that verifies a subscriber can only be deleted if all of their subscriptions have
     * been closed at least 6 weeks
     *
     * @param subscriber
     */
    @InstanceLifecycleListener(InstanceLifecycleListenerType.PRE_DELETE)
    void deleteAllowed(Subscriber subscriber);
}
