package org.motechproject.nms.testing.it.kilkari;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionError;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionRejectionReason;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryUpdateService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.LanguageService;
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.inject.Inject;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createDistrict;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createHealthBlock;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createHealthFacility;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createHealthFacilityType;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createHealthSubFacility;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createState;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createTaluka;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createVillage;


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
    private LanguageService languageService;
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
    @Inject
    private SubscriptionErrorDataService subscriptionErrorDataService;
    @Inject
    private SubscriberService subscriberService;
    @Inject
    private MctsChildDataService mctsChildDataService;
    @Inject
    private MctsMotherDataService mctsMotherDataService;
    @Inject
    private MctsBeneficiaryImportService mctsBeneficiaryImportService;
    @Inject
    PlatformTransactionManager transactionManager;

    private SubscriptionHelper sh;

    @Before
    public void setUp() {
        testingService.clearDatabase();

        sh = new SubscriptionHelper(subscriptionService, subscriberDataService, subscriptionPackDataService,
                languageDataService, languageService, circleDataService, stateDataService, districtDataService,
                districtService);

        sh.pregnancyPack();
        sh.childPack();
    }


    private void createLocationData() {
        // specific locations from the mother and child data files:
        final Circle circle = new Circle();
        circle.setName("Square");
        circleDataService.create(circle);

        final Circle circle2 = new Circle();
        circle2.setName("Rectangle");
        circleDataService.create(circle2);

        final State state21 = createState(21L, "State 21");
        District district2 = createDistrict(state21, 2L, "Jharsuguda", null, circle);
        District district3 = createDistrict(state21, 3L, "Sambalpur", null, circle);
        District district4 = createDistrict(state21, 4L, "Debagarh", null, circle);
        District district5 = createDistrict(state21, 5L, "Rectangle", null, circle2);
        state21.getDistricts().addAll(Arrays.asList(district2, district3, district4, district5));

        Taluka taluka24 = createTaluka(district2, "0024", "Laikera P.S.", 24);
        district2.getTalukas().add(taluka24);

        Taluka taluka26 = createTaluka(district3, "0026", "Govindpur P.S.", 26);
        district3.getTalukas().add(taluka26);

        Taluka taluka46 = createTaluka(district4, "0046", "Debagarh P.S.", 46);
        district4.getTalukas().add(taluka46);

        HealthBlock healthBlock259 = createHealthBlock(taluka24, 259L, "Laikera", "hq");
        taluka24.getHealthBlocks().add(healthBlock259);

        HealthBlock healthBlock453 = createHealthBlock(taluka26, 453L, "Bamara", "hq");
        taluka26.getHealthBlocks().add(healthBlock453);

        HealthBlock healthBlock153 = createHealthBlock(taluka46, 153L, "Tileibani", "hq");
        taluka46.getHealthBlocks().add(healthBlock153);

        HealthFacilityType facilityType635 = createHealthFacilityType("Mundrajore CHC", 635L);
        HealthFacility healthFacility635 = createHealthFacility(healthBlock259, 635L, "Mundrajore CHC", facilityType635);
        healthBlock259.getHealthFacilities().add(healthFacility635);

        HealthFacilityType facilityType41 = createHealthFacilityType("Garposh CHC", 41L);
        HealthFacility healthFacility41 = createHealthFacility(healthBlock453, 41L, "Garposh CHC", facilityType41);
        healthBlock453.getHealthFacilities().add(healthFacility41);

        HealthFacilityType facilityType114 = createHealthFacilityType("CHC Tileibani", 114L);
        HealthFacility healthFacility114 = createHealthFacility(healthBlock153, 114L, "CHC Tileibani", facilityType114);
        healthBlock153.getHealthFacilities().add(healthFacility114);

        HealthSubFacility subFacilityType7389 = createHealthSubFacility("Babuniktimal", 7389L, healthFacility41);
        healthFacility41.getHealthSubFacilities().add(subFacilityType7389);

        HealthSubFacility subFacilityType7393 = createHealthSubFacility("Jarabaga", 7393L, healthFacility41);
        healthFacility41.getHealthSubFacilities().add(subFacilityType7393);

        HealthSubFacility subFacilityType2104 = createHealthSubFacility("Chupacabra", 2104L, healthFacility635);
        healthFacility635.getHealthSubFacilities().add(subFacilityType2104);

        HealthSubFacility subFacilityType342 = createHealthSubFacility("El Dorado", 342L, healthFacility114);
        healthFacility114.getHealthSubFacilities().add(subFacilityType342);

        Village village10004693 = createVillage(taluka24, 10004693L, 0, "Khairdihi");
        Village village10004691 = createVillage(taluka24, 10004691L, 0, "Gambhariguda");
        Village village1509 = createVillage(taluka24, 0, 1509L, "Mundrajore");
        Village village1505 = createVillage(taluka24, 0, 1505L, "Kulemura");
        Village village10004690 = createVillage(taluka24, 10004690L, 0, "Ampada");
        Village village10004697 = createVillage(taluka24, 10004697L, 0, "Saletikra");

        taluka24.getVillages().addAll(Arrays.asList(village10004693, village10004691, village1509, village1505,
                village10004690, village10004697));

        Village village3089 = createVillage(taluka46, 0, 3089L, "Podapara");
        taluka46.getVillages().add(village3089);

        stateDataService.create(state21);
    }

    @Test
    public void testUpdateMsisdn() throws Exception {
        createLocationData();

        Long oldMsisdn = sh.makeNumber();
        Long newMsisdn = sh.makeNumber();

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),
                SubscriptionPackType.CHILD, oldMsisdn);
        String mctsId = "0123456789";

        Subscriber subscriber = subscription.getSubscriber();
        MctsChild child = new MctsChild(mctsId);
        child.setState(stateDataService.findByCode(21L));
        child.setDistrict(districtService.findByStateAndCode(child.getState(), 3L));
        subscriber.setChild(new MctsChild(mctsId));
        subscriberDataService.update(subscriber);

        DateTime updatedDOB = DateTime.now().minusDays(100);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Reader reader = createUpdateReaderWithHeaders("1," + mctsId + ",," + getDateString(updatedDOB) +",,21,3,,,,,,,," + newMsisdn);
        mctsBeneficiaryUpdateService.updateBeneficiaryData(reader);

        Subscriber oldSubscriber = subscriberDataService.findByNumber(oldMsisdn);
        assertNull(oldSubscriber.getChild());
        assertEquals(0, oldSubscriber.getActiveAndPendingSubscriptions().size());

        subscriber = subscriberDataService.findByNumber(newMsisdn);
        assertNotNull(subscriber);
        assertEquals(mctsId, subscriber.getChild().getBeneficiaryId());
        transactionManager.commit(status);
    }

    @Test
    public void testUpdateMsisdnForSubscriberWithBothPacks() throws Exception {
        createLocationData();
        Long oldMsisdn = sh.makeNumber();
        Long newMsisdn = sh.makeNumber();

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        Subscription childSubscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),
                SubscriptionPackType.CHILD, oldMsisdn);
        String childId = "0123456789";
        childSubscription.getSubscriber().setChild(new MctsChild(childId));
        subscriberDataService.update(childSubscription.getSubscriber());

        Subscription pregnancySubscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT,
                DateTime.now().minusDays(150), SubscriptionPackType.PREGNANCY, oldMsisdn);
        String motherId = "9876543210";
        pregnancySubscription.getSubscriber().setMother(new MctsMother(motherId));
        subscriberDataService.update(pregnancySubscription.getSubscriber());
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertEquals(2, subscriberDataService.findByNumber(oldMsisdn).getActiveAndPendingSubscriptions().size());
        transactionManager.commit(status);

        String lmpString = getDateString(DateTime.now().minus(120));
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Reader reader = createUpdateReaderWithHeaders("1," + motherId + ",,," + lmpString +",21,3,,,,,,,," + newMsisdn);
        mctsBeneficiaryUpdateService.updateBeneficiaryData(reader);
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber pregnancySubscriber = subscriberDataService.findByNumber(newMsisdn);
        Subscriber childSubscriber = subscriberDataService.findByNumber(oldMsisdn);

        assertNotNull(pregnancySubscriber);
        assertNotNull(childSubscriber);
        assertNotEquals(childSubscriber, pregnancySubscriber);
        assertEquals(newMsisdn, pregnancySubscriber.getCallingNumber());
        assertEquals(oldMsisdn, childSubscriber.getCallingNumber());
        assertNull(pregnancySubscriber.getChild());
        assertNull(childSubscriber.getMother());
        assertEquals(1, pregnancySubscriber.getActiveAndPendingSubscriptions().size());
        assertEquals(1, childSubscriber.getActiveAndPendingSubscriptions().size());
        transactionManager.commit(status);
    }

    @Test
    public void testUpdateMsisdnForMotherWithChildPackACTIVE() throws Exception {
        createLocationData();
        Long oldMsisdn = sh.makeNumber();
        Long newMsisdn = sh.makeNumber();

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        // new mother
        Subscription pregnancySubscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT,
                DateTime.now().minusDays(150), SubscriptionPackType.PREGNANCY, oldMsisdn);
        String motherId = "9876543210";
        pregnancySubscription.getSubscriber().setMother(new MctsMother(motherId));
        subscriberDataService.update(pregnancySubscription.getSubscriber());
        transactionManager.commit(status);

        // import child
        DateTime dob = DateTime.now().minusDays(5);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567891\tBaby1 of Shanti Ekka\t9876543210\t"+oldMsisdn+"\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertEquals(1, subscriberDataService.findByNumber(oldMsisdn).getActiveAndPendingSubscriptions().size());
        transactionManager.commit(status);

        //update mother with new msisdn
        String lmpString = getDateString(DateTime.now().minus(120));
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        reader = createUpdateReaderWithHeaders("1," + motherId + ",,," + lmpString +",21,3,,,,,,,," + newMsisdn);
        mctsBeneficiaryUpdateService.updateBeneficiaryData(reader);
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber pregnancySubscriber = subscriberDataService.findByNumber(newMsisdn);
        Subscriber childSubscriber = subscriberDataService.findByNumber(oldMsisdn);

        // mother should not get activated again.
        // subscription error table hould get this error, ie mother cant be updated as child is ACTIVE
        assertEquals(1,subscriptionErrorDataService.retrieveAll().size());
        assertNull(pregnancySubscriber);
        assertNotNull(childSubscriber);
        assertEquals(1, childSubscriber.getActiveAndPendingSubscriptions().size());
        transactionManager.commit(status);
    }

    @Test
    public void testUpdateMsisdnNumberAlreadyInUse() throws Exception {
        Long firstMsisdn = sh.makeNumber();
        Long secondMsisdn = sh.makeNumber();
        DateTime originalDOB = DateTime.now();

        // create two child subscriptions with different MSISDNs
        Subscription firstSubscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),
                SubscriptionPackType.CHILD, firstMsisdn);
        String firstChildId = "0123456789";
        firstSubscription.getSubscriber().setChild(new MctsChild(firstChildId));
        subscriberDataService.update(firstSubscription.getSubscriber());

        Subscription secondSubscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),
                SubscriptionPackType.CHILD, secondMsisdn);
        String secondChildId = "9123456789";
        secondSubscription.getSubscriber().setChild(new MctsChild(secondChildId));
        subscriberDataService.update(secondSubscription.getSubscriber());

        // try to set the second child's MSISDN to the same number as the first child's MSISDN
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Reader reader = createUpdateReaderWithHeaders("1," + secondChildId + ",," + getDateString(originalDOB) + ",,21,3,,,,,,,," + firstMsisdn);
        mctsBeneficiaryUpdateService.updateBeneficiaryData(reader);
        transactionManager.commit(status);

        List<SubscriptionError> errors = subscriptionErrorDataService.findByContactNumber(firstMsisdn);
        assertEquals(1, errors.size());
    }

    @Test
    public void testUpdateDOB() throws Exception {
        createLocationData();

        Long msisdn = sh.makeNumber();
        String childId = "0123456789";
        DateTime originalDOB = DateTime.now();
        DateTime updatedDOB = originalDOB.minusDays(100);

        MctsChild child = new MctsChild(childId);
        child.setState(stateDataService.findByCode(21L));
        child.setDistrict(districtService.findByStateAndCode(child.getState(), 3L));
        makeMctsSubscription(child, originalDOB, SubscriptionPackType.CHILD, msisdn);

        // this updates the db with the new data (DOB)
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Reader reader = createUpdateReaderWithHeaders("1," + childId + ",," + getDateString(updatedDOB) + ",,21,3,,,,,,,," + msisdn);
        mctsBeneficiaryUpdateService.updateBeneficiaryData(reader);

        // This query should return the updated subscriber information (but it doesn't...causing the assert to fail)
        Subscriber updatedSubscriber = subscriberDataService.findByNumber(msisdn);
        assertEquals(getDateString(updatedDOB), getDateString(updatedSubscriber.getDateOfBirth()));
        Subscription updatedSubscription = updatedSubscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(getDateString(updatedDOB), getDateString(updatedSubscription.getStartDate()));
        assertEquals(SubscriptionStatus.ACTIVE, updatedSubscription.getStatus());
        transactionManager.commit(status);
    }

    @Test
    public void testUpdateLMP() throws Exception {
        createLocationData();

        Long msisdn = sh.makeNumber();
        String motherId = "0123456789";
        DateTime originalLMP = DateTime.now().minusDays(100);
        DateTime updatedLMP = originalLMP.minusDays(200);

        MctsMother mother = new MctsMother(motherId);
        mother.setState(stateDataService.findByCode(21L));
        mother.setDistrict(districtService.findByStateAndCode(mother.getState(), 3L));
        makeMctsSubscription(mother, originalLMP, SubscriptionPackType.PREGNANCY, msisdn);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Reader reader = createUpdateReaderWithHeaders("1," + motherId + ",,," + getDateString(updatedLMP) + ",21,3,,,,,,,," + msisdn);
        mctsBeneficiaryUpdateService.updateBeneficiaryData(reader);

        Subscriber updatedSubscriber = subscriberDataService.findByNumber(msisdn);
        assertEquals(getDateString(updatedLMP), getDateString(updatedSubscriber.getLastMenstrualPeriod()));
        Subscription updatedSubscription = updatedSubscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(getDateString(updatedLMP.plusDays(90)), getDateString(updatedSubscription.getStartDate()));
        assertEquals(SubscriptionStatus.ACTIVE, updatedSubscription.getStatus());
        transactionManager.commit(status);
    }

    @Test
    public void testUpdateLMPAndReactivateCompletedSubscription() throws Exception {
        createLocationData();

        Long msisdn = sh.makeNumber();
        String motherId = "0123456789";
        DateTime originalLMP = DateTime.now().minusDays(100);
        DateTime updatedLMP = originalLMP.minusDays(100);


        MctsMother mother = new MctsMother(motherId);
        mother.setState(stateDataService.findByCode(21L));
        mother.setDistrict(districtService.findByStateAndCode(mother.getState(), 3L));
        makeMctsSubscription(mother, originalLMP, SubscriptionPackType.PREGNANCY, msisdn);

        // pre-date the LMP so that the subscription will be marked completed
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(msisdn);
        subscriber.setLastMenstrualPeriod(originalLMP.minusDays(600));
        subscriberService.updateStartDate(subscriber);
        subscriber = subscriberDataService.findByNumber(msisdn);
        Subscription subscription = subscriber.getAllSubscriptions().iterator().next();
        assertEquals(SubscriptionStatus.COMPLETED, subscription.getStatus());

        // now, via CSV update, change the LMP to a valid subscription date; subscription should get reactivated
        Reader reader = createUpdateReaderWithHeaders("1," + motherId + ",,," + getDateString(updatedLMP) + ",21,3,,,,,,,," + msisdn);
        mctsBeneficiaryUpdateService.updateBeneficiaryData(reader);

        Subscriber updatedSubscriber = subscriberDataService.findByNumber(msisdn);
        assertEquals(getDateString(updatedLMP), getDateString(updatedSubscriber.getLastMenstrualPeriod()));
        Subscription updatedSubscription = updatedSubscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(getDateString(updatedLMP.plusDays(90)), getDateString(updatedSubscription.getStartDate()));
        assertEquals(SubscriptionStatus.ACTIVE, updatedSubscription.getStatus());
        transactionManager.commit(status);
    }

    @Test
    public void testUpdateDOBAndCompleteActiveSubscription() throws Exception {
        createLocationData();

        Long msisdn = sh.makeNumber();
        String childId = "0123456789";
        DateTime originalDOB = DateTime.now().minusDays(100);
        DateTime updatedDOB = originalDOB.minusDays(400);

        MctsChild child = new MctsChild(childId);
        child.setState(stateDataService.findByCode(21L));
        child.setDistrict(districtService.findByStateAndCode(child.getState(), 3L));
        makeMctsSubscription(child, originalDOB, SubscriptionPackType.CHILD, msisdn);

        // verify that the subscription is active
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(msisdn);
        Subscription subscription = subscriber.getAllSubscriptions().iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        // now, via CSV update, change the DOB to a past subscription date; subscription should be marked completed
        Reader reader = createUpdateReaderWithHeaders("1," + childId + ",," + getDateString(updatedDOB) + ",,21,3,,,,,,,," + msisdn);
        mctsBeneficiaryUpdateService.updateBeneficiaryData(reader);

        Subscriber updatedSubscriber = subscriberDataService.findByNumber(msisdn);
        assertEquals(getDateString(updatedDOB), getDateString(updatedSubscriber.getDateOfBirth()));
        Subscription updatedSubscription = updatedSubscriber.getAllSubscriptions().iterator().next();
        assertEquals(getDateString(updatedDOB), getDateString(updatedSubscription.getStartDate()));
        assertEquals(SubscriptionStatus.COMPLETED, updatedSubscription.getStatus());
        transactionManager.commit(status);
    }

    @Test
    public void testUpdateLocation() throws Exception {
        createLocationData();

        DateTime originalDOB = DateTime.now();
        Long msisdn = sh.makeNumber();
        String childId = "0123456789";

        MctsChild child = new MctsChild(childId);
        child.setState(stateDataService.findByCode(21L));
        child.setDistrict(districtService.findByStateAndCode(child.getState(), 2L));
        makeMctsSubscription(child, DateTime.now().minusDays(100), SubscriptionPackType.CHILD, msisdn);

        Reader reader = createUpdateReaderWithHeaders("1," + childId + ",," + getDateString(originalDOB) + ",,21,3,0026,453,,,,,," + msisdn);
        mctsBeneficiaryUpdateService.updateBeneficiaryData(reader);

        MctsChild updatedChild = mctsChildDataService.findByBeneficiaryId(childId);
        assertEquals(21L, (long) updatedChild.getState().getCode());
        assertEquals(3L, (long) updatedChild.getDistrict().getCode());
        assertEquals("0026", updatedChild.getTaluka().getCode());
        assertEquals(453L, (long) updatedChild.getHealthBlock().getCode());
    }

    @Test
    public void testUpdateBeneficiariesFromFile() throws Exception {
        createLocationData();

        // ----Create 4 beneficiaries:----

        String child1id = "1234567890";
        MctsChild child1 = new MctsChild(child1id);
        child1.setState(stateDataService.findByCode(21L));
        child1.setDistrict(districtService.findByStateAndCode(child1.getState(), 3L));
        Long child1msisdn = sh.makeNumber();
        makeMctsSubscription(child1, DateTime.now().minusDays(100), SubscriptionPackType.CHILD, child1msisdn);

        String mother2id = "1234567899";
        MctsMother mother2 = new MctsMother(mother2id);
        mother2.setState(stateDataService.findByCode(21L));
        mother2.setDistrict(districtService.findByStateAndCode(mother2.getState(), 3L));
        Long mother2msisdn = sh.makeNumber();
        makeMctsSubscription(mother2, DateTime.now().minusDays(100), SubscriptionPackType.PREGNANCY, mother2msisdn);

        String mother3id = "9876543210";
        MctsMother mother3 = new MctsMother(mother3id);
        mother3.setState(stateDataService.findByCode(21L));
        mother3.setDistrict(districtService.findByStateAndCode(mother3.getState(), 3L));
        Long mother3msisdn = 9439986199L;
        makeMctsSubscription(mother3, DateTime.now().minusDays(100), SubscriptionPackType.PREGNANCY, mother3msisdn);

        MctsChild child4 = new MctsChild("9876543211");
        child4.setState(stateDataService.findByCode(21L));
        child4.setDistrict(districtService.findByStateAndCode(child4.getState(), 3L));
        Long child4msisdn = 9439986200L;
        makeMctsSubscription(child4, DateTime.now().minusDays(100), SubscriptionPackType.CHILD, child4msisdn);

        // ----Update all 4 via CSV:----

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        mctsBeneficiaryUpdateService.updateBeneficiaryData(read("csv/mcts_beneficiary_update.csv"));
        transactionManager.commit(status);

        // ----Validate updates to each:----
        TransactionStatus statusCheck = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // MSISDN update:
        Subscriber oldSubscriber1 = subscriberDataService.findByNumber(child1msisdn);
        assertNull(oldSubscriber1.getChild());
        assertEquals(0, oldSubscriber1.getActiveAndPendingSubscriptions().size());

        Subscriber subscriber1 = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber1);
        assertEquals(child1id, subscriber1.getChild().getBeneficiaryId());

        // MSISDN update:
        Subscriber oldSubscriber2 = subscriberDataService.findByNumber(mother2msisdn);
        assertNull(oldSubscriber2.getMother());
        assertEquals(0, oldSubscriber2.getActiveAndPendingSubscriptions().size());

        Subscriber subscriber2 = subscriberDataService.findByNumber(9439986188L);
        assertNotNull(subscriber2);
        assertEquals(mother2id, subscriber2.getMother().getBeneficiaryId());

        // Location update:
        MctsMother updatedMother3 = mctsMotherDataService.findByBeneficiaryId(mother3id);
        assertEquals(21L, (long) updatedMother3.getState().getCode());
        assertEquals(3L, (long) updatedMother3.getDistrict().getCode());
        assertEquals("0026", updatedMother3.getTaluka().getCode());
        assertEquals(453L, (long) updatedMother3.getHealthBlock().getCode());

        // DOB update:
        String updatedDOB = "01-12-2015";
        Subscriber subscriber4 = subscriberDataService.findByNumber(child4msisdn);
        assertEquals(updatedDOB, getDateString(subscriber4.getDateOfBirth()));
        Subscription updatedSubscription = subscriber4.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(updatedDOB, getDateString(updatedSubscription.getStartDate()));
        assertEquals(SubscriptionStatus.ACTIVE, updatedSubscription.getStatus());

        // Location insert:
        assertEquals("Taluka", subscriber4.getChild().getTaluka().getName());
        transactionManager.commit(statusCheck);
    }


    private void makeMctsSubscription(MctsBeneficiary beneficiary, DateTime startDate, SubscriptionPackType packType, Long number) {
        sh.mksub(SubscriptionOrigin.MCTS_IMPORT, startDate, packType, number);
        Subscriber subscriber = subscriberDataService.findByNumber(number);
        if (packType == SubscriptionPackType.CHILD) {
            subscriber.setChild((MctsChild) beneficiary);
            subscriber.setDateOfBirth(startDate);
        } else {
            subscriber.setMother((MctsMother) beneficiary);
            subscriber.setLastMenstrualPeriod(startDate.minusDays(90));
        }
        subscriberDataService.update(subscriber);
    }

    private Reader createUpdateReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("Sr No,MCTS ID,STATE ID,Beneficiary New DOB change,Beneficiary New LMP change,StateID,"); // 6 columns
        builder.append("District_ID,Taluka_ID,HealthBlock_ID,PHC_ID,SubCentre_ID,Village_ID,GP_Village,Address,"); // +8 columns
        builder.append("Beneficiary New Mobile no change"); // +1 column
        builder.append("\n");

        for (String line : lines) {
            builder.append(line).append("\n");
        }

        return new StringReader(builder.toString());
    }

    private Reader createChildDataReader(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("StateID\tDistrict_ID\tTaluka_ID\tHealthBlock_ID\tPHC_ID\tVillage_ID\tID_No\tName\tMother_ID\tWhom_PhoneNo\tBirthdate\tEntry_Type");
        builder.append("\n");

        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader read(String resource) {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource));
    }

    private String getDateString(DateTime date) {
        return date.toString("dd-MM-yyyy");
    }
    
    /*
     * To verify that NMS is not able to change the location of 
     * beneficiary using MCTS ID when location information is wrong.
     * 
     * https://applab.atlassian.net/browse/NMS-231
     */
    @Test
    public void verifyFT325() throws Exception {
        createLocationData();
        DateTime originalDOB = DateTime.now().minusDays(100);

        Long msisdn = sh.makeNumber();
        String childId = "0123456789";

        MctsChild child = new MctsChild(childId);
        child.setState(stateDataService.findByCode(21L));
        child.setDistrict(districtService.findByStateAndCode(child.getState(), 2L));
        makeMctsSubscription(child, DateTime.now().minusDays(100), SubscriptionPackType.CHILD, msisdn);

        //district provided in request doesn't exist in nms-db
        Reader reader = createUpdateReaderWithHeaders("1," + childId + ",," + getDateString(originalDOB) + ",,21,8,0026,453,,,,,," + msisdn);
        mctsBeneficiaryUpdateService.updateBeneficiaryData(reader);

        Subscriber subscriber = subscriberDataService.findByNumber(msisdn);
        assertNotNull(subscriber);
        assertNotEquals(subscriber.getChild().getDistrict().getCode(), new Long(7));
        
        List<SubscriptionError> susbErrors = subscriptionErrorDataService.findByBeneficiaryId(childId);
        SubscriptionError susbError = susbErrors.iterator().next();
    	assertNotNull(susbError);
        assertEquals(SubscriptionRejectionReason.INVALID_LOCATION, susbError.getRejectionReason());
    }
}
