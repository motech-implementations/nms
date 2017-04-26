package org.motechproject.nms.testing.it.kilkari;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.BlockedMsisdnRecord;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
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
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);
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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        Assert.assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        Assert.assertEquals("Shanti Ekka", subscriber.getMother().getName());
        List<SubscriptionError> se = subscriptionErrorDataService.findByContactNumber(9439986187L);
        Assert.assertEquals(0, se.size());
        transactionManager.commit(status);

        DateTime newLmp = DateTime.now().minusDays(150);
        String newLmpString = getDateString(newLmp);
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                newLmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);

        // Lmp update should fail
        assertNotEquals(newLmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        Assert.assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.BENEFICIARY_ALREADY_SUBSCRIBED, "240");
        transactionManager.commit(status);
    }

    @Test
    public void testImportMotherInvalidState() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("9\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" + lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);
        List<SubscriptionError> se = subscriptionErrorDataService.findByContactNumber(9439986187L);
        Assert.assertEquals(1, se.size());
        Assert.assertEquals(SubscriptionRejectionReason.INVALID_LOCATION, se.get(0).getRejectionReason());
    }

    @Test
    public void testImportMotherDataFromSampleFile() throws Exception {
        mctsBeneficiaryImportService.importMotherData(read("csv/rch_mother.txt"), SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_LMP'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.INVALID_LMP, "240");
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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_LMP'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.INVALID_LMP, "240");
    }

    /**
     * To verify RCH upload is rejected when RCH doesn’t contain LMP.
     */
    @Test
    public void verifyFT288_2() throws Exception {

        //LMP is missing
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t\t\t\t\t\t8");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'MISSING_LMP'.
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.MISSING_LMP, "240");
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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        //Make subscription completed
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        subscriber.setLastMenstrualPeriod(lmp.minusDays(650));
        subscriberService.updateStartDate(subscriber);

        //create a new subscription for subscriber whose subscription is completed.
        lmpString = getDateString(lmp.minus(200));
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        Assert.assertEquals(2, subscriber.getAllSubscriptions().size());
        Assert.assertEquals(1, subscriber.getActiveAndPendingSubscriptions().size());
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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
     * To verify pregnancyPack is marked deactivated with reason abortion via CSV.
     * checked with abortion value 'MTP<12 Weeks'
     */
    @Test
    public void verifyFT313_1() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with abortion value 'MTP<12 Weeks'
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tMTP<12 Weeks\t\t\t\t8");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.ABORT_STILLBIRTH_DEATH, "240");
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
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tSpontaneous\t\t\t\t8");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.ABORT_STILLBIRTH_DEATH, "240");
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
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tMTP>12 Weeks\t\t\t\t8");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.ABORT_STILLBIRTH_DEATH, "240");
        transactionManager.commit(status);
    }

    @Test
    public void testDeactivateMotherSubscriptionDueToAbortion() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
     * To verify pregnancyPack is marked deactivated with reason mother death via CSV.
     */
    @Test
    public void verifyFT314() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with entry_type value '9'
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t9\t\t8");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.ABORT_STILLBIRTH_DEATH, "240");
        transactionManager.commit(status);
    }

    @Test
    public void testDeactivateMotherSubscriptionDueToDeath() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
     * To verify pregnancyPack is marked deactivated with reason still birth via CSV.
     */
    @Test
    public void verifyFT315() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);

        //attempt to create mother data with delivery_outcomes value '0'
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t0\t\t\t8");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.ABORT_STILLBIRTH_DEATH, "240");
        transactionManager.commit(status);
    }

    @Test
    public void testDeactivateMotherSubscriptionDueToStillbirth() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);
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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        List<Subscriber> subscriber = subscriberDataService.findByNumber(9439986187L);
        assertNotNull(subscriber.get(0));
        Assert.assertEquals(lmp.toLocalDate(), mctsMotherDataService.findByBeneficiaryId("1234567890").getLastMenstrualPeriod().toLocalDate());


        blockedMsisdnRecordDataService.create(new BlockedMsisdnRecord(9439986188L, DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED));
        transactionManager.commit(status);


        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t240\tShanti Ekka\t9439986188\t\t" +
                lmpString + "\t\t\t\t\t8");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscribers = subscriberDataService.findByNumber(9439986187L);
        assertTrue(subscribers.isEmpty());
        List<SubscriptionError> se = subscriptionErrorDataService.findByContactNumber(9439986187L);
        assertEquals(1, se.size());
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.INVALID_CASE_NO, "2234567890");
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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscribers = subscriberDataService.findByNumber(9439986187L);
        assertTrue(subscribers.isEmpty());
        List<SubscriptionError> se = subscriptionErrorDataService.findByContactNumber(9439986187L);
        assertEquals(1, se.size());
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.INVALID_CASE_NO, "2234567890");
        assertEquals("Case no is less than the maxCaseNo encountered so far", se.get(0).getRejectionMessage());
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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(3L, subscriber.getCaseNo().longValue());
        assertEquals(3L, subscriber.getMother().getMaxCaseNo().longValue());

        List<SubscriptionError> se = subscriptionErrorDataService.findByContactNumber(9439986187L);
        assertEquals(1, se.size());
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.MSISDN_ALREADY_SUBSCRIBED, "2234567890");
        transactionManager.commit(status);
    }

    // Test mctsmother update with RchId when both the id's are provided
    @Test
    public void testMctsMotherUpdateWithRchId() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMctsMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        transactionManager.commit(status);

        lmp = DateTime.now().minusDays(120);
        lmpString = getDateString(lmp);
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t1234567890\t2234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t4");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        MctsMother mother = mctsMotherDataService.findByBeneficiaryId("1234567890");
        assertEquals("2234567890", mother.getRchId());
        assertEquals(mother.getId(),subscriber.getMother().getId());
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        transactionManager.commit(status);
    }

    // Test if rch mother is updated with mctsId when both the id's are provided
    @Test
    public void testRchMotherUpdateWithMctsId() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t\t2234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t4");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
    public void testMctsMotherImportWithRchId() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMctsMotherDataReader("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.MCTS_IMPORT);

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        transactionManager.commit(status);

        DateTime lmpNew = DateTime.now().minusDays(120);
        lmpString = getDateString(lmpNew);
        reader = createRchMotherDataReader("21\t3\t\t\t\t\t\t\t2234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t\t\t4");
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberDataService.findByNumber(9439986187L).get(0);
        assertEquals("1234567890", subscriber.getMother().getBeneficiaryId());
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());

        List<SubscriptionError> se = subscriptionErrorDataService.findByContactNumber(9439986187L);
        assertEquals(1, se.size());
        assertSubscriptionError(9439986187L, SubscriptionPackType.PREGNANCY, SubscriptionRejectionReason.MSISDN_ALREADY_SUBSCRIBED, "2234567890");
        transactionManager.commit(status);
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
        mctsBeneficiaryImportService.importMotherData(reader, SubscriptionOrigin.RCH_IMPORT);

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
