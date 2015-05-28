package org.motechproject.nms.testing.it.kilkari;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.motechproject.mds.ex.JdoListenerInvocationException;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.InboxCallDataDataService;
import org.motechproject.nms.kilkari.repository.InboxCallDetailRecordDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackMessageDataService;
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
import org.motechproject.nms.testing.it.api.utils.SubscriptionPackBuilder;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

/**
 * Verify that SubscriberService is present & functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class SubscriberServiceBundleIT extends BasePaxIT {
    @Inject
    private SubscriberService subscriberService;
    @Inject
    private SubscriptionService subscriptionService;
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

        Subscriber subscriber2 = subscriberDataService.create(new Subscriber(2000000000L));

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
        gLanguageLocation = languageLocationDataService.create(languageLocation);

        gPack1 = subscriptionPackDataService.create(
                SubscriptionPackBuilder.createSubscriptionPack(
                        "childPack",
                        SubscriptionPackType.CHILD,
                        SubscriptionPackBuilder.CHILD_PACK_WEEKS,
                        1));
        gPack2 = subscriptionPackDataService.create(
                SubscriptionPackBuilder.createSubscriptionPack(
                        "pregnancyPack",
                        SubscriptionPackType.PREGNANCY,
                        SubscriptionPackBuilder.PREGNANCY_PACK_WEEKS,
                        2));
    }

    private void cleanupData() {
        for (Subscription subscription: subscriptionDataService.retrieveAll()) {
            subscription.setStatus(SubscriptionStatus.COMPLETED);
            subscription.setEndDate(new DateTime().withDate(2011, 8, 1));

            subscriptionDataService.update(subscription);
        }

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
        assertNotNull(subscriberService);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testDeleteSubscriberWithOpenSubscription() {
        setupData();

        Subscriber subscriber = subscriberService.getSubscriber(2000000000L);
        assertNotNull(subscriber);

        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscriptionDataService.update(subscription);

        exception.expect(JdoListenerInvocationException.class);
        subscriberDataService.delete(subscriber);
    }

    @Test
    public void testDeleteSubscriberWithAllClosedSubscriptions() {
        setupData();

        Subscriber subscriber = subscriberService.getSubscriber(2000000000L);
        assertNotNull(subscriber);

        for (Subscription subscription: subscriber.getSubscriptions()) {
            subscription.setStatus(SubscriptionStatus.COMPLETED);
            subscription.setEndDate(new DateTime().withDate(2011, 8, 1));
            subscriptionDataService.update(subscription);
        }

        subscriberDataService.delete(subscriber);
    }
}
