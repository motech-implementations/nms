package org.motechproject.nms.testing.it.kilkari;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.InboxService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.it.utils.RegionHelper;
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class InboxServiceBundleIT extends BasePaxIT {

	@Inject
	private SubscriberService subscriberService;
	@Inject
	private SubscriptionService subscriptionService;
	@Inject
	private InboxService inboxService;
	@Inject
	private SubscriptionPackDataService subscriptionPackDataService;
	@Inject
	private LanguageDataService languageDataService;
	@Inject
	private StateDataService stateDataService;
	@Inject
	private TestingService testingService;
    @Inject
    private CircleDataService circleDataService;
    @Inject
    private DistrictDataService districtDataService;
    @Inject
    private SubscriberDataService subscriberDataService;


    private RegionHelper rh;
    private SubscriptionHelper sh;


    @Before
    public void setupTestData() {
        rh = new RegionHelper(languageDataService, circleDataService, stateDataService,
                districtDataService);

        sh = new SubscriptionHelper(subscriptionService,
                subscriberDataService, subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService);
    }


    @Before
    public void clearDatabase() {
        testingService.clearDatabase();
    }


	@Test
	public void testServicePresent() throws Exception {
		assertNotNull(inboxService);
	}


	@Test
	@Ignore
	public void verifyFT108() throws Exception {

		//https://applab.atlassian.net/browse/NMS-190

		/*
		 * To check NMS is able to make available a single message of current week in inbox
		 * when user is subscribed to 72Weeks Pack with 2 message per week configuration.
		 */
		DateTime now = DateTime.now();

		// Configuration for second msg of the week
		Subscriber subscriber = new Subscriber(1000000002L, rh.hindiLanguage());
		subscriber.setLastMenstrualPeriod(now.minusDays(94));
		subscriberService.create(subscriber);

		subscriptionService.createSubscription(subscriber.getCallingNumber(), rh.hindiLanguage(),
                sh.pregnancyPack(), SubscriptionOrigin.MCTS_IMPORT);

		subscriber = subscriberService.getSubscriber(subscriber.getCallingNumber());
		Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
		Subscription subscription = subscriptions.iterator().next();
		SubscriptionPackMessage msg = inboxService.getInboxMessage(subscription);

		// second msg should be in inbox
		assertEquals(msg.getWeekId(), "w1_2");
		assertEquals(msg.getMessageFileName(), "w1_2.wav");

		// Configuration for first msg of the week
		subscriber.setLastMenstrualPeriod(DateTime.now().minusDays(90));
		subscriberService.update(subscriber);

		subscriber = subscriberService.getSubscriber(subscriber.getCallingNumber());
		subscriptions = subscriber.getAllSubscriptions();
		subscription = subscriptions.iterator().next();
		msg = inboxService.getInboxMessage(subscription);

		// first msg should be in inbox
		assertEquals(msg.getWeekId(), "w1_1");
		assertEquals(msg.getMessageFileName(), "w1_1.wav");

	}

	@Test
	public void verifyFT121() throws Exception {

		/*
		 *  To check NMS is able to make a message available for 7 days
		 *  after user's subscription gets completed for 72Weeks Pack.
		 */
		DateTime now = DateTime.now();

		// Configuration for second msg of the week
		Subscriber subscriber = new Subscriber(1000000002L, rh.hindiLanguage());
		subscriber.setLastMenstrualPeriod(now.minusDays(94));
		subscriberService.create(subscriber);

		subscriptionService.createSubscription(subscriber.getCallingNumber(), rh.hindiLanguage(),
				sh.pregnancyPack(), SubscriptionOrigin.MCTS_IMPORT);

		subscriber = subscriberService.getSubscriber(subscriber.getCallingNumber());
		Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
		Subscription subscription = subscriptions.iterator().next();
		SubscriptionPackMessage msg = inboxService.getInboxMessage(subscription);

		// second msg should be in inbox
		assertEquals(msg.getWeekId(), "w1_2");
		assertEquals(msg.getMessageFileName(), "w1_2.wav");

		// Configuration for last msg of the week
		subscriber.setLastMenstrualPeriod(now.minusDays(595));
		subscriberService.update(subscriber);

		subscriber = subscriberService.getSubscriber(subscriber.getCallingNumber());
		subscriptions = subscriber.getAllSubscriptions();
		subscription = subscriptions.iterator().next();
		msg = inboxService.getInboxMessage(subscription);

		// last msg should be in inbox
		assertEquals(msg.getWeekId(), "w72_2");
		assertEquals(msg.getMessageFileName(), "w72_2.wav");

	}

}
