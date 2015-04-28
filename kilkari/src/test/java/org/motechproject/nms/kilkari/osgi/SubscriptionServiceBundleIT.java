package org.motechproject.nms.kilkari.osgi;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.InboxCallData;
import org.motechproject.nms.kilkari.domain.InboxCallDetails;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionMode;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    @Test
    public void testServicePresent() throws Exception {
        assertNotNull(subscriptionService);
    }

    @Test
    public void testServiceFunctional() throws Exception {
        subscriptionDataService.deleteAll();
        subscriptionPackDataService.deleteAll();
        subscriberDataService.deleteAll();
        languageDataService.deleteAll();
        inboxCallDataDataService.deleteAll();
        inboxCallDetailsDataService.deleteAll();

        Language ta = languageDataService.create(new Language("tamil", "10"));

        SubscriptionPack pack1 = subscriptionPackDataService.create(new SubscriptionPack("pack1"));
        SubscriptionPack pack2 = subscriptionPackDataService.create(new SubscriptionPack("pack2"));

        Subscriber subscriber = subscriberDataService.create(new Subscriber(1000000000L));

        /*
        subscriptionService.createSubscription(subscriber.getCallingNumber(), ta.getCode(), pack1.getName(), SubscriptionMode.IVR);
        subscriptionService.createSubscription(subscriber.getCallingNumber(), ta.getCode(), pack2.getName(), SubscriptionMode.IVR);
        */

        Subscription subscription1 = subscriptionDataService.create(new Subscription(subscriber, pack1, ta, SubscriptionMode.IVR));
        Subscription subscription2 = subscriptionDataService.create(new Subscription(subscriber, pack2, ta, SubscriptionMode.IVR));

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
}
