package org.motechproject.nms.kilkari.service.impl;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.joda.time.DateTime;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionError;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionRejectionReason;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.domain.BlockedMsisdnRecord;
import org.motechproject.nms.kilkari.domain.DeactivationSubscriptionAuditRecord;
import org.motechproject.nms.kilkari.domain.AuditStatus;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.repository.BlockedMsisdnRecordDataService;
import org.motechproject.nms.kilkari.repository.DeactivationSubscriptionAuditRecordDataService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jdo.Query;
import java.util.Iterator;
import java.util.Set;


/**
 * Implementation of the {@link SubscriberService} interface.
 */
@Service("subscriberService")
public class SubscriberServiceImpl implements SubscriberService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriberServiceImpl.class);

    private SubscriberDataService subscriberDataService;
    private SubscriptionService subscriptionService;
    private SubscriptionDataService subscriptionDataService;
    private SubscriptionErrorDataService subscriptionErrorDataService;
    private SubscriptionPackDataService subscriptionPackDataService;
    private DeactivationSubscriptionAuditRecordDataService deactivationSubscriptionAuditRecordDataService;
    private BlockedMsisdnRecordDataService blockedMsisdnRecordDataService;

    @Autowired
    public SubscriberServiceImpl(SubscriberDataService subscriberDataService, SubscriptionService subscriptionService,
                                 SubscriptionDataService subscriptionDataService,
                                 SubscriptionErrorDataService subscriptionErrorDataService,
                                 SubscriptionPackDataService subscriptionPackDataService,
                                 BlockedMsisdnRecordDataService blockedMsisdnRecordDataService,
                                 DeactivationSubscriptionAuditRecordDataService deactivationSubscriptionAuditRecordDataService) {
        this.subscriberDataService = subscriberDataService;
        this.subscriptionService = subscriptionService;
        this.subscriptionDataService = subscriptionDataService;
        this.subscriptionErrorDataService = subscriptionErrorDataService;
        this.subscriptionPackDataService = subscriptionPackDataService;
        this.deactivationSubscriptionAuditRecordDataService = deactivationSubscriptionAuditRecordDataService;
        this.blockedMsisdnRecordDataService = blockedMsisdnRecordDataService;
    }

    @Override
    public Subscriber getSubscriber(final long callingNumber) {

        SqlQueryExecution<Subscriber> queryExecution = new SqlQueryExecution<Subscriber>() {

            @Override
            public String getSqlQuery() {
                return KilkariConstants.SELECT_SUBSCRIBERS_BY_NUMBER;
            }

            @Override
            public Subscriber execute(Query query) {
                query.setClass(Subscriber.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(callingNumber);
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (Subscriber) fqr.get(0);
                }
                throw new IllegalStateException(String.format(KilkariConstants.MORE_THAN_ONE_SUBSCRIBER, callingNumber));
            }
        };

        return subscriberDataService.executeSQLQuery(queryExecution);

    }

    @Override
    public Subscriber getSubscriberByBeneficiary(final MctsBeneficiary beneficiary) {

        SqlQueryExecution<Subscriber> queryExecution = new SqlQueryExecution<Subscriber>() {

            @Override
            public String getSqlQuery() {
                return "select *  from nms_subscribers where mother_id_OID = ? or child_id_OID = ?";
            }

            @Override
            public Subscriber execute(Query query) {
                query.setClass(Subscriber.class);
                Long id = beneficiary.getId();
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(id, id);
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (Subscriber) fqr.get(0);
                }
                throw new IllegalStateException(String.format("More than one row returned for beneficiary %s", id));
            }
        };

        return subscriberDataService.executeSQLQuery(queryExecution);

    }

    @Override
    public Subscriber create(Subscriber subscriber) {
        return subscriberDataService.create(subscriber);
    }

    @Override
    @Transactional
    public void updateStartDate(Subscriber subscriber) {

        Subscriber retrievedSubscriber = subscriberDataService.update(subscriber);

        // update start dates for subscriptions if reference date (LMP/DOB) has changed
        Set<Subscription> subscriptions = retrievedSubscriber.getActiveAndPendingSubscriptions();
        Iterator<Subscription> subscriptionIterator = subscriptions.iterator();
        Subscription subscription;

        while (subscriptionIterator.hasNext()) {
            subscription = subscriptionIterator.next();

            if (subscription.getSubscriptionPack().getType() == SubscriptionPackType.PREGNANCY) {

                if (subscriber.getLastMenstrualPeriod() != null) { // Subscribers via IVR will not have LMP
                    subscriptionService.updateStartDate(subscription, subscriber.getLastMenstrualPeriod());
                }

            } else { // SubscriptionPackType.CHILD

                if (subscriber.getDateOfBirth() != null) {  // Subscribers via IVR will not have DOB
                    subscriptionService.updateStartDate(subscription, subscriber.getDateOfBirth());
                }
            }
        }
    }

    @Override
    public void updateMsisdnForSubscriber(Subscriber subscriber, MctsBeneficiary beneficiary, Long newMsisdn) {
        SubscriptionPackType packType;
        packType = (beneficiary instanceof MctsChild) ? SubscriptionPackType.CHILD : SubscriptionPackType.PREGNANCY;

        Subscriber subscriberWithMsisdn = getSubscriber(newMsisdn);
        if (subscriberWithMsisdn != null) {
            // this number is in use
            if (subscriptionService.getActiveSubscription(subscriberWithMsisdn, packType) != null) {
                // in fact, it's in use for this pack -- reject the subscription
                throw new IllegalStateException(
                        String.format("Can't change subscriber's MSISDN (%s) because it is already in use for %s pack",
                                newMsisdn, packType));
            }
        }

        // do the update -- by creating a new Subscriber object and re-linking the beneficiary and subscription to it

        Subscriber newSubscriber = new Subscriber(newMsisdn, subscriber.getLanguage(), subscriber.getCircle());
        Subscription subscription = subscriptionService.getActiveSubscription(subscriber, packType);
        newSubscriber.getSubscriptions().add(subscription);
        subscription.setSubscriber(newSubscriber);
        subscriber.getSubscriptions().remove(subscription);

        if (packType == SubscriptionPackType.CHILD) {
            subscriber.setChild(null);
            newSubscriber.setChild((MctsChild) beneficiary);
            newSubscriber.setDateOfBirth(subscriber.getDateOfBirth());
        } else {
            subscriber.setMother(null);
            newSubscriber.setMother((MctsMother) beneficiary);
            newSubscriber.setLastMenstrualPeriod(subscriber.getLastMenstrualPeriod());
        }

        subscriberDataService.create(newSubscriber);
        subscriberDataService.update(subscriber);
        subscriptionDataService.update(subscription);
    }

    @Override
    public Subscription updateMotherSubscriber(Long msisdn, MctsMother motherUpdate, DateTime lmp) { //NOPMD NcssMethodCount
        District district = motherUpdate.getDistrict(); // district should never be null here since we validate upstream on setLocation
        Circle circle = district.getCircle();
        Language language = district.getLanguage();
        SubscriptionPack pack = subscriptionPackDataService.byType(SubscriptionPackType.PREGNANCY);
        Subscriber subscriberByMsisdn = getSubscriber(msisdn);
        Subscriber subscriberByMctsId = getSubscriberByBeneficiary(motherUpdate);

        if (subscriberByMctsId == null) {   // No existing subscriber(number) attached to mother MCTS id
            if (subscriberByMsisdn == null) {   // No subscriber attached to the number
                // create subscriber, beneficiary, subscription and return
                subscriberByMsisdn = new Subscriber(msisdn, language);
                subscriberByMsisdn.setLastMenstrualPeriod(lmp);
                subscriberByMsisdn.setMother(motherUpdate);
                create(subscriberByMsisdn);
                return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
            } else { // subscriber (number) is already in use
                if (subscriberByMsisdn.getMother() == null) {   // number has no mother attached
                    subscriberByMsisdn.setLastMenstrualPeriod(lmp);
                    subscriberByMsisdn.setMother(motherUpdate);
                    updateStartDate(subscriberByMsisdn);
                    return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
                } else {
                    subscriptionErrorDataService.create(new SubscriptionError(msisdn, SubscriptionRejectionReason.ALREADY_SUBSCRIBED, pack.getType()));
                    return null;
                }
            }
        } else { // subscriberByBeneficiary != null aka. MCTS mother exists in motech
            if (subscriberByMsisdn == null) {   //no subscriber attached to the new number
                // We got here because beneficiary's phone number changed
                // detach mother from existing subscriber
                deactivateSubscriptionForSubscriberAndPackType(subscriberByMctsId, pack.getType(), DeactivationReason.MCTS_UPDATE);
                subscriberByMctsId.setMother(null);
                subscriberDataService.update(subscriberByMctsId);

                // create new subscriber and attach mother
                Subscriber newSubscriber = new Subscriber(msisdn, language);
                newSubscriber.setLastMenstrualPeriod(lmp);
                newSubscriber.setMother(motherUpdate);
                create(newSubscriber);
                return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
            } else {    // we have a subscriber by phone# and also one with the MCTS id

                if (subscriberByMsisdn.getId().equals(subscriberByMctsId.getId())) {    //we pulled the same subscriber
                    Subscription subscription = subscriptionService.getActiveSubscription(subscriberByMctsId, pack.getType());
                    subscriberByMsisdn.setLastMenstrualPeriod(lmp);
                    subscriberByMsisdn.getMother().deepCopyFrom(motherUpdate);
                    updateStartDate(subscriberByMsisdn);
                    if (subscription != null) { // update existing active subscription
                        subscriptionService.updateStartDate(subscription, lmp);
                        return subscriptionService.getActiveSubscription(subscriberByMctsId, pack.getType());
                    } else {    // just create a new subscription
                        return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
                    }
                } else {    // msisdn is already taken by another beneficiary

                    if (subscriberByMsisdn.getMother() == null) { // if existing subscriber has no mother attached
                        // Deactivate mother from existing subscriber (by mcts id)
                        deactivateSubscriptionForSubscriberAndPackType(subscriberByMctsId, pack.getType(), DeactivationReason.MCTS_UPDATE);
                        subscriberByMctsId.setMother(null);
                        updateStartDate(subscriberByMctsId);

                        // transfer mother to new subscriber (number)
                        subscriberByMsisdn.setMother(motherUpdate);
                        updateStartDate(subscriberByMsisdn);
                        return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);

                    } else {    // No way to resolve this since msisdn already has a mother attached. Reject the update
                        // Deactivate subscription attached to the old msisdn and log error.
                        Subscription subscription = subscriptionService.getActiveSubscription(subscriberByMctsId, pack.getType());
                        subscriptionService.deactivateSubscription(subscription, DeactivationReason.MCTS_UPDATE);
                        subscriptionErrorDataService.create(new SubscriptionError(msisdn, SubscriptionRejectionReason.ALREADY_SUBSCRIBED, pack.getType()));
                        return null;
                    }
                }
            }
        }
    }

    @Override
    public Subscription updateChildSubscriber(Long msisdn, MctsChild childUpdate, DateTime dob) { //NOPMD NcssMethodCount
        District district = childUpdate.getDistrict(); // district should never be null here since we validate upstream on setLocation
        Circle circle = district.getCircle();
        Language language = district.getLanguage();
        SubscriptionPack pack = subscriptionPackDataService.byType(SubscriptionPackType.CHILD);
        Subscriber subscriberByMsisdn = getSubscriber(msisdn);
        Subscriber subscriberByMctsId = getSubscriberByBeneficiary(childUpdate);

        if (subscriberByMctsId == null) {   // No existing subscriber(number) attached to child MCTS id

            if (subscriberByMsisdn == null) {   // No subscriber attached to new msisdn
                // create subscriber, beneficiary, subscription and return
                subscriberByMsisdn = new Subscriber(msisdn, language);
                subscriberByMsisdn.setDateOfBirth(dob);
                subscriberByMsisdn.setChild(childUpdate);
                create(subscriberByMsisdn);
                return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);

            } else { // subscriber number is already in use

                if (subscriberByMsisdn.getChild() == null) {    // number has no child attached
                    subscriberByMsisdn.setDateOfBirth(dob);
                    subscriberByMsisdn.setChild(childUpdate);
                    updateStartDate(subscriberByMsisdn);
                    return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
                } else {    // Reject the update, number in use and has existing child subscription
                    subscriptionErrorDataService.create(new SubscriptionError(msisdn, SubscriptionRejectionReason.ALREADY_SUBSCRIBED, pack.getType()));
                    return null;
                }
            }
        } else { // Found existing child beneficiary in our system

            if (subscriberByMsisdn == null) {   // no subscriber attached to the new number
                // We got here because beneficiary's phone number changed
                // detach child from existing subscriber
                deactivateSubscriptionForSubscriberAndPackType(subscriberByMctsId, pack.getType(), DeactivationReason.MCTS_UPDATE);
                subscriberByMctsId.setChild(null);
                subscriberDataService.update(subscriberByMctsId);

                // create new subscriber and attach child
                Subscriber newSubscriber = new Subscriber(msisdn, language);
                newSubscriber.setDateOfBirth(dob);
                newSubscriber.setChild(childUpdate);
                create(newSubscriber);
                return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
            } else {    // we have a subscriber by phone# and also one with the MCTS id

                if (subscriberByMsisdn.getId().equals(subscriberByMctsId.getId())) {    //we pulled the same subscriber
                    Subscription subscription = subscriptionService.getActiveSubscription(subscriberByMctsId, pack.getType());
                    subscriberByMsisdn.setDateOfBirth(dob);
                    subscriberByMsisdn.getChild().deepCopyFrom(childUpdate);
                    updateStartDate(subscriberByMsisdn);
                    if (subscription != null) {     // update start date on existing active subscription
                        subscriptionService.updateStartDate(subscription, dob);
                        return subscriptionService.getActiveSubscription(subscriberByMctsId, pack.getType());
                    } else {    // just create a new subscription
                        return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
                    }

                } else {    // msisdn is already taken by another subscriber
                    if (subscriberByMsisdn.getChild() == null) {  // no child attached to existing phone number
                        // Deactivate child from existing subscriber (by mcts id)
                        deactivateSubscriptionForSubscriberAndPackType(subscriberByMctsId, pack.getType(), DeactivationReason.MCTS_UPDATE);
                        subscriberByMctsId.setChild(null);
                        updateStartDate(subscriberByMctsId);

                        // transfer child to new subscriber (number)
                        subscriberByMsisdn.setChild(childUpdate);
                        updateStartDate(subscriberByMsisdn);
                        return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
                    } else {    // No way to resolve this since msisdn already has a child attached. Reject the update
                        Subscription subscription = subscriptionService.getActiveSubscription(subscriberByMctsId, pack.getType());
                        subscriptionService.deactivateSubscription(subscription, DeactivationReason.MCTS_UPDATE);
                        subscriptionErrorDataService.create(new SubscriptionError(msisdn, SubscriptionRejectionReason.ALREADY_SUBSCRIBED, pack.getType()));
                        return null;
                    }
                }
            }
        }
    }

    private void deactivateSubscriptionForSubscriberAndPackType(Subscriber subscriber, SubscriptionPackType packType, DeactivationReason deactivationReason) {
        Subscription subscription = subscriptionService.getActiveSubscription(subscriber, packType);
        if (subscription != null) {
            subscriptionService.deactivateSubscription(subscription, deactivationReason);
        }
    }

    public void deleteAllowed(Subscriber subscriber) {
        for (Subscription subscription: subscriber.getSubscriptions()) {
            subscriptionService.deletePreconditionCheck(subscription);
        }
    }

    @Override
    public void deactivateAllSubscriptionsForSubscriber(long callingNumber) {
        LOGGER.info("Recieved Release Number {} for Deactivation.", callingNumber);
        Subscriber subscriberByMsisdn = this.getSubscriber(callingNumber);
        if (subscriberByMsisdn == null) {
            LOGGER.info("Subscriber for msisdn {} is not found." , callingNumber);
            throw new IllegalArgumentException(String.format(KilkariConstants.SUBSCRIBER_NOT_FOUND, callingNumber));
        }
        LOGGER.info("Found Subscriber for msisdn {} .", callingNumber);
        int counter = 0;
        for (Subscription subscription : subscriberByMsisdn.getAllSubscriptions()) {
            if ((subscription.getStatus() == SubscriptionStatus.PENDING_ACTIVATION) || (subscription.getStatus() == SubscriptionStatus.ACTIVE) || (subscription.getStatus() == SubscriptionStatus.HOLD)) {
                try {
                    LOGGER.info("Deactivating Subscription with Id {} for msisdn.", subscription.getSubscriptionId());
                    subscriptionService.deactivateSubscription(subscription, DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED);
                    deactivationSubscriptionAuditRecordDataService.create(new DeactivationSubscriptionAuditRecord(subscription.getSubscriptionId(), subscriberByMsisdn.getId(), subscription.getOrigin(), callingNumber, subscription.getStatus(), AuditStatus.SUCCESS, ""));
                    counter++;
                } catch (Exception e) {
                    String error = ExceptionUtils.getFullStackTrace(e);
                    String truncatedError;
                    LOGGER.error(String.format("Unexpected exception in deactivating subscription %s: %s", subscription.getSubscriptionId(), error));
                    if (error.length() > DeactivationSubscriptionAuditRecord.MAX_OUTCOME_LENGTH) {
                        truncatedError = error.substring(0, DeactivationSubscriptionAuditRecord.MAX_OUTCOME_LENGTH);
                    } else {
                        truncatedError = error;
                    }
                    deactivationSubscriptionAuditRecordDataService.create(new DeactivationSubscriptionAuditRecord(subscription.getSubscriptionId(), subscriberByMsisdn.getId(), subscription.getOrigin(), callingNumber, subscription.getStatus(), AuditStatus.FAILURE, truncatedError));
                    throw new IllegalStateException(e);
                }
            }
        }
        LOGGER.info("Deactivated {} Subscritions for msisdn {}.", counter, callingNumber);
        // Add callingNumber to WeeklyCallsNotAnsweredMsisdnRecord (tableName = "nms_blocked_msisdn")
        BlockedMsisdnRecord record = blockedMsisdnRecordDataService.findByNumber(callingNumber);
        //TODO: we can use createOrUpdate method of MotechDataService once the bug is fixed.
        if (record == null) {
            blockedMsisdnRecordDataService.create(new BlockedMsisdnRecord(callingNumber, DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED));
            LOGGER.info("Added callingNumber {} to BlockedMsisdnRecord.", callingNumber);
        } else {
            record.setDeactivationReason(DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED);
            blockedMsisdnRecordDataService.update(record);
            LOGGER.info("Updated existing BlockedMsisdnRecord for callingNumber {}", callingNumber);
        }
    }

}
