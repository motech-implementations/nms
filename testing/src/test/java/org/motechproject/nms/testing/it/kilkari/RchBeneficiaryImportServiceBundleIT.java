package org.motechproject.nms.testing.it.kilkari;

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
import org.motechproject.nms.kilkari.domain.BlockedMsisdnRecord;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.RejectionReasons;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.SubscriberMsisdnTracker;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionError;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionRejectionReason;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.repository.BlockedMsisdnRecordDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.repository.SubscriberMsisdnTrackerDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
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

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNotEquals;
import static org.motechproject.nms.testing.it.utils.RegionHelper.*;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createHealthSubFacility;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createVillage;


@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class RchBeneficiaryImportServiceBundleIT extends BasePaxIT {
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
    MotherRejectionDataService motherRejectionDataService;
    @Inject
    ChildRejectionDataService childRejectionDataService;
    @Inject
    SubscriberMsisdnTrackerDataService subscriberMsisdnTrackerDataService;
    @Inject
    BlockedMsisdnRecordDataService blockedMsisdnRecordDataService;

    @Inject
    PlatformTransactionManager transactionManager;

    SubscriptionHelper sh;
    RegionHelper rh;

    @Before
    public void setUp() {
        testingService.clearDatabase();
        createLocationData();

        sh = new SubscriptionHelper(subscriptionService, subscriberDataService, subscriptionPackDataService,
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
        //taluka24.addHealthBlock(healthBlock259);

        HealthBlock healthBlock453 = createHealthBlock(taluka26, 453L, "Bamara", "hq");
        //taluka26.addHealthBlock(healthBlock453);

        HealthBlock healthBlock153 = createHealthBlock(taluka46, 153L, "Tileibani", "hq");
        //taluka46.addHealthBlock(healthBlock153);

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
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        Assert.assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        Assert.assertEquals("Shanti Ekka", subscriber.getMother().getName());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        Assert.assertEquals(SubscriptionOrigin.RCH_IMPORT, subscription.getOrigin());
        assertNotNull(subscriber.getCircle());
        Assert.assertEquals("Square", subscriber.getCircle().getName());
        Assert.assertEquals("240", subscriber.getMother().getRchId());
        Assert.assertEquals(8L, (long) subscriber.getMother().getMaxCaseNo());
        transactionManager.commit(status);
    }

    @Test
    public void testImportMotherAlternateDateFormat() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = lmp.toString("dd/MM/yyyy");
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        Assert.assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
    }

    @Test
    public void testImportMotherWhoAlreadyExistsUpdateLmp() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        Assert.assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        Assert.assertEquals("Shanti Ekka", subscriber.getMother().getName());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        Assert.assertEquals(90, Days.daysBetween(lmp.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        transactionManager.commit(status);

        DateTime newLmp = DateTime.now().minusDays(150);
        String newLmpString = getDateString(newLmp);
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                newLmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        Assert.assertEquals(newLmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        Assert.assertEquals(90, Days.daysBetween(newLmp.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        transactionManager.commit(status);
    }

    @Test
    public void testMotherUpdateWithLastUpdateDate() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t03-10-2016\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        Assert.assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        Assert.assertEquals("Shanti Ekka", subscriber.getMother().getName());
        List<MotherImportRejection> motherImportRejections = motherRejectionDataService.retrieveAll();
        Assert.assertEquals(0, motherImportRejections.size());
        transactionManager.commit(status);

        DateTime newLmp = DateTime.now().minusDays(150);
        String newLmpString = getDateString(newLmp);
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                newLmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        transactionManager.commit(status);

        // Lmp update should fail
        assertNotEquals(newLmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        Assert.assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        motherImportRejections = motherRejectionDataService.retrieveAll();
        Assert.assertEquals(1, motherImportRejections.size());
        Assert.assertEquals("9439986187", motherImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.UPDATED_RECORD_ALREADY_EXISTS.toString(), motherImportRejections.get(0).getRejectionReason());
    }

    /* Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void testImportMotherInvalidState() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("9\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" + lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);
        List<SubscriptionError> se = subscriptionErrorDataService.findByContactNumber(9439986187L);
        Assert.assertEquals(1, se.size());
        Assert.assertEquals(SubscriptionRejectionReason.INVALID_LOCATION, se.get(0).getRejectionReason());
    }

    /*Ignored the test case due to IndexOutOfBoundException
     */
    @Ignore
    @Test
    public void testImportMotherDataFromSampleFile() throws Exception {
        mctsBeneficiaryImportReaderService.importMotherData(read("csv/rch_mother.txt"), SubscriptionOrigin.RCH_IMPORT);

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        Subscriber subscriber1 = subscriberDataService.findByNumber(9439986187L).get(0);
        assertMother(subscriber1, "310302604211400000", getDateTime("22/11/2016"), "Shanti Ekka", expectedState,
                expectedDistrict);

        Assert.assertEquals(1, subscriberDataService.count());

        // verify location data was created on the fly
        State state = stateDataService.findByCode(21L);
        District district = districtService.findByStateAndCode(state, 3L);
        Taluka taluka = talukaDataService.findByDistrictAndCode(district, "26");
        Assert.assertEquals("Govindpur P.S.", taluka.getName());

        HealthBlock healthBlock = healthBlockService.findByTalukaAndCode(taluka, 453L);
        Assert.assertEquals("Bamara", healthBlock.getName());

        HealthFacility healthFacility = healthFacilityService.findByHealthBlockAndCode(healthBlock, 41L);
        Assert.assertEquals("Garposh CHC", healthFacility.getName());

        HealthSubFacility healthSubFacility = healthSubFacilityService.findByHealthFacilityAndCode(healthFacility, 7389L);
        Assert.assertEquals("Babuniktimal", healthSubFacility.getName());

        Village village = villageService.findByTalukaAndVcodeAndSvid(taluka, 555L, 0L);
        Assert.assertEquals("Village", village.getName());

    }

    /*
     * To verify mother subscription is rejected when future LMP is provided
     */
    @Test
    public void verifyFT282() throws Exception {
        DateTime lmp = DateTime.now().plusDays(1);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_mother_rejects with reason 'INVALID_LMP_DATE'.
        List<MotherImportRejection> motherImportRejections = motherRejectionDataService.retrieveAll();
        Assert.assertEquals(1, motherImportRejections.size());
        assertNoSubscriber(9439986187L);
        Assert.assertEquals("9439986187", motherImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.INVALID_LMP_DATE.toString(), motherImportRejections.get(0).getRejectionReason());
        Assert.assertEquals("240", motherImportRejections.get(0).getRegistrationNo());
    }

    /*
     * To verify mother subscription is rejected when LMP provided is 72 weeks back.
     */
    @Test
    public void verifyFT284() throws Exception {
        DateTime lmp = DateTime.now().minusDays(7 * 72 + 90);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_mother_rejects with reason 'INVALID_LMP_DATE'.
        List<MotherImportRejection> motherImportRejections = motherRejectionDataService.retrieveAll();
        Assert.assertEquals(1, motherImportRejections.size());
        assertNoSubscriber(9439986187L);
        Assert.assertEquals("9439986187", motherImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.INVALID_LMP_DATE.toString(), motherImportRejections.get(0).getRejectionReason());
        Assert.assertEquals("240", motherImportRejections.get(0).getRegistrationNo());
    }

    /**
     * To verify RCH upload is rejected when RCH doesn’t contain LMP.
     */
    @Test
    public void verifyFT288_2() throws Exception {

        //LMP is missing
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_mother_rejects with reason 'MISSING_LMP'.
        List<MotherImportRejection> motherImportRejections = motherRejectionDataService.retrieveAll();
        Assert.assertEquals(1, motherImportRejections.size());
        assertNoSubscriber(9439986187L);
        Assert.assertEquals("9439986187", motherImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.INVALID_LMP_DATE.toString(), motherImportRejections.get(0).getRejectionReason());
        Assert.assertEquals("240", motherImportRejections.get(0).getRegistrationNo());
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
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        //Make subscription completed
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        subscriber.setLastMenstrualPeriod(lmp.minusDays(650));
        subscriberService.updateStartDate(subscriber);

        //create a new subscription for subscriber whose subscription is completed.
        lmpString = getDateString(lmp.minus(200));
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        Assert.assertEquals(2, subscriber.getAllSubscriptions().size());
        Assert.assertEquals(1, subscriber.getActiveAndPendingSubscriptions().size());
        Assert.assertEquals(lmpString, getDateString(subscriber.getLastMenstrualPeriod()));
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
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        //Mark subscription deactivate
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.MISCARRIAGE_OR_ABORTION);
        transactionManager.commit(status);

        //create a new subscription for subscriber whose subscription is deactivated.
        lmpString = getDateString(lmp.minus(200));
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        Assert.assertEquals(1, subscriber.getAllSubscriptions().size());
        Assert.assertEquals(0, subscriber.getActiveAndPendingSubscriptions().size());
        Assert.assertEquals(lmpString, getDateString(subscriber.getLastMenstrualPeriod()));
        transactionManager.commit(status);
    }

    /*
     * To verify LMP is changed successfully via CSV when subscription
     * already exist for pregnancyPack having status as "Pending Activation"
     */
    @Test
    public void verifyFT308() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        Assert.assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        Assert.assertEquals("Shanti Ekka", subscriber.getMother().getName());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        Assert.assertEquals(90, Days.daysBetween(lmp.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        Assert.assertEquals(subscription.getStatus(), SubscriptionStatus.PENDING_ACTIVATION);
        transactionManager.commit(status);

        DateTime newLmp = DateTime.now().minusDays(90);
        String newLmpString = getDateString(newLmp);
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                newLmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        Assert.assertEquals(newLmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        Assert.assertEquals(90, Days.daysBetween(newLmp.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        Assert.assertEquals(subscription.getStatus(), SubscriptionStatus.ACTIVE);
        transactionManager.commit(status);
    }

    /*
     * To verify pregnancyPack is rejected with reason abortion via CSV.
     * checked with abortion value 'MTP<12 Weeks'
     */
    @Test
    public void verifyFT313_1() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with abortion value 'MTP<12 Weeks'
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tMTP<12 Weeks\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertNoSubscriber(9439986187L);
        MotherImportRejection mother = motherRejectionDataService.findRejectedMother("1234567890","240");
        Assert.assertEquals(mother.getAccepted(), false);
        Assert.assertEquals(mother.getRejectionReason(), RejectionReasons.ABORT_STILLBIRTH_DEATH.toString());
        Assert.assertEquals(mother.getMobileNo(), "9439986187");
        transactionManager.commit(status);
    }

    /*
     * To verify pregnancyPack is rejected with reason abortion via CSV.
     * checked with abortion value 'Spontaneous'
     */
    @Test
    public void verifyFT313_2() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with abortion value 'Spontaneous'
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tSpontaneous\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertNoSubscriber(9439986187L);
        MotherImportRejection mother = motherRejectionDataService.findRejectedMother("1234567890","240");
        Assert.assertEquals(mother.getAccepted(), false);
        Assert.assertEquals(mother.getRejectionReason(), RejectionReasons.ABORT_STILLBIRTH_DEATH.toString());
        Assert.assertEquals(mother.getMobileNo(), "9439986187");
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
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tMTP>12 Weeks\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertNoSubscriber(9439986187L);
        MotherImportRejection mother = motherRejectionDataService.findRejectedMother("1234567890","240");
        Assert.assertEquals(mother.getAccepted(), false);
        Assert.assertEquals(mother.getRejectionReason(), RejectionReasons.ABORT_STILLBIRTH_DEATH.toString());
        Assert.assertEquals(mother.getMobileNo(), "9439986187");
        transactionManager.commit(status);
    }

    @Test
    public void testDeactivateMotherSubscriptionDueToAbortion() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        Assert.assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        Assert.assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        assertMother(subscriber, "240", getDateTime(lmpString), "Shanti Ekka", expectedState, expectedDistrict);
        transactionManager.commit(status);

        // import record for same mother with Abortion_Type set to "Spontaneous" -- her subscription should be deactivated
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tSpontaneous\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        subscriptions = subscriber.getAllSubscriptions();
        Assert.assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        Assert.assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        Assert.assertEquals(DeactivationReason.MISCARRIAGE_OR_ABORTION, subscription.getDeactivationReason());
        transactionManager.commit(status);
    }

    /*
     * To verify pregnancyPack is rejected with reason mother death via CSV.
     */
    @Test
    public void verifyFT314() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with entry_type value '9'
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t9\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertNoSubscriber(9439986187L);
        MotherImportRejection mother = motherRejectionDataService.findRejectedMother("1234567890","240");
        Assert.assertEquals(mother.getAccepted(), false);
        Assert.assertEquals(mother.getRejectionReason(), RejectionReasons.ABORT_STILLBIRTH_DEATH.toString());
        Assert.assertEquals(mother.getMobileNo(), "9439986187");
        transactionManager.commit(status);
    }

    @Test
    public void testDeactivateMotherSubscriptionDueToDeath() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        Assert.assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        Assert.assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        assertMother(subscriber, "240", getDateTime(lmpString), "Shanti Ekka", expectedState, expectedDistrict);
        transactionManager.commit(status);

        // import record for same mother with Entry_Type set to 9 -- her subscription should be deactivated
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t9\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        subscriptions = subscriber.getAllSubscriptions();
        Assert.assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        Assert.assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        Assert.assertEquals(DeactivationReason.MATERNAL_DEATH, subscription.getDeactivationReason());
        transactionManager.commit(status);
    }

    /*
     * To verify pregnancyPack is rejected with reason still birth via CSV.
     */
    @Test
    public void verifyFT315() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with delivery_outcomes value '0'
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t0\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        assertNoSubscriber(9439986187L);
        MotherImportRejection mother = motherRejectionDataService.findRejectedMother("1234567890","240");
        Assert.assertEquals(mother.getAccepted(), false);
        Assert.assertEquals(mother.getRejectionReason(), RejectionReasons.ABORT_STILLBIRTH_DEATH.toString());
        Assert.assertEquals(mother.getMobileNo(), "9439986187");
        transactionManager.commit(status);
    }

    @Test
    public void testDeactivateMotherSubscriptionDueToStillbirth() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        Assert.assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        Assert.assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        assertMother(subscriber, "240", getDateTime(lmpString), "Shanti Ekka", expectedState, expectedDistrict);
        transactionManager.commit(status);

        // import record for same mother with Delivery_Outcomes set to 0 -- her subscription should be deactivated
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t0\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        subscriptions = subscriber.getAllSubscriptions();
        Assert.assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        Assert.assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        Assert.assertEquals(DeactivationReason.STILL_BIRTH, subscription.getDeactivationReason());
        transactionManager.commit(status);
    }

    /*
     * Verify correct circle when inserting mothers in state with two districts with each district a different circle
     *
     */
    @Test
    public void testImportIntoStateWithTwoCircles() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                        lmpString + "\t\t\t\t\t8",
                "21\t5\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986188\t\t" +
                        lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        List<Subscriber> subscriber = subscriberDataService.findByNumber(9439986187L);
        assertTrue(subscriber.isEmpty());

        subscriber = subscriberDataService.findByNumber(9439986188L);
        assertNotNull(subscriber.get(0));
        Assert.assertEquals("Square", subscriber.get(0).getCircle().getName());
    }

    // when subscriber purged, the next time it is imported case no. should be incremented accordingly
    @Test
    public void testForSubscriberAbsent() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscriber = subscriberDataService.findByNumber(9439986187L);
        Set<Subscription> subscriptions = subscriber.get(0).getAllSubscriptions();

        //the mother subscription should be DEACTIVATED
        Assert.assertEquals(1, subscriptions.size());
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        for (Subscription subscription : subscriptions
                ) {
            subscription.setStatus(SubscriptionStatus.DEACTIVATED);
            subscription.setEndDate(new DateTime().withDate(2011, 8, 1));
            subscriptionDataService.update(subscription);
        }

        subscriptionService.purgeOldInvalidSubscriptions();
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        assertTrue(subscriber.isEmpty());
        List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
        Assert.assertEquals(1, mothers.size());
        transactionManager.commit(status);

