package org.motechproject.nms.kilkari.osgi;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.*;
import org.motechproject.nms.kilkari.repository.InboxCallDataDataService;
import org.motechproject.nms.kilkari.repository.InboxCallDetailsDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.language.repository.LanguageDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.language.domain.Language;
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
    private SubscriptionService subscriptionService;
    @Inject
    private SubscriberDataService subscriberDataService;
    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    private SubscriptionDataService subscriptionDataService;
    @Inject
    private LanguageDataService languageDataService;
    @Inject
    private InboxCallDetailsDataService inboxCallDetailsDataService;
    @Inject
    private InboxCallDataDataService inboxCallDataDataService;

    private Language ta;
    private Language en;
    private SubscriptionPack pack1;
    private SubscriptionPack pack2;

    private void createLanguageAndSubscriptionPacks() {
        ta = languageDataService.create(new Language("tamil", "10"));
        en = languageDataService.create(new Language("english", "99"));

        pack1 = subscriptionPackDataService.create(new SubscriptionPack("pack1", SubscriptionPackType.CHILD));
        pack2 = subscriptionPackDataService.create(new SubscriptionPack("pack2", SubscriptionPackType.PREGNANCY));
    }

    private void cleanupData() {
        subscriptionDataService.deleteAll();
        subscriptionPackDataService.deleteAll();
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

        Subscriber subscriber = subscriberDataService.create(new Subscriber(1000000000L));

        subscriptionService.createSubscription(subscriber.getCallingNumber(), ta, pack1,
                                               SubscriptionMode.IVR);
        subscriptionService.createSubscription(subscriber.getCallingNumber(), ta, pack2,
                                               SubscriptionMode.IVR);

        subscriber = subscriberDataService.findByCallingNumber(1000000000L);
        Set<Subscription> subscriptions = subscriber.getSubscriptions();

        Set<SubscriptionPack> packs = new HashSet<>();
        for (Subscription subscription : subscriptions) {
            packs.add(subscription.getSubscriptionPack());
        }

        assertEquals(new HashSet<>(Arrays.asList(pack1, pack2)), packs);

        long id = subscriptionService.addInboxCallDetails(new InboxCallDetails(
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

        InboxCallDetails inboxCallDetailsFromDatabase = inboxCallDetailsDataService.findById(id);

        assertEquals(1111111111L, (long) inboxCallDetailsFromDatabase.getCallingNumber());

//        InboxCallDetails inboxCallDetails = inboxCallDetailsDataService.create(new InboxCallDetails(
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
//        InboxCallDetails inboxCallDetailsFromDatabase = inboxCallDetailsDataService.findById(
//                (Long)inboxCallDetailsDataService.getDetachedField(inboxCallDetails, "id"));
//
//        assertEquals(1111111111L, (long)inboxCallDetailsFromDatabase.getCallingNumber());
    }

    @Test
    public void testCreateSubscriptionNoSubscriber() throws Exception {
        cleanupData();
        createLanguageAndSubscriptionPacks();

        // Just verify the db is clean
        Subscriber s = subscriptionService.getSubscriber(1111111111L);
        assertNull(s);

        subscriptionService.createSubscription(1111111111L, ta, pack1, SubscriptionMode.IVR);

        Subscriber subscriber = subscriptionService.getSubscriber(1111111111L);
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

        // Just verify the db is clean
        Subscriber s = subscriptionService.getSubscriber(1111111111L);
        assertNull(s);

        subscriptionService.createSubscription(1111111111L, ta, pack1, SubscriptionMode.IVR);

        // Since the user exists we will not change their language
        subscriptionService.createSubscription(1111111111L, en, pack1, SubscriptionMode.IVR);

        Subscriber subscriber = subscriptionService.getSubscriber(1111111111L);
        assertNotNull(subscriber);
        assertEquals(ta, subscriber.getLanguage());
        assertEquals(1, subscriber.getSubscriptions().size());

        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertEquals(pack1, subscription.getSubscriptionPack());
    }
}
