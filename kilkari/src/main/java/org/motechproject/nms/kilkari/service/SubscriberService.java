package org.motechproject.nms.kilkari.service;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.InstanceLifecycleListener;
import org.motechproject.mds.annotations.InstanceLifecycleListenerType;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;

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
     * @param beneficiary The MCTS beneficiary.
     * @return The subscriber who has a Mother or Child field with the specified ID.
     */
    Subscriber getSubscriberByBeneficiary(final MctsBeneficiary beneficiary);

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
     * Update or create MCTS subscriber and subscription based on data provided by MCTS.
     * @param beneficiary The MCTS beneficiary whose information has been imported from MCTS
     * @param msisdn The MSISDN for the subscriber
     * @param referenceDate The reference data (LMP/DOB) for the beneficiary's subscription
     * @param packType The type of subscription (pregnancy/child)
     * @return New or updated subscription
     */
    Subscription updateOrCreateMctsSubscriber(MctsBeneficiary beneficiary, Long msisdn, DateTime referenceDate,
                                              SubscriptionPackType packType);

    Subscription UpdateMotherSubscriber(Long msisdn, MctsMother mother, DateTime lmp);
    Subscription UpdateChildSubscriber(Long msisdn, MctsChild child, DateTime lmp);

    /**
     * Lifecycle listener that verifies a subscriber can only be deleted if all of their subscriptions have
     * been closed at least 6 weeks
     *
     * @param subscriber
     */
    @InstanceLifecycleListener(InstanceLifecycleListenerType.PRE_DELETE)
    void deleteAllowed(Subscriber subscriber);

}