//        import mother again. This time subscriber should be created with case no incremented.
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t10\n");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
        Assert.assertEquals(1, subscriber.size());
        Assert.assertEquals(10L, (long)subscriber.get(0).getCaseNo());
        transactionManager.commit(status);
    }

    // Test SubscriberMsisdnTracker in Mother Import
    @Test
    public void testMotherMsisdnTracker() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        DateTime newLmp = DateTime.now().minusDays(110);
        String newLmpString = getDateString(newLmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                        lmpString + "\t\t\t\t\t8",
                "21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986188\t\t" +
                        newLmpString + "\t\t\t\t\t8\n");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Assert.assertEquals(newLmp.toLocalDate(), mctsMotherDataService.findByBeneficiaryId("1234567890").getLastMenstrualPeriod().toLocalDate());
        List<Subscriber> subscriber = subscriberDataService.findByNumber(9439986187L);
        assertTrue(subscriber.isEmpty());

        subscriber = subscriberDataService.findByNumber(9439986188L);
        assertNotNull(subscriber.get(0));

        List<SubscriberMsisdnTracker> msisdnTrackers = subscriberMsisdnTrackerDataService.retrieveAll();
        Assert.assertEquals(1, msisdnTrackers.size());
        SubscriberMsisdnTracker msisdnTracker = msisdnTrackers.get(0);
        Assert.assertEquals(9439986187L, msisdnTracker.getOldCallingNumber().longValue());
        Assert.assertEquals(9439986188L, msisdnTracker.getNewCallingNumber().longValue());
        Assert.assertEquals(subscriber.get(0).getMother().getId(), msisdnTracker.getMotherId());
        transactionManager.commit(status);
    }

    //Test an existing mother subscriber through IVR doesn't get updated by RCH
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
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t5000000000\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Assert.assertNull(mctsMotherDataService.findByBeneficiaryId("1234567890"));
        List<Subscriber> subscribers = subscriberDataService.findByNumber(5000000000L);
        Assert.assertEquals(1, subscribers.size());
        assertNull(subscribers.get(0).getMother());
        Set<Subscription> subscriptions = subscribers.get(0).getActiveAndPendingSubscriptions();
        Assert.assertEquals(1, subscriptions.size());
        Assert.assertEquals(SubscriptionOrigin.IVR, subscriptions.iterator().next().getOrigin());
        transactionManager.commit(status);
    }

    // Test Mother RCH import when a child through IVR already exists with same msisdn
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
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t5000000000\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Assert.assertNull(mctsMotherDataService.findByBeneficiaryId("1234567890")); //mother data is not imported as child is already present in database with the same MSISDN
        List<Subscriber> subscribers = subscriberDataService.findByNumber(5000000000L);
        Assert.assertEquals(1, subscribers.size());


        List<Subscription> subscriptions = subscriptionDataService.retrieveAll();
        Assert.assertEquals(1, subscriptions.size());
        Assert.assertEquals(subscribers.get(0), subscriptions.get(0).getSubscriber());
        Assert.assertEquals(SubscriptionPackType.CHILD, subscriptions.get(0).getSubscriptionPack().getType());
        Assert.assertEquals(SubscriptionOrigin.IVR, subscriptions.get(0).getOrigin());
        transactionManager.commit(status);
    }

    // Create a mother and try to update msisdn which is blocked
    @Test
    public void testMotherBlockedMsisdnUpdate() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        List<Subscriber> subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber.get(0));
        Assert.assertEquals(lmp.toLocalDate(), mctsMotherDataService.findByBeneficiaryId("1234567890").getLastMenstrualPeriod().toLocalDate());


        blockedMsisdnRecordDataService.create(new BlockedMsisdnRecord(9439986188L, DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED));
        transactionManager.commit(status);


        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986188\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<SubscriberMsisdnTracker> msisdnTrackers = subscriberMsisdnTrackerDataService.retrieveAll();
        Assert.assertEquals(1, msisdnTrackers.size());
        SubscriberMsisdnTracker msisdnTracker = msisdnTrackers.get(0);
        Assert.assertEquals(9439986187L, msisdnTracker.getOldCallingNumber().longValue());
        Assert.assertEquals(9439986188L, msisdnTracker.getNewCallingNumber().longValue());
        Assert.assertEquals(subscriber.get(0).getMother().getId(), msisdnTracker.getMotherId());
        assertNull(blockedMsisdnRecordDataService.findByNumber(9439986188L));
        transactionManager.commit(status);
    }

    @Test
    public void testMotherImportWithValidCaseNo() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t2234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t3");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals("Shanti Ekka", subscriber.getMother().getName());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(SubscriptionOrigin.RCH_IMPORT, subscription.getOrigin());
        assertNotNull(subscriber.getCircle());
        assertEquals("Square", subscriber.getCircle().getName());
        assertEquals(3L, subscriber.getCaseNo().longValue());
        assertEquals(3L, subscriber.getMother().getMaxCaseNo().longValue());
        transactionManager.commit(status);
    }

    @Test
    public void testMotherImportWithNoCaseno() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t2234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscribers = subscriberDataService.findByNumber(9439986187L);
        assertTrue(subscribers.isEmpty());
        List<MotherImportRejection> motherImportRejections = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejections.size());
        assertNoSubscriber(9439986187L);
        Assert.assertEquals("9439986187", motherImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.INVALID_CASE_NO.toString(), motherImportRejections.get(0).getRejectionReason());
        assertEquals("2234567890", motherImportRejections.get(0).getRegistrationNo());
        transactionManager.commit(status);
    }

    @Test
    public void testMotherUpdateWithValidCaseNo() throws Exception {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // create mother with maxCaseNo 3
        MctsMother mother= new MctsMother("2234567890","1234567890");
        mother.setMaxCaseNo(3L);
        mctsMotherDataService.create(mother);
        Subscriber subscriber = new Subscriber(9439986187L);
        subscriber.setCaseNo(3L);
        subscriber.setMother(mother);
        subscriberDataService.create(subscriber);
        transactionManager.commit(status);

        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t2234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t3");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals(3L, subscriber.getCaseNo().longValue());
        assertEquals(3L, subscriber.getMother().getMaxCaseNo().longValue());
        transactionManager.commit(status);
    }

    @Test
    public void testMotherImportWithInvalidCaseNo1() throws Exception {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // create mother with maxCaseNo 3
        MctsMother mother= new MctsMother("2234567890","1234567890");
        mother.setMaxCaseNo(3L);
        mctsMotherDataService.create(mother);
        transactionManager.commit(status);

        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t2234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t2");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscribers = subscriberDataService.findByNumber(9439986187L);
        assertTrue(subscribers.isEmpty());
        List<MotherImportRejection> motherImportRejections = motherRejectionDataService.retrieveAll();
        Assert.assertEquals(1, motherImportRejections.size());
        assertNoSubscriber(9439986187L);
        Assert.assertEquals("9439986187", motherImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.INVALID_CASE_NO.toString(), motherImportRejections.get(0).getRejectionReason());
        assertEquals("2234567890", motherImportRejections.get(0).getRegistrationNo());
        transactionManager.commit(status);
    }

    @Test
    public void testMotherUpdateWithInvalidCaseNo2() throws Exception {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // create mother with maxCaseNo 3
        MctsMother mother= new MctsMother("2234567890","1234567890");
        mother.setMaxCaseNo(3L);
        mctsMotherDataService.create(mother);
        Subscriber subscriber = new Subscriber(9439986187L);
        subscriber.setCaseNo(3L);
        subscriber.setMother(mother);
        subscriberDataService.create(subscriber);
        transactionManager.commit(status);

        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t2234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t4");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(4L, subscriber.getCaseNo().longValue());
        assertEquals(4L, subscriber.getMother().getMaxCaseNo().longValue());

        List<MotherImportRejection> motherImportRejections = motherRejectionDataService.retrieveAll();
        Assert.assertEquals(0, motherImportRejections.size());
        transactionManager.commit(status);
    }

    // Test mctsmother update with RchId when both the id's are provided
    @Test
    public void testMctsMotherUpdateWithRchId() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMctsMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        transactionManager.commit(status);

        lmp = DateTime.now().minusDays(120);
        lmpString = getDateString(lmp);
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t2234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t4");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        MctsMother mother = mctsMotherDataService.findByBeneficiaryId("1234567890");
        assertEquals("2234567890", mother.getRchId());
        assertEquals(mother.getId(),subscriber.getMother().getId());
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        transactionManager.commit(status);
    }

    /* Test if rch mother is updated with mctsId when both the id's are provided
    * Ignored due to IndexOutOfBoundException
    */
    @Ignore
    @Test
    public void testRchMotherUpdateWithMctsId() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t\t2234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t4");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals("2234567890", subscriber.getMother().getRchId());
        assertNull(subscriber.getMother().getBeneficiaryId());
        transactionManager.commit(status);

        lmp = DateTime.now().minusDays(120);
        lmpString = getDateString(lmp);
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t2234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t4");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        MctsMother mother = mctsMotherDataService.findByBeneficiaryId("1234567890");
        assertEquals("2234567890", mother.getRchId());
        assertEquals("1234567890", mother.getBeneficiaryId());
        assertEquals(mother.getId(),subscriber.getMother().getId());
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        transactionManager.commit(status);
    }

    // Import record with MctsId and update it with just RchId. It should be rejected as mctsId is not provided
    @Test
    @Ignore
    public void testMctsMotherImportWithRchId() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMctsMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        transactionManager.commit(status);

        DateTime lmpNew = DateTime.now().minusDays(120);
        lmpString = getDateString(lmpNew);
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t\t2234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t4");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        transactionManager.commit(status);
        assertEquals("1234567890", subscriber.getMother().getBeneficiaryId());
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());

        List<MotherImportRejection> motherImportRejections = motherRejectionDataService.retrieveAll();
        Assert.assertEquals(1, motherImportRejections.size());
        Assert.assertEquals("9439986187", motherImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.MOBILE_NUMBER_ALREADY_SUBSCRIBED.toString(), motherImportRejections.get(0).getRejectionReason());
        assertEquals("2234567890", motherImportRejections.get(0).getRegistrationNo());
    }

    // Import records with MctsId and RchId (M1,R1), (M2,R2) and then try to import record (M1,R2). It should fail with InvalidRegistrationIdException
    @Test
    public void testInvalidRegistrationIdException() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t2234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t4\n" +
                "21\t3\t\t\t\t\t\t1234567891\t2234567891\tShanti Ekka\t9439986188\t\t" +
                lmpString + "\t\t\t\t\t4\n" +
                "21\t3\t\t\t\t\t\t1234567890\t2234567891\tShanti Ekka\t9439986189\t\t" +
                lmpString + "\t\t\t\t\t4\n");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals("1234567890", subscriber.getMother().getBeneficiaryId());
        assertEquals("2234567890", subscriber.getMother().getRchId());

        subscriber = subscriberDataService.findByNumber(9439986188L).get(0);
        assertNotNull(subscriber);
        assertEquals("1234567891", subscriber.getMother().getBeneficiaryId());
        assertEquals("2234567891", subscriber.getMother().getRchId());

        List<Subscriber> subscribers = subscriberDataService.findByNumber(9439986189L);
        assertTrue(subscribers.isEmpty());
        MctsMother mother = mctsMotherDataService.findByRchId("2234567891");
        assertNotEquals("1234567890", mother.getBeneficiaryId());
        mother = mctsMotherDataService.findByBeneficiaryId("1234567890");
        assertNotEquals("2234567891", mother.getRchId());
        transactionManager.commit(status);
    }

    @Test
    public void testChangeExistingMotherMsisdn() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader rchReader = createRchMotherDataReader("21\t3\t\t\t\t\t\t200100201311500052\t121004563168\tChumuki Sahoo\t8658577903\t\t" +
                lmpString + "\t\t\t\t\t4");
        mctsBeneficiaryImportReaderService.importMotherData(rchReader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);
        Subscriber subscriber = subscriberDataService.findByNumber(8658577903L).get(0);
        assertNotNull(subscriber);
        assertMother(subscriber, "121004563168", lmp, "Chumuki Sahoo", expectedState, expectedDistrict);
        transactionManager.commit(status);

        //update msisdn for the mother
        rchReader = createRchMotherDataReader("21\t3\t\t\t\t\t\t200100201311500052\t121004563168\tChumuki Sahoo\t8658577904\t\t" +
                lmpString + "\t\t\t\t\t4");
        mctsBeneficiaryImportReaderService.importMotherData(rchReader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(8658577904L).get(0);
        assertNotNull(subscriber);
        assertMother(subscriber, "121004563168", lmp, "Chumuki Sahoo", expectedState, expectedDistrict);
        transactionManager.commit(status);
    }
    //Import first mother record through MCTS, purge the existing record and try to import a second mother record with the same MSISDN but different RCH Id
    @Test
    public void testImportMotherWithExistingMsisdn() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader mctsReader = createMctsMotherDataReader("21\t3\t\t\t\t\t200101000811500030\tChandini Devi\t9199722680\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(mctsReader, SubscriptionOrigin.MCTS_IMPORT);

        Reader rchReader = createRchMotherDataReader("21\t3\t\t\t\t\t\t200100201311500052\t121004563168\tChumuki Sahoo\t8658577903\t\t" +
                lmpString + "\t\t\t\t\t4");
        mctsBeneficiaryImportReaderService.importMotherData(rchReader, SubscriptionOrigin.RCH_IMPORT);

        //purging the first import
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscriber = subscriberDataService.findByNumber(9199722680L);
        Set<Subscription> subscriptions = subscriber.get(0).getAllSubscriptions();

        //the mother subscription should be DEACTIVATED
        Assert.assertEquals(1, subscriptions.size());
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        for (Subscription subscription : subscriptions
                ) {
            subscription.setStatus(SubscriptionStatus.DEACTIVATED);
            subscription.setEndDate(new DateTime().withDate(2011, 8, 1));
            subscriptionDataService.update(subscription);
        }

        subscriptionService.purgeOldInvalidSubscriptions();
        transactionManager.commit(status);

        rchReader = createRchMotherDataReader("21\t3\t\t\t\t\t\t200101000811500030\t121004563170\tShanti Ekka\t8658577903\t\t" +
                lmpString + "\t\t\t\t\t4");
        mctsBeneficiaryImportReaderService.importMotherData(rchReader, SubscriptionOrigin.RCH_IMPORT);
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        //import of the third record should have failed as a record with the same MCTS id exists through MCTS import
        List<Subscriber> subscribers = subscriberDataService.findByNumber(8658577903L);
        assertEquals(1, subscribers.size());
        assertSubscriptionError(8658577903L, SubscriptionPackType.PREGNANCY,
                SubscriptionRejectionReason.MSISDN_ALREADY_SUBSCRIBED, "121004563170");
        transactionManager.commit(status);
    }

    //Import first mother record through MCTS, and try to import a second mother record with the same MSISDN but different RCH Id
    @Test
    public void testImportMotherWithExistingMsisdnDiffRchId() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader mctsReader = createMctsMotherDataReader("21\t3\t\t\t\t\t200101000811500030\tChandini Devi\t9199722680\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportReaderService.importMotherData(mctsReader, SubscriptionOrigin.MCTS_IMPORT);

        Reader rchReader = createRchMotherDataReader("21\t3\t\t\t\t\t\t200100201311500052\t121004563168\tChumuki Sahoo\t8658577903\t\t" +
                lmpString + "\t\t\t\t\t4");
        mctsBeneficiaryImportReaderService.importMotherData(rchReader, SubscriptionOrigin.RCH_IMPORT);

        rchReader = createRchMotherDataReader("21\t3\t\t\t\t\t\t200101000811500030\t121004563170\tShanti Ekka\t8658577903\t\t" +
                lmpString + "\t\t\t\t\t4");
        mctsBeneficiaryImportReaderService.importMotherData(rchReader, SubscriptionOrigin.RCH_IMPORT);
        //import of the third record should have failed as a record with the same MCTS id exists through MCTS import
        List<Subscriber> subscribers = subscriberDataService.findByNumber(8658577903L);
        assertEquals(1, subscribers.size());
        assertSubscriptionError(8658577903L, SubscriptionPackType.PREGNANCY,
                SubscriptionRejectionReason.MSISDN_ALREADY_SUBSCRIBED, "121004563170");
    }

    @Test
    public void testImportChildNewSubscriber() throws Exception {
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        assertEquals("Baby1 of Lilima Kua", subscriber.getChild().getName());
        assertEquals("7000000000", subscriber.getChild().getRchId());
        assertEquals(9439986187L, (long) subscriber.getCallingNumber());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(SubscriptionOrigin.RCH_IMPORT, subscription.getOrigin());

        transactionManager.commit(status);
    }

    @Test
    public void testImportChildNewSubscriberNoMotherId() throws Exception {
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t7000000000\t\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscriber = subscriberDataService.findByNumber(9439986187L);
        assertFalse(subscriber.isEmpty());
        transactionManager.commit(status);
    }

    @Test
    public void testImportMotherAndChildSameMsisdn() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportReaderService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscriber = subscriberDataService.findByNumber(9439986187L);
        assertEquals(1, subscriber.size());
        assertEquals(lmp.toLocalDate(), subscriber.get(0).getLastMenstrualPeriod().toLocalDate());
        Set<Subscription> subscriptions = subscriber.get(0).getActiveAndPendingSubscriptions();
        assertEquals(1, subscriptions.size());
        transactionManager.commit(status);

        // import child with same MSISDN and matching MotherID
        DateTime dob = DateTime.now().minusDays(200);
        String dobString = getDateString(dob);
        reader = createRchChildDataReader("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L);
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

    /* Ignored due to IndexOutOfBoundException
    */
    @Ignore
    @Test
    public void testDeactivateChildSubscriptionDueToDeath() throws Exception {
        // import mother
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        assertChild(subscriber, "7000000000", getDateTime(dobString), "Baby1 of Lilima Kua", expectedState, expectedDistrict);
        transactionManager.commit(status);

        // import record for same child with Entry_Type set to 9 -- her subscription should be deactivated
        reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t9\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.CHILD_DEATH, subscription.getDeactivationReason());
        transactionManager.commit(status);
    }

    @Test
    @Ignore
    public void testImportChildDataFromSampleFile() throws Exception {
        mctsBeneficiaryImportReaderService.importChildData(read("csv/RCHChild.csv"), SubscriptionOrigin.RCH_IMPORT);

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict4 = districtService.findByStateAndCode(expectedState, 4L);

        Subscriber subscriber1 = subscriberDataService.findByNumber(9696969696L).get(0);
        assertChild(subscriber1, "1122336523", getDateTime("24/02/2018"), "test", expectedState,
                expectedDistrict4);

        // our RCH data file consists of just 1 record
        assertEquals(1, subscriberDataService.count());
    }

    /*
     * To verify child subscriber is rejected when future DOB is provided.
     */
    @Test
    public void verifyFT283() throws Exception {
        DateTime dob = DateTime.now().plusDays(1);
        String dobString = getDateString(dob);
        Reader reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_child_rejects with reason 'INVALID_DOB'.
        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        Assert.assertEquals(1, childImportRejections.size());
        assertNoSubscriber(9439986187L);
        Assert.assertEquals("9439986187", childImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.INVALID_DOB.toString(), childImportRejections.get(0).getRejectionReason());
        Assert.assertEquals("7000000000", childImportRejections.get(0).getRegistrationNo());
    }

    /*
     * To verify child subscription is rejected when DOB provided is 48 weeks back.
     */
    @Test
    public void verifyFT285() throws Exception {
        DateTime dob = DateTime.now().minusDays(7 * 48);
        String dobString = getDateString(dob);
        Reader reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_child_rejects with reason 'INVALID_DOB'.
        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        Assert.assertEquals(1, childImportRejections.size());
        assertNoSubscriber(9439986187L);
        Assert.assertEquals("9439986187", childImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.INVALID_DOB.toString(), childImportRejections.get(0).getRejectionReason());
        Assert.assertEquals("7000000000", childImportRejections.get(0).getRegistrationNo());
    }

    /*
     * To verify RCH upload is rejected when MSISDN number already exist
     * for subscriber with new rch id.
     */
    @Test
    public void verifyFT287() throws Exception {

        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);

        // create subscriber and subscription
        Reader reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        // attempt to create subscriber with same msisdn but different rch id.
        reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567891\tBaby2 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t8000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        //second subscriber should have been rejected

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscribersByMsisdn = subscriberDataService.findByNumber(9439986187L);
        assertEquals(1, subscribersByMsisdn.size());
        assertChild(subscribersByMsisdn.get(0), "7000000000", dob, "Baby1 of Lilima Kua", stateDataService.findByCode(21L), districtService.findByStateAndCode(stateDataService.findByCode(21L), 3L));
        transactionManager.commit(status);


        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        Assert.assertEquals(1, childImportRejections.size());
        Assert.assertEquals("9439986187", childImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.MOBILE_NUMBER_ALREADY_SUBSCRIBED.toString(), childImportRejections.get(0).getRejectionReason());
        Assert.assertEquals("8000000000", childImportRejections.get(0).getRegistrationNo());

    }

    /*
     * To verify RCH upload is rejected when data doesn’t contain DOB.
     */
    @Test
    public void verifyFT288_1() throws Exception {

        //DOB is missing
        Reader reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_child_rejects with reason 'MISSING_DOB'.
        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        Assert.assertEquals(1, childImportRejections.size());
        assertNoSubscriber(9439986187L);
        Assert.assertEquals("9439986187", childImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.INVALID_DOB.toString(), childImportRejections.get(0).getRejectionReason());
        Assert.assertEquals("7000000000", childImportRejections.get(0).getRegistrationNo());
    }

    /*
     * To verify RCH upload is rejected when location information is incorrect.
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
        Reader reader = createRchChildDataReader("31\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_LOCATION'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION, "7000000000");
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

        Reader reader = createRchChildDataReader("\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_LOCATION'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION, "7000000000");
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

        Reader reader = createRchChildDataReader("31\t\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_LOCATION'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION, "7000000000");
    }

    /*
     * To verify DOB is changed successfully via CSV when subscription
     * already exists for childPack having status as "Deactivated"
     */
    @Test
    public void verifyFT309() throws Exception {
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);
        Reader reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        //Mark subscription deactivate
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.STILL_BIRTH);
        transactionManager.commit(status);

        //create a new subscription for subscriber whose subscription is deactivated.
        dobString = getDateString(dob.minus(50));
        reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        Assert.assertEquals(1, subscriber.getAllSubscriptions().size());
        Assert.assertEquals(0, subscriber.getActiveAndPendingSubscriptions().size());
        Assert.assertEquals(dobString, getDateString(subscriber.getDateOfBirth()));
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
        Reader reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        //Make subscription completed
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        subscriber.setDateOfBirth(dob.minusDays(500));
        subscriberService.updateStartDate(subscriber);

        //create a new subscription for subscriber whose subscription is deactivated.
        dobString = getDateString(dob.minus(50));
        reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        Assert.assertEquals(2, subscriber.getAllSubscriptions().size());
        Assert.assertEquals(1, subscriber.getActiveAndPendingSubscriptions().size());
        Assert.assertEquals(dobString, getDateString(subscriber.getDateOfBirth()));
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
        Reader reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        Assert.assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        Assert.assertEquals("Baby1 of Lilima Kua", subscriber.getChild().getName());

        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        Assert.assertEquals(0, Days.daysBetween(dob.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        transactionManager.commit(status);

        // attempt to update dob through rch upload
        DateTime newDob = DateTime.now().minusDays(150);
        String newDobString = getDateString(newDob);
        reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + newDobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        Assert.assertEquals(newDob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        Assert.assertEquals(0, Days.daysBetween(newDob.toLocalDate(), subscription.getStartDate().toLocalDate())
                .getDays());
        transactionManager.commit(status);
    }

    /*
     * To verify child RCH upload is rejected when stateId is missing
     *
     * https://applab.atlassian.net/browse/NMS-228
     * Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void verifyFT525() throws Exception {
        String dobString = getDateString(DateTime.now().minusDays(30));
        //state id is missing
        Reader reader = createRchChildDataReader("\t6\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION, "7000000000");
    }

    /*
     * To verify child RCH upload is rejected with invalid state id
     * Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void verifyFT526() throws Exception {
        String dobString = getDateString(DateTime.now().minusDays(30));
        //state id with invalid value
        Reader reader = createRchChildDataReader("31\t6\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION, "7000000000");
    }

    /*
     * To verify child RCH upload is rejected with invalid district id
     * Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void verifyFT527() throws Exception {
        String dobString = getDateString(DateTime.now().minusDays(30));
        Reader reader = createRchChildDataReader("21\t6\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        //district id with invalid value
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION, "7000000000");
    }

    /*
     * To verify child RCH upload is rejected when mandatory parameter district is missing.
     *
     * https://applab.atlassian.net/browse/NMS-228
     * Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void verifyFT529() throws Exception {
        DateTime dob = DateTime.now().minusDays(60);
        String dobString = getDateString(dob);
        //district id is missing
        Reader reader = createRchChildDataReader("21\t\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION, "7000000000");
    }

    /*
     * To verify child RCH upload is rejected when mandatory parameter state is having invalid value.
     * Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void verifyFT530() throws Exception {
        DateTime dob = DateTime.now().minusDays(60);
        String dobString = getDateString(dob);
        //state id with invalid value
        Reader reader = createRchChildDataReader("31\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION, "7000000000");
    }

    /*
     * To verify child RCH upload is rejected when mandatory parameter district is having invalid value.
     * Ignored as it is failing due to Null Pointer Exception
     */
    @Ignore
    @Test
    public void verifyFT531() throws Exception {
        DateTime dob = DateTime.now().minusDays(60);
        String dobString = getDateString(dob);
        //district id with invalid value
        Reader reader = createRchChildDataReader("21\t6\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION, "7000000000");
    }

    /* Ignored due to IndexOutOfBoundException
    */
    @Ignore
    @Test
    public void testImportChildUpdateEntryTypeStatus() throws Exception {
        DateTime dob = DateTime.now().minusDays(60);
        String dobString = getDateString(dob);
        Reader reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                 + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        Assert.assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        Assert.assertEquals("Baby1 of Lilima Kua", subscriber.getChild().getName());
        transactionManager.commit(status);

        //update entry type to 9
        reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t9\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.CHILD_DEATH, subscription.getDeactivationReason());
        transactionManager.commit(status);

        //update entry type to 1
        //a new subscription should not be created as a child once deactivated by death cannot be reactivated with same RCH id
        reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t1\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size()); // the first subscription was deactivated and a new subscription was not created
        assertEquals(DeactivationReason.CHILD_DEATH, subscriptions.iterator().next().getDeactivationReason());
        transactionManager.commit(status);
    }

    /* Ignored due to IndexOutOfBoundException
    */
    @Ignore
    @Test
    public void testChildImportMotherMctsNull() throws Exception {
        DateTime dob = DateTime.now().minusDays(60);
        String dobString = getDateString(dob);
        Reader reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertChild(subscriber, "7000000000", dob, "Baby1 of Lilima Kua", stateDataService.findByCode(21L), districtService.findByStateAndCode(stateDataService.findByCode(21L), 3L));
        transactionManager.commit(status);
    }

    /* Ignored due to IndexOutOfBoundException
    */
    @Ignore
    @Test
    public void testCreateNewChildRecordSameMsisdn() throws Exception {
        DateTime dob = DateTime.now().minusDays(60);
        String dobString = getDateString(dob);
        Reader reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertChild(subscriber, "7000000000", dob, "Baby1 of Lilima Kua", stateDataService.findByCode(21L), districtService.findByStateAndCode(stateDataService.findByCode(21L), 3L));
        transactionManager.commit(status);

        //deactivate child subscription due to death
        subscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscription.setEndDate(new DateTime().withDate(2016, 8, 1));
        subscriptionDataService.update(subscription);
        subscriptionService.purgeOldInvalidSubscriptions();

        //import a new child record for the same mother with same msisdn
        dob = DateTime.now().minusDays(30);
        dobString = getDateString(dob);
        reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567891\tBaby2 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t8000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscribers = subscriberDataService.findByNumber(9439986187L);
        assertEquals(1, subscribers.size());
        assertChild(subscribers.get(0), "8000000000", dob, "Baby2 of Lilima Kua", stateDataService.findByCode(21L), districtService.findByStateAndCode(stateDataService.findByCode(21L), 3L));
        transactionManager.commit(status);
    }

    @Test
    public void testCreateNewChildRecordDifferentMsisdn() throws Exception {
        DateTime dob = DateTime.now().minusDays(60);
        String dobString = getDateString(dob);
        Reader reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertChild(subscriber, "7000000000", dob, "Baby1 of Lilima Kua", stateDataService.findByCode(21L), districtService.findByStateAndCode(stateDataService.findByCode(21L), 3L));
        transactionManager.commit(status);

        //deactivate child subscription due to death
        subscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscription.setEndDate(new DateTime().withDate(2016, 8, 1));
        subscriptionDataService.update(subscription);
        subscriptionService.purgeOldInvalidSubscriptions();

        //import a new child record for the same mother with same msisdn
        dob = DateTime.now().minusDays(30);
        dobString = getDateString(dob);
        reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567891\tBaby2 of Lilima Kua\t9876453210\t9439986188\t"
                + dobString + "\t8000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscribers = subscriberDataService.findByNumber(9439986188L);
        assertEquals(1, subscribers.size());
        assertChild(subscribers.get(0), "8000000000", dob, "Baby2 of Lilima Kua", stateDataService.findByCode(21L), districtService.findByStateAndCode(stateDataService.findByCode(21L), 3L));
        transactionManager.commit(status);
    }

    @Test
    public void testUpdateMsisdnForChildRecord() throws Exception {
        DateTime dob = DateTime.now().minusDays(60);
        String dobString = getDateString(dob);
        Reader reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986187\t"
                + dobString + "\t7000000000\t2000000000\t\t10-05-2016");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertChild(subscriber, "7000000000", dob, "Baby1 of Lilima Kua", stateDataService.findByCode(21L), districtService.findByStateAndCode(stateDataService.findByCode(21L), 3L));
        transactionManager.commit(status);

        //update msisdn of the existing child
        reader = createRchChildDataReader("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t9876453210\t9439986188\t"
                + dobString + "\t7000000000\t2000000000\t\t");
        mctsBeneficiaryImportReaderService.importChildData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986188L).get(0);
        assertNotNull(subscriber);
        assertChild(subscriber, "7000000000", dob, "Baby1 of Lilima Kua", stateDataService.findByCode(21L), districtService.findByStateAndCode(stateDataService.findByCode(21L), 3L));
        transactionManager.commit(status);
    }

    private Reader createRchMotherDataReader(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("StateID\tDistrict_ID\tTaluka_ID\tHealthBlock_ID\tPHC_ID\tSubCentre_ID\tVillage_ID\tMCTS_ID_No\tRegistration_no\tName\tMobile_no\tBirthdate\tLMP_Date\t");
        builder.append("Abortion_Type\tDelivery_Outcomes\tEntry_Type\tExec_date\tCase_no");
        builder.append("\n");

        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }



    private Reader createMctsMotherDataReader(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("StateID\tDistrict_ID\tTaluka_ID\tHealthBlock_ID\tPHC_ID\tVillage_ID\tID_No\tName\tWhom_PhoneNo\tBirthdate\tLMP_Date\t");
        builder.append("Abortion\tOutcome_Nos\tEntry_Type\tLast_Update_Date");
        builder.append("\n");

        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader createRchChildDataReader(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("StateID\tDistrict_ID\tTaluka_ID\tHealthBlock_ID\tPHC_ID\tVillage_ID\tMCTS_ID_No\tName\tMCTS_Mother_ID_No\tMobile_no\tBirthdate\tRegistration_no\tMother_Registration_no\tEntry_Type\tExec_Date");
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

    private void assertSubscriptionError(Long callingNumber, SubscriptionPackType packType,
                                         SubscriptionRejectionReason rejectionReason, String rchId) {

        List<SubscriptionError> susbErrors = subscriptionErrorDataService.findByContactNumber(callingNumber);
        SubscriptionError susbError = susbErrors.iterator().next();

        assertNotNull(susbError);
        Assert.assertEquals(packType, susbError.getPackType());
        Assert.assertEquals(rejectionReason, susbError.getRejectionReason());
        Assert.assertEquals(rchId, susbError.getBeneficiaryId());
        assertEquals(SubscriptionOrigin.RCH_IMPORT, susbError.getImportOrigin());
    }

    private void assertMother(Subscriber subscriber, String motherId, DateTime lmp, String name, State state, District district) {
        assertNotNull(subscriber);
        assertNotNull(subscriber.getMother());
        Assert.assertEquals(motherId, subscriber.getMother().getRchId());
        Assert.assertEquals(name, subscriber.getMother().getName());
        Assert.assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        Assert.assertEquals(state, subscriber.getMother().getState());
        Assert.assertEquals(district, subscriber.getMother().getDistrict());
    }

    private void assertChild(Subscriber subscriber, String childId, DateTime dob, String name, State state, District district) {
        assertNotNull(subscriber);
        assertNotNull(subscriber.getChild());
        Assert.assertEquals(childId, subscriber.getChild().getRchId());
        Assert.assertEquals(name, subscriber.getChild().getName());
        Assert.assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        Assert.assertEquals(state, subscriber.getChild().getState());
        Assert.assertEquals(district, subscriber.getChild().getDistrict());
    }

    private DateTime getDateTime(String dateString) {
        DateTimeParser[] parsers = {
                DateTimeFormat.forPattern("dd-MM-yyyy").getParser(),
                DateTimeFormat.forPattern("dd/MM/yyyy").getParser()};
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

        return formatter.parseDateTime(dateString);
    }

    private void assertNoSubscriber(long callingNumber) {
        List<Subscriber> subscriber = subscriberDataService.findByNumber(callingNumber);
        assertTrue(subscriber.isEmpty());
    }
}
