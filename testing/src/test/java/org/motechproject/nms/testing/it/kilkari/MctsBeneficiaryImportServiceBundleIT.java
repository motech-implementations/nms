package org.motechproject.nms.testing.it.kilkari;


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

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.junit.Before;
import org.junit.Ignore;
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
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
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
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class MctsBeneficiaryImportServiceBundleIT extends BasePaxIT {

    @Inject
    TestingService testingService;
    @Inject
    LanguageDataService languageDataService;
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

    @Before
    public void setUp() {
        testingService.clearDatabase();
        createLocationData();

        SubscriptionHelper sh = new SubscriptionHelper(subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService, stateDataService,
                districtDataService, districtService);
        sh.pregnancyPack();
        sh.childPack();
    }

    private void createLocationData() {
        // specific locations from the mother and child data files:

        State state21 = createState(21L, "State 21");
        District district2 = createDistrict(state21, 2L, "Jharsuguda");
        District district3 = createDistrict(state21, 3L, "Sambalpur");
        District district4 = createDistrict(state21, 4L, "Debagarh");
        state21.getDistricts().addAll(Arrays.asList(district2, district3, district4));

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
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals("Shanti Ekka", subscriber.getMother().getName());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(SubscriptionOrigin.MCTS_IMPORT, subscription.getOrigin());
    }

    @Test
    public void testImportMotherAlternateDateFormat() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = lmp.toString("dd/MM/yyyy");
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
    }

    @Test
    public void testImportMotherWhoAlreadyExistsUpdateLmp() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals("Shanti Ekka", subscriber.getMother().getName());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(90, Days.daysBetween(lmp.toLocalDate(), subscription.getStartDate().toLocalDate()).getDays());

        DateTime newLmp = DateTime.now().minusDays(150);
        String newLmpString = getDateString(newLmp);
        reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                newLmpString + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(newLmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(90, Days.daysBetween(newLmp.toLocalDate(), subscription.getStartDate().toLocalDate()).getDays());
    }

    @Test
    public void testImportMotherInvalidState() throws Exception {
        Reader reader = createMotherDataReaderWithHeaders("9\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t22-11-2014\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);
        List<SubscriptionError> se = subscriptionErrorDataService.findByContactNumber(9439986187L);
        assertEquals(1, se.size());
        assertEquals(SubscriptionRejectionReason.INVALID_LOCATION, se.get(0).getRejectionReason());
    }

    @Test
    public void testImportChildNewSubscriberNoMotherId() throws Exception {
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        assertEquals("Baby1 of Lilima Kua", subscriber.getChild().getName());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(SubscriptionOrigin.MCTS_IMPORT, subscription.getOrigin());
        assertEquals(SubscriptionPackType.CHILD, subscription.getSubscriptionPack().getType());
    }

    @Test
    public void testImportMotherAndChildSameMsisdn() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        Set<Subscription> subscriptions = subscriber.getActiveAndPendingSubscriptions();
        assertEquals(1, subscriptions.size());

        // import child with same MSISDN and matching MotherID
        DateTime dob = DateTime.now().minusDays(200);
        String dobString = getDateString(dob);
        reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);

        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());

        subscriptions = subscriber.getActiveAndPendingSubscriptions();
        Subscription childSubscription = subscriptionService.getActiveSubscription(subscriber, SubscriptionPackType.CHILD);
        Subscription pregnancySubscription = subscriptionService.getActiveSubscription(subscriber, SubscriptionPackType.PREGNANCY);

        // the pregnancy subscription should have been deactivated
        assertEquals(1, subscriptions.size());
        assertNotNull(childSubscription);
        assertNull(pregnancySubscription);
    }

    @Test
    public void testDeactivateMotherSubscriptionDueToAbortion() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        assertMother(subscriber, "1234567890", getDateTime(lmpString), "Shanti Ekka", expectedState, expectedDistrict);

        // import record for same mother with Abortion set to "Spontaneous" -- her subscription should be deactivated
        reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tSpontaneous\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.MISCARRIAGE_OR_ABORTION, subscription.getDeactivationReason());
    }

    @Test
    public void testDeactivateMotherSubscriptionDueToStillbirth() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        assertMother(subscriber, "1234567890", getDateTime(lmpString), "Shanti Ekka", expectedState, expectedDistrict);

        // import record for same mother with Outcome_Nos set to 0 -- her subscription should be deactivated
        reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t0\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.STILL_BIRTH, subscription.getDeactivationReason());
    }

    @Test
    public void testDeactivateMotherSubscriptionDueToDeath() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        assertMother(subscriber, "1234567890", getDateTime(lmpString), "Shanti Ekka", expectedState, expectedDistrict);

        // import record for same mother with Entry_Type set to 9 -- her subscription should be deactivated
        reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t9");
        mctsBeneficiaryImportService.importMotherData(reader);

        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.MATERNAL_DEATH, subscription.getDeactivationReason());
    }

    @Test
    public void testDeactivateChildSubscriptionDueToDeath() throws Exception {
        // import mother
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        assertChild(subscriber, "1234567890", getDateTime(dobString), "Baby1 of Lilima Kua", expectedState, expectedDistrict);

        // import record for same child with Entry_Type set to 9 -- her subscription should be deactivated
        reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t9");
        mctsBeneficiaryImportService.importChildData(reader);

        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.CHILD_DEATH, subscription.getDeactivationReason());
    }

    @Test
    public void testImportChildDataFromSampleFile() throws Exception {
        mctsBeneficiaryImportService.importChildData(read("csv/child.txt"));

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict4 = districtService.findByStateAndCode(expectedState, 4L);

        Subscriber subscriber1 = subscriberDataService.findByCallingNumber(9439998253L);
        assertChild(subscriber1, "210404600521400116", getDateTime("2/12/2014"), "Baby1 of PANI HEMRAM", expectedState,
                expectedDistrict4);

        // although our MCTS data file contains 10 children, we only create 8 subscribers due to duplicate phone numbers
        assertEquals(8, subscriberDataService.count());
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

    private Reader createChildDataReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("State Name : State 1").append("\n");
        builder.append("\n");
        builder.append("StateID\tDistrict_ID\tTaluka_ID\tHealthBlock_ID\tPHC_ID\tVillage_ID\tID_No\tName\tMother_ID\tWhom_PhoneNo\tBirthdate\tEntry_Type");
        builder.append("\n");

        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader createMotherDataReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("State Name : State 1").append("\n");
        builder.append("\n");
        builder.append("StateID\tDistrict_ID\tTaluka_ID\tHealthBlock_ID\tPHC_ID\tVillage_ID\tID_No\tName\tWhom_PhoneNo\tBirthdate\tLMP_Date\t");
        builder.append("Abortion\tOutcome_Nos\tEntry_Type");
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
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
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
        Reader reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
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
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
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
        Reader reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
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
        Reader reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);

        // attempt to create subscriber with same msisdn but different mcts.
        reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567891\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
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
    	Reader reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t\t");
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
    	Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t\t\t\t");
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
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        //Make subscription completed
        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        subscriber.setLastMenstrualPeriod(lmp.minusDays(650));
        subscriberService.update(subscriber);
        
        //create a new subscription for subscriber whose subscription is completed.
        lmpString = getDateString(lmp.minus(200));
        reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" + lmpString
                + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);
        
        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertEquals(2, subscriber.getAllSubscriptions().size());
        assertEquals(1, subscriber.getActiveAndPendingSubscriptions().size());
        assertEquals(lmpString, getDateString(subscriber.getLastMenstrualPeriod()));
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
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        //Mark subscription deactivate
        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        Subscription subscription =subscriber.getActiveAndPendingSubscriptions().iterator().next();
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.MISCARRIAGE_OR_ABORTION);
        
        //create a new subscription for subscriber whose subscription is deactivated.
        lmpString = getDateString(lmp.minus(200));
        reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" + lmpString
                + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);
        
        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertEquals(2, subscriber.getAllSubscriptions().size());
        assertEquals(1, subscriber.getActiveAndPendingSubscriptions().size());
        assertEquals(lmpString, getDateString(subscriber.getLastMenstrualPeriod()));
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
        Reader reader = createChildDataReaderWithHeaders("31\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
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

        Reader reader = createChildDataReaderWithHeaders("\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t" + dobString + "\t");
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

        Reader reader = createChildDataReaderWithHeaders("31\t\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t" + dobString + "\t");
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
        Reader reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);
        
        //Mark subscription deactivate
        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        Subscription subscription =subscriber.getActiveAndPendingSubscriptions().iterator().next();
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.STILL_BIRTH);
        
        //create a new subscription for subscriber whose subscription is deactivated.
        dobString = getDateString(dob.minus(50));
        reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);

        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertEquals(2, subscriber.getAllSubscriptions().size());
        assertEquals(1, subscriber.getActiveAndPendingSubscriptions().size());
        assertEquals(dobString, getDateString(subscriber.getDateOfBirth()));
    }
    
    /*
     * To verify DOB is changed successfully via CSV when subscription 
     * already exist for childPack having status as "Completed"
     */
    @Test
    public void verifyFT310() throws Exception {
    	DateTime dob = DateTime.now();
        String dobString = getDateString(dob);
        Reader reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);
        
        //Make subscription completed
        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        subscriber.setDateOfBirth(dob.minusDays(500));
        subscriberService.update(subscriber);
        
        //create a new subscription for subscriber whose subscription is deactivated.
        dobString = getDateString(dob.minus(50));
        reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);

        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertEquals(2, subscriber.getAllSubscriptions().size());
        assertEquals(1, subscriber.getActiveAndPendingSubscriptions().size());
        assertEquals(dobString, getDateString(subscriber.getDateOfBirth()));
    }
    
    /*
     * To verify DOB is changed successfully via CSV when subscription 
     * already exist for childPack having status as "Active"
     */
    @Test
    public void verifyFT311() throws Exception {
    	DateTime dob = DateTime.now();
        String dobString = getDateString(dob);
        Reader reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        assertEquals("Baby1 of Lilima Kua", subscriber.getChild().getName());

        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(0, Days.daysBetween(dob.toLocalDate(), subscription.getStartDate().toLocalDate()).getDays());

        // attempt to update dob through mcts upload
        DateTime newDob = DateTime.now().minusDays(150);
        String newDobString = getDateString(newDob);
        reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t" +
                newDobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);

        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(newDob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(0, Days.daysBetween(newDob.toLocalDate(), subscription.getStartDate().toLocalDate()).getDays());
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
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        Set<Subscription> subscriptions = subscriber.getActiveAndPendingSubscriptions();
        assertEquals(1, subscriptions.size());

        // import child with same MSISDN and no MotherID
        DateTime dob = DateTime.now().minusDays(200);
        String dobString = getDateString(dob);
        reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t\t9439986187\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);

        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());

        subscriptions = subscriber.getActiveAndPendingSubscriptions();
        Subscription childSubscription = subscriptionService.getActiveSubscription(subscriber, SubscriptionPackType.CHILD);
        Subscription pregnancySubscription = subscriptionService.getActiveSubscription(subscriber, SubscriptionPackType.PREGNANCY);

        // both subscription should have been activated
        assertEquals(2, subscriptions.size());
        assertNotNull(childSubscription);
        assertNotNull(pregnancySubscription);

        // import child with same MSISDN and above MotherID --> child should be updated and mother be deactivated
        reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);

        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        subscriptions = subscriber.getActiveAndPendingSubscriptions();
        childSubscription = subscriptionService.getActiveSubscription(subscriber, SubscriptionPackType.CHILD);
        pregnancySubscription = subscriptionService.getActiveSubscription(subscriber, SubscriptionPackType.PREGNANCY);

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
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals("Shanti Ekka", subscriber.getMother().getName());
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(90, Days.daysBetween(lmp.toLocalDate(), subscription.getStartDate().toLocalDate()).getDays());
        assertEquals(subscription.getStatus(), SubscriptionStatus.PENDING_ACTIVATION);
        
        DateTime newLmp = DateTime.now().minusDays(90);
        String newLmpString = getDateString(newLmp);
        reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                newLmpString + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(newLmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(90, Days.daysBetween(newLmp.toLocalDate(), subscription.getStartDate().toLocalDate()).getDays());
        assertEquals(subscription.getStatus(), SubscriptionStatus.ACTIVE);
    }

    @Test
    public void testImportMotherDataFromSampleFile() throws Exception {
        mctsBeneficiaryImportService.importMotherData(read("csv/mother.txt"));

        State expectedState = stateDataService.findByCode(21L);
        District expectedDistrict = districtService.findByStateAndCode(expectedState, 3L);

        Subscriber subscriber1 = subscriberDataService.findByCallingNumber(9439986187L);
        assertMother(subscriber1, "210302604211400029", getDateTime("22/11/2014"), "Shanti Ekka", expectedState,
                expectedDistrict);

        Subscriber subscriber2 = subscriberDataService.findByCallingNumber(7894221701L);
        assertMother(subscriber2, "210302604611400023", getDateTime("15/6/2014"), "Damayanti Khadia", expectedState,
                expectedDistrict);

        // although our MCTS data file contains 10 mothers, we only create 3 subscribers due to duplicate phone numbers
        assertEquals(3, subscriberDataService.count());
    }

    private void assertNoSubscriber(long callingNumber) {
		Subscriber subscriber = subscriberDataService.findByCallingNumber(callingNumber);
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
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tMTP<12 Weeks\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);
        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertNotNull(subscription);
        assertEquals(subscription.getStatus(), SubscriptionStatus.DEACTIVATED);
        assertEquals(subscription.getDeactivationReason(), DeactivationReason.MISCARRIAGE_OR_ABORTION);
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
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tSpontaneous\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);
        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertNotNull(subscription);
        assertEquals(subscription.getStatus(), SubscriptionStatus.DEACTIVATED);
        assertEquals(subscription.getDeactivationReason(), DeactivationReason.MISCARRIAGE_OR_ABORTION);
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
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\tMTP>12 Weeks\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);
        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertNotNull(subscription);
        assertEquals(subscription.getStatus(), SubscriptionStatus.DEACTIVATED);
        assertEquals(subscription.getDeactivationReason(), DeactivationReason.MISCARRIAGE_OR_ABORTION);
    }
    
    /*
     * To verify pregnancyPack is marked deactivated with reason still birth via CSV. 
     */
    @Test
    public void verifyFT315() throws Exception {
        DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);
        
        //attempt to create mother data with Outcome_Nos value '0'
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t0\t");
        mctsBeneficiaryImportService.importMotherData(reader);
        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertNotNull(subscription);
        assertEquals(subscription.getStatus(), SubscriptionStatus.DEACTIVATED);
        assertEquals(subscription.getDeactivationReason(), DeactivationReason.STILL_BIRTH);
    }
    
    /*
     * To verify mother MCTS upload is rejected when mandatory parameter state is missing.
     *
     * https://applab.atlassian.net/browse/NMS-228
     */
    @Test
    public void verifyFT524() throws Exception {
    	DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);
        //state id is missing
        Reader reader = createMotherDataReaderWithHeaders("\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        mctsBeneficiaryImportService.importChildData(reader);
        
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }
    
    /*
     * To verify mother MCTS upload is rejected when mandatory parameter district is missing. 
     *
     * https://applab.atlassian.net/browse/NMS-228
     */
    @Test
    public void verifyFT525() throws Exception {
    	DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);
        //district id is missing
        Reader reader = createMotherDataReaderWithHeaders("21\t\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        mctsBeneficiaryImportService.importChildData(reader);
        
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }
    
    /*
     * To verify mother MCTS upload is rejected when mandatory parameter state is having invalid value. 
     */
    @Test
    public void verifyFT526() throws Exception {
    	DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);
        //state id with invalid value
        Reader reader = createMotherDataReaderWithHeaders("31\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        mctsBeneficiaryImportService.importChildData(reader);
        
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }
    
    /*
     * To verify mother MCTS upload is rejected when mandatory parameter state is having invalid value. 
     */
    @Test
    public void verifyFT527() throws Exception {
    	DateTime lmp = DateTime.now().minusDays(30);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReaderWithHeaders("21\t6\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        //district id with invalid value
        mctsBeneficiaryImportService.importChildData(reader);
        
        assertNoSubscriber(9439986187L);
        assertSubscriptionError(9439986187L, SubscriptionPackType.CHILD, SubscriptionRejectionReason.INVALID_LOCATION);
    }
    
    /*
     * To verify child MCTS upload is rejected when mandatory parameter state is missing.
     *
     * https://applab.atlassian.net/browse/NMS-228
     */
    @Test
    public void verifyFT528() throws Exception {
    	DateTime dob = DateTime.now();
        String dobString = getDateString(dob);
        //state id is missing.
        Reader reader = createChildDataReaderWithHeaders("\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t\t" + dobString);
        mctsBeneficiaryImportService.importChildData(reader);
        
        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_DOB'.
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
        Reader reader = createChildDataReaderWithHeaders("21\t\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);
        
        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_DOB'.
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
        Reader reader = createChildDataReaderWithHeaders("31\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);
        
        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_DOB'.
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
        Reader reader = createChildDataReaderWithHeaders("21\t6\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);
        
        //subscriber should not be created and rejected entry should be in nms_subscription_errors with reason 'INVALID_DOB'.
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
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t9");
        mctsBeneficiaryImportService.importMotherData(reader);
        
        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertNotNull(subscription);
        assertEquals(subscription.getStatus(), SubscriptionStatus.DEACTIVATED);
        assertEquals(subscription.getDeactivationReason(), DeactivationReason.MATERNAL_DEATH);
    }
    
    /*
     * To verify child pack is marked deactivated with reason child death via CSV. 
     */
    @Test
    public void verifyFT316() throws Exception {
        DateTime dob = DateTime.now();
        String dobString = getDateString(dob);
        
        //attempt to create child data with entry_type '9'
        Reader reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t9");
        mctsBeneficiaryImportService.importChildData(reader);
        
        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertNotNull(subscription);
        assertEquals(subscription.getStatus(), SubscriptionStatus.DEACTIVATED);
        assertEquals(subscription.getDeactivationReason(), DeactivationReason.CHILD_DEATH);
    }
    
    /*
     * To verify mother record gets created when child record exist with status as deactivated.
     * 
     * https://applab.atlassian.net/browse/NMS-234
     */
    @Test
    @Ignore
    public void testCreateMotherSubscriptionWhenDeactivatedChildSubscriptionExists() throws Exception {
        // import child
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t"
                + dobString + "\t");
        mctsBeneficiaryImportService.importChildData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());
        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        //Deactivate child subscription
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.CHILD_DEATH);
        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.CHILD_DEATH, subscription.getDeactivationReason());
        
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567891\tShanti Ekka\t9439986187\t\t" +
                lmpString + "\t\t\t");
        mctsBeneficiaryImportService.importMotherData(reader);

        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertEquals(1, subscriber.getActiveAndPendingSubscriptions().size());
        assertEquals(2, subscriber.getAllSubscriptions().size());
        
    }
    
}