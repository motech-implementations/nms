package org.motechproject.nms.testing.it.kilkari;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.MctsChildFixService;
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
import java.io.Reader;
import java.io.StringReader;
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
public class MctsChildFixServiceBundleIT extends BasePaxIT {

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
    MctsChildDataService mctsChildDataService;
    @Inject
    MctsMotherDataService mctsMotherDataService;
    @Inject
    MctsChildFixService mctsChildFixService;

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

    // Create a Subscriber with no mother and test if this updates mother in the child and respective subscriber record
    @Test
    public void testSubscriberWithNoMother() throws Exception {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        MctsChild child = mctsChildDataService.create(new MctsChild("9876543210"));
        Subscriber childSubscriber = new Subscriber(5000000000L);
        childSubscriber.setChild(child);
        DateTime dob = DateTime.now().minusDays(100);
        childSubscriber.setDateOfBirth(dob);
        childSubscriber = subscriberDataService.create(childSubscriber);
        assertNull(childSubscriber.getMother());
        transactionManager.commit(status);

        Reader reader = createDataReader("9876543210,1234567890,08-01-2016");
        mctsChildFixService.updateMotherChild(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        childSubscriber = subscriberDataService.findByNumber(5000000000L).get(0);
        assertNotNull(childSubscriber);
        assertNotNull(childSubscriber.getMother());
        assertEquals(childSubscriber.getDateOfBirth(), childSubscriber.getChild().getDateOfBirth());
        assertEquals("1234567890", childSubscriber.getMother().getBeneficiaryId());
        transactionManager.commit(status);
    }

    // Create a Subscriber with mother M1 and test if this updates mother M2 in the child and creates new subscriber record with this mother M2
    @Test
    public void testSubscriberWithDiffMother() throws Exception {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        MctsChild child = mctsChildDataService.create(new MctsChild("9876543210"));
        Subscriber childSubscriber = new Subscriber(5000000000L);
        childSubscriber.setChild(child);
        DateTime dob = DateTime.now().minusDays(100);
        childSubscriber.setDateOfBirth(dob);
        MctsMother mother = mctsMotherDataService.create(new MctsMother("1234567888"));
        childSubscriber.setMother(mother);
        childSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(120));
        childSubscriber = subscriberDataService.create(childSubscriber);
        subscriptionService.createSubscription(childSubscriber, childSubscriber.getCallingNumber(), rh.kannadaLanguage(), rh.karnatakaCircle(),
                sh.pregnancyPack(), SubscriptionOrigin.MCTS_IMPORT);
        subscriptionService.createSubscription(childSubscriber, childSubscriber.getCallingNumber(), rh.kannadaLanguage(), rh.karnatakaCircle(),
                sh.childPack(), SubscriptionOrigin.MCTS_IMPORT);
        Set<Subscription> subscriptions = childSubscriber.getActiveAndPendingSubscriptions();
        assertEquals(2, subscriptions.size());
        transactionManager.commit(status);

        //Same child with different mother
        Reader reader = createDataReader("9876543210,1234567890,08-01-2016");
        mctsChildFixService.updateMotherChild(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        List<Subscriber>  subscribers = subscriberDataService.findByNumber(5000000000L);
       assertEquals(2, subscribers.size());
        // first subscriber is of mother
        assertEquals(mother.getBeneficiaryId(), subscribers.get(0).getMother().getBeneficiaryId());
        assertNull(subscribers.get(0).getChild());
        assertNull(subscribers.get(0).getDateOfBirth());
        subscriptions = subscribers.get(0).getActiveAndPendingSubscriptions();
        assertEquals(1, subscriptions.size());
        Subscription subscription = subscriptions.iterator().next();
        assertEquals(subscribers.get(0), subscription.getSubscriber());
        assertEquals(SubscriptionPackType.PREGNANCY, subscription.getSubscriptionPack().getType());

        // second one is of child
        assertEquals("1234567890", subscribers.get(1).getMother().getBeneficiaryId());
        assertEquals("9876543210", subscribers.get(1).getChild().getBeneficiaryId());
        assertNotNull(childSubscriber.getMother());
        assertEquals(dob.toLocalDate(), subscribers.get(1).getChild().getDateOfBirth().toLocalDate());
        subscriptions = subscribers.get(1).getActiveAndPendingSubscriptions();
        assertEquals(1, subscriptions.size());
        subscription = subscriptions.iterator().next();
        assertEquals(subscribers.get(1), subscription.getSubscriber());
        assertEquals(SubscriptionPackType.CHILD, subscription.getSubscriptionPack().getType());
        assertEquals(dob.toLocalDate(), subscribers.get(1).getDateOfBirth().toLocalDate());
        assertEquals(dob.toLocalDate(), subscribers.get(1).getChild().getDateOfBirth().toLocalDate());
        transactionManager.commit(status);
    }

    // Case where subscriber is purged. Check if dob is copied to child
    @Test
    public void testChildWithNoSubscriber() throws Exception {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        MctsChild child = mctsChildDataService.create(new MctsChild("9876543210"));
        assertNull(child.getDateOfBirth());
        transactionManager.commit(status);

        Reader reader = createDataReader("9876543210,1234567890,08-01-2016");
        mctsChildFixService.updateMotherChild(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        List<Subscriber>  subscribers = subscriberDataService.findByNumber(5000000000L);
        assertTrue(subscribers.isEmpty());
        child = mctsChildDataService.findByBeneficiaryId("9876543210");
        assertEquals("08-01-2016", child.getDateOfBirth().toLocalDate().toString());
        assertEquals("1234567890", child.getMother().getBeneficiaryId());
        transactionManager.commit(status);
    }

    private Reader createDataReader(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("ID_No,Mother_ID,Birthdate");
        builder.append("\n");

        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    // Case where mother is null in csv. Should update dob in child
    @Test
    public void testChildWithNoMotherInCsv() throws Exception {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        MctsChild child = mctsChildDataService.create(new MctsChild("9876543210"));
        assertNull(child.getDateOfBirth());
        transactionManager.commit(status);

        // Case where subscriber purged
        Reader reader = createDataReader("9876543210,,08-01-2016");
        mctsChildFixService.updateMotherChild(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        List<Subscriber>  subscribers = subscriberDataService.findByNumber(5000000000L);
        assertTrue(subscribers.isEmpty());
        child = mctsChildDataService.findByBeneficiaryId("9876543210");
        assertNull(child.getMother());
        assertEquals("08-01-2016", child.getDateOfBirth().toLocalDate().toString());
        transactionManager.commit(status);

        // Create a subscriber and check  if dob in child is that of subscriber
        Subscriber childSubscriber = new Subscriber(5000000000L);
        childSubscriber.setChild(child);
        DateTime dob = DateTime.now().minusDays(100);
        childSubscriber.setDateOfBirth(dob);
        childSubscriber = subscriberDataService.create(childSubscriber);

        reader = createDataReader("9876543210,,08-01-2016");
        mctsChildFixService.updateMotherChild(reader);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscribers = subscriberDataService.findByNumber(5000000000L);
        assertNotNull(subscribers.get(0));
        assertEquals(dob.toLocalDate(), subscribers.get(0).getDateOfBirth().toLocalDate());
        assertEquals(dob.toLocalDate(), subscribers.get(0).getChild().getDateOfBirth().toLocalDate());
        transactionManager.commit(status);
    }
}
