package org.motechproject.nms.testing.it.kilkari;


import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.csv.domain.CsvAuditRecord;
import org.motechproject.nms.csv.repository.CsvAuditRecordDataService;
import org.motechproject.nms.kilkari.domain.*;
import org.motechproject.nms.kilkari.repository.*;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportReaderService;
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
import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;
import org.motechproject.nms.rejectionhandler.domain.MotherImportRejection;
import org.motechproject.nms.rejectionhandler.repository.ChildRejectionDataService;
import org.motechproject.nms.rejectionhandler.repository.MotherRejectionDataService;
import org.motechproject.nms.testing.it.api.utils.RequestBuilder;
import org.motechproject.nms.testing.it.utils.RegionHelper;
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.http.SimpleHttpClient;
import org.motechproject.testing.utils.TestContext;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.supercsv.exception.SuperCsvException;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
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
    MotherRejectionDataService motherRejectionDataService;
    @Inject
    ChildRejectionDataService childRejectionDataService;
    @Inject
    SubscriptionService subscriptionService;
    @Inject
    SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    MctsBeneficiaryImportReaderService mctsBeneficiaryImportReaderService;
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
    SubscriberMsisdnTrackerDataService subscriberMsisdnTrackerDataService;
    @Inject
    BlockedMsisdnRecordDataService blockedMsisdnRecordDataService;
    @Inject
    MctsChildDataService mctsChildDataService;
    @Inject
    ReactivatedBeneficiaryAuditDataService reactivatedBeneficiaryAuditDataService;

    @Inject
    PlatformTransactionManager transactionManager;

    @Inject
    private CsvAuditRecordDataService csvAuditRecordDataService;

    SubscriptionHelper sh;
    RegionHelper rh;

    public static final String SUCCESS = "Success";
    private String deactivationRequest = String.format("http://localhost:%d/api/ops/deactivationRequest",
            TestContext.getJettyPort());

    @Before
    public void setUp() {
        testingService.clearDatabase();
        createLocationData();

        sh = new SubscriptionHelper(subscriberService,subscriptionService, subscriberDataService, subscriptionPackDataService,
                languageDataService, languageService, circleDataService, stateDataService, districtDataService,
                districtService);
        rh = new RegionHelper(languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, districtService);

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

        //TODO HARITHA commented 2 lines m-n taluka hb
        HealthBlock healthBlock259 = createHealthBlock(taluka24, 259L, "Laikera", "hq");
//        taluka24.addHealthBlock(healthBlock259);
//
        HealthBlock healthBlock453 = createHealthBlock(taluka26, 453L, "Bamara", "hq");
//        taluka26.addHealthBlock(healthBlock453);
//
        HealthBlock healthBlock153 = createHealthBlock(taluka46, 153L, "Tileibani", "hq");
//        taluka46.addHealthBlock(healthBlock153);

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
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals("Shanti Ekka", subscriber.getMother().getName());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(SubscriptionOrigin.MCTS_IMPORT, subscription.getOrigin());
        assertNotNull(subscriber.getCircle());
        assertEquals("Square", subscriber.getCircle().getName());
        transactionManager.commit(status);
    }

    //Ignored due to IndexOutOfBoundException
    @Test
    @Ignore
    public void testImportMotherAlternateDateFormat() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = lmp.toString("dd/MM/yyyy");
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
    }

    @Test
    public void testImportMotherWhoAlreadyExistsUpdateLmp() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
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
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
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
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals("Shanti Ekka", subscriber.getMother().getName());
        List<MotherImportRejection> motherImportRejectionList = motherRejectionDataService.retrieveAll();
        assertEquals(0, motherImportRejectionList.size());
        transactionManager.commit(status);

        DateTime newLmp = DateTime.now().minusDays(150);
        String newLmpString = getDateString(newLmp);
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                newLmpString + "\t\t\t\t01-10-2016");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        assertNotNull(subscriber);

        // Lmp update should fail
        assertNotEquals(newLmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        List<MotherImportRejection> motherImportRejections = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejections.size());
        assertEquals("9439986187", motherImportRejections.get(0).getMobileNo());
        assertEquals(RejectionReasons.UPDATED_RECORD_ALREADY_EXISTS.toString(), motherImportRejections.get(0).getRejectionReason());
        transactionManager.commit(status);
    }

    /* Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void testImportMotherInvalidState() throws Exception {
        Reader reader = createMotherDataReader("9\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t22-11-2016\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);
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
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscriber = subscriberService.getSubscriber(9439986187L);
        assertEquals(1, subscriber.size());
        transactionManager.commit(status);
    }

    @Test
    public void testImportMotherAndChildSameMsisdn() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscriber = subscriberService.getSubscriber(9439986187L);
        assertEquals(1, subscriber.size());
        assertEquals(lmp.toLocalDate(), subscriber.get(0).getLastMenstrualPeriod().toLocalDate());
        Set<Subscription> subscriptions = subscriber.get(0).getActiveAndPendingSubscriptions();
        assertEquals(1, subscriptions.size());
        transactionManager.commit(status);

        // import child with same MSISDN and matching MotherID
        DateTime dob = DateTime.now().minusDays(200);
        String dobString = getDateString(dob);
        reader = createChildDataReader("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L);
        assertEquals(1, subscriber.size());
        assertEquals(dob.toLocalDate(), subscriber.get(0).getDateOfBirth().toLocalDate());

        subscriptions = subscriber.get(0).getActiveAndPendingSubscriptions();
        Subscription childSubscription = subscriptionService
                .getActiveSubscription(subscriber.get(0), SubscriptionPackType.CHILD);
        Subscription pregnancySubscription = subscriptionService
                .getActiveSubscription(subscriber.get(0), SubscriptionPackType.PREGNANCY);

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
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);
        transactionManager.commit(status);

        assertMother(subscriber, "1234567890", getDateTime(lmpString), "Shanti Ekka", expectedState, expectedDistrict);

        // import record for same mother with Abortion set to "Spontaneous" -- her subscription should be deactivated
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tSpontaneous\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.MISCARRIAGE_OR_ABORTION, subscription.getDeactivationReason());
        transactionManager.commit(status);
    }

    //Ignored due to IndexOutOfBoundException
    @Test
    @Ignore
    public void testDeactivateMotherSubscriptionDueToStillbirth() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
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
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
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
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);
        transactionManager.commit(status);

        assertMother(subscriber, "1234567890", getDateTime(lmpString), "Shanti Ekka", expectedState, expectedDistrict);

        // import record for same mother with Entry_Type set to 9 -- her subscription should be deactivated
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t9\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.MATERNAL_DEATH, subscription.getDeactivationReason());
        transactionManager.commit(status);
    }

    /* Ignored due to IndexOutOfBoundException
     */
    @Ignore
    @Test
    public void testDeactivateChildSubscriptionDueToDeath() throws Exception {
        // import mother
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        assertChild(subscriber, "1234567890", getDateTime(dobString), "Baby1 of Lilima Kua", expectedState, expectedDistrict);
        transactionManager.commit(status);

        // import record for same child with Entry_Type set to 9 -- her subscription should be deactivated
        reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t9\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.CHILD_DEATH, subscription.getDeactivationReason());
        transactionManager.commit(status);
    }

    //Ignored due to IndexOutOfBoundException
    @Ignore
    @Test
    public void testImportChildDataFromSampleFile() throws Exception {
        mctsBeneficiaryImportReaderService.importChildData(read("csv/child.txt"), SubscriptionOrigin.MCTS_IMPORT);

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict4 = districtService.findByStateAndCode(expectedState, 4L);

        Subscriber subscriber1 = subscriberService.getSubscriber(9439998253L).get(0);
        assertChild(subscriber1, "210404600521400116", getDateTime("2/12/2017"), "Baby1 of PANI HEMRAM", expectedState,
                expectedDistrict4);

        // although our MCTS data file contains 10 children, we only create 8 subscribers due to -1 duplicate phone numbers and
        // -1 for old dob which has no messages
        assertEquals(8, subscriberDataService.count());
    }

    //Ignored as it is related to Location Module
    @Ignore
    @Test
    public void verifyNIP94() throws Exception {
        RegionHelper rh = new RegionHelper(languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, districtService);

        rh.newDelhiDistrict();

        sh.mksub(SubscriptionOrigin.IVR, new DateTime(), sh.pregnancyPack().getType(), 2222222221L);

        Subscriber subscriber = subscriberService.getSubscriber(2222222221L).get(0);
        assertNotNull(subscriber);

        mctsBeneficiaryImportReaderService.importMotherData(read("csv/nip94.csv"), SubscriptionOrigin.MCTS_IMPORT);

        State expectedState = rh.delhiState();
        District expectedDistrict = rh.newDelhiDistrict();

        subscriber = subscriberService.getSubscriber(2222222221L).get(0);
        assertMother(subscriber, "11111222299999999", getDateTime("28/2/2017"), "Shanti", expectedState, expectedDistrict);
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
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_mother_rejects with reason 'INVALID_LMP_DATE'.
        List<MotherImportRejection> motherImportRejections = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejections.size());
        assertNoSubscriber(9439986187L);
        assertEquals("9439986187", motherImportRejections.get(0).getMobileNo());
        assertEquals(RejectionReasons.INVALID_LMP_DATE.toString(), motherImportRejections.get(0).getRejectionReason());
    }

    /*
     * To verify child subscriber is rejected when future DOB is provided.
     */
    @Test
    public void verifyFT283() throws Exception {
        DateTime dob = DateTime.now().plusDays(1);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_child_rejects with reason 'INVALID_DOB'.
        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        assertEquals(1, childImportRejections.size());
        assertNoSubscriber(9439986187L);
        assertEquals("9439986187", childImportRejections.get(0).getMobileNo());
        assertEquals(RejectionReasons.INVALID_DOB.toString(), childImportRejections.get(0).getRejectionReason());
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
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_mother_rejects with reason 'INVALID_LMP_DATE'.
        List<MotherImportRejection> motherImportRejections = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejections.size());
        assertNoSubscriber(9439986187L);
        assertEquals("9439986187", motherImportRejections.get(0).getMobileNo());
        assertEquals(RejectionReasons.INVALID_LMP_DATE.toString(), motherImportRejections.get(0).getRejectionReason());
    }

    /*
     * To verify child subscription is rejected when DOB provided is 48 weeks back. 
     */
    @Test
    public void verifyFT285() throws Exception {
        DateTime dob = DateTime.now().minusDays(7 * 48);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_child_rejects with reason 'INVALID_DOB'.
        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        assertEquals(1, childImportRejections.size());
        assertNoSubscriber(9439986187L);
        assertEquals("9439986187", childImportRejections.get(0).getMobileNo());
        assertEquals(RejectionReasons.INVALID_DOB.toString(), childImportRejections.get(0).getRejectionReason());
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
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        // attempt to create subscriber with same msisdn but different mcts.
        reader = createChildDataReader("21\t3\t\t\t\t\t1234567891\tBaby1 of Lilima Kua\t9876453211\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        //rejected entry should be in nms_child_rejects with reason 'ALREADY_SUBSCRIBED'.
        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        assertEquals(1, childImportRejections.size());
        assertEquals("9439986187", childImportRejections.get(0).getMobileNo());
        assertEquals(RejectionReasons.MOBILE_NUMBER_ALREADY_SUBSCRIBED.toString(), childImportRejections.get(0).getRejectionReason());
        transactionManager.commit(status);
    }

    /*
     * To verify MCTS upload is rejected when MCTS doesn’t contain DOB.
     */
    @Test
    public void verifyFT288_1() throws Exception {

        //DOB is missing
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_child_rejects with reason 'MISSING_DOB'.
        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        assertEquals(1, childImportRejections.size());
        assertNoSubscriber(9439986187L);
        assertEquals("9439986187", childImportRejections.get(0).getMobileNo());
        assertEquals(RejectionReasons.INVALID_DOB.toString(), childImportRejections.get(0).getRejectionReason());
    }

    /**
     * To verify MCTS upload is rejected when MCTS doesn’t contain LMP.
     */
    @Test
    public void verifyFT288_2() throws Exception {

        //LMP is missing
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_mother_rejects with reason 'MISSING_LMP'.
        List<MotherImportRejection> motherImportRejections = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejections.size());
        assertNoSubscriber(9439986187L);
        assertEquals("9439986187", motherImportRejections.get(0).getMobileNo());
        assertEquals(RejectionReasons.INVALID_LMP_DATE.toString(), motherImportRejections.get(0).getRejectionReason());
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
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        //Make subscription completed
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        subscriber.setLastMenstrualPeriod(lmp.minusDays(650));
        subscriberService.updateStartDate(subscriber);
        transactionManager.commit(status);

        //create a new subscription for subscriber whose subscription is completed.
        lmpString = getDateString(lmp.minus(200));
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" + lmpString
                + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
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
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        //Mark subscription deactivate
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.MISCARRIAGE_OR_ABORTION);
        transactionManager.commit(status);

        //create a new subscription for subscriber whose subscription is deactivated.
        lmpString = getDateString(lmp.minus(200));
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" + lmpString
                + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        assertEquals(1, subscriber.getAllSubscriptions().size());
        assertEquals(0, subscriber.getActiveAndPendingSubscriptions().size());
        assertEquals(lmpString, getDateString(subscriber.getLastMenstrualPeriod()));
        transactionManager.commit(status);
    }

    /*
     * To verify MCTS upload is rejected when location information is incorrect.
     * 
     * https://applab.atlassian.net/browse/NMS-208
     * Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void verifyFT286() throws Exception {
        State state31 = createState(31L, "State 31");
        stateDataService.create(state31);
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);

        //attempt to create subscriber and subscription with wrong state-district combination. it should be rejected
        Reader reader = createChildDataReader("31\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_LOCATION'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    /* Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void verifyRejectedWithNoState() throws Exception {
        State state31 = createState(31L, "State 31");
        stateDataService.create(state31);
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);

        Reader reader = createChildDataReader("\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t" + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_LOCATION'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    /* Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void verifyRejectedWithNoDistrict() throws Exception {
        State state31 = createState(31L, "State 31");
        stateDataService.create(state31);
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);

        Reader reader = createChildDataReader("31\t\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t" + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

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
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        //Mark subscription deactivate
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.STILL_BIRTH);

        //create a new subscription for subscriber whose subscription is deactivated.
        dobString = getDateString(dob.minus(50));
        reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        assertEquals(1, subscriber.getAllSubscriptions().size());
        assertEquals(0, subscriber.getActiveAndPendingSubscriptions().size());
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
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        //Make subscription completed
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        subscriber.setDateOfBirth(dob.minusDays(500));
        subscriberService.updateStartDate(subscriber);
        transactionManager.commit(status);

        //create a new subscription for subscriber whose subscription is deactivated.
        dobString = getDateString(dob.minus(50));
        reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
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
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        assertEquals("Baby1 of Lilima Kua", subscriber.getChild().getName());

        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        transactionManager.commit(status);
        assertEquals(0, Days.daysBetween(dob.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());

        // attempt to update dob through mcts upload
        DateTime newDob = DateTime.now().minusDays(150);
        String newDobString = getDateString(newDob);
        reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t" +
                newDobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
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
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        Set<Subscription> subscriptions = subscriber.getActiveAndPendingSubscriptions();
        transactionManager.commit(status);
        assertEquals(1, subscriptions.size());

        // import child with same MSISDN and above MotherID --> child should be updated and mother be deactivated
        DateTime dob = DateTime.now().minusDays(200);
        String dobString = getDateString(dob);
        reader = createChildDataReader("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        subscriptions = subscriber.getActiveAndPendingSubscriptions();
        Subscription childSubscription = subscriptionService.getActiveSubscription(subscriber, SubscriptionPackType.CHILD);
        Subscription pregnancySubscription = subscriptionService
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
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
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
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(newLmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(90, Days.daysBetween(newLmp.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        assertEquals(subscription.getStatus(), SubscriptionStatus.ACTIVE);
        transactionManager.commit(status);
    }

    // Ignored due to IndexOutOfBoundException
    @Test
    @Ignore
    public void testImportMotherDataFromSampleFile() throws Exception {
        mctsBeneficiaryImportReaderService.importMotherData(read("csv/mother.txt"), SubscriptionOrigin.MCTS_IMPORT);

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        Subscriber subscriber1 = subscriberService.getSubscriber(9439986187L).get(0);
        assertMother(subscriber1, "210302604211400029", getDateTime("22/11/2016"), "Shanti Ekka", expectedState,
                expectedDistrict);

        Subscriber subscriber2 = subscriberService.getSubscriber(7894221701L).get(0);
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
        List<Subscriber> subscriber = subscriberService.getSubscriber(callingNumber);
        assertTrue(subscriber.isEmpty());
    }

    @Test
    @Ignore
    public void testRecordRejectedWhenMctsIdIsEmptyString() throws Exception {
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t  \t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        assertNoSubscriber(9439986187L);
    }
    /*
     * To verify new pregnancyPack subscription is rejected with reason mother abortion via CSV.
     */
    @Test
    public void verifyFT313_1() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with abortion value 'MTP<12 Weeks'
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tMTP<12 Weeks\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertNoSubscriber(9439986187L);
        MotherImportRejection mother = motherRejectionDataService.findRejectedMother("1234567890",null);
        assertEquals(mother.getAccepted(), false);
        assertEquals(mother.getRejectionReason(), RejectionReasons.ABORT_STILLBIRTH_DEATH.toString());
        assertEquals(mother.getMobileNo(), "9439986187");
        transactionManager.commit(status);
    }

    /*
     * To verify pregnancyPack is rejected with reason abortion via CSV.
     */
    @Test
    public void verifyFT313_2() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with abortion value 'Spontaneous'
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tSpontaneous\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertNoSubscriber(9439986187L);
        MotherImportRejection mother = motherRejectionDataService.findRejectedMother("1234567890",null);
        assertEquals(mother.getAccepted(), false);
        assertEquals(mother.getRejectionReason(), RejectionReasons.ABORT_STILLBIRTH_DEATH.toString());
        assertEquals(mother.getMobileNo(), "9439986187");
        transactionManager.commit(status);
    }

    /*
     * To verify pregnancyPack is rejected with reason abortion via CSV.
     * with abortion value 'MTP>12 Weeks'
     */
    @Test
    public void verifyFT313_3() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with abortion value 'MTP>12 Weeks'
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tMTP>12 Weeks\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertNoSubscriber(9439986187L);
        MotherImportRejection mother = motherRejectionDataService.findRejectedMother("1234567890",null);
        assertEquals(mother.getAccepted(), false);
        assertEquals(mother.getRejectionReason(), RejectionReasons.ABORT_STILLBIRTH_DEATH.toString());
        assertEquals(mother.getMobileNo(), "9439986187");
        transactionManager.commit(status);
    }

    /*
     * To verify pregnancyPack is rejected with reason still birth via CSV.
     */
    @Test
    public void verifyFT315() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with Outcome_Nos value '0'
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t0\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        assertNoSubscriber(9439986187L);
        MotherImportRejection mother = motherRejectionDataService.findRejectedMother("1234567890",null);
        assertEquals(mother.getAccepted(), false);
        assertEquals(mother.getRejectionReason(), RejectionReasons.ABORT_STILLBIRTH_DEATH.toString());
        assertEquals(mother.getMobileNo(), "9439986187");
        transactionManager.commit(status);
    }

    /*
     * To verify mother MCTS upload is rejected when stateId is missing
     *
     * https://applab.atlassian.net/browse/NMS-228
     * Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void verifyFT525() throws Exception {
        String dobString = getDateString(DateTime.now().minusDays(30));
        //state id is missing
        Reader reader = createChildDataReader("\t6\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    /*
     * To verify mother MCTS upload is rejected with invalid state id
     * Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void verifyFT526() throws Exception {
        String dobString = getDateString(DateTime.now().minusDays(30));
        //state id with invalid value
        Reader reader = createChildDataReader("31\t6\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    /*
     * To verify child MCTS upload is rejected with invalid district id
     * Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void verifyFT527() throws Exception {
        String dobString = getDateString(DateTime.now().minusDays(30));
        Reader reader = createChildDataReader("21\t6\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        //district id with invalid value
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    /*
     * To verify child MCTS upload is rejected when mandatory parameter district is missing. 
     *
     * https://applab.atlassian.net/browse/NMS-228
     * Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void verifyFT529() throws Exception {
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);
        //district id is missing
        Reader reader = createChildDataReader("21\t\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    /*
     * To verify child MCTS upload is rejected when mandatory parameter state is having invalid value. 
     * Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void verifyFT530() throws Exception {
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);
        //state id with invalid value
        Reader reader = createChildDataReader("31\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    /*
     * To verify child MCTS upload is rejected when mandatory parameter district is having invalid value. 
     * Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void verifyFT531() throws Exception {
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);
        //district id with invalid value
        Reader reader = createChildDataReader("21\t6\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }

    /*
     * To verify new pregnancyPack subscription is rejected with reason mother death via CSV.
     */
    @Test
    public void verifyFT314() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with entry_type value '9'
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t9\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertNoSubscriber(9439986187L);
        MotherImportRejection mother = motherRejectionDataService.findRejectedMother("1234567890",null);
        assertEquals(mother.getAccepted(), false);
        assertEquals(mother.getRejectionReason(), RejectionReasons.ABORT_STILLBIRTH_DEATH.toString());
        assertEquals(mother.getMobileNo(), "9439986187");
        transactionManager.commit(status);
    }

    /*
     * To verify new child pack record is rejected with reason child death via CSV.
     */
    @Test
    @Ignore
    public void verifyFT316() throws Exception {
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);

        //attempt to create child data with entry_type '9'
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t9\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertNoSubscriber(9439986187L);
        ChildImportRejection child = childRejectionDataService.findByIdno("1234567890");
        assertEquals("9439986187", child.getMobileNo());
        assertEquals(child.getAccepted(), false);
        assertEquals(child.getRejectionReason(), RejectionReasons.CHILD_DEATH.toString());
        assertEquals(child.getmCTSMotherIDNo(), "9876453210");
        transactionManager.commit(status);
    }

    /*
     * To verify pregnancy record gets created when child record exist with status as deactivated.
     *
     * https://applab.atlassian.net/browse/NMS-234
     * Ignored due to IndexOutOfBoundException
     */
    @Ignore
    @Test
    public void testCreateMotherSubscriptionWhenDeactivatedChildSubscriptionExists() throws Exception {
        // import child
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscribers = subscriberService.getSubscriber(9439986187L);
        Set<Subscription> subscriptions = subscribers.get(0).getAllSubscriptions();
        assertEquals(1, subscriptions.size());
        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        //Deactivate child subscription
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.CHILD_DEATH);
        subscribers = subscriberService.getSubscriber(9439986187L);
        subscriptions = subscribers.get(0).getAllSubscriptions();
        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.CHILD_DEATH, subscription.getDeactivationReason());
        transactionManager.commit(status);

        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567891\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        //pregnancy record should be activated.
        subscribers = subscriberService.getSubscriber(9439986187L);
        assertEquals(2, subscribers.size());
        for (Subscriber subscriber : subscribers) {
            if (subscriber.getLastMenstrualPeriod() == null) {
                assertEquals(0, subscriber.getActiveAndPendingSubscriptions().size());
            } else {
                assertEquals(1, subscriber.getActiveAndPendingSubscriptions().size());
            }
        }
        assertEquals(2, subscribers.get(0).getAllSubscriptions().size() + subscribers.get(1).getAllSubscriptions().size());
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
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        List<Subscriber> subscriber = subscriberService.getSubscriber(9439986187L);
        assertTrue(subscriber.isEmpty());

        subscriber = subscriberService.getSubscriber(9439986188L);
        assertNotNull(subscriber.get(0));
        assertEquals("Square", subscriber.get(0).getCircle().getName());
    }

    @Test
    public void testMotherDeactivatesIfChildActive() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        // import child
        DateTime dob = DateTime.now().minusDays(5);
        String dobString = getDateString(dob);
        reader = createChildDataReader("21\t3\t\t\t\t\t1234567891\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
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
    @Ignore
    @Test
    public void testChildUpdateWithLastUpdateDate() throws Exception {
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876543210\t9439986187\t"
                + dobString + "\t\t03-10-2016");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        Assert.assertEquals(0, childImportRejections.size());
        transactionManager.commit(status);

        DateTime newdob = DateTime.now().minusDays(150);
        String newdobString = getDateString(newdob);
        reader = createChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876543210\t9439986187\t"
                + newdobString + "\t\t01-10-2016");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        assertNotNull(subscriber);
        // Update DOB should fail
        assertNotEquals(newdob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        Assert.assertEquals("9439986187", childImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.ALREADY_SUBSCRIBED.toString(), childImportRejections.get(0).getRejectionReason());
        transactionManager.commit(status);
    }

    // Fixed issue  "when subscriber purged, it can't be reimported"
    @Test
    public void testForSubscriberAbsent() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscriber = subscriberService.getSubscriber(9439986187L);
        Set<Subscription> subscriptions = subscriber.get(0).getAllSubscriptions();
        transactionManager.commit(status);

        //the mother subscription should be DEACTIVATED
        assertEquals(1, subscriptions.size());

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        for (Subscription subscription:subscriptions
                ) {
            subscription.setStatus(SubscriptionStatus.DEACTIVATED);
            subscription.setEndDate(new DateTime().withDate(2011, 8, 1));
            subscriptionDataService.update(subscription);
        }

        subscriptionService.purgeOldInvalidSubscriptions();
        transactionManager.commit(status);

        subscriber = subscriberService.getSubscriber(9439986187L);
        assertTrue(subscriber.isEmpty());
        List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
        assertEquals(1,mothers.size());

