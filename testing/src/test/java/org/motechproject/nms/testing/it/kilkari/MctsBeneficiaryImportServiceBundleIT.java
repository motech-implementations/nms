package org.motechproject.nms.testing.it.kilkari;


import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionError;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionRejectionReason;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
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
import org.motechproject.nms.region.service.HealthBlockService;
import org.motechproject.nms.region.service.HealthFacilityService;
import org.motechproject.nms.region.service.HealthSubFacilityService;
import org.motechproject.nms.region.service.LanguageService;
import org.motechproject.nms.region.service.TalukaService;
import org.motechproject.nms.region.service.VillageService;
import org.motechproject.nms.testing.it.utils.RegionHelper;
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
import java.util.Set;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;
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
public class MctsBeneficiaryImportServiceBundleIT extends BasePaxIT {

    @Inject
    TestingService testingService;
    @Inject
    LanguageDataService languageDataService;
    @Inject
    LanguageService languageService;
    @Inject
    StateDataService stateDataService;
    @Inject
    DistrictDataService districtDataService;
    @Inject
    DistrictService districtService;
    @Inject
    CircleDataService circleDataService;
    @Inject
    SubscriberDataService subscriberDataService;
    @Inject
    SubscriptionService subscriptionService;
    @Inject
    SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    MctsBeneficiaryImportService mctsBeneficiaryImportService;
    @Inject
    SubscriptionErrorDataService subscriptionErrorDataService;
    @Inject
    SubscriberService subscriberService;
    @Inject
    TalukaService talukaDataService;
    @Inject
    HealthBlockService healthBlockService;
    @Inject
    HealthFacilityService healthFacilityService;
    @Inject
    HealthSubFacilityService healthSubFacilityService;
    @Inject
    VillageService villageService;
    @Inject
    SubscriptionDataService subscriptionDataService;
    @Inject
    MctsMotherDataService mctsMotherDataService;

    @Inject
    PlatformTransactionManager transactionManager;

    SubscriptionHelper sh;

    @Before
    public void setUp() {
        testingService.clearDatabase();
        createLocationData();

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
    public void testImportMotherNewSubscriber() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals("Shanti Ekka", subscriber.getMother().getName());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(SubscriptionOrigin.MCTS_IMPORT, subscription.getOrigin());
        assertNotNull(subscriber.getCircle());
        assertEquals("Square", subscriber.getCircle().getName());
        transactionManager.commit(status);
    }

