package org.motechproject.nms.kilkari.service.impl;

import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
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

    private SubscriberDataService subscriberDataService;
    private SubscriptionService subscriptionService;
    private SubscriptionDataService subscriptionDataService;

    @Autowired
    public SubscriberServiceImpl(SubscriberDataService subscriberDataService, SubscriptionService subscriptionService,
                                 SubscriptionDataService subscriptionDataService) {
        this.subscriberDataService = subscriberDataService;
        this.subscriptionService = subscriptionService;
        this.subscriptionDataService = subscriptionDataService;
    }

    @Override
    public Subscriber getSubscriber(long callingNumber) {
        return subscriberDataService.findByCallingNumber(callingNumber);
    }

    @Override
    public Subscriber getSubscriberByBeneficiaryId(final String beneficiaryId) {

        SqlQueryExecution<Subscriber> queryExecution = new SqlQueryExecution<Subscriber>() {

            @Override
            public String getSqlQuery() {
                return "select *  from nms_subscribers where mother_id_oid = ? or child_id_oid = ?";
            }

            @Override
            public Subscriber execute(Query query) {
                query.setClass(Subscriber.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(beneficiaryId, beneficiaryId);
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (Subscriber) fqr.get(0);
                }
                throw new IllegalStateException("More than one row returned!");
            }
        };

        return subscriberDataService.executeSQLQuery(queryExecution);

    }

    @Override
    public void create(Subscriber subscriber) {
        subscriberDataService.create(subscriber);
    }

    @Override
    @Transactional
    public void update(Subscriber subscriber) {

        Subscriber retrievedSubscriber = subscriberDataService.findByCallingNumber(subscriber.getCallingNumber());

        subscriberDataService.update(subscriber);

        // update start dates for subscriptions if reference date (LMP/DOB) has changed
        Set<Subscription> subscriptions = retrievedSubscriber.getActiveAndPendingSubscriptions();
        Iterator<Subscription> subscriptionIterator = subscriptions.iterator();
        Subscription subscription;

        while (subscriptionIterator.hasNext()) {
            subscription = subscriptionIterator.next();

            if (subscription.getSubscriptionPack().getType() == SubscriptionPackType.PREGNANCY) {

                subscriptionService.updateStartDate(subscription, subscriber.getLastMenstrualPeriod());

            } else if (subscription.getSubscriptionPack().getType() == SubscriptionPackType.CHILD) {

                subscriptionService.updateStartDate(subscription, subscriber.getDateOfBirth());

            }
        }
    }

    @Override
    public void updateMsisdnForSubscriber(Subscriber subscriber, MctsBeneficiary beneficiary, Long newMsisdn) {

        SubscriptionPackType packType;
        packType = (beneficiary instanceof MctsChild) ? SubscriptionPackType.CHILD : SubscriptionPackType.PREGNANCY;

        Subscriber subscriberWithMsisdn = subscriberDataService.findByCallingNumber(newMsisdn);
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
        subscriber.getSubscriptions().remove(subscription);
        newSubscriber.getSubscriptions().add(subscription);
        subscription.setSubscriber(newSubscriber);

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



    public void deleteAllowed(Subscriber subscriber) {
        for (Subscription subscription: subscriber.getSubscriptions()) {
            subscriptionService.deletePreconditionCheck(subscription);
        }
    }
}
