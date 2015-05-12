package org.motechproject.nms.kilkari.osgi;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.InboxCallData;
import org.motechproject.nms.kilkari.domain.InboxCallDetailRecord;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.InboxCallDataDataService;
import org.motechproject.nms.kilkari.repository.InboxCallDetailRecordDataService;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

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

    private void createLanguageAndSubscriptionPacks() {
        languageDataService.create(new Language("tamil", "10"));
        languageDataService.create(new Language("english", "99"));

        subscriptionPackDataService.create(new SubscriptionPack("pack1", SubscriptionPackType.CHILD, 1, null));
        subscriptionPackDataService.create(new SubscriptionPack("pack2", SubscriptionPackType.PREGNANCY, 2, null));
    }

    private void cleanupData() {
        subscriptionDataService.deleteAll();
        subscriptionPackDataService.deleteAll();
        subscriptionPackMessageDataService.deleteAll();
        subscriberDataService.deleteAll();
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

        Language ta = languageDataService.findByCode("10");
        Subscriber subscriber = new Subscriber(1000000000L, ta);
        subscriberService.add(subscriber);

        SubscriptionPack pack1 = subscriptionPackDataService.byName("pack1");
        SubscriptionPack pack2 = subscriptionPackDataService.byName("pack2");
        subscriptionService.createSubscription(subscriber.getCallingNumber(), ta, pack1,
                                               SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber.getCallingNumber(), ta, pack2,
                                               SubscriptionOrigin.IVR);

        subscriber = subscriberService.getSubscriber(1000000000L);
        Set<Subscription> subscriptions = subscriber.getSubscriptions();

        Set<SubscriptionPack> packs = new HashSet<>();
        for (Subscription subscription : subscriptions) {
            packs.add(subscription.getSubscriptionPack());
        }

        assertEquals(new HashSet<>(Arrays.asList(pack1, pack2)), packs);

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
                new HashSet<InboxCallData>(Arrays.asList(
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

//        InboxCallDetailRecord inboxCallDetails = inboxCallDetailRecordDataService.create(new InboxCallDetailRecord(
//                1111111111L,
//                "OP",
//                "AA",
//                123456789012345L,
//                new DateTime(123L),
//                new DateTime(456L),
//                1,
//                1,
//                1,
//                new HashSet<InboxCallData>(Arrays.asList(
//                        new InboxCallData(
//                                UUID.randomUUID().toString(),
//                                "48WeeksPack",
//                                "xx",
//                                "foo.wav",
//                                new DateTime(100L),
//                                new DateTime(200L)
//                        ),
//                        new InboxCallData(
//                                UUID.randomUUID().toString(),
//                                "76WeeksPack",
//                                "xx",
//                                "bar.wav",
//                                new DateTime(300L),
//                                new DateTime(400L)
//                        )
//                ))
//        ));
//
//        InboxCallDetailRecord inboxCallDetailRecordFromDatabase = inboxCallDetailRecordDataService.findById(
//                (Long)inboxCallDetailRecordDataService.getDetachedField(inboxCallDetails, "id"));
//
//        assertEquals(1111111111L, (long)inboxCallDetailRecordFromDatabase.getCallingNumber());
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

        SubscriptionPack pack1 = subscriptionPackDataService.byName("pack1");
        SubscriptionPack pack2 = subscriptionPackDataService.byName("pack2");

        // Just verify the db is clean
        Subscriber s = subscriberService.getSubscriber(1111111111L);
        assertNull(s);

        subscriptionService.createSubscription(1111111111L, ta, pack1, SubscriptionOrigin.IVR);

        Subscriber subscriber = subscriberService.getSubscriber(1111111111L);
        assertNotNull(subscriber);
        assertEquals(ta, subscriber.getLanguage());
        assertEquals(1, subscriber.getSubscriptions().size());

        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertEquals(pack1, subscription.getSubscriptionPack());
    }

    @Test
    public void testCreateSubscriptionExistingSubscriberDifferentLanguage() throws Exception {
        cleanupData();
        createLanguageAndSubscriptionPacks();

        Language ta = languageDataService.findByCode("10");
        Language en = languageDataService.findByCode("99");

        SubscriptionPack pack1 = subscriptionPackDataService.byName("pack1");
        SubscriptionPack pack2 = subscriptionPackDataService.byName("pack2");

        // Just verify the db is clean
        Subscriber s = subscriberService.getSubscriber(1111111111L);
        assertNull(s);

        subscriptionService.createSubscription(1111111111L, ta, pack1, SubscriptionOrigin.IVR);

        // Since the user exists we will not change their language
        subscriptionService.createSubscription(1111111111L, en, pack1, SubscriptionOrigin.IVR);

        Subscriber subscriber = subscriberService.getSubscriber(1111111111L);
        assertNotNull(subscriber);
        assertEquals(ta, subscriber.getLanguage());
        assertEquals(1, subscriber.getSubscriptions().size());

        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertEquals(pack1, subscription.getSubscriptionPack());
    }

}
