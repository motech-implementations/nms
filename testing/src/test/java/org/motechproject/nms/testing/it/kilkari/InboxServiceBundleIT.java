package org.motechproject.nms.testing.it.kilkari;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
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
import org.motechproject.nms.testing.it.api.utils.SubscriptionPackBuilder;
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
	private SubscriptionPack pregnancyPack;

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

		LanguageLocation languageLocation = new LanguageLocation("10",
				new Circle("AA"), language, true);
		languageLocation.getDistrictSet().add(district);
		languageLocationDataService.create(languageLocation);

		language = new Language("english");
		languageDataService.create(language);

		languageLocation = new LanguageLocation("99", new Circle("BB"),
				language, true);
		languageLocation.getDistrictSet().add(district2);
		gLanguageLocation = languageLocationDataService
				.create(languageLocation);

		pregnancyPack = subscriptionPackDataService.create(
				SubscriptionPackBuilder.createSubscriptionPack(
						"pregnancyPack",
						SubscriptionPackType.PREGNANCY, 			
						SubscriptionPackBuilder.PREGNANCY_PACK_WEEKS,	//72
						2));											// messages per week

		pregnancyPack = subscriptionPackDataService.byName("pregnancyPack"); 
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
		assertNotNull(inboxService);
	}

	/**
	 * This test case covers the scenario for NMS_FT_108, NMS_FT_121  
	 */
	@Test
	public void verifyFT108_121() throws Exception {

		/*
		 * To check NMS is able to make available a single message of current week  in inbox
		 * when user is subscribed to 72Weeks Pack with 2 message per week configuration.
		 */
		cleanupData();
		createLanguageAndSubscriptionPacks();
		DateTime now = DateTime.now();

		// Configuration for second msg of the week
		Subscriber subscriber = new Subscriber(1000000002L, gLanguageLocation);
		subscriber.setLastMenstrualPeriod(now.minusDays(94));
		subscriberService.create(subscriber);

		subscriptionService.createSubscription(subscriber.getCallingNumber(), gLanguageLocation, pregnancyPack,
				SubscriptionOrigin.MCTS_IMPORT);

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
		
		/*
		 *  To check NMS is able to make a message available for 7 days 
		 *  after user's subscription gets completed for 72Weeks Pack.
		 */
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
