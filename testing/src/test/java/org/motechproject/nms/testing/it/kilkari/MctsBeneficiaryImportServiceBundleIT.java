package org.motechproject.nms.testing.it.kilkari;


import org.joda.time.DateTime;
import org.joda.time.Days;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.repository.TalukaDataService;
import org.motechproject.nms.region.repository.VillageDataService;
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
import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createCircle;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createDistrict;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createHealthFacilityType;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createLanguage;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createState;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createTaluka;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createHealthBlock;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createHealthFacility;
import static org.motechproject.nms.testing.it.utils.LocationDataUtils.createHealthFacilityType;


@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class MctsBeneficiaryImportServiceBundleIT extends BasePaxIT {

    @Inject
    private TestingService testingService;
    @Inject
    private LanguageDataService languageDataService;
    @Inject
    private StateDataService stateDataService;
    @Inject
    private DistrictDataService districtDataService;
    @Inject
    private TalukaDataService talukaDataService;
    @Inject
    private VillageDataService villageDataService;
    @Inject
    private HealthBlockDataService healthBlockDataService;
    @Inject
    private HealthFacilityDataService healthFacilityDataService;
    @Inject
    private CircleDataService circleDataService;
    @Inject
    private SubscriberDataService subscriberDataService;
    @Inject
    private SubscriptionService subscriptionService;
    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    private MctsBeneficiaryImportService mctsBeneficiaryImportService;

    @Before
    public void setUp() {
        testingService.clearDatabase();
        createLocationData();

        SubscriptionHelper subscriptionHelper = new SubscriptionHelper(subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService);
        subscriptionHelper.pregnancyPack();
        subscriptionHelper.childPack();
    }

    private void createLocationData() {
        // specific locations from the mother data file:
        State state21 = createState(21L, "State 21");
        District district3 = createDistrict(state21, 3L, "Sambalpur");
        state21.getDistricts().add(district3);
        Taluka taluka26 = createTaluka(district3, "26", "Govindpur P.S.", 26);
        district3.getTalukas().add(taluka26);
        HealthBlock healthBlock453 = createHealthBlock(taluka26, 453L, "Bamara", "hq");
        taluka26.getHealthBlocks().add(healthBlock453);
        HealthFacilityType facilityType = createHealthFacilityType("Garposh CHC", 41L);
        HealthFacility healthFacility41 = createHealthFacility(healthBlock453, 41L, "Garposh CHC", facilityType);
        healthBlock453.getHealthFacilities().add(healthFacility41);
        stateDataService.create(state21);
    }

    @Test
    public void testImportMotherNewSubscriber() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t" + lmpString);
        mctsBeneficiaryImportService.importMotherData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals("Shanti Ekka", subscriber.getMother().getName());
        Subscription subscription = subscriber.getActiveSubscriptions().iterator().next();
        assertEquals(SubscriptionOrigin.MCTS_IMPORT, subscription.getOrigin());
    }

    @Test
    public void testImportMotherAlternateDateFormat() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = lmp.toString("dd/MM/yyyy");
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t" + lmpString);
        mctsBeneficiaryImportService.importMotherData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
    }

    @Test
    public void testImportMotherWhoAlreadyExistsUpdateLmp() throws Exception {
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t" + lmpString);
        mctsBeneficiaryImportService.importMotherData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        assertEquals("Shanti Ekka", subscriber.getMother().getName());
        Subscription subscription = subscriber.getActiveSubscriptions().iterator().next();
        assertEquals(90, Days.daysBetween(lmp.toLocalDate(), subscription.getStartDate().toLocalDate()).getDays());

        DateTime newLmp = DateTime.now().minusDays(150);
        String newLmpString = getDateString(newLmp);
        reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t" + newLmpString);
        mctsBeneficiaryImportService.importMotherData(reader);

        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(newLmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        subscription = subscriber.getActiveSubscriptions().iterator().next();
        assertEquals(90, Days.daysBetween(newLmp.toLocalDate(), subscription.getStartDate().toLocalDate()).getDays());
    }

    @Test(expected = CsvImportDataException.class)
    public void testImportMotherInvalidState() throws Exception {
        Reader reader = createMotherDataReaderWithHeaders("9\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t22-11-2014");
        mctsBeneficiaryImportService.importMotherData(reader);
    }

    @Test
    public void testImportChildNewSubscriberNoMotherId() throws Exception {
        DateTime dob = DateTime.now().minusDays(100);
        String dobString = getDateString(dob);
        Reader reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tBaby1 of Lilima Kua\t\t9439986187\t" + dobString);
        mctsBeneficiaryImportService.importChildData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());
        assertEquals("Baby1 of Lilima Kua", subscriber.getChild().getName());
        Subscription subscription = subscriber.getActiveSubscriptions().iterator().next();
        assertEquals(SubscriptionOrigin.MCTS_IMPORT, subscription.getOrigin());
        assertEquals(SubscriptionPackType.CHILD, subscription.getSubscriptionPack().getType());
    }

    @Test
    public void testImportMotherAndChildSameMsisdn() throws Exception {
        // import mother
        DateTime lmp = DateTime.now().minusDays(100);
        String lmpString = getDateString(lmp);
        Reader reader = createMotherDataReaderWithHeaders("21\t3\t\t\t\t\t1234567890\tShanti Ekka\t9439986187\t" + lmpString);
        mctsBeneficiaryImportService.importMotherData(reader);

        Subscriber subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(lmp.toLocalDate(), subscriber.getLastMenstrualPeriod().toLocalDate());
        Subscription subscription = subscriber.getActiveSubscriptions().iterator().next();

        // import child with same MSISDN and matching MotherID
        DateTime dob = DateTime.now().minusDays(200);
        String dobString = getDateString(dob);
        reader = createChildDataReaderWithHeaders("21\t3\t\t\t\t\t9876543210\tBaby1 of Shanti Ekka\t1234567890\t9439986187\t" + dobString);
        mctsBeneficiaryImportService.importChildData(reader);

        subscriber = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber);
        assertEquals(dob.toLocalDate(), subscriber.getDateOfBirth().toLocalDate());

        Set<Subscription> subscriptions = subscriber.getActiveSubscriptions();
        assertEquals(2, subscriptions.size());

        // TODO: this passes -- but review spec to make sure this is actually the behavior we want.
        // TODO: more asserts...
    }

    @Test
    public void testImportMotherDataFromSampleFile() throws Exception {
        mctsBeneficiaryImportService.importMotherData(read("csv/mother.txt"));

        Subscriber subscriber1 = subscriberDataService.findByCallingNumber(9439986187L);
        assertNotNull(subscriber1);
    }

    private String getDateString(DateTime date) {
        return date.toString("dd-MM-yyyy");
    }

    private Reader createChildDataReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("StateID\tDistrict_ID\tTaluka_ID\tHealthBlock_ID\tPHC_ID\tVillage_ID\tID_No\tName\tMother_ID\tWhom_PhoneNo\tBirthdate");
        builder.append("\n");

        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader createMotherDataReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("StateID\tDistrict_ID\tTaluka_ID\tHealthBlock_ID\tPHC_ID\tVillage_ID\tID_No\tName\tWhom_PhoneNo\tLMP_Date");
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