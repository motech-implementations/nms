package org.motechproject.nms.testing.it.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.api.web.contract.BadRequest;
import org.motechproject.nms.api.web.contract.kilkari.CallDataRequest;
import org.motechproject.nms.api.web.contract.kilkari.InboxCallDetailsRequest;
import org.motechproject.nms.api.web.contract.kilkari.InboxResponse;
import org.motechproject.nms.api.web.contract.kilkari.InboxSubscriptionDetailResponse;
import org.motechproject.nms.api.web.contract.kilkari.SubscriptionRequest;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.repository.ServiceUsageDataService;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.InboxCallData;
import org.motechproject.nms.kilkari.domain.InboxCallDetailRecord;
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
import org.motechproject.nms.props.domain.DeployedService;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.props.repository.DeployedServiceDataService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.repository.NationalDefaultLanguageLocationDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.it.api.utils.HttpDeleteWithBody;
import org.motechproject.nms.testing.it.api.utils.SubscriptionPackBuilder;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.http.SimpleHttpClient;
import org.motechproject.testing.utils.TestContext;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

/**
 * Verify that Kilkari API is functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class KilkariControllerBundleIT extends BasePaxIT {
    private static final String ADMIN_USERNAME = "motech";
    private static final String ADMIN_PASSWORD = "motech";

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
    private FrontLineWorkerDataService frontLineWorkerDataService;

    @Inject
    private ServiceUsageDataService serviceUsageDataService;

    @Inject
    private ServiceUsageCapDataService serviceUsageCapDataService;

    @Inject
    private LanguageDataService languageDataService;

    @Inject
    private LanguageLocationDataService languageLocationDataService;

    @Inject
    private CircleDataService circleDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DeployedServiceDataService deployedServiceDataService;

    @Inject
    private InboxCallDetailRecordDataService inboxCallDetailsDataService;

    @Inject
    private InboxCallDataDataService inboxCallDataDataService;

    @Inject
    private NationalDefaultLanguageLocationDataService nationalLanguageLocationDataService;

    public KilkariControllerBundleIT() {
        System.setProperty("org.motechproject.testing.osgi.http.numTries", "1");
    }

    private LanguageLocation gLanguageLocation;
    private SubscriptionPack gPack1;
    private SubscriptionPack gPack2;

    private void cleanAllData() {
        for (FrontLineWorker flw: frontLineWorkerDataService.retrieveAll()) {
            flw.setStatus(FrontLineWorkerStatus.INVALID);
            flw.setInvalidationDate(new DateTime().withDate(2011, 8, 1));

            frontLineWorkerDataService.update(flw);
        }

        for (Subscription subscription: subscriptionDataService.retrieveAll()) {
            subscription.setStatus(SubscriptionStatus.COMPLETED);
            subscription.setEndDate(new DateTime().withDate(2011, 8, 1));

            subscriptionDataService.update(subscription);
        }

        subscriptionDataService.deleteAll();
        subscriptionPackDataService.deleteAll();
        subscriptionPackMessageDataService.deleteAll();
        subscriberDataService.deleteAll();
        serviceUsageCapDataService.deleteAll();
        serviceUsageDataService.deleteAll();
        frontLineWorkerDataService.deleteAll();
        nationalLanguageLocationDataService.deleteAll();
        languageLocationDataService.deleteAll();
        languageDataService.deleteAll();
        deployedServiceDataService.deleteAll();
        stateDataService.deleteAll();
        circleDataService.deleteAll();
        inboxCallDataDataService.deleteAll();
        inboxCallDetailsDataService.deleteAll();
    }

    private void createLanguageAndSubscriptionPacks() {
        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);

        stateDataService.create(state);

        deployedServiceDataService.create(new DeployedService(state, Service.KILKARI));

        Language language = new Language("tamil");
        languageDataService.create(language);

        gLanguageLocation = new LanguageLocation("10", new Circle("AA"), language, true);
        gLanguageLocation.getDistrictSet().add(district);
        languageLocationDataService.create(gLanguageLocation);

        subscriptionPackDataService.create(
                SubscriptionPackBuilder.createSubscriptionPack(
                        "childPack",
                        SubscriptionPackType.CHILD,
                        SubscriptionPackBuilder.CHILD_PACK_WEEKS,
                        1));
        subscriptionPackDataService.create(
                SubscriptionPackBuilder.createSubscriptionPack(
                        "pregnancyPack",
                        SubscriptionPackType.PREGNANCY,
                        SubscriptionPackBuilder.PREGNANCY_PACK_WEEKS,
                        2));

        gPack1 = subscriptionPackDataService.byName("childPack"); // 48 weeks, 1 message per week
        gPack2 = subscriptionPackDataService.byName("pregnancyPack"); // 72 weeks, 2 messages per week
    }

    private void setupData() {
        cleanAllData();
        createLanguageAndSubscriptionPacks();

        Subscriber subscriber1 = subscriberDataService.create(new Subscriber(1000000000L));
        Subscriber subscriber2 = subscriberDataService.create(new Subscriber(2000000000L));
        Subscriber subscriber3 = subscriberDataService.create(new Subscriber(4000000000L));

        // subscriber1 subscribed to 48 weeks pack only
        subscriptionService.createSubscription(subscriber1.getCallingNumber(), gLanguageLocation, gPack1,
                                               SubscriptionOrigin.IVR);

        // subscriber2 subscribed to both 48 weeks pack and 72 weeks pack
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), gLanguageLocation, gPack1,
                                               SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), gLanguageLocation, gPack2,
                                               SubscriptionOrigin.IVR);
        // subscriber3 subscribed to 72 weeks pack only
        subscriptionService.createSubscription(subscriber3.getCallingNumber(),
                gLanguageLocation, gPack2, SubscriptionOrigin.IVR);
    }

    private HttpGet createHttpGet(boolean includeCallingNumber, String callingNumber,
                                  boolean includeCallId, String callId) {

        StringBuilder sb = new StringBuilder(String.format("http://localhost:%d/api/kilkari/inbox?",
                TestContext.getJettyPort()));
        String sep = "";
        if (includeCallingNumber) {
            sb.append(String.format("callingNumber=%s", callingNumber));
            sep = "&";
        }
        if (includeCallId) {
            sb.append(String.format("%scallId=%s", sep, callId));
        }

        return new HttpGet(sb.toString());
    }

    private String createInboxResponseJson(Set<InboxSubscriptionDetailResponse> inboxSubscriptionDetailList)
            throws IOException {
        InboxResponse response = new InboxResponse(inboxSubscriptionDetailList);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(response);
    }

    private String createFailureResponseJson(String failureReason) throws IOException {
        BadRequest badRequest = new BadRequest(failureReason);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(badRequest);
    }

    @Test
    public void testInboxRequest() throws IOException, InterruptedException {
        setupData();

        Subscriber subscriber = subscriberDataService.findByCallingNumber(1000000000L); // 1 subscription
        Subscription subscription = subscriber.getSubscriptions().iterator().next();

        // override the default start date (today + 1 day) in order to see a non-empty inbox
        subscription.setStartDate(DateTime.now().minusDays(2));
        subscriptionDataService.update(subscription);

        HttpGet httpGet = createHttpGet(true, "1000000000", true, "123456789012345");
        String expectedJson = createInboxResponseJson(new HashSet<>(Arrays.asList(
                new InboxSubscriptionDetailResponse(
                        subscription.getSubscriptionId().toString(),
                        "childPack",
                        "w1_1",
                        "w1_1.wav"
                )
        )));

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, expectedJson, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testInboxRequestTwoSubscriptions() throws IOException, InterruptedException {
        setupData();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now().minusDays(250));
        subscriberDataService.create(mctsSubscriber);

        // create subscription to child pack
        subscriptionService.createSubscription(9999911122L, gLanguageLocation, gPack1, SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        // due to subscription rules detailed in #157, we need to clear out the DOB and set an LMP in order to
        // create a second subscription for this MCTS subscriber
        mctsSubscriber.setDateOfBirth(null);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(103));
        subscriberDataService.update(mctsSubscriber);

        // create subscription to pregnancy pack
        subscriptionService.createSubscription(9999911122L, gLanguageLocation, gPack2, SubscriptionOrigin.MCTS_IMPORT);

        Pattern childPackJsonPattern = Pattern.compile(".*\"subscriptionPack\":\"childPack\",\"inboxWeekId\":\"w36_1\",\"contentFileName\":\"w36_1\\.wav.*");
        Pattern pregnancyPackJsonPattern = Pattern.compile(".*\"subscriptionPack\":\"pregnancyPack\",\"inboxWeekId\":\"w2_2\",\"contentFileName\":\"w2_2\\.wav.*");

        HttpGet httpGet = createHttpGet(true, "9999911122", true, "123456789012345");
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, childPackJsonPattern, ADMIN_USERNAME,
                ADMIN_PASSWORD));
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, pregnancyPackJsonPattern, ADMIN_USERNAME,
                ADMIN_PASSWORD));
    }

    @Test
    public void testInboxRequestEarlySubscription() throws IOException, InterruptedException {
        setupData();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(30));
        subscriberDataService.create(mctsSubscriber);
        // create subscription to pregnancy pack, not due to start for 60 days
        subscriptionService.createSubscription(9999911122L, gLanguageLocation, gPack2, SubscriptionOrigin.MCTS_IMPORT);

        Pattern expectedJsonPattern = Pattern.compile(".*\"subscriptionPack\":\"pregnancyPack\",\"inboxWeekId\":null,\"contentFileName\":null.*");

        HttpGet httpGet = createHttpGet(true, "9999911122", true, "123456789012345");
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, expectedJsonPattern, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testInboxRequestCompletedSubscription() throws IOException, InterruptedException {
        setupData();

        Subscriber subscriber = subscriberService.getSubscriber(1000000000L);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        // setting the subscription to have ended more than a week ago -- no message should be returned
        subscription.setStartDate(DateTime.now().minusDays(500));
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscriptionDataService.update(subscription);

        String expectedJson = "{\"inboxSubscriptionDetailList\":[]}";

        HttpGet httpGet = createHttpGet(true, "1000000000", true, "123456789012345");
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, expectedJson, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testInboxRequestRecentlyCompletedSubscription() throws IOException, InterruptedException {
        setupData();

        Subscriber subscriber = subscriberService.getSubscriber(1000000000L);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        // setting the subscription to have ended less than a week ago -- the final message should be returned
        subscription.setStartDate(DateTime.now().minusDays(340));
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscriptionDataService.update(subscription);

        Pattern expectedJsonPattern = Pattern.compile(".*\"subscriptionPack\":\"childPack\",\"inboxWeekId\":\"w48_1\",\"contentFileName\":\"w48_1\\.wav.*");

        HttpGet httpGet = createHttpGet(true, "1000000000", true, "123456789012345");
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, expectedJsonPattern, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testInboxRequestDeactivatedSubscription() throws IOException, InterruptedException {
        setupData();

        Subscriber subscriber = subscriberService.getSubscriber(1000000000L);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        subscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscription.setDeactivationReason(DeactivationReason.DEACTIVATED_BY_USER);
        subscriptionDataService.update(subscription);

        String expectedJson = "{\"inboxSubscriptionDetailList\":[]}";

        HttpGet httpGet = createHttpGet(true, "1000000000", true, "123456789012345");
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, expectedJson, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testInboxRequestSeveralSubscriptionsInDifferentStates() throws IOException, InterruptedException {
        setupData();

        // subscriber has two active subscriptions
        Subscriber subscriber = subscriberService.getSubscriber(2000000000L);

        Iterator<Subscription> subscriptionIterator = subscriber.getSubscriptions().iterator();
        Subscription subscription1 = subscriptionIterator.next();
        Subscription subscription2 = subscriptionIterator.next();

        // deactivate one of them
        subscription1.setStatus(SubscriptionStatus.DEACTIVATED);
        subscription1.setDeactivationReason(DeactivationReason.DEACTIVATED_BY_USER);
        subscriptionDataService.update(subscription1);

        // complete the other one
        subscription2.setStartDate(subscription2.getStartDate().minusDays(1000));
        subscription2.setStatus(SubscriptionStatus.COMPLETED);
        subscriptionDataService.update(subscription2);

        // inbox request should return empty inbox
        String expectedJson = "{\"inboxSubscriptionDetailList\":[]}";
        HttpGet httpGet = createHttpGet(true, "2000000000", true, "123456789012345");
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, expectedJson, ADMIN_USERNAME, ADMIN_PASSWORD));

        // create two more subscriptions -- this time using MCTS import as subscription origin

        // create subscription to child pack
        subscriber.setDateOfBirth(DateTime.now().minusDays(250));
        subscriberDataService.update(subscriber);
        subscriptionService.createSubscription(2000000000L, gLanguageLocation, gPack1, SubscriptionOrigin.MCTS_IMPORT);
        subscriber = subscriberDataService.findByCallingNumber(2000000000L);

        // due to subscription rules detailed in #157, we need to clear out the DOB and set an LMP in order to
        // create a second subscription for this subscriber
        subscriber.setDateOfBirth(null);
        subscriber.setLastMenstrualPeriod(DateTime.now().minusDays(103));
        subscriberDataService.update(subscriber);

        // create subscription to pregnancy pack
        subscriptionService.createSubscription(2000000000L, gLanguageLocation, gPack2, SubscriptionOrigin.MCTS_IMPORT);

        subscriber = subscriberDataService.findByCallingNumber(2000000000L);
        assertEquals(2, subscriber.getActiveSubscriptions().size());
        assertEquals(4, subscriber.getAllSubscriptions().size());

        Pattern childPackJsonPattern = Pattern.compile(".*\"subscriptionPack\":\"childPack\",\"inboxWeekId\":\"w36_1\",\"contentFileName\":\"w36_1\\.wav.*");
        Pattern pregnancyPackJsonPattern = Pattern.compile(".*\"subscriptionPack\":\"pregnancyPack\",\"inboxWeekId\":\"w2_2\",\"contentFileName\":\"w2_2\\.wav.*");

        httpGet = createHttpGet(true, "2000000000", true, "123456789012345");

        // inbox request should return two subscriptions

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, childPackJsonPattern, ADMIN_USERNAME,
                ADMIN_PASSWORD));
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, pregnancyPackJsonPattern, ADMIN_USERNAME,
                ADMIN_PASSWORD));
    }

    @Test
    public void testInboxRequestBadSubscriber() throws IOException, InterruptedException {
        setupData();

        HttpGet httpGet = createHttpGet(true, "3000000000", true, "123456789012345");
        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Not Found>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_NOT_FOUND, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testInboxRequestNoSubscriber() throws IOException, InterruptedException {
        setupData();

        HttpGet httpGet = createHttpGet(false, null, true, "123456789012345");
        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Not Present>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    private HttpPost createSubscriptionHttpPost(long callingNumber, String subscriptionPack)
            throws IOException {
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(callingNumber, "A", "AP",
                123456789012545L, "10", subscriptionPack);
        ObjectMapper mapper = new ObjectMapper();
        String subscriptionRequestJson = mapper.writeValueAsString(subscriptionRequest);

        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/kilkari/subscription", TestContext.getJettyPort()));
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(subscriptionRequestJson));
        return httpPost;
    }

    @Test
    public void testCreateSubscriptionRequest() throws IOException, InterruptedException {
        setupData();
        HttpPost httpPost = createSubscriptionHttpPost(9999911122L, "childPack");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testCreateSubscriptionRequestInvalidPack() throws IOException, InterruptedException {
        setupData();
        HttpPost httpPost = createSubscriptionHttpPost(9999911122L, "pack99999");

        // Should return HTTP 404 (Not Found) because the subscription pack won't be found
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testCreateSubscriptionRequestSamePack() throws IOException, InterruptedException {
        setupData();
        long callingNumber = 9999911122L;

        HttpPost httpPost = createSubscriptionHttpPost(callingNumber, "childPack");

        SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK, ADMIN_USERNAME, ADMIN_PASSWORD);

        Subscriber subscriber = subscriberService.getSubscriber(callingNumber);
        int numberOfSubsBefore = subscriber.getActiveSubscriptions().size();

        SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK, ADMIN_USERNAME, ADMIN_PASSWORD);

        subscriber = subscriberService.getSubscriber(callingNumber);
        int numberOfSubsAfter = subscriber.getActiveSubscriptions().size();

        // No additional subscription should be created because subscriber already has an active subscription
        // to this pack
        assertEquals(numberOfSubsBefore, numberOfSubsAfter);
    }

    @Test
    public void testCreateSubscriptionRequestDifferentPacks() throws IOException, InterruptedException {
        setupData();
        long callingNumber = 9999911122L;

        HttpPost httpPost1 = createSubscriptionHttpPost(callingNumber, "childPack");

        SimpleHttpClient.execHttpRequest(httpPost1, HttpStatus.SC_OK, ADMIN_USERNAME, ADMIN_PASSWORD);

        Subscriber subscriber = subscriberService.getSubscriber(callingNumber);
        int numberOfSubsBefore = subscriber.getActiveSubscriptions().size();

        HttpPost httpPost2 = createSubscriptionHttpPost(callingNumber, "pregnancyPack");

        SimpleHttpClient.execHttpRequest(httpPost2, HttpStatus.SC_OK, ADMIN_USERNAME, ADMIN_PASSWORD);

        subscriber = subscriberService.getSubscriber(callingNumber);
        int numberOfSubsAfter = subscriber.getActiveSubscriptions().size();

        // Another subscription should be allowed because these are two different packs
        assertTrue((numberOfSubsBefore + 1) == numberOfSubsAfter);
    }

    @Test
    public void testCreateSubscriptionsNoLanguageInDB() throws IOException, InterruptedException {
        setupData();
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(9999911122L, "A", "AP",
                123456789012545L, "99", "childPack");
        ObjectMapper mapper = new ObjectMapper();
        String subscriptionRequestJson = mapper.writeValueAsString(subscriptionRequest);

        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/kilkari/subscription", TestContext.getJettyPort()));
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(subscriptionRequestJson));


        // Should return HTTP 404 (Not Found) because the language won't be found
        assertTrue(SimpleHttpClient
                .execHttpRequest(httpPost, HttpStatus.SC_NOT_FOUND, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testCreateSubscriptionForUndeployedState() throws IOException, InterruptedException {
        setupData();

        District district = new District();
        district.setName("District 2");
        district.setRegionalName("District 2");
        district.setCode(2L);

        State state = new State();
        state.setName("State 2");
        state.setCode(2L);
        state.getDistricts().add(district);

        stateDataService.create(state);

        Language language = new Language("malayalam");
        languageDataService.create(language);

        LanguageLocation undeployedLanguageLocation = new LanguageLocation("77", new Circle("BB"), language, true);
        undeployedLanguageLocation.getDistrictSet().add(district);
        languageLocationDataService.create(undeployedLanguageLocation);

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(9999911122L, "A", "BB",
                123456789012545L, "77", "childPack");
        ObjectMapper mapper = new ObjectMapper();
        String subscriptionRequestJson = mapper.writeValueAsString(subscriptionRequest);

        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/kilkari/subscription", TestContext.getJettyPort()));
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(subscriptionRequestJson));

        // Should return HTTP 501 (Not Implemented) because the service is not deployed for the specified state
        assertTrue(SimpleHttpClient
                .execHttpRequest(httpPost, HttpStatus.SC_NOT_IMPLEMENTED, ADMIN_USERNAME, ADMIN_PASSWORD));
    }


    @Test
    public void testDeactivateSubscriptionRequest() throws IOException, InterruptedException {
        setupData();

        Subscriber subscriber = subscriberService.getSubscriber(1000000000L);
        Subscription subscription = subscriber.getActiveSubscriptions().iterator().next();
        String subscriptionId = subscription.getSubscriptionId();

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(1000000000L, "A", "AP",
                123456789012545L, subscriptionId);
        ObjectMapper mapper = new ObjectMapper();
        String subscriptionRequestJson = mapper.writeValueAsString(subscriptionRequest);

        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(String.format(
                "http://localhost:%d/api/kilkari/subscription", TestContext.getJettyPort()));
        httpDelete.setHeader("Content-type", "application/json");
        httpDelete.setEntity(new StringEntity(subscriptionRequestJson));

        assertTrue(SimpleHttpClient.execHttpRequest(httpDelete, HttpStatus.SC_OK, ADMIN_USERNAME,
                ADMIN_PASSWORD));

        subscription = subscriptionService.getSubscription(subscriptionId);
        assertTrue(subscription.getStatus().equals(SubscriptionStatus.DEACTIVATED));
        assertTrue(subscription.getDeactivationReason().equals(DeactivationReason.DEACTIVATED_BY_USER));
    }

    @Test
    public void testDeactivateSubscriptionRequestAlreadyInactive() throws IOException, InterruptedException {
        setupData();

        Subscriber subscriber = subscriberService.getSubscriber(1000000000L);
        Subscription subscription = subscriber.getActiveSubscriptions().iterator().next();
        String subscriptionId = subscription.getSubscriptionId();
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.DEACTIVATED_BY_USER);

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(1000000000L, "A", "AP",
                123456789012545L, subscriptionId);
        ObjectMapper mapper = new ObjectMapper();
        String subscriptionRequestJson = mapper.writeValueAsString(subscriptionRequest);

        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(String.format(
                "http://localhost:%d/api/kilkari/subscription", TestContext.getJettyPort()));
        httpDelete.setHeader("Content-type", "application/json");
        httpDelete.setEntity(new StringEntity(subscriptionRequestJson));

        // Should return HTTP 200 (OK) because DELETE on a Deactivated subscription is idempotent
        assertTrue(SimpleHttpClient.execHttpRequest(httpDelete, HttpStatus.SC_OK, ADMIN_USERNAME,
                ADMIN_PASSWORD));

        subscription = subscriptionService.getSubscription(subscriptionId);
        assertTrue(subscription.getStatus().equals(SubscriptionStatus.DEACTIVATED));
    }

    @Test
    public void testDeactivateSubscriptionRequestInvalidSubscription() throws IOException, InterruptedException {
        setupData();

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(1000000000L, "A", "AP",
                123456789012545L, "77f13128-037e-4f98-8651-285fa618d94a");
        ObjectMapper mapper = new ObjectMapper();
        String subscriptionRequestJson = mapper.writeValueAsString(subscriptionRequest);

        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(String.format(
                "http://localhost:%d/api/kilkari/subscription", TestContext.getJettyPort()));
        httpDelete.setHeader("Content-type", "application/json");
        httpDelete.setEntity(new StringEntity(subscriptionRequestJson));

        // Should return HTTP 404 (Not Found) because the subscription ID won't be found
        assertTrue(SimpleHttpClient.execHttpRequest(httpDelete, HttpStatus.SC_NOT_FOUND, ADMIN_USERNAME,
                ADMIN_PASSWORD));
    }

    private HttpPost createInboxCallDetailsRequestHttpPost(InboxCallDetailsRequest request) throws IOException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/kilkari/inboxCallDetails",
                TestContext.getJettyPort()));
        ObjectMapper mapper = new ObjectMapper();
        StringEntity params = new StringEntity(mapper.writeValueAsString(request));
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");
        return httpPost;
    }

    @Test
    public void testSaveInboxCallDetails() throws IOException, InterruptedException {
        HttpPost httpPost = createInboxCallDetailsRequestHttpPost(new InboxCallDetailsRequest(
                1234567890L, //callingNumber
                "A", //operator
                "AP", //circle
                123456789012345L, //callId
                123L, //callStartTime
                456L, //callEndTime
                123, //callDurationInPulses
                1, //callStatus
                1, //callDisconnectReason
                new HashSet<>(Arrays.asList(
                        new CallDataRequest(
                                "00000000-0000-0000-0000-000000000000", //subscriptionId
                                "48WeeksPack", //subscriptionPack
                                "123", //inboxWeekId
                                "foo", //contentFileName
                                123L, //startTime
                                456L), //endTime
                        new CallDataRequest(
                                "00000000-0000-0000-0000-000000000001", //subscriptionId
                                "72WeeksPack", //subscriptionPack
                                "123", //inboxWeekId
                                "foo", //contentFileName
                                123L, //startTime
                                456L) //endTime
                )))); //content

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testSaveInboxCallDetailsInvalidParams() throws IOException, InterruptedException {
        HttpPost httpPost = createInboxCallDetailsRequestHttpPost(new InboxCallDetailsRequest(
                1234567890L, //callingNumber
                "A", //operator
                "AP", //circle
                123456789012345L, //callId
                123L, //callStartTime
                456L, //callEndTime
                123, //callDurationInPulses
                9, //callStatus
                9, //callDisconnectReason
                null)); //content
        String expectedJsonResponse = createFailureResponseJson("<callStatus: Invalid><callDisconnectReason: Invalid>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testSaveInboxCallDetailsInvalidContent() throws IOException, InterruptedException {
        HttpPost httpPost = createInboxCallDetailsRequestHttpPost(new InboxCallDetailsRequest(
                1234567890L, //callingNumber
                "A", //operator
                "AP", //circle
                123456789012345L, //callId
                123L, //callStartTime
                456L, //callEndTime
                123, //callDurationInPulses
                1, //callStatus
                1, //callDisconnectReason
                new HashSet<>(Arrays.asList(
                        new CallDataRequest(
                                "00000000-0000-0000-0000-000000000000", //subscriptionId
                                "48WeeksPack", //subscriptionPack
                                "123", //inboxWeekId
                                "foo", //contentFileName
                                123L, //startTime
                                456L), //endTime
                        new CallDataRequest(
                                "00000000-0000-0000-0000", //subscriptionId
                                "foobar", //subscriptionPack
                                "123", //inboxWeekId
                                "foo", //contentFileName
                                123L, //startTime
                                456L) //endTime
                )))); //content
        String expectedJsonResponse = createFailureResponseJson(
                "<subscriptionId: Invalid><subscriptionPack: Invalid><content: Invalid>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void verifyFT77()
            throws IOException, InterruptedException {
        /**
         * NMS_FT_77 To check that message should be returned from inbox within
         * 7 days of user's subscription gets completed for 72Weeks Pack.
         */
        setupData();
        // 4000000000L subscribed to 72Week Pack subscription
        Subscriber subscriber = subscriberService.getSubscriber(4000000000L);
        Subscription subscription = subscriber.getSubscriptions().iterator()
                .next();
        // setting the subscription to have ended less than a week ago -- the
        // final message should be returned
        subscription.setStartDate(DateTime.now().minusDays(505));
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscriptionDataService.update(subscription);

        Pattern expectedJsonPattern = Pattern
                .compile(".*\"subscriptionPack\":\"pregnancyPack\",\"inboxWeekId\":\"w72_2\",\"contentFileName\":\"w72_2\\.wav.*");

        HttpGet httpGet = createHttpGet(true, "4000000000", true,
                "123456789012345");

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_OK,
                expectedJsonPattern, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void verifyFT35()
            throws IOException, InterruptedException {
        /**
         * To verify the that Save Inbox call Details API request should not
         * succeed with content provided for only 1 subscription Pack and not
         * even a place holder for second Pack details
         */
        cleanAllData();
        createLanguageAndSubscriptionPacks();
        // subscriber subscribed to both pack
        Subscriber subscriber = subscriberDataService.create(new Subscriber(
                3000000000L));
        Subscription subscription1 = subscriptionService.createSubscription(
                subscriber.getCallingNumber(), gLanguageLocation, gPack1,
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber.getCallingNumber(),
                gLanguageLocation, gPack2, SubscriptionOrigin.IVR);
        HttpPost httpPost = createInboxCallDetailsRequestHttpPost(new InboxCallDetailsRequest(
                3000000000L, // callingNumber
                "A", // operator
                "AP", // circle
                123456789012345L, // callId
                123L, // callStartTime
                456L, // callEndTime
                123, // callDurationInPulses
                1, // callStatus
                1, // callDisconnectReason
                new HashSet<>(Arrays.asList(new CallDataRequest(subscription1
                        .getSubscriptionId(), // subscriptionId
                        "48WeeksPack", // subscriptionPack
                        "123", // inboxWeekId
                        "foo1.wav", // contentFileName
                        123L, // startTime
                        456L))))); // content

        String expectedJsonResponse = createFailureResponseJson("<content: Invalid>");
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost,
                HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
        // assert inboxCallDetailRecord
        InboxCallDetailRecord inboxCallDetailRecord = inboxCallDetailsDataService
                .retrieve("callingNumber", 3000000000L);
        assertNull(inboxCallDetailRecord);
    }

    @Test
    @Ignore
    public void verifyFT186()
            throws IOException, InterruptedException {
        /**
         * To verify that Save Inbox call Details API request should succeed
         * with content provided for only 1 subscription Pack and place holder
         * for second Pack also present with no details.
         */
        cleanAllData();
        createLanguageAndSubscriptionPacks();
        Subscriber subscriber = subscriberDataService.create(new Subscriber(
                3000000000L));
        Subscription subscription1 = subscriptionService.createSubscription(
                subscriber.getCallingNumber(), gLanguageLocation, gPack1,
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber.getCallingNumber(),
                gLanguageLocation, gPack2, SubscriptionOrigin.IVR);
        HttpPost httpPost = createInboxCallDetailsRequestHttpPost(new InboxCallDetailsRequest(
                3000000000L, // callingNumber
                "A", // operator
                "AP", // circle
                123456789012345L, // callId
                123L, // callStartTime
                456L, // callEndTime
                123, // callDurationInPulses
                1, // callStatus
                1, // callDisconnectReason
                new HashSet<>(Arrays.asList(
                        new CallDataRequest(subscription1.getSubscriptionId(), // subscriptionId
                                "48WeeksPack", // subscriptionPack
                                "123", // inboxWeekId
                                "foo1.wav", // contentFileName
                                123L, // startTime
                                456L), null)))); // place holder for pack2

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK,
                ADMIN_USERNAME, ADMIN_PASSWORD));
        // assert inboxCallDetailRecord
        InboxCallDetailRecord inboxCallDetailRecord = inboxCallDetailsDataService
                .retrieve("callingNumber", 3000000000L);
        assertTrue(3000000000L == inboxCallDetailRecord.getCallingNumber());
        assertTrue(123456789012345L == inboxCallDetailRecord.getCallId());
        assertEquals("A", inboxCallDetailRecord.getOperator());
        assertEquals("AP", inboxCallDetailRecord.getCircle());
        assertTrue(123 == inboxCallDetailRecord.getCallDurationInPulses());
        assertTrue(1 == inboxCallDetailRecord.getCallStatus());
        assertTrue(1 == inboxCallDetailRecord.getCallDisconnectReason());
        assertTrue(123L == inboxCallDetailRecord.getCallStartTime().getMillis());
        assertTrue(456L == inboxCallDetailRecord.getCallEndTime().getMillis());

        // assert inboxCallData for 48WeeksPack
        InboxCallData inboxCallData48Pack = inboxCallDataDataService.retrieve(
                "contentFileName", "foo1.wav");
        assertEquals("foo1.wav", inboxCallData48Pack.getContentFileName());
        assertTrue(456L == inboxCallData48Pack.getEndTime().getMillis());
        assertEquals("123", inboxCallData48Pack.getInboxWeekId());
        assertTrue(123L == inboxCallData48Pack.getStartTime().getMillis());
        assertEquals(subscription1.getSubscriptionId(),
                inboxCallData48Pack.getSubscriptionId());
        assertEquals("48WeeksPack", inboxCallData48Pack.getSubscriptionPack());
    }
}
