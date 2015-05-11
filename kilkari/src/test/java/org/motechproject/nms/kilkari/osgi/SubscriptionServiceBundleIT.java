package org.motechproject.nms.kilkari.osgi;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.InboxCallData;
import org.motechproject.nms.kilkari.domain.InboxCallDetails;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionMode;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.InboxCallDataDataService;
import org.motechproject.nms.kilkari.repository.InboxCallDetailsDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackMessageDataService;
import org.motechproject.nms.kilkari.service.InboxService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
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
    private InboxCallDetailsDataService inboxCallDetailsDataService;
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

        LanguageLocation languageLocation = new LanguageLocation("10", new Circle("AA"), language);
        languageLocation.getDistrictSet().add(district);
        languageLocationDataService.create(languageLocation);

        language = new Language("english");
        languageDataService.create(language);

        languageLocation = new LanguageLocation("99", new Circle("BB"), language);
        languageLocation.getDistrictSet().add(district2);
        languageLocationDataService.create(languageLocation);

        subscriptionPackDataService.create(new SubscriptionPack("pack1", SubscriptionPackType.CHILD, 1, null));
        subscriptionPackDataService.create(new SubscriptionPack("pack2", SubscriptionPackType.PREGNANCY, 2, null));
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

        LanguageLocation languageLocation = languageLocationDataService.findByCode("10");
        Subscriber subscriber = new Subscriber(1000000000L, languageLocation);
        subscriberService.add(subscriber);

        SubscriptionPack pack1 = subscriptionPackDataService.byName("pack1");
        SubscriptionPack pack2 = subscriptionPackDataService.byName("pack2");
        subscriptionService.createSubscription(subscriber.getCallingNumber(), languageLocation, pack1,
                                               SubscriptionMode.IVR);
        subscriptionService.createSubscription(subscriber.getCallingNumber(), languageLocation, pack2,
                                               SubscriptionMode.IVR);

        subscriber = subscriberService.getSubscriber(1000000000L);
        Set<Subscription> subscriptions = subscriber.getSubscriptions();

        Set<SubscriptionPack> packs = new HashSet<>();
        for (Subscription subscription : subscriptions) {
            packs.add(subscription.getSubscriptionPack());
        }

        assertEquals(new HashSet<>(Arrays.asList(pack1, pack2)), packs);

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

        LanguageLocation languageLocation = languageLocationDataService.findByCode("10");

        SubscriptionPack pack1 = subscriptionPackDataService.byName("pack1");
        SubscriptionPack pack2 = subscriptionPackDataService.byName("pack2");

        // Just verify the db is clean
        Subscriber s = subscriberService.getSubscriber(1111111111L);
        assertNull(s);

        subscriptionService.createSubscription(1111111111L, languageLocation, pack1, SubscriptionMode.IVR);

        Subscriber subscriber = subscriberService.getSubscriber(1111111111L);
        assertNotNull(subscriber);
        assertEquals(languageLocation, subscriber.getLanguageLocation());
        assertEquals(1, subscriber.getSubscriptions().size());

        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertEquals(pack1, subscription.getSubscriptionPack());
    }

    @Test
    public void testCreateSubscriptionExistingSubscriberDifferentLanguage() throws Exception {
        cleanupData();
        createLanguageAndSubscriptionPacks();

        LanguageLocation ta = languageLocationDataService.findByCode("10");
        LanguageLocation en = languageLocationDataService.findByCode("99");

        SubscriptionPack pack1 = subscriptionPackDataService.byName("pack1");
        SubscriptionPack pack2 = subscriptionPackDataService.byName("pack2");

        // Just verify the db is clean
        Subscriber s = subscriberService.getSubscriber(1111111111L);
        assertNull(s);

        subscriptionService.createSubscription(1111111111L, ta, pack1, SubscriptionMode.IVR);

        // Since the user exists we will not change their language
        subscriptionService.createSubscription(1111111111L, en, pack1, SubscriptionMode.IVR);

        Subscriber subscriber = subscriberService.getSubscriber(1111111111L);
        assertNotNull(subscriber);
        assertEquals(ta, subscriber.getLanguageLocation());
        assertEquals(1, subscriber.getSubscriptions().size());

        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        assertEquals(pack1, subscription.getSubscriptionPack());
    }

}