//        import mother again. This time subscriber should not get created due to NullPointerException.
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\n" +
                "21\t3\t\t\t\t\t210302604211400029\tShanti Ekkam\t9439986140\t\t" +
                lmpString + "\t\t\t\t\n" +
                "21\t3\t\t\t\t\t210302604611400025\tSanjukta Bhainsa\t7894221701\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L);
        assertEquals(1, subscriber.size());
        Subscriber subscriber1 = subscriberService.getSubscriber(9439986140L).get(0);
        Subscriber subscriber2 = subscriberService.getSubscriber(7894221701L).get(0);
        assertNotNull(subscriber1);
        assertNotNull(subscriber2);
        transactionManager.commit(status);
    }

    /* Test SubscriberMsisdnTracker in Mother Import
     * Ignored due to NullPointerException
     */
    @Test
    @Ignore
    public void testMotherMsisdnTracker() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        DateTime newLmp = DateTime.now().minusDays(110);
        String newLmpString = getDateString(newLmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                        lmpString + "\t\t\t\t",
                "21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986188\t\t" +
                        newLmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertEquals(newLmp.toLocalDate(), mctsMotherDataService.findByBeneficiaryId("1234567890").getLastMenstrualPeriod().toLocalDate());
        List<Subscriber> subscriber = subscriberService.getSubscriber(9439986187L);
        assertTrue(subscriber.isEmpty());

        subscriber = subscriberService.getSubscriber(9439986188L);
        assertNotNull(subscriber.get(0));

        List<SubscriberMsisdnTracker> msisdnTrackers = subscriberMsisdnTrackerDataService.retrieveAll();
        assertEquals(1, msisdnTrackers.size());
        SubscriberMsisdnTracker msisdnTracker = msisdnTrackers.get(0);
        assertEquals(9439986187L, msisdnTracker.getOldCallingNumber().longValue());
        assertEquals(9439986188L, msisdnTracker.getNewCallingNumber().longValue());
        assertEquals(subscriber.get(0).getMother().getId(), msisdnTracker.getMotherId());
        transactionManager.commit(status);
    }

    // Test SubscriberMsisdnTracker in Child Import
    @Test
    public void testChildMsisdnTracker() throws Exception {

        DateTime dob = DateTime.now().minusDays(100);
        DateTime newDob = DateTime.now().minusDays(90);
        String dobString = getDateString(dob);
        String newDobString = getDateString(newDob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t"
                + dobString + "\t\t",
                "21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t1234567890\t9439986188\t"
                        + newDobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertEquals(newDob.toLocalDate(), mctsChildDataService.findByBeneficiaryId("9876543210").getDateOfBirth().toLocalDate());
        List<Subscriber> subscribers = subscriberService.getSubscriber(9439986187L);
        assertEquals(0, subscribers.size());
        subscribers = subscriberService.getSubscriber(9439986188L);
        assertEquals(1, subscribers.size());
        assertEquals(newDob.toLocalDate(), subscribers.get(0).getDateOfBirth().toLocalDate());

        List<SubscriberMsisdnTracker> msisdnTrackers = subscriberMsisdnTrackerDataService.retrieveAll();
        assertEquals(1, msisdnTrackers.size());
        SubscriberMsisdnTracker msisdnTracker = msisdnTrackers.get(0);
        assertEquals(9439986187L, msisdnTracker.getOldCallingNumber().longValue());
        assertEquals(9439986188L, msisdnTracker.getNewCallingNumber().longValue());
        assertEquals(subscribers.get(0).getMother().getId(), msisdnTracker.getMotherId());
        transactionManager.commit(status);
    }

    // Test SubscriberMsisdnTracker in Child Import without mother
    @Ignore
    @Test
    public void testChildWithoutMotherMsisdnTracker() throws Exception {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // create child subscriber with no  mother attached to it
        Subscriber subscriberMCTS = subscriberDataService.create(new Subscriber(6000000000L));
        DateTime dobChild = DateTime.now().minusWeeks(3);
        subscriberMCTS.setDateOfBirth(dobChild);
        subscriberMCTS.setChild(new MctsChild("9876543210"));
        subscriberMCTS = subscriberDataService.update(subscriberMCTS);
        subscriptionService.createSubscription(subscriberMCTS, subscriberMCTS.getCallingNumber(), rh.kannadaLanguage(), rh.karnatakaCircle(),
                sh.pregnancyPack(), SubscriptionOrigin.MCTS_IMPORT);
        transactionManager.commit(status);

        // import the same child without motherId - It shouldnt fail
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t\t7000000000\t"
                        + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscribers = subscriberService.getSubscriber(6000000000L);
        assertEquals(1, subscribers.size());
        assertEquals(dobChild.toLocalDate(), subscribers.get(0).getDateOfBirth().toLocalDate());
        subscribers = subscriberService.getSubscriber(7000000000L);
        assertEquals(0, subscribers.size());
        List<SubscriberMsisdnTracker> msisdnTrackers = subscriberMsisdnTrackerDataService.retrieveAll();
        assertEquals(0, msisdnTrackers.size());
        transactionManager.commit(status);

        // import the same child with motherId
        reader = createChildDataReader("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t1234567890\t7000000000\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscribers = subscriberService.getSubscriber(6000000000L);
        assertEquals(0, subscribers.size());
        subscribers = subscriberService.getSubscriber(7000000000L);
        assertEquals(1, subscribers.size());
        assertEquals(dob.toLocalDate(), subscribers.get(0).getDateOfBirth().toLocalDate());
        assertNotEquals(dobChild.toLocalDate(), subscribers.get(0).getDateOfBirth().toLocalDate());

        msisdnTrackers = subscriberMsisdnTrackerDataService.retrieveAll();
        assertEquals(1, msisdnTrackers.size());
        SubscriberMsisdnTracker msisdnTracker = msisdnTrackers.get(0);
        assertEquals(6000000000L, msisdnTracker.getOldCallingNumber().longValue());
        assertEquals(7000000000L, msisdnTracker.getNewCallingNumber().longValue());
        assertEquals(subscribers.get(0).getMother().getId(), msisdnTracker.getMotherId());
        transactionManager.commit(status);
    }

    /* Test an existing mother subscriber through IVR gets updated by mcts
     * Ignored due to NullPointerException
     */
    @Test
    @Ignore
    public void testUpdateIVRMother() throws Exception {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriberIVR = subscriberDataService.create(new Subscriber(5000000000L));
        subscriberIVR = subscriberDataService.update(subscriberIVR);
        subscriptionService.createSubscription(subscriberIVR, subscriberIVR.getCallingNumber(), rh.kannadaLanguage(), rh.karnatakaCircle(),
                sh.pregnancyPack(), SubscriptionOrigin.IVR);
        assertNull(subscriberIVR.getMother()); // change this to get subscriber and then mother
        transactionManager.commit(status);

        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t5000000000\t\t" +
                        lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertEquals(lmp.toLocalDate(), mctsMotherDataService.findByBeneficiaryId("1234567890").getLastMenstrualPeriod().toLocalDate());
        List<Subscriber> subscribers = subscriberService.getSubscriber(5000000000L);
        assertEquals(1, subscribers.size());
        assertEquals(lmp.toLocalDate(), subscribers.get(0).getLastMenstrualPeriod().toLocalDate());
        assertNotNull(subscribers.get(0).getMother());
        Set<Subscription> subscriptions = subscribers.get(0).getActiveAndPendingSubscriptions();
        assertEquals(1, subscriptions.size());
        assertEquals(SubscriptionOrigin.MCTS_IMPORT, subscriptions.iterator().next().getOrigin());
        transactionManager.commit(status);
    }

    //Test an existing child subscriber through IVR gets updated by mcts
    @Test
    public void testUpdateIVRChild() throws Exception {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriberIVR = subscriberDataService.create(new Subscriber(5000000000L));
        subscriberIVR = subscriberDataService.update(subscriberIVR);
        Subscription subscription = subscriptionService.createSubscription(subscriberIVR, subscriberIVR.getCallingNumber(), rh.kannadaLanguage(), rh.karnatakaCircle(),
                sh.childPack(), SubscriptionOrigin.IVR);
        assertNull(subscriberIVR.getChild());
        assertNull(subscriberIVR.getMother());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
//        subscription.setStartDate(DateTime.now().minusDays(70));
        transactionManager.commit(status);

        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t1234567890\t5000000000\t"
                        + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertEquals(dob.toLocalDate(), mctsChildDataService.findByBeneficiaryId("9876543210").getDateOfBirth().toLocalDate());
        List<Subscriber> subscribers = subscriberService.getSubscriber(5000000000L);
        assertEquals(1, subscribers.size());
        assertEquals(dob.toLocalDate(), subscribers.get(0).getDateOfBirth().toLocalDate());
        assertNotNull(subscribers.get(0).getMother());
        assertNotNull(subscribers.get(0).getChild());
        Set<Subscription> subscriptions = subscribers.get(0).getActiveAndPendingSubscriptions();
        assertEquals(1, subscriptions.size());
        assertEquals(SubscriptionOrigin.MCTS_IMPORT, subscriptions.iterator().next().getOrigin());
        transactionManager.commit(status);
    }

    // Test Child mcts import when a mother through IVR already exists with same msisdn
    @Test
    public void testImportChildWhenIVRMotherExists() throws  Exception {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriberIVR = subscriberDataService.create(new Subscriber(5000000000L));
        subscriberIVR = subscriberDataService.update(subscriberIVR);
        subscriptionService.createSubscription(subscriberIVR, subscriberIVR.getCallingNumber(), rh.kannadaLanguage(), rh.karnatakaCircle(),
                sh.pregnancyPack(), SubscriptionOrigin.IVR);
        assertNull(subscriberIVR.getMother());
        transactionManager.commit(status);

        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t1234567890\t5000000000\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertEquals(dob.toLocalDate(), mctsChildDataService.findByBeneficiaryId("9876543210").getDateOfBirth().toLocalDate());
        List<Subscriber> subscribers = subscriberService.getSubscriber(5000000000L);
        assertEquals(2, subscribers.size());
        assertEquals(dob.toLocalDate(), subscribers.get(1).getDateOfBirth().toLocalDate());
        assertNotNull(subscribers.get(1).getMother());
        assertNotNull(subscribers.get(1).getChild());

        List<Subscription> subscriptions = subscriptionDataService.retrieveAll();
        assertEquals(2, subscriptions.size());
        assertEquals(subscribers.get(0), subscriptions.get(0).getSubscriber());
        assertEquals(subscribers.get(1), subscriptions.get(1).getSubscriber());
        assertEquals(SubscriptionPackType.PREGNANCY, subscriptions.get(0).getSubscriptionPack().getType());
        assertEquals(SubscriptionPackType.CHILD, subscriptions.get(1).getSubscriptionPack().getType());
        assertEquals(SubscriptionOrigin.IVR, subscriptions.get(0).getOrigin());
        assertEquals(SubscriptionOrigin.MCTS_IMPORT, subscriptions.get(1).getOrigin());
        transactionManager.commit(status);
    }

    /* Test Mother mcts import when a child through IVR already exists with same msisdn
     * Ignored sue to NoSuchElementException
     */
    @Ignore
    @Test
    public void testImportMotherWhenIVRChildExists() throws Exception {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriberIVR = subscriberDataService.create(new Subscriber(5000000000L));
        subscriberIVR = subscriberDataService.update(subscriberIVR);
        subscriptionService.createSubscription(subscriberIVR, subscriberIVR.getCallingNumber(), rh.kannadaLanguage(), rh.karnatakaCircle(),
                sh.childPack(), SubscriptionOrigin.IVR);
        assertNull(subscriberIVR.getChild());
        assertNull(subscriberIVR.getMother());
        transactionManager.commit(status);

        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t5000000000\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertEquals(lmp.toLocalDate(), mctsMotherDataService.findByBeneficiaryId("1234567890").getLastMenstrualPeriod().toLocalDate());
        List<Subscriber> subscribers = subscriberService.getSubscriber(5000000000L);
        assertEquals(2, subscribers.size());
        assertEquals(lmp.toLocalDate(), subscribers.get(1).getLastMenstrualPeriod().toLocalDate());
        assertNotNull(subscribers.get(1).getMother());

        List<Subscription> subscriptions = subscriptionDataService.retrieveAll();
        assertEquals(2, subscriptions.size());
        assertEquals(subscribers.get(0), subscriptions.get(0).getSubscriber());
        assertEquals(subscribers.get(1), subscriptions.get(1).getSubscriber());
        assertEquals(SubscriptionPackType.CHILD, subscriptions.get(0).getSubscriptionPack().getType());
        assertEquals(SubscriptionPackType.PREGNANCY, subscriptions.get(1).getSubscriptionPack().getType());
        assertEquals(SubscriptionOrigin.IVR, subscriptions.get(0).getOrigin());
        assertEquals(SubscriptionOrigin.MCTS_IMPORT, subscriptions.get(1).getOrigin());
        transactionManager.commit(status);
    }

    // Create a mother and try to update msisdn which is blocked
    @Test
    public void testMotherBlockedMsisdnUpdate() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                        lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        List<Subscriber> subscriber = subscriberService.getSubscriber(9439986187L);
        assertNotNull(subscriber.get(0));
        assertEquals(lmp.toLocalDate(), mctsMotherDataService.findByBeneficiaryId("1234567890").getLastMenstrualPeriod().toLocalDate());


        blockedMsisdnRecordDataService.create(new BlockedMsisdnRecord(9439986188L, DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED));
        transactionManager.commit(status);


        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986188\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<SubscriberMsisdnTracker> msisdnTrackers = subscriberMsisdnTrackerDataService.retrieveAll();
        assertEquals(1, msisdnTrackers.size());
        SubscriberMsisdnTracker msisdnTracker = msisdnTrackers.get(0);
        transactionManager.commit(status);
        assertEquals(9439986187L, msisdnTracker.getOldCallingNumber().longValue());
        assertEquals(9439986188L, msisdnTracker.getNewCallingNumber().longValue());
        assertEquals(subscriber.get(0).getMother().getId(), msisdnTracker.getMotherId());
        assertNull(blockedMsisdnRecordDataService.findByNumber(9439986188L));
    }

    /* Create a mother and try to import child of that mother with different msisdn
    * Ignored due to IndexOutOfBoundException
     */
    @Test
    @Ignore
    public void testImportChildWithDiffMsisdnMother() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        reader = createChildDataReader("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t1234567890\t5000000000\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscribers = subscriberService.getSubscriber(9439986187L);
        assertTrue(subscribers.isEmpty());
        subscribers = subscriberService.getSubscriber(5000000000L);
        assertNotNull(subscribers.get(0).getChild());
        assertNotNull(subscribers.get(0).getMother());
        assertEquals(dob.toLocalDate(), mctsChildDataService.findByBeneficiaryId("9876543210").getDateOfBirth().toLocalDate());
        assertEquals(2, subscribers.get(0).getAllSubscriptions().size());
        assertEquals(SubscriptionPackType.CHILD, subscribers.get(0).getActiveAndPendingSubscriptions().iterator().next().getSubscriptionPack().getType());
        List<SubscriberMsisdnTracker> msisdnTrackers = subscriberMsisdnTrackerDataService.retrieveAll();
        assertEquals(1, msisdnTrackers.size());
        transactionManager.commit(status);

        // This import should fail since mother has a different child attached to it
        dob = DateTime.now().minusDays(110);
        dobString = getDateString(dob);
        reader = createChildDataReader("21\t3\t\t\t\t\t8876543210\tBaby1 of Shanti Ekka\t1234567890\t6000000000\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertNotEquals("8876543210", subscribers.get(0).getChild().getBeneficiaryId());
        subscribers = subscriberService.getSubscriber(6000000000L);
        assertTrue(subscribers.isEmpty());
        assertNull(mctsChildDataService.findByBeneficiaryId("8876543210"));
        transactionManager.commit(status);
        List<MotherImportRejection> motherImportRejections = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejections.size());
        assertEquals(RejectionReasons.ALREADY_SUBSCRIBED, motherImportRejections.get(0).getRejectionReason());
        //assertEquals("Another Child exists for the same Mother", errors.get(0).getRejectionMessage());

        // This import should fail since msisdn is already taken by different child
        dob = DateTime.now().minusDays(110);
        dobString = getDateString(dob);
        reader = createChildDataReader("21\t3\t\t\t\t\t8876543210\tBaby1 of Shanti Ekka\t1234567890\t5000000000\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscribers = subscriberService.getSubscriber(5000000000L);
        assertNotEquals("8876543210", subscribers.get(0).getChild().getBeneficiaryId());
        assertNull(mctsChildDataService.findByBeneficiaryId("8876543210"));
        transactionManager.commit(status);
        motherImportRejections = motherRejectionDataService.retrieveAll();
        assertEquals(2, motherImportRejections.size());
        assertEquals(RejectionReasons.MOBILE_NUMBER_ALREADY_IN_USE, motherImportRejections.get(1).getRejectionReason());
        //assertEquals("Msisdn already has an active Subscription", errors.get(1).getRejectionMessage());
    }

    //Ignored due to IndexOutOfBoundException
    @Test
    @Ignore
    public void verifySelfDeactivatedUserIsNotImported() throws Exception {
        DateTime lmp = DateTime.now().minus(100);
        String lmpString = getDateString(lmp);

        // create subscriber and subscription
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        //Mark subscription deactivate
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.DEACTIVATED_BY_USER);
        transactionManager.commit(status);

        //try to import same user
        lmpString = getDateString(lmp.minus(101));
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" + lmpString
                + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        assertEquals(1, subscriber.getAllSubscriptions().size());
        assertEquals(0, subscriber.getActiveAndPendingSubscriptions().size());
        transactionManager.commit(status);
    }

    /* Ignored due to IndexOutOfBoundException
     */
    @Ignore
    @Test
    public void testDummyMotherRecordLocationFields() throws Exception {
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t8876543210\tBaby1 of Shanti Ekka\t1234567890\t6000000000\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        Subscriber subscriber = subscriberService.getSubscriber(6000000000L).get(0);
        assertNotNull(subscriber);
        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);
        assertEquals(expectedState, subscriber.getMother().getState());
        assertEquals(expectedDistrict, subscriber.getMother().getDistrict());
    }

    private void testDeactivationRequestByMsisdn(Long msisdn, String deactivationReason, int status) throws IOException, InterruptedException, URISyntaxException {
        StringBuilder sb = new StringBuilder(deactivationRequest);
        sb.append("?");
        sb.append(String.format("msisdn=%s", msisdn.toString()));
        sb.append("&");
        sb.append(String.format("deactivationReason=%s", deactivationReason));
        HttpDelete httpRequest = new HttpDelete(sb.toString());
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, status, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    //Ignored due to IndexOutOfBoundException
    @Test
    @Ignore
    public void verifyDeactivatedBecauseOfNoWeeklyCallsNumberIsImportedAccordingToProperty() throws Exception {
        DateTime lmp = DateTime.now().minus(100);
        String lmpString = getDateString(lmp);
        Subscription subscription = null;

        // create subscriber and subscription
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        //Mark subscription deactivate
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        testDeactivationRequestByMsisdn(9439986187L, "WEEKLY_CALLS_NOT_ANSWERED", HttpStatus.SC_OK);
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        BlockedMsisdnRecord blockedMsisdnRecord = blockedMsisdnRecordDataService.findByNumber(9439986187L);
        assertNotNull(blockedMsisdnRecord);
        transactionManager.commit(status);

        //try to import another user with same number
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        lmpString = getDateString(lmp.minus(101));
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234512345\tShanti Ekka\t9439986187\t\t" + lmpString
                + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);
        List<Subscriber> subscribers = subscriberService.getSubscriber(9439986187L);

        // when property is set to true in kilkari.properties
        assertNull(blockedMsisdnRecordDataService.findByNumber(9439986187L));
        for (Subscriber sub: subscribers
             ) {
            subscription =  subscriptionService.getActiveSubscription(sub, SubscriptionPackType.PREGNANCY);
        }
        assertEquals(2, subscribers.size());
        if (subscription != null) {
            assertEquals("1234512345", subscription.getSubscriber().getMother().getBeneficiaryId());
        }

        // when property is set to false in kilkari.properties
//        assertNotNull(blockedMsisdnRecordDataService.findByNumber(9439986187L));
//        assertEquals(false, subscriptionService.activeSubscriptionByMsisdn(9439986187L, SubscriptionPackType.PREGNANCY, "1234512345", null));
        transactionManager.commit(status);
    }


    @Test
    public void testMctsImportRejection() throws Exception {
        DateTime dob = DateTime.now().plusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReader("21\t3\t\t\t\t\t8876543210\tBaby1 of Shanti Ekka\t1234567890\t6000000000\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);


        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        assertEquals(1, childImportRejections.size());
        assertEquals(RejectionReasons.INVALID_DOB.toString(), childImportRejections.get(0).getRejectionReason());
    }


    @Test
    public void testRollBack() throws Exception {
        try{
            mctsBeneficiaryImportReaderService.importChildData(read("csv/childForRollBackTest.txt"), SubscriptionOrigin.MCTS_IMPORT);
        } catch (SuperCsvException e) {
            List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
            assertEquals(0, childImportRejections.size());
            List<Subscriber> subscribers = subscriberDataService.retrieveAll();
            assertEquals(0, subscribers.size());
        }

    }

    // Failing due to AssertionError
    @Test
    @Ignore
    public void testReactivationMotherSubscription() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        DateTime dob = DateTime.now().minusDays(10);
        String dobString = getDateString(dob);

        // create subscriber and subscription
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);
        subscriberService.deactivateAllSubscriptionsForSubscriber(9439986187L, DeactivationReason.LOW_LISTENERSHIP);
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);
        List<Subscription> subscriptions = subscriptionDataService.retrieveAll();
        assertEquals(1, subscriptions.size());
        assertEquals(null, subscriptions.get(0).getDeactivationReason());
        assertEquals(SubscriptionStatus.ACTIVE, subscriptions.get(0).getStatus());
        List<ReactivatedBeneficiaryAudit> reactivatedBeneficiaryAudits = reactivatedBeneficiaryAuditDataService.retrieveAll();
        assertEquals(1, reactivatedBeneficiaryAudits.size());
        subscriberService.deactivateAllSubscriptionsForSubscriber(9439986187L, DeactivationReason.LOW_LISTENERSHIP);
        reader = createChildDataReader("21\t3\t\t\t\t\t8876543210\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);
        subscriberService.deactivateAllSubscriptionsForSubscriber(9439986187L, DeactivationReason.LOW_LISTENERSHIP);
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986100\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);
        subscriptions = subscriptionDataService.retrieveAll();
        assertEquals(2, subscriptions.size());
        assertEquals(null, subscriptions.get(0).getDeactivationReason());
        assertEquals(SubscriptionStatus.ACTIVE, subscriptions.get(0).getStatus());
        assertEquals(DeactivationReason.LOW_LISTENERSHIP, subscriptions.get(1).getDeactivationReason());

        reactivatedBeneficiaryAudits = reactivatedBeneficiaryAuditDataService.retrieveAll();
        assertEquals(2, reactivatedBeneficiaryAudits.size());
        transactionManager.commit(status);

    }

    @Test
    @Ignore
    public void testReactivationChildSubscription() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        DateTime dob = DateTime.now().minusDays(10);
        String dobString = getDateString(dob);

        Reader reader = createChildDataReader("21\t3\t\t\t\t\t8876543210\tBaby1 of Shanti Ekka\t1234567890\t6000000000\t"
                + dobString + "\t\t");
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);
        subscriberService.deactivateAllSubscriptionsForSubscriber(6000000000L, DeactivationReason.LOW_LISTENERSHIP);
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        reader = createChildDataReader("21\t3\t\t\t\t\t8876543210\tBaby1 of Shanti Ekka\t1234567890\t7000000000\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);
        List<Subscription> subscriptions = subscriptionDataService.retrieveAll();
        assertEquals(1, subscriptions.size());
        assertEquals(null, subscriptions.get(0).getDeactivationReason());
        assertEquals(SubscriptionStatus.ACTIVE, subscriptions.get(0).getStatus());
        List<ReactivatedBeneficiaryAudit> reactivatedBeneficiaryAudits = reactivatedBeneficiaryAuditDataService.retrieveAll();
        assertEquals(1, reactivatedBeneficiaryAudits.size());
        transactionManager.commit(status);
    }


    @Test
    @Ignore
    public void testReactivationMotherAfterChildDeactivatedSubscription() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        DateTime dob = DateTime.now().minusDays(10);
        String dobString = getDateString(dob);

        // create subscriber and subscription
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);
        subscriberService.deactivateAllSubscriptionsForSubscriber(9439986187L, DeactivationReason.LOW_LISTENERSHIP);
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        reader = createChildDataReader("21\t3\t\t\t\t\t8876543210\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);
        subscriberService.deactivateAllSubscriptionsForSubscriber(9439986187L, DeactivationReason.DEACTIVATED_BY_USER);
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        List<Subscription> subscriptions = subscriptionDataService.retrieveAll();
        assertEquals(2, subscriptions.size());
        for (Subscription sub: subscriptions
             ) {
            if (sub.getSubscriptionPack().getType() == SubscriptionPackType.PREGNANCY) {
                assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
            } else {
                assertEquals(SubscriptionStatus.DEACTIVATED, sub.getStatus());
                assertEquals(DeactivationReason.DEACTIVATED_BY_USER, sub.getDeactivationReason());
            }
        }
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        reader = createChildDataReader("21\t3\t\t\t\t\t8876543210\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);

        subscriptions = subscriptionDataService.retrieveAll();
        assertEquals(2, subscriptions.size());
        for (Subscription sub: subscriptions
                ) {
            if (sub.getSubscriptionPack().getType() == SubscriptionPackType.PREGNANCY) {
                assertEquals(SubscriptionStatus.DEACTIVATED, sub.getStatus());
                assertEquals(DeactivationReason.LIVE_BIRTH, sub.getDeactivationReason());
            } else {
                assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
            }
        }
        transactionManager.commit(status);
    }

    @Test
    public void testEarlySubscription() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Pooja\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals("Shanti Pooja", subscriber.getMother().getName());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(90, Days.daysBetween(lmp.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        assertEquals(subscription.getStatus(), SubscriptionStatus.PENDING_ACTIVATION);
        transactionManager.commit(status);
    }

    @Test
    public void testLmpChangeFromActiveToActive() throws Exception {
        DateTime lmp = DateTime.now().minusDays(90);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals("Shanti Ekka", subscriber.getMother().getName());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(90, Days.daysBetween(lmp.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        assertEquals(subscription.getStatus(), SubscriptionStatus.ACTIVE);
        transactionManager.commit(status);

        DateTime newLmp = DateTime.now().minusDays(150);
        String newLmpString = getDateString(newLmp);
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                newLmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(newLmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(90, Days.daysBetween(newLmp.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        assertEquals(subscription.getStatus(), SubscriptionStatus.ACTIVE);
        transactionManager.commit(status);


    }



    @Test
    public void testSubscriptionAlreadyExistsWithMsisdn() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);

        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tPooja Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567456\tPooja Shanthi\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);
        //import of the second record should fail as a record with the same MSISDN exists through MCTS import
        List<Subscriber> subscribers = subscriberService.getSubscriber(9439986187L);
        assertEquals(1, subscribers.size());
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY,
                SubscriptionRejectionReason.MSISDN_ALREADY_SUBSCRIBED);



    }

    @Ignore
    @Test
    public void testInvalidMsisdn() throws Exception {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // create child subscriber with no  mother attached to it
        Subscriber subscriberMCTS = subscriberDataService.create(new Subscriber(6000000000L));
        DateTime dobChild = DateTime.now().minusWeeks(3);
        subscriberMCTS.setDateOfBirth(dobChild);
        subscriberMCTS.setChild(new MctsChild("9876543210"));
        subscriberMCTS = subscriberDataService.update(subscriberMCTS);
        subscriptionService.createSubscription(subscriberMCTS, subscriberMCTS.getCallingNumber(), rh.kannadaLanguage(), rh.karnatakaCircle(),
                sh.pregnancyPack(), SubscriptionOrigin.MCTS_IMPORT);
        assertSubscriptionError(6000000000L, SubscriptionPackType.CHILD,
                SubscriptionRejectionReason.MISSING_MSISDN);
        transactionManager.commit(status);

    }

    @Test
    @Ignore
    public void testValidMsisdn() throws Exception {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // create child subscriber with no  mother attached to it
        Subscriber subscriberMCTS = subscriberDataService.create(new Subscriber(6000000000L));
        DateTime dobChild = DateTime.now().minusWeeks(3);
        subscriberMCTS.setDateOfBirth(dobChild);
        subscriberMCTS.setChild(new MctsChild("9876543210"));
        subscriberMCTS = subscriberDataService.update(subscriberMCTS);
        subscriptionService.createSubscription(subscriberMCTS, subscriberMCTS.getCallingNumber(), rh.kannadaLanguage(), rh.karnatakaCircle(),
                sh.pregnancyPack(), SubscriptionOrigin.MCTS_IMPORT);
        transactionManager.commit(status);

        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);

        Reader reader = createChildDataReader("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t1234567890\t7000000000\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);
        List<Subscriber> subscribers = subscriberService.getSubscriber(6000000000L);
        assertEquals(1, subscribers.size());
        assertEquals(dobChild.toLocalDate(), subscribers.get(0).getDateOfBirth().toLocalDate());
        subscribers = subscriberService.getSubscriber(7000000000L);
        assertEquals(0, subscribers.size());
        List<SubscriberMsisdnTracker> msisdnTrackers = subscriberMsisdnTrackerDataService.retrieveAll();
        assertEquals(0, msisdnTrackers.size());
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscribers = subscriberService.getSubscriber(6000000000L);
        assertEquals(0, subscribers.size());
        subscribers = subscriberService.getSubscriber(7000000000L);
        assertEquals(1, subscribers.size());
        assertEquals(dob.toLocalDate(), subscribers.get(0).getDateOfBirth().toLocalDate());
        assertNotEquals(dobChild.toLocalDate(), subscribers.get(0).getDateOfBirth().toLocalDate());

        msisdnTrackers = subscriberMsisdnTrackerDataService.retrieveAll();
        assertEquals(1, msisdnTrackers.size());
        SubscriberMsisdnTracker msisdnTracker = msisdnTrackers.get(0);
        assertEquals(6000000000L, msisdnTracker.getOldCallingNumber().longValue());
        assertEquals(7000000000L, msisdnTracker.getNewCallingNumber().longValue());
        assertEquals(subscribers.get(0).getMother().getId(), msisdnTracker.getMotherId());
        transactionManager.commit(status);

    }

    @Ignore
    @Test
    public void testDeactivationMotherSubscriptionLowListenership() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        DateTime dob = DateTime.now().minusDays(10);
        String dobString = getDateString(dob);

        // create subscriber and subscription
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);
        subscriberService.deactivateAllSubscriptionsForSubscriber(9439986187L, DeactivationReason.LOW_LISTENERSHIP);
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);
        List<Subscription> subscriptions = subscriptionDataService.retrieveAll();
        assertEquals(1, subscriptions.size());
        assertEquals(null, subscriptions.get(0).getDeactivationReason());
        assertEquals(SubscriptionStatus.ACTIVE, subscriptions.get(0).getStatus());
        List<ReactivatedBeneficiaryAudit> reactivatedBeneficiaryAudits = reactivatedBeneficiaryAuditDataService.retrieveAll();
        assertEquals(1, reactivatedBeneficiaryAudits.size());
        subscriberService.deactivateAllSubscriptionsForSubscriber(9439986187L, DeactivationReason.LOW_LISTENERSHIP);
        reader = createChildDataReader("21\t3\t\t\t\t\t8876543210\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);
        subscriberService.deactivateAllSubscriptionsForSubscriber(9439986187L, DeactivationReason.LOW_LISTENERSHIP);
        reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986100\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);
        subscriptions = subscriptionDataService.retrieveAll();
        assertEquals(2, subscriptions.size());
        assertEquals(null, subscriptions.get(0).getDeactivationReason());
        assertEquals(SubscriptionStatus.ACTIVE, subscriptions.get(0).getStatus());
        assertEquals(DeactivationReason.LOW_LISTENERSHIP, subscriptions.get(1).getDeactivationReason());
    }

    @Ignore
    @Test
    public void testDeactivationWeeklyCallsNotAnswered() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        // create subscriber and subscription
        Reader reader = createMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        //deactivating subscriber
        subscriberService.deactivateAllSubscriptionsForSubscriber(9439986187L, DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED);
        transactionManager.commit(status);
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());


        List<Subscription> subscriptions = subscriptionDataService.retrieveAll();
        assertEquals(1, subscriptions.size());
        assertEquals(DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED, subscriptions.get(0).getDeactivationReason());

        List<ReactivatedBeneficiaryAudit> reactivatedBeneficiaryAudits = reactivatedBeneficiaryAuditDataService.retrieveAll();
        assertEquals(1, reactivatedBeneficiaryAudits.size());
        //subscriberService.deactivateAllSubscriptionsForSubscriber(9439986187L, DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED);

        //add child to deactivated mother
        DateTime dob = DateTime.now().minusDays(10);
        String dobString = getDateString(dob);
        reader = createChildDataReader("21\t3\t\t\t\t\t8876543210\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t"
                + dobString + "\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.MCTS_IMPORT);
        subscriberService.deactivateAllSubscriptionsForSubscriber(9439986187L, DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED);
        subscriptions = subscriptionDataService.retrieveAll();
        //assertEquals(DeactivationReason.LOW_LISTENERSHIP, subscriptions.get(1).getDeactivationReason());

    }






}