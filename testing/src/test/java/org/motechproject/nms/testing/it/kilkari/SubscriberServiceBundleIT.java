package org.motechproject.nms.testing.it.kilkari;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.motechproject.mds.ex.JdoListenerInvocationException;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.LanguageService;
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Verify that SubscriberService is present & functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class SubscriberServiceBundleIT extends BasePaxIT {

    @Inject
    SubscriberService subscriberService;
    @Inject
    SubscriptionService subscriptionService;
    @Inject
    SubscriberDataService subscriberDataService;
    @Inject
    SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    SubscriptionDataService subscriptionDataService;
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
    TestingService testingService;
    @Inject
    PlatformTransactionManager transactionManager;

    private SubscriptionHelper sh;


    @Before
    public void setupTestData() {
        sh = new SubscriptionHelper(subscriptionService, subscriberDataService, subscriptionPackDataService,
                languageDataService, languageService, circleDataService, stateDataService, districtDataService,
                districtService);

        clearDatabase();

        sh.mksub(SubscriptionOrigin.IVR, new DateTime(), SubscriptionPackType.CHILD, 2000000000L);
        sh.mksub(SubscriptionOrigin.IVR, new DateTime(), SubscriptionPackType.PREGNANCY, 2000000000L);
    }

    public void clearDatabase() {
        testingService.clearDatabase();
    }


    @Test
    public void testServicePresent() throws Exception {
        assertNotNull(subscriberService);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();


    @Test
    public void testDeleteSubscriberWithOpenSubscription() {


        List<Subscriber> subscriber = subscriberService.getSubscriber(2000000000L);
        assertFalse(subscriber.isEmpty());

        Set<Subscription> subscriptions = (Set<Subscription>) subscriberDataService.getDetachedField(subscriber.get(0),
                "subscriptions");
                Subscription subscription = subscriptions.iterator().next();
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscriptionDataService.update(subscription);

        exception.expect(JdoListenerInvocationException.class);
        subscriberDataService.delete(subscriber.get(0));
    }


    @Test
    public void testDeleteSubscriberWithAllClosedSubscriptions() {

        List<Subscriber> subscriber = subscriberService.getSubscriber(2000000000L);
        assertFalse(subscriber.isEmpty());

        Set<Subscription> subscriptions = (Set<Subscription>) subscriberDataService.getDetachedField(subscriber.get(0),
                "subscriptions");
        for (Subscription subscription: subscriptions) {
            subscription.setStatus(SubscriptionStatus.COMPLETED);
            subscription.setEndDate(new DateTime().withDate(2011, 8, 1));
            subscriptionDataService.update(subscription);
        }

        subscriberDataService.delete(subscriber.get(0));
    }
}
