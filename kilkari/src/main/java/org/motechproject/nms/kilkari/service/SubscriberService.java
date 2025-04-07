package org.motechproject.nms.kilkari.service;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.InstanceLifecycleListener;
import org.motechproject.mds.annotations.InstanceLifecycleListenerType;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service interface for managing Kilkari subscribers
 */
public interface SubscriberService {

    /**
     * Get the Kilkari subscriber with the specified MSISDN.
     * @param callingNumber MSISDN of the subscriber to retrieve
     * @return The subscriber.
     */
    List<Subscriber> getSubscriber(long callingNumber);

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
    Subscriber create(Subscriber subscriber);

    /**
     * Update start date subscriber's subscriptions. If subscriber has any subscriptions and the update changes her
     * LMP or DOB, then subscription start date (and potentially status) will also be updated.
     * @param subscriber The subscriber to update
     */
    void updateStartDate(Subscriber subscriber);

    /**
     * Update MSISDN for subscriber. If the new MSISDN is already in use by a different MCTS beneficiary for the same
     * subscription pack, the update will be rejected.
     * @param subscriber Existing subscriber object
     * @param beneficiary The MCTS beneficary (mother or child) to update
     * @param newMsisdn The new MSISDN for the subscriber
     */
    void updateMsisdnForSubscriber(Subscriber subscriber, MctsBeneficiary beneficiary, Long newMsisdn);

    /**
     * Update the MCTS mother subscriber with the msisdn and mother object
     * @param msisdn msisdn (to switch to) for new/existing mother
     * @param mother mother object to update
     * @param lmp the reference date for the mother (last menstrual period)
     * @return New or updated subscription, null if the creation/update fails
     */
    Subscription updateMotherSubscriber(Long msisdn, MctsMother mother, DateTime lmp, Map<String, Object> record, String action, String name,DateTime motherDOB,LocalDate lastUpdatedDateNic);

    /**
     * Update the RCH mother subscriber with the msisdn and mother object
     * @param msisdn msisdn (to switch to) for new/existing mother
     * @param mother mother object to update
     * @param lmp the reference date for the mother (last menstrual period)
     * @param caseNo the pregnancy number for new/existing mother
     * @param deactivate boolean to check if subscription needs to be deactivated due to abortion, stillbirth or death
     * @return New or updated subscription, null if the creation/update fails
     */
    Subscription updateRchMotherSubscriber(Long msisdn, MctsMother mother, DateTime lmp, Long caseNo, Boolean deactivate, Map<String, Object> record, String action,String name,DateTime motherDOB,LocalDate lastUpdatedDateNic , DateTime motherRegistrationDate);

    /**
     * Update the child subscriber with the msisdn and child object
     * @param msisdn msisdn (to switch to) for new/existing child
     * @param child child object to update
     * @param dob the reference date for the child (date of birth)
     * @return New or updated subscription, null if creation/update fails
     */
    ChildImportRejection updateChildSubscriber(Long msisdn, MctsChild child, DateTime dob, Map<String, Object> record, String action);

    /**
     * Lifecycle listener that verifies a subscriber can only be deleted if all of their subscriptions have
     * been closed at least 6 weeks
     *
     * @param subscriber
     */
    @InstanceLifecycleListener(InstanceLifecycleListenerType.PRE_DELETE)
    void deleteAllowed(Subscriber subscriber);


    /**
     * Deactivate all Subscriptions of a given callingNumber on MOHFW request
     * @param callingNumber
     */
    void deactivateAllSubscriptionsForSubscriber(long callingNumber, DeactivationReason deactivationReason);

    /**
     * Get the Kilkari subscriber with the specified mother id OID.
     * @param motherId Mother id oid of the subscriber to retrieve
     * @return The subscriber.
     */
    Subscriber getSubscriberListByMother(final long motherId);

    /**
     * Update the RCH child subscriber with msisdn and child object
     * @param msisdn msisdn (to switch to) for new/existing child
     * @param child child object to update
     * @param dob reference date for the child
     * @return new or updated subscription, null if create/update fails
     */
    ChildImportRejection updateRchChildSubscriber(Long msisdn, MctsChild child, DateTime dob, Map<String, Object> record, String action);

    void processChunkOfDeactivation(List<String[]> chunk, AtomicInteger successCount, List<String> failureMessages);
}
