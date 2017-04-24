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
import org.motechproject.nms.kilkari.domain.SubscriberMsisdnTracker;
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
import org.motechproject.nms.kilkari.exception.MultipleSubscriberException;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriberMsisdnTrackerDataService;
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
import java.util.List;
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
    private SubscriberMsisdnTrackerDataService subscriberMsisdnTrackerDataService;

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
    public List<Subscriber> getSubscriber(final long callingNumber) {

        SqlQueryExecution<List<Subscriber>> queryExecution = new SqlQueryExecution<List<Subscriber>>() {

            @Override
            public String getSqlQuery() {
                return KilkariConstants.SELECT_SUBSCRIBERS_BY_NUMBER;
            }

            @Override
            public List<Subscriber> execute(Query query) {
                query.setClass(Subscriber.class);
                return (List<Subscriber>) query.execute(callingNumber);
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

        List<Subscriber> subscriberWithMsisdns = getSubscriber(newMsisdn);
        if (!subscriberWithMsisdns.isEmpty()) {
            // this number is in use
            for (Subscriber subscriberWithMsisdn : subscriberWithMsisdns) {
                if (subscriptionService.getActiveSubscription(subscriberWithMsisdn, packType) != null) {
                    // in fact, it's in use for this pack -- reject the subscription
                    throw new IllegalStateException(
                            String.format("Can't change subscriber's MSISDN (%s) because it is already in use for %s pack",
                                    newMsisdn, packType));
                }
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

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    public Subscription updateMotherSubscriber(Long msisdn, MctsMother motherUpdate, DateTime lmp) { //NOPMD NcssMethodCount
        District district = motherUpdate.getDistrict(); // district should never be null here since we validate upstream on setLocation
        Circle circle = district.getCircle();
        Language language = district.getLanguage();
        SubscriptionPack pack = subscriptionPackDataService.byType(SubscriptionPackType.PREGNANCY);
        List<Subscriber> subscriberByMsisdns = getSubscriber(msisdn);
        Subscriber subscriberByMctsId = getSubscriberByBeneficiary(motherUpdate);

        if (subscriberByMctsId == null) {   // No existing subscriber(number) attached to mother MCTS id
            if (subscriberByMsisdns.isEmpty()) {   // No subscriber attached to the number
                // create subscriber, beneficiary, subscription and return
                return createSubscriber(msisdn, motherUpdate, lmp, pack, language, circle);
            } else { // subscriber (number) is already in use
                for (Subscriber subscriber : subscriberByMsisdns) {
                    MctsMother mother = subscriber.getMother();
                    if (mother != null && mother.getLastMenstrualPeriod() != null) {
                        subscriptionErrorDataService.create(new SubscriptionError(msisdn, motherUpdate.getBeneficiaryId(), SubscriptionRejectionReason.MSISDN_ALREADY_SUBSCRIBED, pack.getType(), "Subscriber exists with this Msisdn", SubscriptionOrigin.MCTS_IMPORT));
                        return null;
                    } else if (mother == null) {  // Check if it's an existing anonymous mother
                        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
                        Subscription motherSubscription = subscriptionService.getIVRSubscription(subscriptions, SubscriptionPackType.PREGNANCY);
                        if (motherSubscription != null) {
                            Subscription childSubscription = subscriptionService.getIVRSubscription(subscriptions, SubscriptionPackType.CHILD);
                            if (childSubscription == null) {
                                //update the anonymous mother with MCTS details
                                motherUpdate.setLastMenstrualPeriod(lmp);
                                subscriber.setLastMenstrualPeriod(lmp);
                                subscriber.setMother(motherUpdate);
                                return updateOrCreateSubscription(subscriber, motherSubscription, lmp, pack, language, circle, SubscriptionOrigin.MCTS_IMPORT);
                            } else {
                                // Both IVR mother and child exists. Create a new subscriber record for mother and set the same in existing motherSubscription
                                subscriberByMctsId = new Subscriber(msisdn, language);
                                motherUpdate.setLastMenstrualPeriod(lmp);
                                subscriberByMctsId.setLastMenstrualPeriod(lmp);
                                subscriberByMctsId.setMother(motherUpdate);
                                subscriberByMctsId = create(subscriberByMctsId);
                                motherSubscription.setSubscriber(subscriberByMctsId);
                                return updateOrCreateSubscription(subscriberByMctsId, motherSubscription, lmp, pack, language, circle, SubscriptionOrigin.MCTS_IMPORT);
                            }
                        }
                    }
                }
                return createSubscriber(msisdn, motherUpdate, lmp, pack, language, circle);
            }
        } else { // subscriberByBeneficiary != null aka. MCTS mother exists in motech
            if (subscriberByMsisdns.isEmpty()) {   //no subscriber attached to the new number
                // We got here because beneficiary's phone number changed
                deleteBlockedMsisdn(motherUpdate.getId(), subscriberByMctsId.getCallingNumber(), msisdn);
                subscriberByMctsId.setCallingNumber(msisdn);
                Subscription subscription = subscriptionService.getActiveSubscription(subscriberByMctsId, pack.getType());
                motherUpdate.setLastMenstrualPeriod(lmp);
                subscriberByMctsId.setLastMenstrualPeriod(lmp);
                return updateOrCreateSubscription(subscriberByMctsId, subscription, lmp, pack, language, circle, SubscriptionOrigin.MCTS_IMPORT);
            } else {    // we have a subscriber by phone# and also one with the MCTS id
                for (Subscriber subscriber : subscriberByMsisdns) {
                    if (subscriberByMctsId.getId().equals(subscriber.getId())) {
                        Subscription subscription = subscriptionService.getActiveSubscription(subscriberByMctsId, pack.getType());
                        motherUpdate.setLastMenstrualPeriod(lmp);
                        subscriberByMctsId.setLastMenstrualPeriod(lmp);
                        return updateOrCreateSubscription(subscriberByMctsId, subscription, lmp, pack, language, circle, SubscriptionOrigin.MCTS_IMPORT);
                    }
                }
                subscriptionErrorDataService.create(new SubscriptionError(msisdn, motherUpdate.getBeneficiaryId(), SubscriptionRejectionReason.MSISDN_ALREADY_SUBSCRIBED, pack.getType(), "Unrelated Subscribers exists with this Msisdn and MctsId", SubscriptionOrigin.MCTS_IMPORT));
                return null;
            }
        }
    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    public Subscription updateRchMotherSubscriber(Long msisdn, MctsMother motherUpdate, DateTime lmp, Long caseNo, Boolean deactivate) {
        District district = motherUpdate.getDistrict(); // district should never be null here since we validate upstream on setLocation
        Circle circle = district.getCircle();
        Language language = district.getLanguage();
        SubscriptionPack pack = subscriptionPackDataService.byType(SubscriptionPackType.PREGNANCY);
        List<Subscriber> subscribersByMsisdn = getSubscriber(msisdn);
        Subscriber subscriberByRchId = getSubscriberByBeneficiary(motherUpdate);

        if (subscriberByRchId == null) { // No existing subscriber(number) attached to mother RCH id
            if (subscribersByMsisdn.isEmpty()) {  // No subscriber attached to the number
                // Reject the record if it's aborted/stillbirth or death
                if (deactivate) {
                    subscriptionErrorDataService.create(new SubscriptionError(msisdn, motherUpdate.getBeneficiaryId(), SubscriptionRejectionReason.ABORT_STILLBIRTH_DEATH, pack.getType(), "", SubscriptionOrigin.RCH_IMPORT));
                    return null;
                }
                // create subscriber, beneficiary, subscription and return
                Subscriber subscriberByMsisdn = new Subscriber(msisdn, language);
                motherUpdate.setLastMenstrualPeriod(lmp);
                subscriberByMsisdn.setLastMenstrualPeriod(lmp);
                subscriberByMsisdn.setMother(motherUpdate);
                subscriberByMsisdn.setCaseNo(caseNo);
                motherUpdate.setMaxCaseNo(caseNo);
                create(subscriberByMsisdn);
                return subscriptionService.createSubscription(subscriberByMsisdn, msisdn, language, circle, pack, SubscriptionOrigin.RCH_IMPORT);
            } else {  // subscriber (number) is already in use
                subscriptionErrorDataService.create(new SubscriptionError(msisdn, motherUpdate.getBeneficiaryId(), SubscriptionRejectionReason.MSISDN_ALREADY_SUBSCRIBED, pack.getType(), "Subscriber exists with this Msisdn", SubscriptionOrigin.RCH_IMPORT));
                return null;
            }
        } else { // subscriberByBeneficiary != null aka. RCH mother exists in motech
            if (subscribersByMsisdn.isEmpty()) {  //no subscriber attached to the new number
                // We got here because beneficiary's phone number changed
                if (subscriberByRchId.getCaseNo() == null) {
                    subscriberByRchId.setCaseNo(caseNo);
                    motherUpdate.setMaxCaseNo(caseNo);
                } else if (subscriberByRchId.getCaseNo() != caseNo) {
                    subscriptionErrorDataService.create(new SubscriptionError(msisdn, motherUpdate.getBeneficiaryId(), SubscriptionRejectionReason.INVALID_CASE_NO, pack.getType(), "Active subscription exists with different caseNo", SubscriptionOrigin.RCH_IMPORT));
                    return null;
                }
                deleteBlockedMsisdn(motherUpdate.getId(), subscriberByRchId.getCallingNumber(), msisdn);
                subscriberByRchId.setCallingNumber(msisdn);
                Subscription subscription = subscriptionService.getActiveSubscription(subscriberByRchId, pack.getType());
                motherUpdate.setLastMenstrualPeriod(lmp);
                subscriberByRchId.setLastMenstrualPeriod(lmp);
                return updateOrCreateSubscription(subscriberByRchId, subscription, lmp, pack, language, circle, SubscriptionOrigin.RCH_IMPORT);
            } else {  // we have a subscriber by phone# and also one with the RCH id
                for (Subscriber subscriber : subscribersByMsisdn) {
                    if ((subscriberByRchId.getId().equals(subscriber.getId())) && (subscriberByRchId.getCaseNo() == null || subscriberByRchId.getCaseNo() == caseNo)) {
                        Subscription subscription = subscriptionService.getActiveSubscription(subscriberByRchId, pack.getType());
                        motherUpdate.setMaxCaseNo(caseNo);
                        motherUpdate.setLastMenstrualPeriod(lmp);
                        subscriberByRchId.setLastMenstrualPeriod(lmp);
                        subscriberByRchId.setCaseNo(caseNo);
                        return updateOrCreateSubscription(subscriberByRchId, subscription, lmp, pack, language, circle, SubscriptionOrigin.RCH_IMPORT);
                    }
                }
                subscriptionErrorDataService.create(new SubscriptionError(msisdn, motherUpdate.getBeneficiaryId(), SubscriptionRejectionReason.MSISDN_ALREADY_SUBSCRIBED, pack.getType(), "Unrelated Subscribers exists with this Msisdn and RchId", SubscriptionOrigin.RCH_IMPORT));
                return null;
            }
        }
    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    public Subscription updateChildSubscriber(Long msisdn, MctsChild childUpdate, DateTime dob) { //NOPMD NcssMethodCount
        District district = childUpdate.getDistrict(); // district should never be null here since we validate upstream on setLocation
        Circle circle = district.getCircle();
        Language language = district.getLanguage();
        SubscriptionPack pack = subscriptionPackDataService.byType(SubscriptionPackType.CHILD);
        List<Subscriber> subscriberByMsisdns = getSubscriber(msisdn);
        Subscriber subscriberByMctsId = getSubscriberByBeneficiary(childUpdate);

        if (subscriberByMctsId == null) {   // No existing subscriber(number) attached to child MCTS id

            if (subscriberByMsisdns.isEmpty()) {   // No subscriber attached to new msisdn
                Subscriber subscriberByMotherMctsId = getSubscriberByBeneficiary(childUpdate.getMother());
                // If Mother of the child is already subscribed and has no other child attached to it, update msisdn in the same record
                if (subscriberByMotherMctsId != null) {
                    if (subscriberByMotherMctsId.getChild() != null) {
                        subscriptionErrorDataService.create(new SubscriptionError(msisdn, childUpdate.getBeneficiaryId(), SubscriptionRejectionReason.ALREADY_SUBSCRIBED, pack.getType(), "Another Child exists for the same Mother", SubscriptionOrigin.MCTS_IMPORT));
                        return null;
                    } else { // Update child and msisdn in same subscriber
                        childUpdate.setDateOfBirth(dob);
                        subscriberByMotherMctsId.setChild(childUpdate);
                        subscriberByMotherMctsId.setDateOfBirth(dob);
                        deleteBlockedMsisdn(childUpdate.getMother().getId(), subscriberByMotherMctsId.getCallingNumber(), msisdn);
                        subscriberByMotherMctsId.setCallingNumber(msisdn);
                        return subscriptionService.createSubscription(subscriberByMotherMctsId, msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);

                    }
                } else {
                    // create subscriber, beneficiary, subscription and return
                    return createSubscriber(msisdn, childUpdate, dob, pack, language, circle);
                }
            } else { // subscriber number is already in use
                for (Subscriber subscriber : subscriberByMsisdns) {
                    if (subscriber.getChild() != null) {
                        subscriptionErrorDataService.create(new SubscriptionError(msisdn, childUpdate.getBeneficiaryId(), SubscriptionRejectionReason.MSISDN_ALREADY_SUBSCRIBED, pack.getType(), "Subscriber exists with this Msisdn", SubscriptionOrigin.MCTS_IMPORT));
                        return null;
                    } else if (subscriber.getMother() != null) {
                        if (childUpdate.getMother().equals(subscriber.getMother())) { // If existing mother is of child then update only if no other active child is present for same msisdn
                            subscriberByMctsId = subscriber;
                         } else {  // We got here because this record is of MCTS mother. Check if it has IVR child
                            Subscription childSubscription = splitSubscribers(subscriber, msisdn, childUpdate, dob, pack, language, circle);
                            if (childSubscription != null) {
                                return childSubscription;
                            }
                        }
                    } else { // Mother and child both are null. So must be an anonymous user. Check for IVR mother
                        Subscription motherSubscription = subscriptionService.getIVRSubscription(subscriber.getAllSubscriptions(), SubscriptionPackType.PREGNANCY);
                        if (motherSubscription == null) { // Update the Child anonymous user
                            childUpdate.setDateOfBirth(dob);
                            subscriber.setDateOfBirth(dob);
                            subscriber.setChild(childUpdate);
                            subscriber.setMother(childUpdate.getMother());
                            Subscription childSubscription = subscriptionService.getActiveSubscription(subscriber, SubscriptionPackType.CHILD);
                            return updateOrCreateSubscription(subscriber, childSubscription, dob, pack, language, circle, SubscriptionOrigin.MCTS_IMPORT);
                        } else { // IVR mother. Check for IVR child. If not found, create a new subscriber
                            Subscription childSubscription = splitSubscribers(subscriber, msisdn, childUpdate, dob, pack, language, circle);
                            if (childSubscription != null) {
                                return childSubscription;
                            }
                        }
                    }
                }

                if (subscriberByMctsId != null) {
                    childUpdate.setDateOfBirth(dob);
                    subscriberByMctsId.setDateOfBirth(dob);
                    subscriberByMctsId.setChild(childUpdate);
                    return subscriptionService.createSubscription(subscriberByMctsId, msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
                }
                return createSubscriber(msisdn, childUpdate, dob, pack, language, circle);
            }
        } else { // Found existing child beneficiary in our system

            if (subscriberByMsisdns.isEmpty()) {   // no subscriber attached to the new number
                // We got here because beneficiary's phone number changed
                deleteBlockedMsisdn(childUpdate.getMother().getId(), subscriberByMctsId.getCallingNumber(), msisdn);
                subscriberByMctsId.setCallingNumber(msisdn);
                if (subscriberByMctsId.getMother() == null) {
                    subscriberByMctsId.setMother(childUpdate.getMother());
                }
                Subscription subscription = subscriptionService.getActiveSubscription(subscriberByMctsId, pack.getType());
                childUpdate.setDateOfBirth(dob);
                subscriberByMctsId.setDateOfBirth(dob);
                return updateOrCreateSubscription(subscriberByMctsId, subscription, dob, pack, language, circle, SubscriptionOrigin.MCTS_IMPORT);
            } else {    // we have a subscriber by phone# and also one with the MCTS id

                for (Subscriber subscriber : subscriberByMsisdns) {
                    if (subscriberByMctsId.getId().equals(subscriber.getId())) {
                        Subscription subscription = subscriptionService.getActiveSubscription(subscriberByMctsId, pack.getType());
                        childUpdate.setDateOfBirth(dob);
                        subscriberByMctsId.setDateOfBirth(dob);
                        if (subscriberByMctsId.getMother() == null) {
                            subscriberByMctsId.setMother(childUpdate.getMother());
                        }
                        return updateOrCreateSubscription(subscriberByMctsId, subscription, dob, pack, language, circle, SubscriptionOrigin.MCTS_IMPORT);
                    }
                }
                subscriptionErrorDataService.create(new SubscriptionError(msisdn, childUpdate.getBeneficiaryId(), SubscriptionRejectionReason.MSISDN_ALREADY_SUBSCRIBED, pack.getType(), "Unrelated Subscribers exists with this Msisdn and MctsId", SubscriptionOrigin.MCTS_IMPORT));
                return null;
            }
        }
    }

    public Subscription updateOrCreateSubscription(Subscriber subscriber, Subscription subscription, DateTime dateTime, SubscriptionPack pack, Language language, Circle circle, SubscriptionOrigin origin) {
        if (subscription != null && (subscription.getStatus() == SubscriptionStatus.ACTIVE || subscription.getStatus() == SubscriptionStatus.PENDING_ACTIVATION)) {
            subscriptionService.updateStartDate(subscription, dateTime);
            return subscription;
        } else {
            return subscriptionService.createSubscription(subscriber, subscriber.getCallingNumber(), language, circle, pack, origin);
        }
    }

    public Subscription createSubscriber(Long msisdn, MctsBeneficiary beneficiary, DateTime dateTime, SubscriptionPack pack, Language language, Circle circle) {
        Subscriber subscriber = new Subscriber(msisdn, language);
        if (pack.getType() == SubscriptionPackType.PREGNANCY) {
            MctsMother mother = (MctsMother) beneficiary;
            mother.setLastMenstrualPeriod(dateTime);
            subscriber.setLastMenstrualPeriod(dateTime);
            subscriber.setMother(mother);
        } else {
            MctsChild child = (MctsChild) beneficiary;
            child.setDateOfBirth(dateTime);
            subscriber.setDateOfBirth(dateTime);
            subscriber.setChild(child);
            subscriber.setMother(child.getMother());
        }
        create(subscriber);
        return subscriptionService.createSubscription(subscriber, msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
    }

    public Subscription splitSubscribers(Subscriber subscriber, Long msisdn, MctsChild childUpdate, DateTime dob, SubscriptionPack pack, Language language, Circle circle) {
        Subscription childSubscription = subscriptionService.getIVRSubscription(subscriber.getAllSubscriptions(), SubscriptionPackType.CHILD);
        // If childSubscription is null We can go ahead and create a new record since no child is attached to this
        // else it's IVR mother and child.create a new subscriber and update the same in existing childSubscription
        if (childSubscription != null) {
            Subscriber newSubscriber = new Subscriber(msisdn, language);
            childUpdate.setDateOfBirth(dob);
            newSubscriber.setDateOfBirth(dob);
            newSubscriber.setChild(childUpdate);
            newSubscriber.setMother(childUpdate.getMother());
            newSubscriber = create(newSubscriber);
            childSubscription.setSubscriber(newSubscriber);
            if (childSubscription.getStatus() == SubscriptionStatus.ACTIVE) {
                subscriptionService.updateStartDate(childSubscription, dob);
                return subscriptionService.getActiveSubscription(newSubscriber, pack.getType());
            } else {
                return subscriptionService.createSubscription(newSubscriber, msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
            }
        }
        return null;   //create a new record
    }

    public void deleteBlockedMsisdn(Long motherId, Long oldCallingNumber, Long newCallingNumber) {
        // Check if the callingNumber is in Blocked Msisdn_Records
        BlockedMsisdnRecord blockedMsisdnRecord = blockedMsisdnRecordDataService.findByNumber(newCallingNumber);
        if (blockedMsisdnRecord != null) {
            LOGGER.info("Deleting msisdn {} from Blocked list.", newCallingNumber);
            blockedMsisdnRecordDataService.delete(blockedMsisdnRecord);
        }
        subscriberMsisdnTrackerDataService.create(new SubscriberMsisdnTracker(motherId, oldCallingNumber, newCallingNumber));
    }

    public void deleteAllowed(Subscriber subscriber) {
        for (Subscription subscription: subscriber.getSubscriptions()) {
            subscriptionService.deletePreconditionCheck(subscription);
        }
    }

    @Override
    public void deactivateAllSubscriptionsForSubscriber(long callingNumber, DeactivationReason deactivationReason) {
        LOGGER.info("Recieved Release Number {} for Deactivation.", callingNumber);
        List<Subscriber> subscriberByMsisdns = this.getSubscriber(callingNumber);
        if (subscriberByMsisdns.isEmpty()) {
            LOGGER.info("Subscriber for msisdn {} is not found." , callingNumber);
            throw new IllegalArgumentException(String.format(KilkariConstants.SUBSCRIBER_NOT_FOUND, callingNumber));
        }
        LOGGER.info("Found Subscriber for msisdn {} .", callingNumber);
        int counter = 0;
        for (Subscriber subscriber : subscriberByMsisdns) {
            for (Subscription subscription : subscriber.getAllSubscriptions()) {
                if ((subscription.getStatus() == SubscriptionStatus.PENDING_ACTIVATION) || (subscription.getStatus() == SubscriptionStatus.ACTIVE) || (subscription.getStatus() == SubscriptionStatus.HOLD)) {
                    try {
                        LOGGER.info("Deactivating Subscription with Id {} for msisdn.", subscription.getSubscriptionId());
                        subscriptionService.deactivateSubscription(subscription, deactivationReason);
                        deactivationSubscriptionAuditRecordDataService.create(new DeactivationSubscriptionAuditRecord(subscription.getSubscriptionId(), subscriber.getId(), subscription.getOrigin(), callingNumber, subscription.getStatus(), AuditStatus.SUCCESS, ""));
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
                        deactivationSubscriptionAuditRecordDataService.create(new DeactivationSubscriptionAuditRecord(subscription.getSubscriptionId(), subscriber.getId(), subscription.getOrigin(), callingNumber, subscription.getStatus(), AuditStatus.FAILURE, truncatedError));
                        throw new IllegalStateException(e);
                    }
                }
            }
        }

        LOGGER.info("Deactivated {} Subscritions for msisdn {}.", counter, callingNumber);
        // Add callingNumber to WeeklyCallsNotAnsweredMsisdnRecord (tableName = "nms_blocked_msisdn")
        BlockedMsisdnRecord record = blockedMsisdnRecordDataService.findByNumber(callingNumber);
        //TODO: we can use createOrUpdate method of MotechDataService once the bug is fixed.
        if (record == null) {
            blockedMsisdnRecordDataService.create(new BlockedMsisdnRecord(callingNumber, deactivationReason));
            LOGGER.info("Added callingNumber {} to BlockedMsisdnRecord.", callingNumber);
        } else {
            record.setDeactivationReason(deactivationReason);
            blockedMsisdnRecordDataService.update(record);
            LOGGER.info("Updated existing BlockedMsisdnRecord for callingNumber {}", callingNumber);
        }
    }

    @Override
    public Subscriber getSubscriberByMother(final long motherId) {
        SqlQueryExecution<Subscriber> queryExecution = new SqlQueryExecution<Subscriber>() {

            @Override
            public String getSqlQuery() {
                return KilkariConstants.SELECT_SUBSCRIBERS_BY_MOTHER_ID_OID;
            }

            @Override
            public Subscriber execute(Query query) {
                query.setClass(Subscriber.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(motherId);

                if (fqr.isEmpty()) {
                    return null;
                }

                if (fqr.size() == 1) {
                    return (Subscriber) fqr.get(0);
                }

                throw new MultipleSubscriberException(String.format(KilkariConstants.MORE_THAN_ONE_SUBSCRIBER_WITH_SAME_MOTHERID, motherId));
            }
        };

        return subscriberDataService.executeSQLQuery(queryExecution);
    }

    @Autowired
    public void setSubscriberMsisdnTrackerDataService(SubscriberMsisdnTrackerDataService subscriberMsisdnTrackerDataService) {
        this.subscriberMsisdnTrackerDataService = subscriberMsisdnTrackerDataService;
    }
}
