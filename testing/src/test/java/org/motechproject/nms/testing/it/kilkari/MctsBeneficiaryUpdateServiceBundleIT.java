package org.motechproject.nms.testing.it.kilkari;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.*;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryUpdateService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.domain.*;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.motechproject.nms.testing.it.utils.RegionHelper.*;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createHealthSubFacilityType;
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


    private void createLocationData() {
        // specific locations from the mother and child data files:

        State state21 = createState(21L, "State 21");
        District district2 = createDistrict(state21, 2L, "Jharsuguda", new Language("21", "English"));
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

        HealthSubFacility subFacilityType7389 = createHealthSubFacilityType("Babuniktimal", 7389L, healthFacility41);
        healthFacility41.getHealthSubFacilities().add(subFacilityType7389);

        HealthSubFacility subFacilityType7393 = createHealthSubFacilityType("Jarabaga", 7393L, healthFacility41);
        healthFacility41.getHealthSubFacilities().add(subFacilityType7393);

        HealthSubFacility subFacilityType2104 = createHealthSubFacilityType("Chupacabra", 2104L, healthFacility635);
        healthFacility635.getHealthSubFacilities().add(subFacilityType2104);

        HealthSubFacility subFacilityType342 = createHealthSubFacilityType("El Dorado", 342L, healthFacility114);
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
        Long oldMsisdn = subscriptionHelper.makeNumber();
        Long newMsisdn = subscriptionHelper.makeNumber();

        Subscription subscription = subscriptionHelper.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),
                SubscriptionPackType.CHILD, oldMsisdn);
        String mctsId = "0123456789";

        subscription.getSubscriber().setChild(new MctsChild(mctsId));
        subscriberDataService.update(subscription.getSubscriber());

        Reader reader = createUpdateReaderWithHeaders("1," + mctsId + ",,,,,,,,,,,,," + newMsisdn);
        mctsBeneficiaryUpdateService.updateBeneficiary(reader);

        Subscriber oldSubscriber = subscriberDataService.findByCallingNumber(oldMsisdn);
        assertNull(oldSubscriber.getChild());
        assertEquals(0, oldSubscriber.getActiveAndPendingSubscriptions().size());

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
        assertNull(pregnancySubscriber.getChild());
        assertNull(childSubscriber.getMother());
        assertEquals(1, pregnancySubscriber.getActiveAndPendingSubscriptions().size());
        assertEquals(1, childSubscriber.getActiveAndPendingSubscriptions().size());
    }

    @Test
    public void testUpdateMsisdnNumberAlreadyInUse() throws Exception {
        Long firstMsisdn = subscriptionHelper.makeNumber();
        Long secondMsisdn = subscriptionHelper.makeNumber();

        // create two child subscriptions with different MSISDNs
        Subscription firstSubscription = subscriptionHelper.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),
                SubscriptionPackType.CHILD, firstMsisdn);
        String firstChildId = "0123456789";
        firstSubscription.getSubscriber().setChild(new MctsChild(firstChildId));
        subscriberDataService.update(firstSubscription.getSubscriber());

        Subscription secondSubscription = subscriptionHelper.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),
                SubscriptionPackType.CHILD, secondMsisdn);
        String secondChildId = "9123456789";
        secondSubscription.getSubscriber().setChild(new MctsChild(secondChildId));
        subscriberDataService.update(secondSubscription.getSubscriber());

        // try to set the second child's MSISDN to the same number as the first child's MSISDN
        Reader reader = createUpdateReaderWithHeaders("1," + secondChildId + ",,,,,,,,,,,,," + firstMsisdn);
        mctsBeneficiaryUpdateService.updateBeneficiary(reader);

        List<SubscriptionError> errors = subscriptionErrorDataService.findByContactNumber(firstMsisdn);
        assertEquals(1, errors.size());
    }

    @Test
    public void testUpdateDOB() throws Exception {
        createLocationData();

        Long msisdn = subscriptionHelper.makeNumber();
        String childId = "0123456789";
        DateTime originalDOB = DateTime.now();
        DateTime updatedDOB = originalDOB.minusDays(100);

        subscriptionHelper.mksub(SubscriptionOrigin.MCTS_IMPORT, originalDOB, SubscriptionPackType.CHILD, msisdn);
        Subscriber subscriber = subscriberDataService.findByCallingNumber(msisdn);
        subscriber.setDateOfBirth(originalDOB);
        MctsChild child = new MctsChild(childId);
        child.setDistrict(stateDataService.findByCode(21L).getDistricts().get(0));
        subscriber.setChild(child);
        subscriberDataService.update(subscriber);

        Reader reader = createUpdateReaderWithHeaders("1," + childId + ",," + getDateString(updatedDOB) + ",,,,,,,,,,,");
        mctsBeneficiaryUpdateService.updateBeneficiary(reader);

        Subscriber updatedSubscriber = subscriberDataService.findByCallingNumber(msisdn);
        assertEquals(getDateString(updatedDOB), getDateString(updatedSubscriber.getDateOfBirth()));
        Subscription updatedSubscription = updatedSubscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(getDateString(updatedDOB), getDateString(updatedSubscription.getStartDate()));
        assertEquals(SubscriptionStatus.ACTIVE, updatedSubscription.getStatus());
    }

    @Test
    public void testUpdateLMP() throws Exception {
        createLocationData();

        Long msisdn = subscriptionHelper.makeNumber();
        String motherId = "0123456789";
        DateTime originalLMP = DateTime.now().minusDays(100);
        DateTime updatedLMP = originalLMP.minusDays(200);

        subscriptionHelper.mksub(SubscriptionOrigin.MCTS_IMPORT, originalLMP, SubscriptionPackType.PREGNANCY, msisdn);
        Subscriber subscriber = subscriberDataService.findByCallingNumber(msisdn);
        subscriber.setLastMenstrualPeriod(originalLMP);
        MctsMother mother = new MctsMother(motherId);
        mother.setDistrict(stateDataService.findByCode(21L).getDistricts().get(0));
        subscriber.setMother(mother);
        subscriberDataService.update(subscriber);

        Reader reader = createUpdateReaderWithHeaders("1," + motherId + ",,," + getDateString(updatedLMP) + ",,,,,,,,,,");
        mctsBeneficiaryUpdateService.updateBeneficiary(reader);

        Subscriber updatedSubscriber = subscriberDataService.findByCallingNumber(msisdn);
        assertEquals(getDateString(updatedLMP), getDateString(updatedSubscriber.getLastMenstrualPeriod()));
        Subscription updatedSubscription = updatedSubscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(getDateString(updatedLMP.plusDays(90)), getDateString(updatedSubscription.getStartDate()));
        assertEquals(SubscriptionStatus.ACTIVE, updatedSubscription.getStatus());
    }

    @Test
    public void testUpdateLMPAndReactivateCompletedSubscription() throws Exception {
        createLocationData();

        Long msisdn = subscriptionHelper.makeNumber();
        String motherId = "0123456789";
        DateTime originalLMP = DateTime.now().minusDays(100);
        DateTime updatedLMP = originalLMP.minusDays(100);

        subscriptionHelper.mksub(SubscriptionOrigin.MCTS_IMPORT, originalLMP, SubscriptionPackType.PREGNANCY, msisdn);
        Subscriber subscriber = subscriberDataService.findByCallingNumber(msisdn);
        subscriber.setLastMenstrualPeriod(originalLMP);
        MctsMother mother = new MctsMother(motherId);
        mother.setDistrict(stateDataService.findByCode(21L).getDistricts().get(0));
        subscriber.setMother(mother);
        subscriberDataService.update(subscriber);

        // pre-date the LMP so that the subscription will be marked completed
        subscriber.setLastMenstrualPeriod(originalLMP.minusDays(600));
        subscriberService.update(subscriber);
        subscriber = subscriberDataService.findByCallingNumber(msisdn);
        Subscription subscription = subscriber.getAllSubscriptions().iterator().next();
        assertEquals(SubscriptionStatus.COMPLETED, subscription.getStatus());

        // now, via CSV update, change the LMP to a valid subscription date; subscription should get reactivated
        Reader reader = createUpdateReaderWithHeaders("1," + motherId + ",,," + getDateString(updatedLMP) + ",,,,,,,,,,");
        mctsBeneficiaryUpdateService.updateBeneficiary(reader);

        Subscriber updatedSubscriber = subscriberDataService.findByCallingNumber(msisdn);
        assertEquals(getDateString(updatedLMP), getDateString(updatedSubscriber.getLastMenstrualPeriod()));
        Subscription updatedSubscription = updatedSubscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(getDateString(updatedLMP.plusDays(90)), getDateString(updatedSubscription.getStartDate()));
        assertEquals(SubscriptionStatus.ACTIVE, updatedSubscription.getStatus());
    }

    @Test
    public void testUpdateDOBAndCompleteActiveSubscription() throws Exception {
        createLocationData();

        Long msisdn = subscriptionHelper.makeNumber();
        String childId = "0123456789";
        DateTime originalDOB = DateTime.now().minusDays(100);
        DateTime updatedDOB = originalDOB.minusDays(400);

        subscriptionHelper.mksub(SubscriptionOrigin.MCTS_IMPORT, originalDOB, SubscriptionPackType.CHILD, msisdn);
        Subscriber subscriber = subscriberDataService.findByCallingNumber(msisdn);
        subscriber.setDateOfBirth(originalDOB);
        MctsChild child = new MctsChild(childId);
        child.setDistrict(stateDataService.findByCode(21L).getDistricts().get(0));
        subscriber.setChild(child);
        subscriberDataService.update(subscriber);

        // verify that the subscription is active
        subscriber = subscriberDataService.findByCallingNumber(msisdn);
        Subscription subscription = subscriber.getAllSubscriptions().iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        // now, via CSV update, change the DOB to a past subscription date; subscription should be marked completed
        Reader reader = createUpdateReaderWithHeaders("1," + childId + ",," + getDateString(updatedDOB) + ",,,,,,,,,,,");
        mctsBeneficiaryUpdateService.updateBeneficiary(reader);

        Subscriber updatedSubscriber = subscriberDataService.findByCallingNumber(msisdn);
        assertEquals(getDateString(updatedDOB), getDateString(updatedSubscriber.getDateOfBirth()));
        Subscription updatedSubscription = updatedSubscriber.getAllSubscriptions().iterator().next();
        assertEquals(getDateString(updatedDOB), getDateString(updatedSubscription.getStartDate()));
        assertEquals(SubscriptionStatus.COMPLETED, updatedSubscription.getStatus());
    }

    @Test
    public void testUpdateLocation() throws Exception {
        createLocationData();

        Long msisdn = subscriptionHelper.makeNumber();
        String childId = "0123456789";

        subscriptionHelper.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(100),
                SubscriptionPackType.CHILD, msisdn);
        Subscriber subscriber = subscriberDataService.findByCallingNumber(msisdn);
        MctsChild child = new MctsChild(childId);
        child.setState(stateDataService.findByCode(21L));
        child.setDistrict(child.getState().getDistricts().get(0));
        subscriber.setChild(child);
        subscriberDataService.update(subscriber);

        Reader reader = createUpdateReaderWithHeaders("1," + childId + ",,,,21,3,0026,453,,,,,,");
        mctsBeneficiaryUpdateService.updateBeneficiary(reader);

        MctsChild updatedChild = mctsChildDataService.findByBeneficiaryId(childId);
        assertEquals(21L, (long) updatedChild.getState().getCode());
        assertEquals(3L, (long) updatedChild.getDistrict().getCode());
        assertEquals("0026", updatedChild.getTaluka().getCode());
        assertEquals(453L, (long) updatedChild.getHealthBlock().getCode());
    }

    @Test
    public void testUpdateLocationInvalid() throws Exception {
        createLocationData();

        Long msisdn = subscriptionHelper.makeNumber();
        String childId = "0123456789";

        subscriptionHelper.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(100),
                SubscriptionPackType.CHILD, msisdn);
        Subscriber subscriber = subscriberDataService.findByCallingNumber(msisdn);
        MctsChild child = new MctsChild(childId);
        child.setState(stateDataService.findByCode(21L));
        child.setDistrict(child.getState().getDistricts().get(0));
        subscriber.setChild(child);
        subscriberDataService.update(subscriber);

        Reader reader = createUpdateReaderWithHeaders("1," + childId + ",,,,21,3,0026,453,,,,,,");
        mctsBeneficiaryUpdateService.updateBeneficiary(reader);
    }

    @Test
    @Ignore
    public void testUpdateBeneficiariesFromFile() throws Exception {
        mctsBeneficiaryUpdateService.updateBeneficiary(read("csv/mcts_beneficiary_update.csv"));
    }

    private Reader createUpdateReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("Sr No,MCTS ID,STATE ID,Beneficiary New DOB change,Beneficiary New LMP change,StateID,"); // 6
        builder.append("District_ID,Taluka_ID,HealthBlock_ID,PHC_ID,SubCentre_ID,Village_ID,GP_Village,Address,"); // +8
        builder.append("Beneficiary New Mobile no change"); // +1
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

}
