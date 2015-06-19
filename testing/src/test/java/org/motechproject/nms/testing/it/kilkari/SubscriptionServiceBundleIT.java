package org.motechproject.nms.testing.it.kilkari;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.motechproject.event.MotechEvent;
import org.motechproject.mds.ex.JdoListenerInvocationException;
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
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.testing.it.utils.RegionHelper;
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

/**
 * Verify that SubscriptionService is present & functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class SubscriptionServiceBundleIT extends BasePaxIT {

    @Inject
    SubscriberService subscriberService;
    @Inject
    SubscriptionService subscriptionService;
    @Inject
    InboxService inboxService;
    @Inject
    SubscriberDataService subscriberDataService;
    @Inject
    SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    SubscriptionPackMessageDataService subscriptionPackMessageDataService;
    @Inject
    SubscriptionDataService subscriptionDataService;
    @Inject
    LanguageDataService languageDataService;
    @Inject
    InboxCallDetailRecordDataService inboxCallDetailRecordDataService;
    @Inject
    InboxCallDataDataService inboxCallDataDataService;
    @Inject
    StateDataService stateDataService;
    @Inject
    DistrictDataService districtDataService;
    @Inject
    DistrictService districtService;
    @Inject
    CircleDataService circleDataService;
    @Inject
    TestingService testingService;


    private RegionHelper rh;
    private SubscriptionHelper sh;


    @Before
    public void setupTestData() {
        testingService.clearDatabase();

        rh = new RegionHelper(languageDataService, circleDataService, stateDataService, districtDataService,
                districtService);

        sh = new SubscriptionHelper(subscriptionService, subscriberDataService, subscriptionPackDataService,
                languageDataService, circleDataService, stateDataService, districtDataService, districtService);

        Subscriber subscriber1 = subscriberDataService.create(new Subscriber(1000000000L));
        subscriptionService.createSubscription(subscriber1.getCallingNumber(), rh.hindiLanguage(),
                sh.childPack(), SubscriptionOrigin.IVR);

        Subscriber subscriber2 = subscriberDataService.create(new Subscriber(2000000000L));
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), rh.hindiLanguage(),
                sh.childPack(), SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), rh.hindiLanguage(),
                sh.pregnancyPack(), SubscriptionOrigin.IVR);
    }


    @Test
    public void testServicePresent() throws Exception {
        assertNotNull(subscriptionService);
    }

    
    @Test
    public void testPurgeOldClosedSubscriptionsNothingToPurge() {

        // s1 & s2 should remain untouched
        subscriptionService.createSubscription(1000000000L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);

        Subscriber subscriber = subscriberService.getSubscriber(1000000000L);
        assertNotNull(subscriber);
        Set<Subscription> subscriptions = subscriber.getSubscriptions();
        assertEquals(1, subscriptions.size());


        Subscriber s2 = new Subscriber(1000000001L, rh.hindiLanguage());
        subscriberService.create(s2);

        subscriptionService.createSubscription(s2.getCallingNumber(), rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(s2.getCallingNumber(), rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.IVR);

        subscriber = subscriberService.getSubscriber(1000000001L);
        assertNotNull(subscriber);
        subscriptions = subscriber.getSubscriptions();
        assertEquals(2, subscriptions.size());

        subscriptionService.purgeOldInvalidSubscriptions(new MotechEvent());

        subscriber = subscriberService.getSubscriber(1000000000L);
        assertNotNull(subscriber);
        subscriptions = subscriber.getSubscriptions();
        assertEquals(1, subscriptions.size());

        subscriber = subscriberService.getSubscriber(1000000001L);
        assertNotNull(subscriber);
        subscriptions = subscriber.getSubscriptions();
        assertEquals(2, subscriptions.size());
    }


    @Test
    public void testPurgeOldClosedSubscriptionsSubscribersDeleted() {

        Subscriber subscriber;
        Set<Subscription> subscriptions;

        // s3 & s4 should be deleted
        Subscriber s3 = new Subscriber(1000000002L, rh.hindiLanguage());
        subscriberService.create(s3);

        subscriptionService.createSubscription(s3.getCallingNumber(), rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);

        s3 = subscriberService.getSubscriber(1000000002L);
        Subscription subscription = s3.getSubscriptions().iterator().next();
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscription.setEndDate(new DateTime().withDate(2011, 8, 1));
        subscriptionDataService.update(subscription);

        subscriber = subscriberService.getSubscriber(1000000002L);
        assertNotNull(subscriber);
        subscriptions = subscriber.getSubscriptions();
        assertEquals(1, subscriptions.size());


        Subscriber s4 = new Subscriber(1000000003L, rh.hindiLanguage());
        subscriberService.create(s4);

        subscriptionService.createSubscription(s4.getCallingNumber(), rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(s4.getCallingNumber(), rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.IVR);

        s4 = subscriberService.getSubscriber(1000000003L);
        Iterator<Subscription> subscriptionIterator = s4.getSubscriptions().iterator();
        subscription = subscriptionIterator.next();
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscription.setEndDate(new DateTime().withDate(2011, 8, 1));
        subscriptionDataService.update(subscription);
        subscription = subscriptionIterator.next();
        subscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscription.setEndDate(new DateTime().withDate(2011, 8, 1));
        subscriptionDataService.update(subscription);

        subscriber = subscriberService.getSubscriber(1000000003L);
        assertNotNull(subscriber);
        subscriptions = subscriber.getSubscriptions();
        assertEquals(2, subscriptions.size());

        subscriptionService.purgeOldInvalidSubscriptions(new MotechEvent());

        subscriber = subscriberService.getSubscriber(1000000002L);
        assertNull(subscriber);

        subscriber = subscriberService.getSubscriber(1000000003L);
        assertNull(subscriber);
    }


    @Test
    public void testPurgeOldClosedSubscriptionsRemoveSubscriptionLeaveSubscriber() {

        Subscriber subscriber;
        Set<Subscription> subscriptions;
        Iterator<Subscription> subscriptionIterator;
        Subscription subscription;

        // s5 & s6 should remain but with one less subscription
        Subscriber s5 = new Subscriber(1000000004L, rh.hindiLanguage());
        subscriberService.create(s5);

        subscriptionService.createSubscription(s5.getCallingNumber(), rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(s5.getCallingNumber(), rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.IVR);
        s5 = subscriberService.getSubscriber(1000000004L);
        subscriptionIterator = s5.getSubscriptions().iterator();
        subscription = subscriptionIterator.next();
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscription.setEndDate(new DateTime().withDate(2011, 8, 1));
        subscriptionDataService.update(subscription);

        subscriber = subscriberService.getSubscriber(1000000004L);
        assertNotNull(subscriber);
        subscriptions = subscriber.getSubscriptions();
        assertEquals(2, subscriptions.size());

        Subscriber s6 = new Subscriber(1000000005L, rh.hindiLanguage());
        subscriberService.create(s6);

        subscriptionService.createSubscription(s6.getCallingNumber(), rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(s6.getCallingNumber(), rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.IVR);
        s6 = subscriberService.getSubscriber(1000000005L);
        subscriptionIterator = s6.getSubscriptions().iterator();
        subscription = subscriptionIterator.next();
        subscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscription.setEndDate(new DateTime().withDate(2011, 8, 1));
        subscriptionDataService.update(subscription);

        subscriber = subscriberService.getSubscriber(1000000005L);
        assertNotNull(subscriber);
        subscriptions = subscriber.getSubscriptions();
        assertEquals(2, subscriptions.size());

        subscriptionService.purgeOldInvalidSubscriptions(new MotechEvent());

        subscriber = subscriberService.getSubscriber(1000000004L);
        assertNotNull(subscriber);
        subscriptions = subscriber.getSubscriptions();
        assertEquals(1, subscriptions.size());

        subscriber = subscriberService.getSubscriber(1000000005L);
        assertNotNull(subscriber);
        subscriptions = subscriber.getSubscriptions();
        assertEquals(1, subscriptions.size());
    }


    @Test
    public void testServiceFunctional() throws Exception {

        SubscriptionPack pack1 = subscriptionPackDataService.byName("pack1");
        SubscriptionPack pack2 = subscriptionPackDataService.byName("pack2");
        subscriptionService.createSubscription(1000000000L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(1000000000L, rh.hindiLanguage(),
                sh.pregnancyPack(), SubscriptionOrigin.IVR);

        Subscriber subscriber = subscriberService.getSubscriber(1000000000L);
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
        Subscriber s2 = new Subscriber(1000000001L, rh.hindiLanguage());
        subscriberService.create(s2);

        subscriptionService.createSubscription(s2.getCallingNumber(), rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(s2.getCallingNumber(), rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.IVR);

        SubscriptionPack fortyEightWeekPack = subscriptionPackDataService.byName("childPack");
        assertEquals(48, fortyEightWeekPack.getMessages().size());

        SubscriptionPack seventyTwoWeekPack = subscriptionPackDataService.byName("pregnancyPack");
        assertEquals(144, seventyTwoWeekPack.getMessages().size());
    }


    @Test
    public void testCreateSubscriptionNoSubscriber() throws Exception {
        // Just verify the db is clean
        Subscriber s = subscriberService.getSubscriber(1111111111L);
        assertNull(s);

        subscriptionService.createSubscription(1111111111L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);

        Subscriber subscriber = subscriberService.getSubscriber(1111111111L);
        assertNotNull(subscriber);
        assertEquals(rh.hindiLanguage(), subscriber.getLanguage());
        assertEquals(1, subscriber.getSubscriptions().size());

        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertEquals(sh.childPack(), subscription.getSubscriptionPack());
    }


    @Test
    public void testCreateSubscriptionExistingSubscriberDifferentLanguage() throws Exception {

        // Just verify the db is clean
        Subscriber s = subscriberService.getSubscriber(1111111111L);
        assertNull(s);

        subscriptionService.createSubscription(1111111111L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);

        // Since the user exists we will not change their language
        subscriptionService.createSubscription(1111111111L, rh.kannadaLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);

        Subscriber subscriber = subscriberService.getSubscriber(1111111111L);
        assertNotNull(subscriber);
        assertEquals(rh.hindiLanguage(), subscriber.getLanguage());
        assertEquals(1, subscriber.getSubscriptions().size());

        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertEquals(sh.childPack(), subscription.getSubscriptionPack());
    }


    @Test
    public void testCreateSubscriptionExistingSubscriberWithoutLanguage() throws Exception {

        // Just verify the db is clean
        Subscriber s = subscriberService.getSubscriber(1111111111L);
        assertNull(s);

        subscriberService.create(new Subscriber(1111111111L));
        s = subscriberService.getSubscriber(1111111111L);
        assertNotNull(s);
        assertNull(s.getLanguage());

        subscriptionService.createSubscription(1111111111L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);

        Subscriber subscriber = subscriberService.getSubscriber(1111111111L);
        assertNotNull(subscriber);
        assertEquals(rh.hindiLanguage(), subscriber.getLanguage());
    }


    @Test
    public void testCreateSubscriptionViaMcts() {

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now().minusDays(14));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());
    }


    @Test
    public void testCreateDuplicateChildSubscriptionViaMcts() {

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now().minusDays(14));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());

        // attempt to create subscription to the same pack -- should fail
        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());
    }


    @Test
    public void testCreateDuplicatePregnancySubscriptionViaMcts() {

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(28));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());

        // attempt to create subscription to the same pack -- should fail
        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());
    }


    @Test
    public void testCreateSecondPregnancySubscriptionAfterDeactivationViaMcts() {

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(28));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        Subscription pregnancySubscription = mctsSubscriber.getActiveSubscriptions().iterator().next();
        pregnancySubscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscriptionDataService.update(pregnancySubscription);

        // attempt to create subscription to the same pack -- should succeed
        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());
    }


    @Test
    public void testCreateSubscriptionsToDifferentPacksViaMcts() {

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now().minusDays(14));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        // due to subscription rules detailed in #157, we need to clear out the DOB and set an LMP in order to
        // create a second subscription for this MCTS subscriber
        mctsSubscriber.setDateOfBirth(null);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(100));
        subscriberDataService.update(mctsSubscriber);

        // attempt to create subscription to a different pack
        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(2, mctsSubscriber.getActiveSubscriptions().size());
    }


    @Test
    public void testChangeDOB() {
        DateTime now = DateTime.now();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(now.minusDays(14));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.MCTS_IMPORT);
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
        DateTime now = DateTime.now();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(now.minusDays(180));

        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.MCTS_IMPORT);
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
        DateTime now = DateTime.now();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(now.minusDays(90)); //so the startDate should be today
        subscriberDataService.create(mctsSubscriber);
        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.pregnancyPack(),
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
        DateTime now = DateTime.now();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(now.minusDays(90));
        subscriberDataService.create(mctsSubscriber);
        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.pregnancyPack(), SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        Subscription subscription = mctsSubscriber.getSubscriptions().iterator().next();
        subscription.setNeedsWelcomeMessage(false);
        subscriptionDataService.update(subscription);

        // should throw IllegalStateException
        SubscriptionPackMessage message = subscription.nextScheduledMessage(now.plusDays(1000));
    }


    @Test
    public void testActiveSubscriptionsForDay() {
        DateTime startDate = DateTime.now().minusDays((int) (Math.random() * 100));
        DayOfTheWeek startDay = DayOfTheWeek.fromInt(startDate.getDayOfWeek());

        Subscriber subscriber = subscriberDataService.create(new Subscriber(1111111111L));
        Subscription subscription = new Subscription(subscriber, sh.childPack(), SubscriptionOrigin.IVR);
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


    // NMS shall allow a subscriber deactivated due to DND restrictions to activate the Testing service again
    // via IVR.
    @Test
    public void verifyIssue182() {

        Subscriber subscriber = new Subscriber(4444444444L);
        subscriber.setLastMenstrualPeriod(DateTime.now().minusDays(90));
        subscriber = subscriberDataService.create(subscriber);

        // Make a deactivated subscription
        Subscription subscription = subscriptionService.createSubscription(4444444444L, rh.hindiLanguage(),
                sh.pregnancyPack(), SubscriptionOrigin.MCTS_IMPORT);
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.DO_NOT_DISTURB);

        // Now mimick a subscriber calling
        subscription = subscriptionService.createSubscription(4444444444L, rh.hindiLanguage(),
                sh.pregnancyPack(), SubscriptionOrigin.IVR);

        // And check the subscription is now active
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
    }


    @Rule
    public ExpectedException exception = ExpectedException.none();


    @Test
    public void testDeleteOpenSubscription() {

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now().minusDays(14));
        subscriberDataService.create(mctsSubscriber);

        Subscription subscription = subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(),
                sh.childPack(), SubscriptionOrigin.MCTS_IMPORT);

        exception.expect(JdoListenerInvocationException.class);
        subscriptionDataService.delete(subscription);
    }


    @Test
    public void testDeleteRecentDeactivateSubscription() {

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now().minusDays(14));
        subscriberDataService.create(mctsSubscriber);

        Subscription subscription = subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(),
                sh.childPack(), SubscriptionOrigin.MCTS_IMPORT);
        subscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscriptionDataService.update(subscription);

        exception.expect(JdoListenerInvocationException.class);
        subscriptionDataService.delete(subscription);
    }


    @Test
    public void testDeleteRecentCompletedSubscription() {

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now().minusDays(14));
        subscriberDataService.create(mctsSubscriber);

        Subscription subscription = subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(),
                sh.childPack(), SubscriptionOrigin.MCTS_IMPORT);
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscriptionDataService.update(subscription);

        exception.expect(JdoListenerInvocationException.class);
        subscriptionDataService.delete(subscription);
    }


    @Test
    public void testDeleteOldDeactivatedSubscription() {

        Subscriber subscriber = subscriberService.getSubscriber(2000000000L);
        assertNotNull(subscriber);

        assertEquals(2, subscriber.getSubscriptions().size());

        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        subscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscription.setEndDate(new DateTime().withDate(2011, 8, 1));
        subscriptionDataService.update(subscription);

        subscriptionDataService.delete(subscription);

        subscriber = subscriberDataService.findByCallingNumber(2000000000L);
        assertEquals(1, subscriber.getSubscriptions().size());
    }


    @Test
    public void testDeleteOldCompletedSubscription() {

        Subscriber subscriber = subscriberService.getSubscriber(2000000000L);
        assertNotNull(subscriber);

        assertEquals(2, subscriber.getSubscriptions().size());

        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscription.setEndDate(new DateTime().withDate(2011, 8, 1));
        subscriptionDataService.update(subscription);

        subscriptionDataService.delete(subscription);

        subscriber = subscriberDataService.findByCallingNumber(2000000000L);
        assertEquals(1, subscriber.getSubscriptions().size());
    }

    /*
     * To verify that number of Messages per week shouldn't get configured if invalid value is provided.
     */
    @Test(expected=IllegalArgumentException.class)
    public void verifyFT180() {
            sh.childPack().setMessagesPerWeek(3);
    }

    /*
     * To verify LMP is changed successfully and new subscription created
     * when subscription already exist for pregnancyPack having status as "Completed".
     * Now Added one more assert for updated LMP to cover NMS_FT_134.
     * NMS_FT_134 description ::
     * To check pregnancyPack subscription is successfully created when subscription 
     * already exist for pregnancyPack with status as "Completed".
     */
    @Test
    public void verifyFT156() {
        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(28));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        
        // mark subscription as complete
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(602));
        subscriberService.update(mctsSubscriber);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        DateTime oldLMP = mctsSubscriber.getLastMenstrualPeriod();
        assertEquals(1, mctsSubscriber.getSubscriptions().size()); // Completed subscription should be there
        assertEquals(0, mctsSubscriber.getActiveSubscriptions().size()); // No active subscription
        
        DateTime newLMP = DateTime.now().minusDays(100);
        mctsSubscriber.setLastMenstrualPeriod(newLMP);
        subscriberService.update(mctsSubscriber);
        
        // attempt to create subscription to the same pack -- should succeed
        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertFalse(mctsSubscriber.getLastMenstrualPeriod().equals(oldLMP));
        assertEquals(mctsSubscriber.getLastMenstrualPeriod(), newLMP);
        assertEquals(2, mctsSubscriber.getSubscriptions().size());
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size()); // One active subscription
    }


    /*
     * To verify DOB is changed successfully and new subscription created
     * when subscription already exist for childPack having status as "Deactivated".
     * Now Added one more assert for updated DOB to cover NMS_FT_132.
     * NMS_FT_132 description ::
     * To check subscription for childPack is successfully created when subscription  
     * already exist for  childPack in state "Deactivated"
     */
    @Test
    public void verifyFT159() {
        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now());
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        Subscription childSubscription = mctsSubscriber.getActiveSubscriptions().iterator().next();
        childSubscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscriptionDataService.update(childSubscription);
        
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        DateTime oldDob = mctsSubscriber.getDateOfBirth();
        DateTime newDob = DateTime.now().minusDays(100);
        mctsSubscriber.setDateOfBirth(newDob);
        subscriberService.update(mctsSubscriber);

        // attempt to create subscription to the same pack -- should succeed
        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertFalse(mctsSubscriber.getDateOfBirth().equals(oldDob));
        assertEquals(mctsSubscriber.getDateOfBirth(), newDob);
        assertEquals(2, mctsSubscriber.getSubscriptions().size());
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());
    }


    /*
     * To verify DOB is changed successfully and new subscription created
     * when subscription already exist for childPack having status as "Completed".
     * Now Added one more assert for updated DOB to cover NMS_FT_133.
     * NMS_FT_133 description ::
     * To check childPack subscription is successfully created when subscription 
     * already exist for childPack with status as "Completed".
     */
    @Test
    public void verifyFT160() {

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now());
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.MCTS_IMPORT);
        
        // mark subscription as complete
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now().minusDays(340));
        subscriberService.update(mctsSubscriber);
        
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        DateTime oldDob = mctsSubscriber.getDateOfBirth(); 
        assertEquals(1, mctsSubscriber.getSubscriptions().size());
        assertEquals(0, mctsSubscriber.getActiveSubscriptions().size()); // No active subscription

        DateTime newDob = DateTime.now().minusDays(100);
        mctsSubscriber.setDateOfBirth(DateTime.now().minusDays(100));
        subscriberService.update(mctsSubscriber);
        // attempt to create subscription to the same pack -- should succeed
        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertFalse(mctsSubscriber.getDateOfBirth().equals(oldDob));
        assertEquals(mctsSubscriber.getDateOfBirth(), newDob);
        assertEquals(2, mctsSubscriber.getSubscriptions().size());		 
    }
    
    /*
	 * To verify that MSISDN greater than 10 digit should be accepted during 
	 * MCTS upload. subscriber should be created with last 10 digits of MSISDN.
	 * 
	 * https://applab.atlassian.net/browse/NMS-202
	 */
    @Test
    public void verifyFT182() {
    	
    	//attempt to create subscriber and subscription having calling number more than 10 digit
    	subscriptionService.createSubscription(991111111122L, rh.hindiLanguage(),
				sh.pregnancyPack(), SubscriptionOrigin.MCTS_IMPORT);
    	
        Subscriber subscriber1 = subscriberDataService.findByCallingNumber(991111111122L);
        assertNull(subscriber1);

        Subscriber subscriber2 = subscriberDataService.findByCallingNumber(1111111122L);
        assertNotNull(subscriber2);
    } 
    
    /*
     * To verify that user's subscription should create in pending state
     *
     * JIRA issue: https://applab.atlassian.net/browse/NMS-201
     */
    @Ignore
    @Test
    public void verifyFT153() {

        Subscriber subscriber = new Subscriber(9999911222L);
        subscriberService.create(subscriber);
        
        subscriptionService.createSubscription(subscriber.getCallingNumber(), rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);
        
        subscriber = subscriberDataService.findByCallingNumber(9999911222L);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertEquals(1, subscriber.getSubscriptions().size());
        assertEquals(SubscriptionStatus.PENDING_ACTIVATION, subscription.getStatus());
    }

}
