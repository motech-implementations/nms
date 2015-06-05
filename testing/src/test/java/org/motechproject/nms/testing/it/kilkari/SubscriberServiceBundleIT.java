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
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.InboxCallDataDataService;
import org.motechproject.nms.kilkari.repository.InboxCallDetailRecordDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackMessageDataService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
import org.motechproject.nms.testing.service.TestingService;
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
    private StateDataService stateDataService;
    @Inject
    private DistrictDataService districtDataService;
    @Inject
    private CircleDataService circleDataService;
    @Inject
    private TestingService testingService;


    private SubscriptionHelper sh;


    @Before
    public void setupTestData() {
        sh = new SubscriptionHelper(subscriptionService,
                subscriberDataService, subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService);
    }


    @Before
    public void clearDatabase() {
        testingService.clearDatabase();

        Subscriber subscriber = subscriberDataService.create(new Subscriber(2000000000L));
        subscriptionService.create(new Subscription(subscriber, sh.pregnancyPack() , SubscriptionOrigin.IVR));
        subscriptionService.create(new Subscription(subscriber, sh.childPack() , SubscriptionOrigin.IVR));
    }


    @Test
    public void testServicePresent() throws Exception {
        assertNotNull(subscriberService);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();


    @Test
    public void testDeleteSubscriberWithOpenSubscription() {

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