    @Test
    public void testImportMotherAlternateDateFormat() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = lmp.toString("dd/MM/yyyy");
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
    }

    @Test
    public void testImportMotherWhoAlreadyExistsUpdateLmp() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals("Shanti Ekka", subscriber.getMother().getName());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(90, Days.daysBetween(lmp.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        transactionManager.commit(status);

        DateTime newLmp = DateTime.now().minusDays(150);
        String newLmpString = getDateString(newLmp);
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                newLmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(newLmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(90, Days.daysBetween(newLmp.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        transactionManager.commit(status);
    }

    /*
     * Update of kilkari should fail if Last_Update_Date is earlier than that in the database
     */
    @Test
    public void testMotherUpdateWithLastUpdateDate() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t03-10-2016");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals("Shanti Ekka", subscriber.getMother().getName());
        List<SubscriptionError> se = subscriptionErrorDataService.findByContactNumber(9439986187L);
        assertEquals(0, se.size());
        transactionManager.commit(status);

        DateTime newLmp = DateTime.now().minusDays(150);
        String newLmpString = getDateString(newLmp);
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                newLmpString + "\t\t\t\t01-10-2016");
        mctsBeneficiaryImportService.importMotherData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);

        // Lmp update should fail
        assertNotEquals(newLmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.ALREADY_SUBSCRIBED);
        transactionManager.commit(status);
    }

    @Test
    public void testImportMotherInvalidState() throws Exception {
        Reader reader = createMotherDataReader("9\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t22-11-2015\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);
        List<SubscriptionError> se = subscriptionErrorDataService.findByContactNumber(9439986187L);
        assertEquals(1, se.size());
        assertEquals(SubscriptionRejectionReason.INVALID_LOCATION, se.get(0).getRejectionReason());
    }

    @Test
    public void testImportChildNewSubscriberNoMotherId() throws Exception {
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        assertEquals("Baby1 of Lilima Kua", subscriber.getChild().getName());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(SubscriptionOrigin.MCTS_IMPORT, subscription.getOrigin());
        assertEquals(SubscriptionPackType.CHILD, subscription.getSubscriptionPack().getType());
        assertNotNull(subscriber.getCircle());
        assertEquals("Square", subscriber.getCircle().getName());
        transactionManager.commit(status);
    }

    @Test
    public void testImportMotherAndChildSameMsisdn() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        Set<Subscription> subscriptions = subscriber.getActiveAndPendingSubscriptions();
        assertEquals(1, subscriptions.size());
        transactionManager.commit(status);

        // import child with same MSISDN and matching MotherID
        DateTime dob = DateTime.now().minusDays(200);
        String dobString = getDateString(dob);
        reader = createChildDataReader("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());

        subscriptions = subscriber.getActiveAndPendingSubscriptions();
        Subscription childSubscription = subscriptionService
                .getActiveSubscription(subscriber, SubscriptionPackType.CHILD);
        Subscription pregnancySubscription = subscriptionService
                .getActiveSubscription(subscriber, SubscriptionPackType.PREGNANCY);

        // the pregnancy subscription should have been deactivated
        assertEquals(1, subscriptions.size());
        assertNotNull(childSubscription);
        assertNull(pregnancySubscription);
        transactionManager.commit(status);
    }

    @Test
    public void testDeactivateMotherSubscriptionDueToAbortion() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        assertMother(subscriber, "1234567890", getDateTime(lmpString), "Shanti Ekka", expectedState, expectedDistrict);
        transactionManager.commit(status);

        // import record for same mother with Abortion set to "Spontaneous" -- her subscription should be deactivated
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tSpontaneous\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.MISCARRIAGE_OR_ABORTION, subscription.getDeactivationReason());
        transactionManager.commit(status);
    }

    @Test
    public void testDeactivateMotherSubscriptionDueToStillbirth() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        assertMother(subscriber, "1234567890", getDateTime(lmpString), "Shanti Ekka", expectedState, expectedDistrict);
        transactionManager.commit(status);

        // import record for same mother with Outcome_Nos set to 0 -- her subscription should be deactivated
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t0\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.STILL_BIRTH, subscription.getDeactivationReason());
        transactionManager.commit(status);
    }

    @Test
    public void testDeactivateMotherSubscriptionDueToDeath() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        assertMother(subscriber, "1234567890", getDateTime(lmpString), "Shanti Ekka", expectedState, expectedDistrict);
        transactionManager.commit(status);

        // import record for same mother with Entry_Type set to 9 -- her subscription should be deactivated
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t9\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.MATERNAL_DEATH, subscription.getDeactivationReason());
        transactionManager.commit(status);
    }

    @Test
    public void testDeactivateChildSubscriptionDueToDeath() throws Exception {
        // import mother
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        assertChild(subscriber, "1234567890", getDateTime(dobString), "Baby1 of Lilima Kua", expectedState, expectedDistrict);
        transactionManager.commit(status);

        // import record for same child with Entry_Type set to 9 -- her subscription should be deactivated
        reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t9\t");
        mctsBeneficiaryImportService.importChildData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.CHILD_DEATH, subscription.getDeactivationReason());
        transactionManager.commit(status);
    }

    @Test
    public void testImportChildDataFromSampleFile() throws Exception {
        mctsBeneficiaryImportService.importChildData(read("csv/child.txt"));

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict4 = districtService.findByStateAndCode(expectedState, 4L);

        Subscriber subscriber1 = subscriberDataService.findByNumber(9439998253L);
        assertChild(subscriber1, "210404600521400116", getDateTime("2/09/2016"), "Baby1 of PANI HEMRAM", expectedState,
                expectedDistrict4);

        // although our MCTS data file contains 10 children, we only create 8 subscribers due to -1 duplicate phone numbers and
        // -1 for old dob which has no messages
        assertEquals(8, subscriberDataService.count());
    }

    @Test
    public void verifyNIP94() throws Exception {
        RegionHelper rh = new RegionHelper(languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, districtService);

        rh.newDelhiDistrict();

        sh.mksub(SubscriptionOrigin.IVR, new DateTime(), sh.pregnancyPack().getType(), 2222222221L);

        Subscriber subscriber = subscriberDataService.findByNumber(2222222221L);
        assertNotNull(subscriber);

        mctsBeneficiaryImportService.importMotherData(read("csv/nip94.csv"));

        State expectedState = rh.delhiState();
        District expectedDistrict = rh.newDelhiDistrict();

        subscriber = subscriberDataService.findByNumber(2222222221L);
        assertMother(subscriber, "11111222299999999", getDateTime("30/4/2016"), "Shanti", expectedState, expectedDistrict);
    }

    private void assertMother(Subscriber subscriber, String motherId, DateTime lmp, String name, State state, District district) {
        assertNotNull(subscriber);
        assertNotNull(subscriber.getMother());
        assertEquals(motherId, subscriber.getMother().getBeneficiaryId());
        assertEquals(name, subscriber.getMother().getName());
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals(state, subscriber.getMother().getState());
        assertEquals(district, subscriber.getMother().getDistrict());
    }

    private void assertChild(Subscriber subscriber, String childId, DateTime dob, String name, State state, District district) {
        assertNotNull(subscriber);
        assertNotNull(subscriber.getChild());
        assertEquals(childId, subscriber.getChild().getBeneficiaryId());
        assertEquals(name, subscriber.getChild().getName());
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        assertEquals(state, subscriber.getChild().getState());
        assertEquals(district, subscriber.getChild().getDistrict());
    }

    private String getDateString(DateTime date) {
        return date.toString("dd-MM-yyyy");
    }

    private DateTime getDateTime(String dateString) {
        DateTimeParser[] parsers = {
                DateTimeFormat.forPattern("dd-MM-yyyy").getParser(),
                DateTimeFormat.forPattern("dd/MM/yyyy").getParser()};
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

        return formatter.parseDateTime(dateString);
    }

    private Reader createChildDataReader(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("StateID\tDistrict_ID\tTaluka_ID\tHealthBlock_ID\tPHC_ID\tVillage_ID\tID_No\tName\tMother_ID\tWhom_PhoneNo\tBirthdate\tEntry_Type\tLast_Update_Date");
        builder.append("\n");

        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader createMotherDataReader(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("StateID\tDistrict_ID\tTaluka_ID\tHealthBlock_ID\tPHC_ID\tVillage_ID\tID_No\tName\tWhom_PhoneNo\tBirthdate\tLMP_Date\t");
        builder.append("Abortion\tOutcome_Nos\tEntry_Type\tLast_Update_Date");
        builder.append("\n");

        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader read(String resource) {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource));
    }

    /*
     * To verify mother subscription is rejected when future LMP is provided 
     */
    @Test
    public void verifyFT282() throws Exception {
        DateTime lmp = DateTime.now().plusDays(1);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_LMP'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.INVALID_LMP);
    }

    /*
     * To verify child subscriber is rejected when future DOB is provided.
     */
    @Test
    public void verifyFT283() throws Exception {
        DateTime dob = DateTime.now().plusDays(1);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_DOB'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_DOB);
    }

    /*
     * To verify mother subscription is rejected when LMP provided is 72 weeks back. 
     */
    @Test
    public void verifyFT284() throws Exception {
        DateTime lmp = DateTime.now().minusDays(7 * 72 + 90);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_LMP'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.INVALID_LMP);
    }

    /*
     * To verify child subscription is rejected when DOB provided is 48 weeks back. 
     */
    @Test
    public void verifyFT285() throws Exception {
        DateTime dob = DateTime.now().minusDays(7 * 48);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_DOB'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_DOB);
    }

    /*
     * To verify MCTS upload is rejected when MSISDN number already exist 
     * for subscriber with new mctsid (beneficiary id).
     */
    @Test
    public void verifyFT287() throws Exception {

        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);

        // create subscriber and subscription
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        // attempt to create subscriber with same msisdn but different mcts.
        reader = createChildDataReader("21\t3\t\t\t\t\t1234567891\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        //rejected entry should be in nms_subscription_errors with reason 'ALREADY_SUBSCRIBED'.
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.ALREADY_SUBSCRIBED);
    }

    /*
     * To verify MCTS upload is rejected when MCTS doesn’t contain DOB.
     */
    @Test
    public void verifyFT288_1() throws Exception {

        //DOB is missing
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'MISSING_DOB'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.MISSING_DOB);
    }

    /**
     * To verify MCTS upload is rejected when MCTS doesn’t contain LMP.
     */
    @Test
    public void verifyFT288_2() throws Exception {

        //LMP is missing
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'MISSING_LMP'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.MISSING_LMP);
    }

    private void assertSubscriptionError(Long callingNumber, SubscriptionPackType packType,
                                         SubscriptionRejectionReason rejectionReason) {

        List<SubscriptionError> susbErrors = subscriptionErrorDataService.findByContactNumber(callingNumber);
        SubscriptionError susbError = susbErrors.iterator().next();

        assertNotNull(susbError);
        assertEquals(packType, susbError.getPackType());
        assertEquals(rejectionReason, susbError.getRejectionReason());
    }

    /**
     * NMS_FT_289:: To verify new subscription is created successfully when subscription
     * already exist having status as "Completed" for same MSISDN.
     * NMS_FT_306:: To verify LMP is changed successfully via CSV when subscription
     * already exist for pregnancyPack having status as "Completed"
     */
    @Test
    public void verifyFT289() throws Exception {
        DateTime lmp = DateTime.now();
        String lmpString = getDateString(lmp);

        // create subscriber and subscription
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        //Make subscription completed
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        subscriber.setLastMenstrualPeriod(lmp.minusDays(650));
        subscriberService.updateStartDate(subscriber);

        //create a new subscription for subscriber whose subscription is completed.
        lmpString = getDateString(lmp.minus(200));
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" + lmpString
                + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        assertEquals(2, subscriber.getAllSubscriptions().size());
        assertEquals(1, subscriber.getActiveAndPendingSubscriptions().size());
        assertEquals(lmpString, getDateString(subscriber.getLastMenstrualPeriod()));
        transactionManager.commit(status);
    }

    /*
     * NMS_FT_290:: To verify new subscription is created successfully when subscription 
     * already exist having status as "Deactivated" for same MSISDN.
     * NMS_FT_305:: To verify LMP is changed successfully via CSV when subscription 
     * already exist for pregnancyPack having status as "Deactivated"
     */
    @Test
    public void verifyFT290() throws Exception {
        DateTime lmp = DateTime.now().minus(100);
        String lmpString = getDateString(lmp);

        // create subscriber and subscription
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        //Mark subscription deactivate
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.MISCARRIAGE_OR_ABORTION);
        transactionManager.commit(status);

        //create a new subscription for subscriber whose subscription is deactivated.
        lmpString = getDateString(lmp.minus(200));
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" + lmpString
                + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        assertEquals(2, subscriber.getAllSubscriptions().size());
        assertEquals(1, subscriber.getActiveAndPendingSubscriptions().size());
        assertEquals(lmpString, getDateString(subscriber.getLastMenstrualPeriod()));
        transactionManager.commit(status);
    }

    /*
     * To verify MCTS upload is rejected when location information is incorrect.
     * 
     * https://applab.atlassian.net/browse/NMS-208
     */
    @Test
    public void verifyFT286() throws Exception {
        State state31 = createState(31L, "State 31");
        stateDataService.create(state31);
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);

        //attempt to create subscriber and subscription with wrong state-district combination. it should be rejected
        Reader reader = createChildDataReader("31\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_LOCATION'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    @Test
    public void verifyRejectedWithNoState() throws Exception {
        State state31 = createState(31L, "State 31");
        stateDataService.create(state31);
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);

        Reader reader = createChildDataReader("\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t" + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_LOCATION'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    @Test
    public void verifyRejectedWithNoDistrict() throws Exception {
        State state31 = createState(31L, "State 31");
        stateDataService.create(state31);
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);

        Reader reader = createChildDataReader("31\t\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t" + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_LOCATION'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    /*
     * To verify DOB is changed successfully via CSV when subscription 
     * already exists for childPack having status as "Deactivated"
     */
    @Test
    public void verifyFT309() throws Exception {
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        //Mark subscription deactivate
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.STILL_BIRTH);
        transactionManager.commit(status);

        //create a new subscription for subscriber whose subscription is deactivated.
        dobString = getDateString(dob.minus(50));
        reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        assertEquals(2, subscriber.getAllSubscriptions().size());
        assertEquals(1, subscriber.getActiveAndPendingSubscriptions().size());
        assertEquals(dobString, getDateString(subscriber.getDateOfBirth()));
        transactionManager.commit(status);
    }

    /*
     * To verify DOB is changed successfully via CSV when subscription 
     * already exist for childPack having status as "Completed"
     */
    @Test
    public void verifyFT310() throws Exception {
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        //Make subscription completed
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        subscriber.setDateOfBirth(dob.minusDays(500));
        subscriberService.updateStartDate(subscriber);

        //create a new subscription for subscriber whose subscription is deactivated.
        dobString = getDateString(dob.minus(50));
        reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        assertEquals(2, subscriber.getAllSubscriptions().size());
        assertEquals(1, subscriber.getActiveAndPendingSubscriptions().size());
        assertEquals(dobString, getDateString(subscriber.getDateOfBirth()));
        transactionManager.commit(status);
    }

    /*
     * To verify DOB is changed successfully via CSV when subscription 
     * already exist for childPack having status as "Active"
     */
    @Test
    public void verifyFT311() throws Exception {
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        assertEquals("Baby1 of Lilima Kua", subscriber.getChild().getName());

        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(0, Days.daysBetween(dob.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        transactionManager.commit(status);

        // attempt to update dob through mcts upload
        DateTime newDob = DateTime.now().minusDays(150);
        String newDobString = getDateString(newDob);
        reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t" +
                newDobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(newDob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(0, Days.daysBetween(newDob.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        transactionManager.commit(status);
    }

    /*
     * To verify that NMS shall deactivate pregancyPack if childPack uploads
     * for updation which contains motherId for an active mother beneficiary.
     * 
     * https://applab.atlassian.net/browse/NMS-207
     */
    @Test
    public void verifyFT322() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        Set<Subscription> subscriptions = subscriber.getActiveAndPendingSubscriptions();
        assertEquals(1, subscriptions.size());
        transactionManager.commit(status);

        // import child with same MSISDN and no MotherID
        DateTime dob = DateTime.now().minusDays(200);
        String dobString = getDateString(dob);
        reader = createChildDataReader("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());

        subscriptions = subscriber.getActiveAndPendingSubscriptions();
        Subscription childSubscription = subscriptionService
                .getActiveSubscription(subscriber, SubscriptionPackType.CHILD);
        Subscription pregnancySubscription = subscriptionService
                .getActiveSubscription(subscriber, SubscriptionPackType.PREGNANCY);
        transactionManager.commit(status);

        // both subscription should have been activated
        assertEquals(2, subscriptions.size());
        assertNotNull(childSubscription);
        assertNotNull(pregnancySubscription);

        // import child with same MSISDN and above MotherID --> child should be updated and mother be deactivated
        reader = createChildDataReader("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        subscriptions = subscriber.getActiveAndPendingSubscriptions();
        childSubscription = subscriptionService.getActiveSubscription(subscriber, SubscriptionPackType.CHILD);
        pregnancySubscription = subscriptionService
                .getActiveSubscription(subscriber, SubscriptionPackType.PREGNANCY);
        transactionManager.commit(status);

        //only child subscription should be activated
        assertEquals(1, subscriptions.size());
        assertNotNull(childSubscription);
        assertNull(pregnancySubscription);
    }

    /*
     * To verify LMP is changed successfully via CSV when subscription 
     * already exist for pregnancyPack having status as "Pending Activation"
     */
    @Test
    public void verifyFT308() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals("Shanti Ekka", subscriber.getMother().getName());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(90, Days.daysBetween(lmp.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        assertEquals(subscription.getStatus(), SubscriptionStatus.PENDING_ACTIVATION);
        transactionManager.commit(status);

        DateTime newLmp = DateTime.now().minusDays(90);
        String newLmpString = getDateString(newLmp);
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                newLmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(newLmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(90, Days.daysBetween(newLmp.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        assertEquals(subscription.getStatus(), SubscriptionStatus.ACTIVE);
        transactionManager.commit(status);
    }

    @Test
    public void testImportMotherDataFromSampleFile() throws Exception {
        mctsBeneficiaryImportService.importMotherData(read("csv/mother.txt"));

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        Subscriber subscriber1 = subscriberDataService.findByNumber(9439986187L);
        assertMother(subscriber1, "210302604211400029", getDateTime("22/11/2016"), "Shanti Ekka", expectedState,
                expectedDistrict);

        Subscriber subscriber2 = subscriberDataService.findByNumber(7894221701L);
        assertMother(subscriber2, "210302604611400025", getDateTime("9/12/2016"), "Sanjukta Bhainsa", expectedState,
                expectedDistrict);

        // although our MCTS data file contains 10 mothers, we only create 4 subscribers due to duplicate phone numbers
        // and pack expiration dates
        assertEquals(4, subscriberDataService.count());


        // verify location data was created on the fly
        State state = stateDataService.findByCode(21L);
        District district = districtService.findByStateAndCode(state, 3L);
        Taluka taluka = talukaDataService.findByDistrictAndCode(district, "111");
        assertEquals("Taluka", taluka.getName());

        HealthBlock healthBlock = healthBlockService.findByTalukaAndCode(taluka, 222L);
        assertEquals("HealthBlock", healthBlock.getName());

        HealthFacility healthFacility = healthFacilityService.findByHealthBlockAndCode(healthBlock, 333L);
        assertEquals("PHC", healthFacility.getName());

        HealthSubFacility healthSubFacility = healthSubFacilityService.findByHealthFacilityAndCode(healthFacility, 444L);
        assertEquals("SubCentre", healthSubFacility.getName());

        Village village = villageService.findByTalukaAndVcodeAndSvid(taluka, 555L, 0L);
        assertEquals("Village", village.getName());

    }

    private void assertNoSubscriber(long callingNumber) {
        Subscriber subscriber = subscriberDataService.findByNumber(callingNumber);
        assertNull(subscriber);
    }

    /*
     * To verify pregnancyPack is marked deactivated with reason abortion via CSV. 
     * checked with abortion value 'MTP<12 Weeks'
     */
    @Test
    public void verifyFT313_1() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with abortion value 'MTP<12 Weeks'
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tMTP<12 Weeks\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertNotNull(subscription);
        assertEquals(subscription.getStatus(), SubscriptionStatus.DEACTIVATED);
        assertEquals(subscription.getDeactivationReason(), DeactivationReason.MISCARRIAGE_OR_ABORTION);
        transactionManager.commit(status);
    }

    /*
     * To verify pregnancyPack is marked deactivated with reason abortion via CSV.
     * checked with abortion value 'Spontaneous'
     */
    @Test
    public void verifyFT313_2() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with abortion value 'Spontaneous'
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tSpontaneous\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertNotNull(subscription);
        assertEquals(subscription.getStatus(), SubscriptionStatus.DEACTIVATED);
        assertEquals(subscription.getDeactivationReason(), DeactivationReason.MISCARRIAGE_OR_ABORTION);
        transactionManager.commit(status);
    }

    /*
     * To verify pregnancyPack is marked deactivated with reason abortion via CSV. 
     * with abortion value 'MTP>12 Weeks'
     */
    @Test
    public void verifyFT313_3() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with abortion value 'MTP>12 Weeks'
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tMTP>12 Weeks\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertNotNull(subscription);
        assertEquals(subscription.getStatus(), SubscriptionStatus.DEACTIVATED);
        assertEquals(subscription.getDeactivationReason(), DeactivationReason.MISCARRIAGE_OR_ABORTION);
        transactionManager.commit(status);
    }

    /*
     * To verify pregnancyPack is marked deactivated with reason still birth via CSV. 
     */
    @Test
    public void verifyFT315() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with Outcome_Nos value '0'
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t0\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertNotNull(subscription);
        assertEquals(subscription.getStatus(), SubscriptionStatus.DEACTIVATED);
        assertEquals(subscription.getDeactivationReason(), DeactivationReason.STILL_BIRTH);

        transactionManager.commit(status);
    }

    /*
     * To verify mother MCTS upload is rejected when stateId is missing
     *
     * https://applab.atlassian.net/browse/NMS-228
     */
    @Test
    public void verifyFT525() throws Exception {
        String dobString = getDateString(DateTime.now().minusDays(30));
        //state id is missing
        Reader reader = createChildDataReader("\t6\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    /*
     * To verify mother MCTS upload is rejected with invalid state id
     */
    @Test
    public void verifyFT526() throws Exception {
        String dobString = getDateString(DateTime.now().minusDays(30));
        //state id with invalid value
        Reader reader = createChildDataReader("31\t6\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    /*
     * To verify child MCTS upload is rejected with invalid district id
     */
    @Test
    public void verifyFT527() throws Exception {
        String dobString = getDateString(DateTime.now().minusDays(30));
        Reader reader = createChildDataReader("21\t6\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        //district id with invalid value
        mctsBeneficiaryImportService.importChildData(reader);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    /*
     * To verify child MCTS upload is rejected when mandatory parameter district is missing. 
     *
     * https://applab.atlassian.net/browse/NMS-228
     */
    @Test
    public void verifyFT529() throws Exception {
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);
        //district id is missing
        Reader reader = createChildDataReader("21\t\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    /*
     * To verify child MCTS upload is rejected when mandatory parameter state is having invalid value. 
     */
    @Test
    public void verifyFT530() throws Exception {
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);
        //state id with invalid value
        Reader reader = createChildDataReader("31\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    /*
     * To verify child MCTS upload is rejected when mandatory parameter district is having invalid value. 
     */
    @Test
    public void verifyFT531() throws Exception {
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);
        //district id with invalid value
        Reader reader = createChildDataReader("21\t6\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    /*
     * To verify pregnancyPack is marked deactivated with reason mother death via CSV. 
     */
    @Test
    public void verifyFT314() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with entry_type value '9'
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t9\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertNotNull(subscription);
        assertEquals(subscription.getStatus(), SubscriptionStatus.DEACTIVATED);
        assertEquals(subscription.getDeactivationReason(), DeactivationReason.MATERNAL_DEATH);
        transactionManager.commit(status);
    }

    /*
     * To verify child pack is marked deactivated with reason child death via CSV. 
     */
    @Test
    public void verifyFT316() throws Exception {
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);

        //attempt to create child data with entry_type '9'
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t9\t");
        mctsBeneficiaryImportService.importChildData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertNotNull(subscription);
        assertEquals(subscription.getStatus(), SubscriptionStatus.DEACTIVATED);
        assertEquals(subscription.getDeactivationReason(), DeactivationReason.CHILD_DEATH);
        transactionManager.commit(status);
    }

    /*
     * To verify pregnancy record gets created when child record exist with status as deactivated.
     *
     * https://applab.atlassian.net/browse/NMS-234
     */
    @Test
    public void testCreateMotherSubscriptionWhenDeactivatedChildSubscriptionExists() throws Exception {
        // import child
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());
        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        //Deactivate child subscription
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.CHILD_DEATH);
        subscriber = subscriberDataService.findByNumber(9439986187L);
        subscriptions = subscriber.getAllSubscriptions();
        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.CHILD_DEATH, subscription.getDeactivationReason());
        transactionManager.commit(status);

        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567891\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        //pregnancy record should be activated.
        subscriber = subscriberDataService.findByNumber(9439986187L);
        assertEquals(1, subscriber.getActiveAndPendingSubscriptions().size());
        assertEquals(2, subscriber.getAllSubscriptions().size());
        transactionManager.commit(status);
    }

    /*
     * Verify correct circle when inserting mothers in state with two districts with each district a different circle
     *
     * https://applab.atlassian.net/browse/NMS-234
     */
    @Test
    public void testImportIntoStateWithTwoCircles() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                        lmpString + "\t\t\t\t",
                "21\t5\t\t\t\t\t1234567890\tShanti Ekka\t9439986188\t\t" +
                        lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals("Square", subscriber.getCircle().getName());

        subscriber = subscriberDataService.findByNumber(9439986188L);
        assertNotNull(subscriber);
        assertEquals("Rectangle", subscriber.getCircle().getName());
    }

    @Test
    public void testMotherDeactivatesIfChildActive() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        // import child
        DateTime dob = DateTime.now().minusDays(5);
        String dobString = getDateString(dob);
        reader = createChildDataReader("21\t3\t\t\t\t\t1234567891\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportService.importChildData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();

        Subscription childSubscription = subscriptionService
                .getActiveSubscription(subscriber, SubscriptionPackType.CHILD);
        Subscription pregnancySubscription = subscriptionService
                .getActiveSubscription(subscriber, SubscriptionPackType.PREGNANCY);

        //the mother subscription should be DEACTIVATED
        assertEquals(2, subscriptions.size());
        assertNotNull(childSubscription);
        assertNull(pregnancySubscription);
        transactionManager.commit(status);

    }

    /*
     * To verify child subscription is rejected when Last_Update_Date is earlier than that in database
     */
    @Test
    public void testChildUpdateWithLastUpdateDate() throws Exception {
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t\t03-10-2016");
        mctsBeneficiaryImportService.importChildData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        List<SubscriptionError> se = subscriptionErrorDataService.findByContactNumber(9439986187L);
        assertEquals(0, se.size());
        transactionManager.commit(status);

        DateTime newdob = DateTime.now().minusDays(150);
        String newdobString = getDateString(newdob);
        reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + newdobString + "\t\t01-10-2016");
        mctsBeneficiaryImportService.importChildData(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber);
        // Update DOB should fail
        assertNotEquals(newdob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.ALREADY_SUBSCRIBED);
        transactionManager.commit(status);
    }

    @Test
    public void testForSubscriberAbsent() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();

        //the mother subscription should be DEACTIVATED
        assertEquals(1, subscriptions.size());
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        for (Subscription subscription:subscriptions
                ) {
            subscription.setStatus(SubscriptionStatus.DEACTIVATED);
            subscription.setEndDate(new DateTime().withDate(2011, 8, 1));
            subscriptionDataService.update(subscription);
        }

        subscriptionService.purgeOldInvalidSubscriptions();
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNull(subscriber);
        List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
        assertEquals(1,mothers.size());
        transactionManager.commit(status);

//        import mother again. This time subscriber should not get created.
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\n" +
                "21\t3\t\t\t\t\t210302604211400029\tShanti Ekkam\t9439986140\t\t" +
                lmpString + "\t\t\t\t\n" +
                "21\t3\t\t\t\t\t210302604611400025\tSanjukta Bhainsa\t7894221701\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNull(subscriber);
        Subscriber subscriber1 = subscriberDataService.findByNumber(9439986140L);
        Subscriber subscriber2 = subscriberDataService.findByNumber(7894221701L);
        assertNotNull(subscriber1);
        assertNotNull(subscriber2);
        transactionManager.commit(status);
    }

}