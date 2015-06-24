package org.motechproject.nms.kilkari.service.impl;


import org.joda.time.DateTime;
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
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.domain.Language;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseMctsBeneficiaryService {

    @Autowired
    protected SubscriberService subscriberService;
    @Autowired
    protected SubscriptionService subscriptionService;
    @Autowired
    protected SubscriptionErrorDataService subscriptionErrorDataService;
    @Autowired
    protected SubscriptionPackDataService subscriptionPackDataService;

    protected Subscription processSubscriptionForBeneficiary(MctsBeneficiary beneficiary, Long msisdn, DateTime referenceDate,
                                                           SubscriptionPackType packType) {
        Language language = beneficiary.getDistrict().getLanguage();
        Subscriber subscriber = subscriberService.getSubscriber(msisdn);

        SubscriptionPack pack = subscriptionPackDataService.byType(packType);

        // TODO: Handle the case in which the MCTS beneficiary already exists but with a different phone number

        if (subscriber == null) {
            // there's no subscriber with this MSISDN, create one

            subscriber = new Subscriber(msisdn, language);
            subscriber = updateSubscriber(subscriber, beneficiary, referenceDate, packType);
            subscriberService.create(subscriber);
            return subscriptionService.createSubscription(msisdn, language, pack, SubscriptionOrigin.MCTS_IMPORT);
        }

        if (subscriptionService.getActiveSubscription(subscriber, packType) != null) {
            // subscriber already has an active subscription to this pack

            MctsBeneficiary existingBeneficiary = (packType == SubscriptionPackType.PREGNANCY) ? subscriber.getMother() :
                    subscriber.getChild();

            if (existingBeneficiary == null) {
                // there's already an IVR-originated subscription for this MSISDN
                rejectBeneficiary(msisdn, SubscriptionRejectionReason.ALREADY_SUBSCRIBED, packType);

                // TODO: Do we just reject the subscription request, or do we update the subscriber record with MCTS data?
                // TODO: Should we change the subscription start date based on the provided LMP/DOB?

            } else if (!existingBeneficiary.getBeneficiaryId().equals(beneficiary.getBeneficiaryId())) {
                // if the MCTS ID doesn't match (i.e. there are two beneficiaries with the same phone number), reject the import
                rejectBeneficiary(msisdn, SubscriptionRejectionReason.ALREADY_SUBSCRIBED, packType);
            } else {
                // it's the same beneficiary, treat this import as an update
                subscriber = updateSubscriber(subscriber, beneficiary, referenceDate, packType);
                subscriberService.update(subscriber);
            }

            return null;
        }

        // subscriber exists, but doesn't have a subscription to this pack
        subscriber = updateSubscriber(subscriber, beneficiary, referenceDate, packType);
        subscriberService.update(subscriber);
        return subscriptionService.createSubscription(msisdn, language, pack, SubscriptionOrigin.MCTS_IMPORT);
    }

    private Subscriber updateSubscriber(Subscriber subscriber, MctsBeneficiary beneficiary, DateTime referenceDate,
                                        SubscriptionPackType packType) {
        if (packType == SubscriptionPackType.PREGNANCY) {
            subscriber.setLastMenstrualPeriod(referenceDate);
            subscriber.setMother((MctsMother) beneficiary);
        } else {
            subscriber.setDateOfBirth(referenceDate);
            subscriber.setChild((MctsChild) beneficiary);
        }
        return subscriber;
    }


    protected void rejectBeneficiary(Long msisdn, SubscriptionRejectionReason reason, SubscriptionPackType packType) {
        subscriptionErrorDataService.create(new SubscriptionError(msisdn, reason, packType));
    }

    protected void rejectBeneficiaryWithMessage(Long msisdn, SubscriptionRejectionReason reason, SubscriptionPackType packType, String rejectionMessage) {
        subscriptionErrorDataService.create(new SubscriptionError(msisdn, reason, packType, rejectionMessage));
    }

}
