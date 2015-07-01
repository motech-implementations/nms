package org.motechproject.nms.testing.it.kilkari;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryUpdateService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;


@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class MctsBeneficiaryUpdateServiceBundleIT extends BasePaxIT {


    @Inject
    private TestingService testingService;
    @Inject
    private MctsBeneficiaryUpdateService mctsBeneficiaryUpdateService;
    @Inject
    private LanguageDataService languageDataService;
    @Inject
    private StateDataService stateDataService;
    @Inject
    private DistrictDataService districtDataService;
    @Inject
    private CircleDataService circleDataService;
    @Inject
    private SubscriberDataService subscriberDataService;
    @Inject
    private SubscriptionService subscriptionService;
    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    private DistrictService districtService;

    private SubscriptionHelper subscriptionHelper;

    @Before
    public void setUp() {
        testingService.clearDatabase();

        subscriptionHelper = new SubscriptionHelper(subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService, districtService);

        subscriptionHelper.pregnancyPack();
        subscriptionHelper.childPack();
    }

    @Test
    public void testUpdateMsisdn() throws Exception {
        Long oldMsisdn = subscriptionHelper.makeNumber();
        Long newMsisdn = subscriptionHelper.makeNumber();

        Subscription subscription = subscriptionHelper.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),
                SubscriptionPackType.CHILD, oldMsisdn);
        String mctsId = "0123456789";

        subscription.getSubscriber().setChild(new MctsChild(mctsId));
        subscriberDataService.update(subscription.getSubscriber());

        Reader reader = createUpdateReaderWithHeaders("1," + mctsId + ",,,,,,,,,,,,," + newMsisdn);
        mctsBeneficiaryUpdateService.updateBeneficiary(reader);

        assertNull(subscriberDataService.findByCallingNumber(oldMsisdn));

        Subscriber subscriber = subscriberDataService.findByCallingNumber(newMsisdn);
        assertNotNull(subscriber);
        assertEquals(mctsId, subscriber.getChild().getBeneficiaryId());
    }

    @Test
    public void testUpdateMsisdnForSubscriberWithBothPacks() throws Exception {
        Long oldMsisdn = subscriptionHelper.makeNumber();
        Long newMsisdn = subscriptionHelper.makeNumber();

        Subscription childSubscription = subscriptionHelper.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),
                SubscriptionPackType.CHILD, oldMsisdn);
        String childId = "0123456789";
        childSubscription.getSubscriber().setChild(new MctsChild(childId));
        subscriberDataService.update(childSubscription.getSubscriber());

        Subscription pregnancySubscription = subscriptionHelper.mksub(SubscriptionOrigin.MCTS_IMPORT,
                DateTime.now().minusDays(150), SubscriptionPackType.PREGNANCY, oldMsisdn);
        String motherId = "9876543210";
        pregnancySubscription.getSubscriber().setMother(new MctsMother(motherId));
        subscriberDataService.update(pregnancySubscription.getSubscriber());

        assertEquals(2, subscriberDataService.findByCallingNumber(oldMsisdn).getActiveAndPendingSubscriptions().size());

        Reader reader = createUpdateReaderWithHeaders("1," + motherId + ",,,,,,,,,,,,," + newMsisdn);
        mctsBeneficiaryUpdateService.updateBeneficiary(reader);

        Subscriber pregnancySubscriber = subscriberDataService.findByCallingNumber(newMsisdn);
        Subscriber childSubscriber = subscriberDataService.findByCallingNumber(oldMsisdn);

        assertNotNull(pregnancySubscriber);
        assertNotNull(childSubscriber);
        assertNotEquals(childSubscriber, pregnancySubscriber);
        assertEquals(newMsisdn, pregnancySubscriber.getCallingNumber());
        assertEquals(oldMsisdn, childSubscriber.getCallingNumber());
        assertNull(pregnancySubscriber.getMother());
        assertNull(childSubscriber.getMother());
        assertEquals(1, pregnancySubscriber.getActiveAndPendingSubscriptions().size());
        assertEquals(1, childSubscriber.getActiveAndPendingSubscriptions().size());
    }

    @Test
    @Ignore
    public void testUpdateMsisdnNumberAlreadyInUse() throws Exception {

    }

    @Test
    @Ignore
    public void testupdateBeneficiariesFromFile() throws Exception {
        mctsBeneficiaryUpdateService.updateBeneficiary(read("csv/mcts_beneficiary_update.csv"));
    }

    private Reader createUpdateReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("Sr No,MCTS ID,STATE ID,Beneficiary New DOB change,Beneficiary New LMP change,State_ID,");
        builder.append("District_ID,Taluka_ID,HealthBlock_ID,PHC_ID,SubCentre_ID,Village_ID,GP_Village,Address,");
        builder.append("Beneficiary New Mobile no change");
        builder.append("\n");

        for (String line : lines) {
            builder.append(line).append("\n");
        }

        return new StringReader(builder.toString());
    }

    private Reader read(String resource) {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource));
    }


}
