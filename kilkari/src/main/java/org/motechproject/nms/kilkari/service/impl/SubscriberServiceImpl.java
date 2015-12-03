package org.motechproject.nms.kilkari.service.impl;

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
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.service.DistrictService;
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

    public static final String SELECT_SUBSCRIBER = "select * from nms_subscribers where callingNumber = ?";
    public static final String MULTIPLE_SUBSCRIBERS = "More than one subscriber returned for callingNumber %s";

    private SubscriberDataService subscriberDataService;
    private SubscriptionService subscriptionService;
    private SubscriptionDataService subscriptionDataService;
    private SubscriptionErrorDataService subscriptionErrorDataService;
    private SubscriptionPackDataService subscriptionPackDataService;

    @Autowired
    public SubscriberServiceImpl(SubscriberDataService subscriberDataService, SubscriptionService subscriptionService,
                                 SubscriptionDataService subscriptionDataService,
                                 SubscriptionErrorDataService subscriptionErrorDataService,
                                 SubscriptionPackDataService subscriptionPackDataService) {
        this.subscriberDataService = subscriberDataService;
        this.subscriptionService = subscriptionService;
        this.subscriptionDataService = subscriptionDataService;
        this.subscriptionErrorDataService = subscriptionErrorDataService;
        this.subscriptionPackDataService = subscriptionPackDataService;
    }

    @Override
    public Subscriber getSubscriber(final long callingNumber) {

        SqlQueryExecution<Subscriber> queryExecution = new SqlQueryExecution<Subscriber>() {

            @Override
            public String getSqlQuery() {
                return SELECT_SUBSCRIBER;
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
                throw new IllegalStateException(String.format(MULTIPLE_SUBSCRIBERS, callingNumber));
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
    public void update(Subscriber subscriber) {

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

            } else if (subscription.getSubscriptionPack().getType() == SubscriptionPackType.CHILD) {

                subscriptionService.updateStartDate(subscription, subscriber.getDateOfBirth());

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
    public Subscription UpdateMotherSubscriber(Long msisdn, MctsMother mother, DateTime lmp) { //NOPMD NcssMethodCount
        District district = mother.getDistrict();
        Circle circle = district.getCircle();
        Language language = district.getLanguage();
        SubscriptionPack pack = subscriptionPackDataService.byType(SubscriptionPackType.PREGNANCY);
        Subscriber subscriberByMsisdn = getSubscriber(msisdn);
        Subscriber subscriberByBeneficiary = getSubscriberByBeneficiary(mother);

        // No existing subscriber(number) attached to mother MCTS id
        if (subscriberByBeneficiary == null) {

            if (subscriberByMsisdn == null) {
                // create subscriber, beneficiary, subscription and return
                subscriberByMsisdn = new Subscriber(msisdn, language);
                subscriberByMsisdn.setLastMenstrualPeriod(lmp);
                subscriberByMsisdn.setMother(mother);
                create(subscriberByMsisdn);
                return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
            } else { // subscriber (number) is already in use
                // number has no mother attached
                if (subscriberByMsisdn.getMother() == null) {
                    // no existing mother attached
                    subscriberByMsisdn.setLastMenstrualPeriod(lmp);
                    subscriberByMsisdn.setMother(mother);
                    update(subscriberByMsisdn);
                    return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
                } else {
                    subscriptionErrorDataService.create(new SubscriptionError(msisdn, SubscriptionRejectionReason.ALREADY_SUBSCRIBED, pack.getType()));
                    return null;
                }
            }
        } else { // subscriberByBeneficiary != null
            if (subscriberByMsisdn == null) {
                // We got here because beneficiary's phone number changed and we have no subscriber attached to the new number
                // detach mother from existing subscriber
                Subscription subscription = subscriptionService.getActiveSubscription(subscriberByBeneficiary, pack.getType());
                subscriberByBeneficiary.setMother(null);
                subscriberDataService.update(subscriberByBeneficiary);
                subscriptionService.deactivateSubscription(subscription, DeactivationReason.MCTS_UPDATE);

                // create new subscriber and attach mother
                Subscriber newSubscriber = new Subscriber(msisdn, language);
                newSubscriber.setLastMenstrualPeriod(lmp);
                newSubscriber.setMother(mother);
                create(newSubscriber);
                return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
            } else {
                // we have a subscriber by phone# and also one with the MCTS id
                if (subscriberByMsisdn.getId().equals(subscriberByBeneficiary.getId())) {
                    // Case1: if we pulled the same subscriber
                    Subscription subscription = subscriptionService.getActiveSubscription(subscriberByBeneficiary, pack.getType());
                    subscriberByMsisdn.setLastMenstrualPeriod(lmp);
                    subscriberByMsisdn.getMother().deepCopyFrom(mother);
                    update(subscriberByMsisdn);
                    subscriptionService.updateStartDate(subscription, lmp);
                    return subscriptionService.getActiveSubscription(subscriberByBeneficiary, pack.getType());
                } else {
                    // Case 2: msisdn is already taken by another beneficiary
                    if (subscriberByMsisdn.getMother() == null) {
                        // Deactivate mother from existing subscriber (by mcts id)
                        Subscription subscription = subscriptionService.getActiveSubscription(subscriberByBeneficiary, pack.getType());
                        subscriptionService.deactivateSubscription(subscription, DeactivationReason.MCTS_UPDATE);
                        subscriberByBeneficiary.setMother(null);
                        update(subscriberByBeneficiary);

                        // transfer mother to new subscriber (number)
                        subscriberByMsisdn.setMother(mother);
                        update(subscriberByMsisdn);
                        return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
                    } else {
                        // No way to resolve this since msisdn already has a mother attached. Reject the update
                        Subscription subscription = subscriptionService.getActiveSubscription(subscriberByBeneficiary, pack.getType());
                        subscriptionService.deactivateSubscription(subscription, DeactivationReason.MCTS_UPDATE);
                        subscriptionErrorDataService.create(new SubscriptionError(msisdn, SubscriptionRejectionReason.ALREADY_SUBSCRIBED, pack.getType()));
                        return null;
                    }
                }
            }
        }
    }

    @Override
    public Subscription UpdateChildSubscriber(Long msisdn, MctsChild child, DateTime dob) { //NOPMD NcssMethodCount
        District district = child.getDistrict();
        Circle circle = district.getCircle();
        Language language = district.getLanguage();
        SubscriptionPack pack = subscriptionPackDataService.byType(SubscriptionPackType.CHILD);
        Subscriber subscriberByMsisdn = getSubscriber(msisdn);
        Subscriber subscriberByBeneficiary = getSubscriberByBeneficiary(child);

        // No existing subscriber(number) attached to mother MCTS id
        if (subscriberByBeneficiary == null) {

            if (subscriberByMsisdn == null) {
                // create subscriber, beneficiary, subscription and return
                subscriberByMsisdn = new Subscriber(msisdn, language);
                subscriberByMsisdn.setDateOfBirth(dob);
                subscriberByMsisdn.setChild(child);
                create(subscriberByMsisdn);
                return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
            } else { // subscriber (child) is already in use
                // number has no child attached
                if (subscriberByMsisdn.getChild() == null) {
                    // no existing child attached
                    subscriberByMsisdn.setDateOfBirth(dob);
                    subscriberByMsisdn.setChild(child);
                    update(subscriberByMsisdn);
                    return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
                } else {
                    subscriptionErrorDataService.create(new SubscriptionError(msisdn, SubscriptionRejectionReason.ALREADY_SUBSCRIBED, pack.getType()));
                    return null;
                }
            }
        } else { // subscriberByBeneficiary != null
            if (subscriberByMsisdn == null) {
                // We got here because beneficiary's phone number changed and we have no subscriber attached to the new number
                // detach child from existing subscriber
                Subscription subscription = subscriptionService.getActiveSubscription(subscriberByBeneficiary, pack.getType());
                subscriberByBeneficiary.setMother(null);
                subscriberDataService.update(subscriberByBeneficiary);
                subscriptionService.deactivateSubscription(subscription, DeactivationReason.MCTS_UPDATE);

                // create new subscriber and attach child
                Subscriber newSubscriber = new Subscriber(msisdn, language);
                newSubscriber.setDateOfBirth(dob);
                newSubscriber.setChild(child);
                create(newSubscriber);
                return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
            } else {
                // we have a subscriber by phone# and also one with the MCTS id
                if (subscriberByMsisdn.getId().equals(subscriberByBeneficiary.getId())) {
                    // Case1: if we pulled the same subscriber
                    Subscription subscription = subscriptionService.getActiveSubscription(subscriberByBeneficiary, pack.getType());
                    subscriberByMsisdn.setDateOfBirth(dob);
                    subscriberByMsisdn.getChild().deepCopyFrom(child);
                    update(subscriberByMsisdn);
                    subscriptionService.updateStartDate(subscription, dob);
                    return subscriptionService.getActiveSubscription(subscriberByBeneficiary, pack.getType());
                } else {
                    // Case 2: msisdn is already taken by another beneficiary
                    if (subscriberByMsisdn.getChild() == null) {
                        // Deactivate mother from existing subscriber (by mcts id)
                        Subscription subscription = subscriptionService.getActiveSubscription(subscriberByBeneficiary, pack.getType());
                        subscriptionService.deactivateSubscription(subscription, DeactivationReason.MCTS_UPDATE);
                        subscriberByBeneficiary.setMother(null);
                        update(subscriberByBeneficiary);

                        // transfer mother to new subscriber (number)
                        subscriberByMsisdn.setChild(child);
                        update(subscriberByMsisdn);
                        return subscriptionService.createSubscription(msisdn, language, circle, pack, SubscriptionOrigin.MCTS_IMPORT);
                    } else {
                        // No way to resolve this since msisdn already has a mother attached. Reject the update
                        Subscription subscription = subscriptionService.getActiveSubscription(subscriberByBeneficiary, pack.getType());
                        subscriptionService.deactivateSubscription(subscription, DeactivationReason.MCTS_UPDATE);
                        subscriptionErrorDataService.create(new SubscriptionError(msisdn, SubscriptionRejectionReason.ALREADY_SUBSCRIBED, pack.getType()));
                        return null;
                    }
                }
            }
        }
    }

    public void deleteAllowed(Subscriber subscriber) {
        for (Subscription subscription: subscriber.getSubscriptions()) {
            subscriptionService.deletePreconditionCheck(subscription);
        }
    }
}
