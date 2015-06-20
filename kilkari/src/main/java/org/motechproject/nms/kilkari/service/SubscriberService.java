package org.motechproject.nms.kilkari.service;

import org.motechproject.mds.annotations.InstanceLifecycleListener;
import org.motechproject.mds.domain.InstanceLifecycleListenerType;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.Subscriber;

/**
 * Service interface for managing Kilkari subscribers
 */
public interface SubscriberService {

    /**
     * Get the Kilkari subscriber with the specified MSISDN.
     * @param callingNumber MSISDN of the subscriber to retrieve
     * @return The subscriber.
     */
    Subscriber getSubscriber(long callingNumber);

    /**
     * Get the Kilkari subscriber corresponding to the specified MCTS beneficiary.
     * @param beneficiaryId ID of the MCTS beneficiary.
     * @return The subscriber who has a Mother or Child field with the specified ID.
     */
    Subscriber getSubscriberByBeneficiaryId(final String beneficiaryId);

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
     * Update MSISDN for subscriber. If the new MSISDN is already in use by a different MCTS beneficiary for the same
     * subscription pack, the update will be rejected.
     * @param subscriber Existing subscriber object
     * @param beneficiary The MCTS beneficary (mother or child) to update
     * @param newMsisdn The new MSISDN for the subscriber
     */
    void updateMsisdnForSubscriber(Subscriber subscriber, MctsBeneficiary beneficiary, Long newMsisdn);

    /**
     * Lifecycle listener that verifies a subscriber can only be deleted if all of their subscriptions have
     * been closed at least 6 weeks
     *
     * @param subscriber
     */
    @InstanceLifecycleListener(InstanceLifecycleListenerType.PRE_DELETE)
    void deleteAllowed(Subscriber subscriber);
}
