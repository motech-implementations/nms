package org.motechproject.nms.kilkari.osgi;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.InboxCallData;
import org.motechproject.nms.kilkari.domain.InboxCallDetails;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.InboxCallDataDataService;
import org.motechproject.nms.kilkari.repository.InboxCallDetailsDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackMessageDataService;
import org.motechproject.nms.kilkari.service.InboxService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.language.domain.Language;
import org.motechproject.nms.region.language.repository.LanguageDataService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Arrays;
import java.util.UUID;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
    private InboxCallDetailsDataService inboxCallDetailsDataService;
    @Inject
    private InboxCallDataDataService inboxCallDataDataService;

    private Language gLanguage;
    private SubscriptionPack gPack1;
    private SubscriptionPack gPack2;


    private void setupData() {
        cleanupData();
        createLanguageAndSubscriptionPacks();

        Subscriber subscriber1 = subscriberDataService.create(new Subscriber(1000000000L));
        Subscriber subscriber2 = subscriberDataService.create(new Subscriber(2000000000L));

        subscriptionService.createSubscription(subscriber1.getCallingNumber(), gLanguage, gPack1,
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), gLanguage, gPack1,
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), gLanguage, gPack2,
                SubscriptionOrigin.IVR);
    }


    private void createLanguageAndSubscriptionPacks() {
        gLanguage = languageDataService.create(new Language("tamil", "10"));
        languageDataService.create(new Language("english", "99"));

        subscriptionService.createSubscriptionPacks();
        gPack1 = subscriptionPackDataService.byName("childPack"); // 48 weeks, 1 message per week
        gPack2 = subscriptionPackDataService.byName("pregnancyPack"); // 72 weeks, 2 messages per week
    }

    private void cleanupData() {
        subscriptionDataService.deleteAll();
        subscriptionPackDataService.deleteAll();
        subscriptionPackMessageDataService.deleteAll();
        subscriberDataService.deleteAll();
        languageDataService.deleteAll();
        inboxCallDataDataService.deleteAll();
        inboxCallDetailsDataService.deleteAll();
    }

    @Test
    public void testServicePresent() throws Exception {
        assertNotNull(subscriptionService);
    }

    @Test
    public void testServiceFunctional() throws Exception {
        cleanupData();
        createLanguageAndSubscriptionPacks();

        Language ta = languageDataService.findByCode("10");
        Subscriber subscriber = new Subscriber(1000000000L, ta);
        subscriberService.add(subscriber);

        subscriptionService.createSubscription(subscriber.getCallingNumber(), ta, gPack1,
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber.getCallingNumber(), ta, gPack2,
                SubscriptionOrigin.IVR);

        subscriber = subscriberService.getSubscriber(1000000000L);
        Set<Subscription> subscriptions = subscriber.getSubscriptions();

        Set<String> packs = new HashSet<>();
        for (Subscription subscription : subscriptions) {
            packs.add(subscription.getSubscriptionPack().getName());
        }

        assertEquals(new HashSet<>(Arrays.asList("childPack", "pregnancyPack")), packs);

        long id = inboxService.addInboxCallDetails(new InboxCallDetails(
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

        InboxCallDetails inboxCallDetailsFromDatabase = inboxCallDetailsDataService.findById(id);

        assertEquals(1111111111L, (long) inboxCallDetailsFromDatabase.getCallingNumber());
    }

    @Test
    public void testSubscriptionPackCreation() throws Exception {
        cleanupData();
        subscriptionService.createSubscriptionPacks();

        SubscriptionPack fortyEightWeekPack = subscriptionPackDataService.byName("childPack");
        assertEquals(48, fortyEightWeekPack.getWeeklyMessages().size());

        SubscriptionPack seventyTwoWeekPack = subscriptionPackDataService.byName("pregnancyPack");
        assertEquals(144, seventyTwoWeekPack.getWeeklyMessages().size());
    }


    @Test
    public void testCreateSubscriptionNoSubscriber() throws Exception {
        cleanupData();
        createLanguageAndSubscriptionPacks();

        Language ta = languageDataService.findByCode("10");

        // Just verify the db is clean
        Subscriber s = subscriberService.getSubscriber(1111111111L);
        assertNull(s);

        subscriptionService.createSubscription(1111111111L, ta, gPack1, SubscriptionOrigin.IVR);

        Subscriber subscriber = subscriberService.getSubscriber(1111111111L);
        assertNotNull(subscriber);
        assertEquals(ta, subscriber.getLanguage());
        assertEquals(1, subscriber.getSubscriptions().size());

        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertEquals(gPack1, subscription.getSubscriptionPack());
    }

    @Test
    public void testCreateSubscriptionExistingSubscriberDifferentLanguage() throws Exception {
        cleanupData();
        createLanguageAndSubscriptionPacks();

        Language ta = languageDataService.findByCode("10");
        Language en = languageDataService.findByCode("99");

        // Just verify the db is clean
        Subscriber s = subscriberService.getSubscriber(1111111111L);
        assertNull(s);

        subscriptionService.createSubscription(1111111111L, ta, gPack1, SubscriptionOrigin.IVR);

        // Since the user exists we will not change their language
        subscriptionService.createSubscription(1111111111L, en, gPack1, SubscriptionOrigin.IVR);

        Subscriber subscriber = subscriberService.getSubscriber(1111111111L);
        assertNotNull(subscriber);
        assertEquals(ta, subscriber.getLanguage());
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

        subscriptionService.createSubscription(9999911122L, gLanguage, gPack1, SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());

        // attempt to create subscription to the same pack -- should fail
        subscriptionService.createSubscription(9999911122L, gLanguage, gPack1, SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());
    }

    @Test
    public void testCreateDuplicatePregnancySubscriptionViaMcts() {
        setupData();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(28));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, gLanguage, gPack2, SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());

        // attempt to create subscription to the same pack -- should fail
        subscriptionService.createSubscription(9999911122L, gLanguage, gPack2, SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());
    }

    @Test
    public void testCreateSecondPregnancySubscriptionAfterDeactivationViaMcts() {
        setupData();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(28));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, gLanguage, gPack2, SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        Subscription pregnancySubscription = mctsSubscriber.getActiveSubscriptions().iterator().next();
        pregnancySubscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscriptionDataService.update(pregnancySubscription);

        // attempt to create subscription to the same pack -- should succeed
        subscriptionService.createSubscription(9999911122L, gLanguage, gPack2, SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());
    }

    @Test
    public void testCreateSubscriptionsToDifferentPacksViaMcts() {
        setupData();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now().minusDays(14));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, gLanguage, gPack1, SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        // due to subscription rules detailed in #157, we need to clear out the DOB and set an LMP in order to
        // create a second subscription for this MCTS subscriber
        mctsSubscriber.setDateOfBirth(null);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(100));
        subscriberDataService.update(mctsSubscriber);

        // attempt to create subscription to a different pack
        subscriptionService.createSubscription(9999911122L, gLanguage, gPack2, SubscriptionOrigin.MCTS_IMPORT);

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

        subscriptionService.createSubscription(9999911122L, gLanguage, gPack1, SubscriptionOrigin.MCTS_IMPORT);
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

        subscriptionService.createSubscription(9999911122L, gLanguage, gPack2, SubscriptionOrigin.MCTS_IMPORT);
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

}
