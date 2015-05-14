package org.motechproject.nms.kilkari.osgi;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.InboxCallData;
import org.motechproject.nms.kilkari.domain.InboxCallDetailRecord;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.InboxCallDataDataService;
import org.motechproject.nms.kilkari.repository.InboxCallDetailRecordDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackMessageDataService;
import org.motechproject.nms.kilkari.service.InboxService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Verify that SubscriptionService is present & functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class SubscriptionServiceBundleIT extends BasePaxIT {

    @Inject
    private SubscriberService subscriberService;
    @Inject
    private SubscriptionService subscriptionService;
    @Inject
    private InboxService inboxService;
    @Inject
    private SubscriberDataService subscriberDataService;
    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    private SubscriptionPackMessageDataService subscriptionPackMessageDataService;
    @Inject
    private SubscriptionDataService subscriptionDataService;
    @Inject
    private LanguageDataService languageDataService;
    @Inject
    private InboxCallDetailRecordDataService inboxCallDetailRecordDataService;
    @Inject
    private InboxCallDataDataService inboxCallDataDataService;
    @Inject
    private LanguageLocationDataService languageLocationDataService;
    @Inject
    private StateDataService stateDataService;
    @Inject
    private DistrictDataService districtDataService;
    @Inject
    private CircleDataService circleDataService;

    private LanguageLocation gLanguageLocation;
    private SubscriptionPack gPack1;
    private SubscriptionPack gPack2;


    private void setupData() {
        cleanupData();
        createLanguageAndSubscriptionPacks();

        Subscriber subscriber1 = subscriberDataService.create(new Subscriber(1000000000L));
        Subscriber subscriber2 = subscriberDataService.create(new Subscriber(2000000000L));

        subscriptionService.createSubscription(subscriber1.getCallingNumber(), gLanguageLocation, gPack1,
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), gLanguageLocation, gPack1,
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), gLanguageLocation, gPack2,
                SubscriptionOrigin.IVR);
    }


    private void createLanguageAndSubscriptionPacks() {
        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);

        District district2 = new District();
        district2.setName("District 2");
        district2.setRegionalName("District 2");
        district2.setCode(2L);

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);
        state.getDistricts().add(district2);

        stateDataService.create(state);

        Language language = new Language("tamil");
        languageDataService.create(language);

        LanguageLocation languageLocation = new LanguageLocation("10", new Circle("AA"), language, true);
        languageLocation.getDistrictSet().add(district);
        languageLocationDataService.create(languageLocation);

        language = new Language("english");
        languageDataService.create(language);

        languageLocation = new LanguageLocation("99", new Circle("BB"), language, true);
        languageLocation.getDistrictSet().add(district2);
        languageLocationDataService.create(languageLocation);

        subscriptionService.createSubscriptionPacks();
        gPack1 = subscriptionPackDataService.byName("childPack"); // 48 weeks, 1 message per week
        gPack2 = subscriptionPackDataService.byName("pregnancyPack"); // 72 weeks, 2 messages per week
    }

    private void cleanupData() {
        subscriptionDataService.deleteAll();
        subscriptionPackDataService.deleteAll();
        subscriptionPackMessageDataService.deleteAll();
        subscriberDataService.deleteAll();
        circleDataService.deleteAll();
        districtDataService.deleteAll();
        stateDataService.deleteAll();
        languageLocationDataService.deleteAll();
        languageDataService.deleteAll();
        inboxCallDataDataService.deleteAll();
        inboxCallDetailRecordDataService.deleteAll();
    }

    @Test
    public void testServicePresent() throws Exception {
        assertNotNull(subscriptionService);
    }

    @Test
    public void testServiceFunctional() throws Exception {
        cleanupData();
        createLanguageAndSubscriptionPacks();

        LanguageLocation languageLocation = languageLocationDataService.findByCode("10");
        Subscriber subscriber = new Subscriber(1000000000L, languageLocation);
        subscriberService.create(subscriber);

        SubscriptionPack pack1 = subscriptionPackDataService.byName("pack1");
        SubscriptionPack pack2 = subscriptionPackDataService.byName("pack2");
        subscriptionService.createSubscription(subscriber.getCallingNumber(), languageLocation, gPack1,
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber.getCallingNumber(), languageLocation, gPack2,
                SubscriptionOrigin.IVR);

        subscriber = subscriberService.getSubscriber(1000000000L);
        Set<Subscription> subscriptions = subscriber.getSubscriptions();

        Set<String> packs = new HashSet<>();
        for (Subscription subscription : subscriptions) {
            packs.add(subscription.getSubscriptionPack().getName());
        }

        assertEquals(new HashSet<>(Arrays.asList("childPack", "pregnancyPack")), packs);

        long id = inboxService.addInboxCallDetails(new InboxCallDetailRecord(
                1111111111L,
                "OP",
                "AA",
                123456789012345L,
                new DateTime(123L),
                new DateTime(456L),
                1,
                1,
                1,
                new HashSet<>(Arrays.asList(
                        new InboxCallData(
                                UUID.randomUUID().toString(),
                                "48WeeksPack",
                                "xx",
                                "foo.wav",
                                new DateTime(100L),
                                new DateTime(200L)
                        ),
                        new InboxCallData(
                                UUID.randomUUID().toString(),
                                "76WeeksPack",
                                "xx",
                                "bar.wav",
                                new DateTime(300L),
                                new DateTime(400L)
                        )
                ))
        ));

        InboxCallDetailRecord inboxCallDetailRecordFromDatabase = inboxCallDetailRecordDataService.findById(id);


        assertEquals(1111111111L, (long) inboxCallDetailRecordFromDatabase.getCallingNumber());
    }

    @Test
    public void testSubscriptionPackCreation() throws Exception {
        cleanupData();
        subscriptionService.createSubscriptionPacks();

        SubscriptionPack fortyEightWeekPack = subscriptionPackDataService.byName("childPack");
        assertEquals(48, fortyEightWeekPack.getMessages().size());

        SubscriptionPack seventyTwoWeekPack = subscriptionPackDataService.byName("pregnancyPack");
        assertEquals(144, seventyTwoWeekPack.getMessages().size());
    }


    @Test
    public void testCreateSubscriptionNoSubscriber() throws Exception {
        cleanupData();
        createLanguageAndSubscriptionPacks();

        LanguageLocation languageLocation = languageLocationDataService.findByCode("10");

        // Just verify the db is clean
        Subscriber s = subscriberService.getSubscriber(1111111111L);
        assertNull(s);

        subscriptionService.createSubscription(1111111111L, languageLocation, gPack1, SubscriptionOrigin.IVR);

        Subscriber subscriber = subscriberService.getSubscriber(1111111111L);
        assertNotNull(subscriber);
        assertEquals(languageLocation, subscriber.getLanguageLocation());
        assertEquals(1, subscriber.getSubscriptions().size());

        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertEquals(gPack1, subscription.getSubscriptionPack());
    }

    @Test
    public void testCreateSubscriptionExistingSubscriberDifferentLanguage() throws Exception {
        cleanupData();
        createLanguageAndSubscriptionPacks();

        LanguageLocation ta = languageLocationDataService.findByCode("10");
        LanguageLocation en = languageLocationDataService.findByCode("99");

        // Just verify the db is clean
        Subscriber s = subscriberService.getSubscriber(1111111111L);
        assertNull(s);

        subscriptionService.createSubscription(1111111111L, ta, gPack1, SubscriptionOrigin.IVR);

        // Since the user exists we will not change their language
        subscriptionService.createSubscription(1111111111L, en, gPack1, SubscriptionOrigin.IVR);

        Subscriber subscriber = subscriberService.getSubscriber(1111111111L);
        assertNotNull(subscriber);
        assertEquals(ta, subscriber.getLanguageLocation());
        assertEquals(1, subscriber.getSubscriptions().size());

        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertEquals(gPack1, subscription.getSubscriptionPack());
    }

    @Test
    public void testCreateDuplicateChildSubscriptionViaMcts() {
        setupData();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now().minusDays(14));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, gLanguageLocation, gPack1, SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());

        // attempt to create subscription to the same pack -- should fail
        subscriptionService.createSubscription(9999911122L, gLanguageLocation, gPack1, SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());
    }

    @Test
    public void testCreateDuplicatePregnancySubscriptionViaMcts() {
        setupData();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(28));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, gLanguageLocation, gPack2, SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());

        // attempt to create subscription to the same pack -- should fail
        subscriptionService.createSubscription(9999911122L, gLanguageLocation, gPack2, SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());
    }

    @Test
    public void testCreateSecondPregnancySubscriptionAfterDeactivationViaMcts() {
        setupData();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(28));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, gLanguageLocation, gPack2, SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        Subscription pregnancySubscription = mctsSubscriber.getActiveSubscriptions().iterator().next();
        pregnancySubscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscriptionDataService.update(pregnancySubscription);

        // attempt to create subscription to the same pack -- should succeed
        subscriptionService.createSubscription(9999911122L, gLanguageLocation, gPack2, SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());
    }

    @Test
    public void testCreateSubscriptionsToDifferentPacksViaMcts() {
        setupData();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now().minusDays(14));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, gLanguageLocation, gPack1, SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        // due to subscription rules detailed in #157, we need to clear out the DOB and set an LMP in order to
        // create a second subscription for this MCTS subscriber
        mctsSubscriber.setDateOfBirth(null);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(100));
        subscriberDataService.update(mctsSubscriber);

        // attempt to create subscription to a different pack
        subscriptionService.createSubscription(9999911122L, gLanguageLocation, gPack2, SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(2, mctsSubscriber.getActiveSubscriptions().size());
    }

    @Test
    public void testChangeDOB() {
        setupData();
        DateTime now = DateTime.now();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(now.minusDays(14));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, gLanguageLocation, gPack1, SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        Subscription subscription = mctsSubscriber.getSubscriptions().iterator().next();

        assertEquals(now.minusDays(14), subscription.getStartDate());
        assert(subscription.getStatus() == SubscriptionStatus.ACTIVE);

        mctsSubscriber.setDateOfBirth(now.minusDays(100));
        subscriberService.update(mctsSubscriber);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        subscription = mctsSubscriber.getSubscriptions().iterator().next();

        assertEquals(now.minusDays(100), subscription.getStartDate());
        assert(subscription.getStatus() == SubscriptionStatus.ACTIVE);
    }

    @Test
    public void testChangeLMP() {
        setupData();
        DateTime now = DateTime.now();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(now.minusDays(180));

        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, gLanguageLocation, gPack2, SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        Subscription subscription = mctsSubscriber.getSubscriptions().iterator().next();

        assertEquals(now.minusDays(90), subscription.getStartDate());
        assert(subscription.getStatus() == SubscriptionStatus.ACTIVE);

        mctsSubscriber.setLastMenstrualPeriod(now.minusDays(270));
        subscriberService.update(mctsSubscriber);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        subscription = mctsSubscriber.getSubscriptions().iterator().next();

        assertEquals(now.minusDays(180), subscription.getStartDate());
        assert(subscription.getStatus() == SubscriptionStatus.ACTIVE);

        mctsSubscriber.setLastMenstrualPeriod(now.minusDays(1000));
        subscriberService.update(mctsSubscriber);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        subscription = mctsSubscriber.getSubscriptions().iterator().next();

        assertEquals(now.minusDays(910), subscription.getStartDate());
        assert(subscription.getStatus() == SubscriptionStatus.COMPLETED);
    }

    @Test
    public void testGetNextMessageForSubscription() {
        setupData();
        DateTime now = DateTime.now();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(now.minusDays(90)); //so the startDate should be today
        subscriberDataService.create(mctsSubscriber);
        subscriptionService.createSubscription(9999911122L, gLanguageLocation, gPack2,
                SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        Subscription subscription = mctsSubscriber.getSubscriptions().iterator().next();

        // initially, the welcome message should be played
        SubscriptionPackMessage message = subscription.nextScheduledMessage(now);
        assertEquals("welcome", message.getWeekId());

        subscription.setNeedsWelcomeMessage(false);
        subscriptionDataService.update(subscription);

        message = subscription.nextScheduledMessage(now.plusDays(8)); //one week and a day
        assertEquals("w2_1", message.getWeekId());

        message = subscription.nextScheduledMessage(now.plusDays(75));
        assertEquals("w11_2", message.getWeekId());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetNextMessageForCompletedSubscription() {
        setupData();
        DateTime now = DateTime.now();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(now.minusDays(90));
        subscriberDataService.create(mctsSubscriber);
        subscriptionService.createSubscription(9999911122L, gLanguageLocation, gPack2, SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        Subscription subscription = mctsSubscriber.getSubscriptions().iterator().next();
        subscription.setNeedsWelcomeMessage(false);
        subscriptionDataService.update(subscription);

        // should throw IllegalStateException
        SubscriptionPackMessage message = subscription.nextScheduledMessage(now.plusDays(1000));
    }

    @Test
    public void testActiveSubscriptionsForDay() {
        setupData();
        DateTime startDate = DateTime.now().minusDays((int) (Math.random() * 100));
        DayOfTheWeek startDay = DayOfTheWeek.fromInt(startDate.getDayOfWeek());

        Subscriber subscriber = subscriberDataService.create(new Subscriber(1111111111L));
        Subscription subscription = new Subscription(subscriber, gPack1, SubscriptionOrigin.IVR);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(startDate);
        subscriptionDataService.create(subscription);

        List<Subscription> subscriptions = subscriptionService.findActiveSubscriptionsForDay(startDay, 1, 10);
        assertTrue(subscriptions.size() > 0);
        for (Subscription s : subscriptions) {
            if (s.getSubscriber().getCallingNumber() == 1111111111L) {
                return;
            }
        }
        throw new IllegalStateException("Couldn't find our subscription by its start day!");
    }


    // NMS shall allow a subscriber deactivated due to DND restrictions to activate the Kilkari service again via IVR.
    @Test
    public void verifyIssue182() {
        setupData();

        Subscriber subscriber = new Subscriber(4444444444L);
        subscriber.setLastMenstrualPeriod(DateTime.now().minusDays(90));
        subscriber = subscriberDataService.create(subscriber);

        // Make a deactivated subscription
        Subscription subscription = subscriptionService.createSubscription(4444444444L, gLanguageLocation, gPack2,
                SubscriptionOrigin.MCTS_IMPORT);
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.DO_NOT_DISTURB);

        // Now mimick a subscriber calling
        subscription = subscriptionService.createSubscription(4444444444L, gLanguageLocation, gPack2, SubscriptionOrigin.IVR);

        // And check the subscription is now active
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
    }
}
