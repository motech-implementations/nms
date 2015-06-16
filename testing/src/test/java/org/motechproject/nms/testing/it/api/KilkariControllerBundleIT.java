package org.motechproject.nms.testing.it.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.api.web.contract.BadRequest;
import org.motechproject.nms.api.web.contract.kilkari.CallDataRequest;
import org.motechproject.nms.api.web.contract.kilkari.InboxCallDetailsRequest;
import org.motechproject.nms.api.web.contract.kilkari.InboxResponse;
import org.motechproject.nms.api.web.contract.kilkari.InboxSubscriptionDetailResponse;
import org.motechproject.nms.api.web.contract.kilkari.SubscriptionRequest;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.repository.ServiceUsageDataService;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.InboxCallData;
import org.motechproject.nms.kilkari.domain.InboxCallDetailRecord;
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
import org.motechproject.nms.props.domain.DeployedService;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.props.repository.DeployedServiceDataService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.it.api.utils.HttpDeleteWithBody;
import org.motechproject.nms.testing.it.utils.RegionHelper;
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
import org.motechproject.nms.testing.service.TestingService;
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
    private CircleDataService circleDataService;
    @Inject
    private StateDataService stateDataService;
    @Inject
    private DistrictDataService districtDataService;
    @Inject
    private DeployedServiceDataService deployedServiceDataService;
    @Inject
    private TestingService testingService;
    @Inject
    private InboxCallDetailRecordDataService inboxCallDetailsDataService;
    @Inject
    private InboxCallDataDataService inboxCallDataDataService;


    private RegionHelper rh;
    private SubscriptionHelper sh;


    @Before
    public void setupTestData() {
        testingService.clearDatabase();

        rh = new RegionHelper(languageDataService, circleDataService, stateDataService, districtDataService);

        sh = new SubscriptionHelper(subscriptionService, subscriberDataService, subscriptionPackDataService,
                languageDataService, circleDataService, stateDataService, districtDataService);

        // subscriber1 subscribed to child pack only
        Subscriber subscriber1 = subscriberDataService.create(new Subscriber(1000000000L));
        subscriptionService.createSubscription(subscriber1.getCallingNumber(), rh.hindiLanguage(),
                sh.childPack(), SubscriptionOrigin.IVR);

        // subscriber2 subscribed to both child & pregnancy pack
        Subscriber subscriber2 = subscriberDataService.create(new Subscriber(2000000000L));
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), rh.hindiLanguage(),
                sh.childPack(), SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), rh.hindiLanguage(),
                sh.pregnancyPack(), SubscriptionOrigin.IVR);

        // subscriber3 subscribed to pregnancy pack only
        Subscriber subscriber3 = subscriberDataService.create(new Subscriber(4000000000L));
        subscriptionService.createSubscription(subscriber3.getCallingNumber(), rh.hindiLanguage(),
                sh.pregnancyPack(), SubscriptionOrigin.IVR);

        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.KILKARI));
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

        Subscriber subscriber = subscriberDataService.findByCallingNumber(1000000000L); // 1 subscription
        Subscription subscription = subscriber.getSubscriptions().iterator().next();

        // override the default start date (today + 1 day) in order to see a non-empty inbox
        subscription.setStartDate(DateTime.now().minusDays(2));
        subscriptionDataService.update(subscription);

        HttpGet httpGet = createHttpGet(true, "1000000000", true, "123456789012345");
        String expectedJson = createInboxResponseJson(new HashSet<>(Arrays.asList(
                new InboxSubscriptionDetailResponse(
                        subscription.getSubscriptionId().toString(),
                        sh.childPack().getName(),
                        sh.getWeekId(subscription, 0),
                        sh.getContentMessageFile(subscription, 0)
                )
        )));

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJson, EntityUtils.toString(response.getEntity()));
    }

    
    @Test
    public void testInboxRequestTwoSubscriptions() throws IOException, InterruptedException {

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now().minusDays(250));
        subscriberDataService.create(mctsSubscriber);

        // create subscription to child pack
        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        // due to subscription rules detailed in #157, we need to clear out the DOB and set an LMP in order to
        // create a second subscription for this MCTS subscriber
        mctsSubscriber.setDateOfBirth(null);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(103));
        subscriberDataService.update(mctsSubscriber);

        // create subscription to pregnancy pack
        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.MCTS_IMPORT);

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

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(30));
        subscriberDataService.create(mctsSubscriber);
        // create subscription to pregnancy pack, not due to start for 60 days
        subscriptionService.createSubscription(9999911122L, rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.MCTS_IMPORT);

        Pattern expectedJsonPattern = Pattern.compile(
                ".*\"subscriptionPack\":\"pregnancyPack\",\"inboxWeekId\":null,\"contentFileName\":null.*");

        HttpGet httpGet = createHttpGet(true, "9999911122", true, "123456789012345");
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, expectedJsonPattern, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testInboxRequestCompletedSubscription() throws IOException, InterruptedException {

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
        subscriptionService.createSubscription(2000000000L, rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.MCTS_IMPORT);
        subscriber = subscriberDataService.findByCallingNumber(2000000000L);

        // due to subscription rules detailed in #157, we need to clear out the DOB and set an LMP in order to
        // create a second subscription for this subscriber
        subscriber.setDateOfBirth(null);
        subscriber.setLastMenstrualPeriod(DateTime.now().minusDays(103));
        subscriberDataService.update(subscriber);

        // create subscription to pregnancy pack
        subscriptionService.createSubscription(2000000000L, rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.MCTS_IMPORT);

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

        HttpGet httpGet = createHttpGet(true, "3000000000", true, "123456789012345");
        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Not Found>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_NOT_FOUND, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }


    @Test
    public void testInboxRequestNoSubscriber() throws IOException, InterruptedException {

        HttpGet httpGet = createHttpGet(false, null, true, "123456789012345");
        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Not Present>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }


    private HttpPost createSubscriptionHttpPost(long callingNumber, String subscriptionPack)
            throws IOException {

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
                callingNumber,
                rh.airtelOperator(),
                rh.delhiCircle().getName(),
                123456789012545L,
                rh.hindiLanguage().getCode(),
                subscriptionPack);
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
        HttpPost httpPost = createSubscriptionHttpPost(9999911122L, sh.childPack().getName());

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }


    @Test
    public void testCreateSubscriptionRequestInvalidPack() throws IOException, InterruptedException {
        HttpPost httpPost = createSubscriptionHttpPost(9999911122L, "pack99999");

        // Should return HTTP 404 (Not Found) because the subscription pack won't be found
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
    }


    @Test
    public void testCreateSubscriptionRequestSamePack() throws IOException, InterruptedException {
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
        long callingNumber = 9999911122L;

        HttpPost httpPost = createSubscriptionHttpPost(callingNumber, sh.childPack().getName());
        HttpResponse resp = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());

        Subscriber subscriber = subscriberService.getSubscriber(callingNumber);
        int numberOfSubsBefore = subscriber.getActiveSubscriptions().size();

        httpPost = createSubscriptionHttpPost(callingNumber, sh.pregnancyPack().getName());
        resp = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());

        subscriber = subscriberService.getSubscriber(callingNumber);
        int numberOfSubsAfter = subscriber.getActiveSubscriptions().size();

        // Another subscription should be allowed because these are two different packs
        assertTrue((numberOfSubsBefore + 1) == numberOfSubsAfter);
    }


    @Test
    public void testCreateSubscriptionsNoLanguageInDB() throws IOException, InterruptedException {
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(9999911122L, rh.airtelOperator(),
                rh.delhiCircle().getName(), 123456789012545L, "99", sh.childPack().getName());
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

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
                9999911122L,
                rh.airtelOperator(),
                rh.karnatakaCircle().getName(),
                123456789012545L,
                rh.kannadaLanguage().getCode(),
                sh.childPack().getName());
        ObjectMapper mapper = new ObjectMapper();
        String subscriptionRequestJson = mapper.writeValueAsString(subscriptionRequest);

        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/kilkari/subscription", TestContext.getJettyPort()));
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(subscriptionRequestJson));

        // Should return HTTP 501 (Not Implemented) because the service is not deployed for the specified state
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());
    }


    @Test
    public void testDeactivateSubscriptionRequest() throws IOException, InterruptedException {

        Subscriber subscriber = subscriberService.getSubscriber(1000000000L);
        Subscription subscription = subscriber.getActiveSubscriptions().iterator().next();
        String subscriptionId = subscription.getSubscriptionId();

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(1000000000L, rh.airtelOperator(),
                rh.delhiCircle().getName(), 123456789012545L, subscriptionId);
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

        Subscriber subscriber = subscriberService.getSubscriber(1000000000L);
        Subscription subscription = subscriber.getActiveSubscriptions().iterator().next();
        String subscriptionId = subscription.getSubscriptionId();
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.DEACTIVATED_BY_USER);

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(1000000000L, rh.airtelOperator(), rh.delhiCircle().getName(),
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

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(1000000000L, rh.airtelOperator(), rh.delhiCircle().getName(),
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
                rh.airtelOperator(), //operator
                rh.delhiCircle().getName(), //circle
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
                rh.airtelOperator(), //operator
                rh.delhiCircle().getName(), //circle
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
                rh.airtelOperator(), //operator
                rh.delhiCircle().getName(), //circle
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
    
    /**
     * NMS_FT_22 To verify the that Save Inbox call Details API request should succeed with content being saved for both
     * Packs as blank.
     */
    @Test
    public void verifyFT22()
            throws IOException, InterruptedException {
        HttpPost httpPost = createInboxCallDetailsRequestHttpPost(new InboxCallDetailsRequest(
                3000000000L, // callingNumber
                "A", // operator
                "AP", // circle
                123456789012345L, // callId
                123L, // callStartTime
                456L, // callEndTime
                123, // callDurationInPulses
                2, // callStatus
                4, // callDisconnectReason
                new HashSet<CallDataRequest>())); // content

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
        assertTrue(2 == inboxCallDetailRecord.getCallStatus());
        assertTrue(4 == inboxCallDetailRecord.getCallDisconnectReason());
        assertTrue(123L == inboxCallDetailRecord.getCallStartTime().getMillis());
        assertTrue(456L == inboxCallDetailRecord.getCallEndTime().getMillis());

    }


    /**
     * To verify the that Save Inbox call Details API request should succeed with content being saved for both Packs.
     */
    @Test
    public void verifyFT23() throws IOException, InterruptedException {

        Subscriber subscriber1 = subscriberDataService.findByCallingNumber(1000000000L);
        Subscription subscription1 = subscriber1.getAllSubscriptions().iterator().next();
        Subscriber subscriber2 = subscriberDataService.findByCallingNumber(2000000000L);
        Subscription subscription2 = subscriber2.getAllSubscriptions().iterator().next();

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
                                456L), // endTime
                        new CallDataRequest(subscription2.getSubscriptionId(), // subscriptionId

                                "72WeeksPack", // subscriptionPack
                                "124", // inboxWeekId
                                "foo2.wav", // contentFileName
                                192L, // startTime
                                678L) // endTime
                )))); // content

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

        // assert inboxCallData for both packs
        InboxCallData inboxCallData48Pack = inboxCallDataDataService.retrieve(
                "contentFileName", "foo1.wav");
        assertEquals("foo1.wav", inboxCallData48Pack.getContentFileName());
        assertTrue(456L == inboxCallData48Pack.getEndTime().getMillis());
        assertEquals("123", inboxCallData48Pack.getInboxWeekId());
        assertTrue(123L == inboxCallData48Pack.getStartTime().getMillis());
        assertEquals(subscription1.getSubscriptionId(),
                inboxCallData48Pack.getSubscriptionId());
        assertEquals("48WeeksPack", inboxCallData48Pack.getSubscriptionPack());

        InboxCallData inboxCallData72Pack = inboxCallDataDataService.retrieve(
                "contentFileName", "foo2.wav");
        assertEquals("foo2.wav", inboxCallData72Pack.getContentFileName());
        assertTrue(678L == inboxCallData72Pack.getEndTime().getMillis());
        assertEquals("124", inboxCallData72Pack.getInboxWeekId());
        assertTrue(192L == inboxCallData72Pack.getStartTime().getMillis());
        assertEquals(subscription2.getSubscriptionId(),
                inboxCallData72Pack.getSubscriptionId());
        assertEquals("72WeeksPack", inboxCallData72Pack.getSubscriptionPack());
    }


    /**
     * NMS_FT_77 To check that message should be returned from inbox within days of user's subscription gets completed
     * for Pregnancy Pack.
     */
    @Test
    public void verifyFT77() throws IOException, InterruptedException {

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

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertTrue(Pattern.matches(expectedJsonPattern.pattern(), EntityUtils.toString(response.getEntity())));

    }


    /**
     * NMS_FT_75 To check that no message should be returned from inbox after 7 days of user's subscription gets
     * completed for Pregnancy Pack.
     */
    @Test
    public void verifyFT75() throws IOException, InterruptedException {

        // 4000000000L subscribed to Pregnancy Pack subscription
        Subscriber subscriber = subscriberService.getSubscriber(4000000000L);
        Subscription subscription = subscriber.getSubscriptions().iterator()
                .next();
        // setting the subscription to have ended more than a week ago -- no
        // message should be returned
        subscription.setStartDate(DateTime.now().minusDays(512));
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscriptionDataService.update(subscription);

        String expectedJson = "{\"inboxSubscriptionDetailList\":[]}";

        HttpGet httpGet = createHttpGet(true, "4000000000", true,
                "123456789012345");
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertTrue(expectedJson.equals(EntityUtils.toString(response.getEntity()))  );

    }

    /*
     * To verify that Get Inbox Details API request fails if the provided parameter value of callingNumber is blank.
     */
    @Test
    public void verifyFT92() throws IOException, InterruptedException {

        HttpGet httpGet = createHttpGet(true, "", true, "123456789012345");
        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Not Present>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response.getEntity())));
    }

    /*
     * To verify that Get Inbox Details API request fails if the provided parameter value of callId is blank.
     */
    @Test
    public void verifyFT93() throws IOException, InterruptedException {

        HttpGet httpGet = createHttpGet(true, "1234567890", true, "");
        String expectedJsonResponse = createFailureResponseJson("<callId: Not Present>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response.getEntity())));
    }

    /*
     * To check that message for both Packs should be returned from inbox within 7 days of user's subscription gets
     * completed for Pregnancy Pack while user is subscribed for both Packs.
     */
    public void verifyFT78() throws IOException, InterruptedException {
        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now());
        subscriberDataService.create(mctsSubscriber);

        // create subscription to child pack
        Subscription childPackSubscription = subscriptionService
                .createSubscription(9999911122L, rh.hindiLanguage(),
                        sh.childPack(),
                        SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        // due to subscription rules detailed in #157, we need to clear out the
        // DOB and set an LMP in order to
        // create a second subscription for this MCTS subscriber
        mctsSubscriber.setDateOfBirth(null);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now());
        subscriberDataService.update(mctsSubscriber);

        // create subscription to pregnancy pack
        Subscription pregnancyPackSubscription = subscriptionService
                .createSubscription(9999911122L, rh.hindiLanguage(),
                        sh.pregnancyPack(),
                        SubscriptionOrigin.MCTS_IMPORT);
        // update pregnancy subscription pack to mark complete
        // setting the subscription to have ended less than a week ago -- the
        // final message should be
        // returned
        subscriptionService.updateStartDate(pregnancyPackSubscription, DateTime
                .now().minusDays(505 + 90));

        Pattern childPackJsonPattern = Pattern
                .compile(".*\"subscriptionId\":\""
                        + childPackSubscription.getSubscriptionId()
                        + "\",\"subscriptionPack\":\"childPack\",\"inboxWeekId\":\"w1_1\",\"contentFileName\":\"w1_1.wav.*");
        Pattern pregnancyPackJsonPattern = Pattern
                .compile(".*\"subscriptionId\":\""
                        + pregnancyPackSubscription.getSubscriptionId()
                        + "\",\"subscriptionPack\":\"pregnancyPack\",\"inboxWeekId\":\"w72_2\",\"contentFileName\":\"w72_2.wav.*");

        HttpGet httpGet = createHttpGet(true, "9999911122", true,
                "123456789012345");
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_OK,
                childPackJsonPattern, ADMIN_USERNAME, ADMIN_PASSWORD));
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_OK,
                pregnancyPackJsonPattern, ADMIN_USERNAME, ADMIN_PASSWORD));
    }


    /*
     * To check that only message for Active Pack should be returned from inbox after 7 days of user's subscription gets
     * completed for Pregnancy Pack while user is subscribed to both Packs.
     */
    @Test
    public void verifyFT76() throws IOException, InterruptedException {
        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now());
        subscriberDataService.create(mctsSubscriber);

        // create subscription to child pack for which start date is as per DOB
        Subscription childPackSubscription = subscriptionService.createSubscription(
                9999911122L,
                rh.hindiLanguage(),
                sh.childPack(),
                SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        // due to subscription rules detailed in #157, we need to clear out the DOB and set an LMP in order to
        // create a second subscription for this MCTS subscriber
        mctsSubscriber.setDateOfBirth(null);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now());
        subscriberDataService.update(mctsSubscriber);

        // create subscription to pregnancy pack
        Subscription pregnancyPackSubscription = subscriptionService.createSubscription(
                9999911122L,
                rh.hindiLanguage(),
                sh.pregnancyPack(),
                SubscriptionOrigin.MCTS_IMPORT);

        // update pregnancy subscription pack to mark complete
        // setting the subscription to have ended more than a week ago -- no message should be returned
        subscriptionService.updateStartDate(pregnancyPackSubscription, DateTime
                .now().minusDays(512 + 90));

        String expectedJsonResponse = "{\"inboxSubscriptionDetailList\":[{\"subscriptionId\":\""
                + childPackSubscription.getSubscriptionId()
                + "\",\"subscriptionPack\":\"childPack\",\"inboxWeekId\":\"w1_1\",\"contentFileName\":\"w1_1.wav\"}]}";

        HttpGet httpGet = createHttpGet(true, "9999911122", true,
                "123456789012345");
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_OK,
                expectedJsonResponse, ADMIN_USERNAME, ADMIN_PASSWORD));
    }


    /*
     * To check that messages for both Packs should be returned from inbox within 7 days of user's subscription gets
     * completed for Child Pack while user is subscribed for both Packs.
     */
    @Test
    public void verifyFT82() throws IOException, InterruptedException {

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now());
        subscriberDataService.create(mctsSubscriber);

        // create subscription to child pack
        Subscription childPackSubscription = subscriptionService
                .createSubscription(9999911122L, rh.hindiLanguage(),
                        sh.childPack(),
                        SubscriptionOrigin.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        // update child pack subscription to mark complete
        // setting the subscription to have ended less than a week ago -- the
        // final message should be
        // returned
        subscriptionService.updateStartDate(childPackSubscription, DateTime
                .now().minusDays(337));

        // due to subscription rules detailed in #157, we need to clear out the
        // DOB and set an LMP in order to
        // create a second subscription for this MCTS subscriber
        mctsSubscriber.setDateOfBirth(null);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(91));
        subscriberDataService.update(mctsSubscriber);

        // create subscription to pregnancy pack
        Subscription pregnancyPackSubscription = subscriptionService
                .createSubscription(9999911122L, rh.hindiLanguage(),
                        sh.pregnancyPack(),
                        SubscriptionOrigin.MCTS_IMPORT);

        Pattern childPackJsonPattern = Pattern
                .compile(".*\"subscriptionId\":\""
                        + childPackSubscription.getSubscriptionId()
                        + "\",\"subscriptionPack\":\"childPack\",\"inboxWeekId\":\"w48_1\",\"contentFileName\":\"w48_1.wav.*");
        Pattern pregnancyPackJsonPattern = Pattern
                .compile(".*\"subscriptionId\":\""
                        + pregnancyPackSubscription.getSubscriptionId()
                        + "\",\"subscriptionPack\":\"pregnancyPack\",\"inboxWeekId\":\"w1_1\",\"contentFileName\":\"w1_1.wav.*");

        HttpGet httpGet = createHttpGet(true, "9999911122", true,
                "123456789012345");
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_OK,
                childPackJsonPattern, ADMIN_USERNAME, ADMIN_PASSWORD));
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_OK,
                pregnancyPackJsonPattern, ADMIN_USERNAME, ADMIN_PASSWORD));
    }


    /*
     * To check that only message for Active Pack should be returned from inbox after 7 days of user's subscription gets
     * completed for Child Pack while user is subscribed for both Packs.
     */
    @Test
    public void verifyFT80() throws IOException, InterruptedException {

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(DateTime.now());
        subscriberDataService.create(mctsSubscriber);

        // create subscription to child pack
        Subscription childPackSubscription = subscriptionService
                .createSubscription(9999911122L, rh.hindiLanguage(),
                        sh.childPack(),
                        SubscriptionOrigin.MCTS_IMPORT);

        // update child Pack Subscription to mark complete
        // setting the subscription to have ended more than a week ago -- no
        // message should be returned
        subscriptionService.updateStartDate(childPackSubscription, DateTime
                .now().minusDays(344));

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        // due to subscription rules detailed in #157, we need to clear out the
        // DOB and set an LMP in order to
        // create a second subscription for this MCTS subscriber
        mctsSubscriber.setDateOfBirth(null);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(91));
        subscriberDataService.update(mctsSubscriber);

        // create subscription to pregnancy pack for which start date is
        // yesterday
        // date as per LMP
        Subscription pregnancyPackSubscription = subscriptionService
                .createSubscription(9999911122L, rh.hindiLanguage(),
                        sh.pregnancyPack(),
                        SubscriptionOrigin.MCTS_IMPORT);

        String expectedJsonResponse = "{\"inboxSubscriptionDetailList\":[{\"subscriptionId\":\""
                + pregnancyPackSubscription.getSubscriptionId()
                + "\",\"subscriptionPack\":\"pregnancyPack\",\"inboxWeekId\":\"w1_1\",\"contentFileName\":\"w1_1.wav\"}]}";

        HttpGet httpGet = createHttpGet(true, "9999911122", true,
                "123456789012345");
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_OK,
                expectedJsonResponse, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /*
     * To verify the behavior of Get Inbox Details API if provided beneficiary's callingNumber is less than 10 digits.
     */
    @Test
    public void verifyFT83() throws IOException, InterruptedException {

    	HttpGet httpGet = createHttpGet(true, "123456789", true, "123456789012345");
    	String expectedJsonResponse = createFailureResponseJson("<callingNumber: Invalid>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response.getEntity()))  );
    }


    /*
     * To verify the behavior of  Get Inbox Details API  if provided beneficiary's callingNumber is more than 10 digits.
     */
    @Test
    public void verifyFT84() throws IOException, InterruptedException {
        HttpGet httpGet = createHttpGet(true, "12345678901", true, "123456789012345");
        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Invalid>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response.getEntity()))  );
    }

    /**
     * To verify the behavior of Save Inbox call Details API if provided beneficiary's subscriptionPack does not exist.
     */
    @Test
    public void verifyFT36() throws IOException, InterruptedException {
        Subscriber subscriber = subscriberDataService.create(new Subscriber(
                3000000000L));
        Subscription subscription1 = subscriptionService.createSubscription(
                subscriber.getCallingNumber(), rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);
        Subscription subscription2 = subscriptionService.createSubscription(
                subscriber.getCallingNumber(), rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.IVR);
        HttpPost httpPost = createInboxCallDetailsRequestHttpPost(new InboxCallDetailsRequest(
                1234567890L, // callingNumber
                "A", // operator
                "AP", // circle
                123456789012345L, // callId
                123L, // callStartTime
                456L, // callEndTime
                123, // callDurationInPulses
                1, // callStatus
                1, // callDisconnectReason
                new HashSet<>(Arrays.asList(
                        new CallDataRequest(
                                subscription1.getSubscriptionId(), // subscriptionId
                                sh.childPack().getName(), // subscriptionPack
                                "123", // inboxWeekId
                                "foo", // contentFileName
                                123L, // startTime
                                456L), // endTime
                        new CallDataRequest(
                                subscription2.getSubscriptionId(), // subscriptionId
                                "12WeeksPack", // Invalid subscriptionPack
                                "123", // inboxWeekId
                                "foo", // contentFileName
                                123L, // startTime
                                456L) // endTime
                )))); // content
        String expectedJsonResponse = createFailureResponseJson("<subscriptionPack: Invalid><content: Invalid>");
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost,
                HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }


    /**
     * To verify that Save Inbox call Details API request fails if specified subscription doesn't exist for beneficiary.
     */
    @Test
    //TODO: https://applab.atlassian.net/browse/NMS-178
    @Ignore
    public void verifyFT185() throws IOException, InterruptedException {
        // subscribed caller with deactivated subscription i.e no active and
        // pending subscriptions
        Subscriber subscriber = subscriberDataService.create(new Subscriber(
                3000000000L));
        Subscription subscription1 = subscriptionService.createSubscription(
                subscriber.getCallingNumber(), rh.hindiLanguage(), sh.childPack(),
                SubscriptionOrigin.IVR);
        // deactivate subscription
        subscriptionService.deactivateSubscription(subscription1,
                DeactivationReason.DEACTIVATED_BY_USER);
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
                                // refer
                                // deactivated
                                // subscription
                                "48WeeksPack", // subscriptionPack
                                "123", // inboxWeekId
                                "foo", // contentFileName
                                123L, // startTime
                                456L), // endTime
                        new CallDataRequest(
                                "ae7681ae-1f3c-4dba-365d-4b26e19f4335", // subscriptionId
                                // not
                                // exist
                                "72WeeksPack", // subscriptionPack
                                "123", // inboxWeekId
                                "foo", // contentFileName
                                123L, // startTime
                                456L) // endTime
                )))); // content
        String expectedJsonResponse = createFailureResponseJson("<subscriptionId: Invalid><content: Invalid>");
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost,
                HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));

    }


    /*
     * To verify the behavior of Get Inbox Details API if provided beneficiary's callId is not valid: more than 15 digits.
     */
    @Test
    //TODO: https://applab.atlassian.net/browse/NMS-186
    @Ignore
    public void verifyFT85() throws IOException, InterruptedException {

        // callingNumber alphanumeric
        HttpGet httpGet = createHttpGet(true, "12345DF7890", true,
                "123456789012345");
        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Invalid>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response.getEntity()))  );

    }

    /**
     * To verify the that Save Inbox call Details API request should not succeed with content provided for only 1
     * subscription pack and not even a place holder for second Pack details
     */
    @Test
    public void verifyFT35() throws IOException, InterruptedException {
        // subscriber subscribed to both pack
        Subscriber subscriber = subscriberDataService.create(new Subscriber(3000000000L));
        Subscription subscription = subscriptionService.createSubscription(subscriber.getCallingNumber(),
                rh.hindiLanguage(), sh.childPack(), SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber.getCallingNumber(), rh.hindiLanguage(),
                sh.pregnancyPack(), SubscriptionOrigin.IVR);
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
                new HashSet<>(Arrays.asList(new CallDataRequest(subscription
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


    /**
     * To verify that Save Inbox call Details API request should succeed with content provided for only 1 subscription
     * Pack and place holder for second Pack also present with no details.
     */
    @Test
    //todo: need JIRA issue #
    @Ignore
    public void verifyFT186() throws IOException, InterruptedException {
        Subscriber subscriber = subscriberDataService.create(new Subscriber(3000000000L));
        Subscription subscription = subscriptionService.createSubscription(subscriber.getCallingNumber(),
                rh.hindiLanguage(), sh.childPack(), SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber.getCallingNumber(), rh.hindiLanguage(),
                sh.pregnancyPack(), SubscriptionOrigin.IVR);
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
                        new CallDataRequest(subscription.getSubscriptionId(), // subscriptionId
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
        assertEquals(subscription.getSubscriptionId(), inboxCallData48Pack.getSubscriptionId());
        assertEquals("48WeeksPack", inboxCallData48Pack.getSubscriptionPack());
    }


    /**
     * This method is a utility method for running the test cases.
     */
    private HttpGet createGetSubscriberDetailsRequest(String callingNumber, String operator, String circle,
                                                      String callId) {

        StringBuilder sb = new StringBuilder(String.format(
                "http://localhost:%d/api/kilkari/user?",
                TestContext.getJettyPort()));
        String sep = "";
        if (callingNumber != null) {
            sb.append(String.format("callingNumber=%s", callingNumber));
            sep = "&";
        }
        if (operator != null) {
            sb.append(String.format("%soperator=%s", sep, operator));
            sep = "&";
        }
        if (circle != null) {
            sb.append(String.format("%scircle=%s", sep, circle));
            sep = "&";
        }
        if (callId != null) {
            sb.append(String.format("%scallId=%s", sep, callId));
            sep = "&";
        }

        return new HttpGet(sb.toString());
    }


    /**
     * test GetSubscriberDetails API with Blank Params
     */
    @Test
    public void verifyFT17() throws IOException, InterruptedException {
        HttpGet httpGet = createGetSubscriberDetailsRequest("", // callingNumber
                // Blank
                "A", // operator
                "AP", // circle
                "123456789012345" // callId
        );

        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Not Present>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet,
                HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }


    /**
     * test GetSubscriberDetails API with Blank Params
     */
    @Test
    public void verifyFT18() throws IOException, InterruptedException {
        HttpGet httpGet = createGetSubscriberDetailsRequest("1234567890", // callingNumber
                "", // operator Blank(optional param)
                "AP", // circle
                "123456789012345" // callId
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine()
                .getStatusCode());
    }


    /**
     * test GetSubscriberDetails API with Blank Params
     */
    @Test
    public void verifyFT19() throws IOException, InterruptedException {
        HttpGet httpGet = createGetSubscriberDetailsRequest("1234567890", // callingNumber
                "A", // operator
                "", // circle Blank (optional param)
                "123456789012345" // callId
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine()
                .getStatusCode());
    }


    /**
     * test GetSubscriberDetails API with Blank Params
     */
    @Test
    public void verifyFT20() throws IOException, InterruptedException {
        HttpGet httpGet = createGetSubscriberDetailsRequest("1234567890", // callingNumber
                "A", // operator
                "AP", // circle
                "" // callId Blank
        );

        String expectedJsonResponse = createFailureResponseJson("<callId: Not Present>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet,
                HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /**
     * To check NMS is able to make available a single message of current week in inbox with single message per week
     * configuration, when:
     *  (a) user's MSISDN is subscribed for Pregnancy Pack.
     *  (b) user's MSISDN is deactivated for an old subscription of Pregnancy Pack.
     */
    @Test
    public void verifyFT109() throws IOException, InterruptedException {
        // setup data to remove 2 messages per week configuration for Pregnancy pack
        testingService.clearDatabase();

        rh = new RegionHelper(languageDataService, circleDataService, stateDataService, districtDataService);

        sh = new SubscriptionHelper(subscriptionService, subscriberDataService, subscriptionPackDataService,
                languageDataService, circleDataService, stateDataService, districtDataService);

        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.KILKARI));

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(null);

        // set LMP for old pack
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(180));
        subscriberDataService.create(mctsSubscriber);

        // create old subscription for pregnancy pack and deactivate it
        Subscription oldSubscription = subscriptionService.createSubscription(
                9999911122L, rh.hindiLanguage(), sh.pregnancyPack(1), SubscriptionOrigin.MCTS_IMPORT);
        subscriptionService.deactivateSubscription(oldSubscription, DeactivationReason.DEACTIVATED_BY_USER);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        // create new subscription for pregnancy pack in Active state such that next OBD date falls on current date
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(90));
        subscriberDataService.update(mctsSubscriber);
        Subscription newSubscription = subscriptionService.createSubscription(
                9999911122L, rh.hindiLanguage(), sh.pregnancyPack(1), SubscriptionOrigin.MCTS_IMPORT);

        String expectedJsonResponse = "{\"inboxSubscriptionDetailList\":[{\"subscriptionId\":\""
                + newSubscription.getSubscriptionId()
                + "\",\"subscriptionPack\":\"pregnancyPack\",\"inboxWeekId\":\"w1_1\",\"contentFileName\":\"w1_1.wav\"}]}";

        HttpGet httpGet = createHttpGet(true, "9999911122", true, "123456789012345");
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_OK,
                expectedJsonResponse, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /**
     * To check NMS is able to make available a single message of current week in inbox with single message per week
     * configuration, when:
     *  (a) user's MSISDN is subscribed for Pregnancy Pack.
     *  (b) user's MSISDN status is completed for an old subscription of Pregnancy Pack.
     */
    @Test
    public void verifyFT110() throws IOException, InterruptedException {
        // setup data to remove 2 messages per week configuration for Pregnancy pack
        testingService.clearDatabase();

        rh = new RegionHelper(languageDataService, circleDataService, stateDataService, districtDataService);

        sh = new SubscriptionHelper(subscriptionService, subscriberDataService, subscriptionPackDataService,
                languageDataService, circleDataService, stateDataService, districtDataService);

        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.KILKARI));

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(null);

        // set LMP for old pack
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(180));
        subscriberDataService.create(mctsSubscriber);

        // create old subscription for pregnancy pack
        Subscription oldSubscription = subscriptionService.createSubscription(
                9999911122L, rh.hindiLanguage(), sh.pregnancyPack(1), SubscriptionOrigin.MCTS_IMPORT);

        // update old pregnancy subscription pack to complete, setting the subscription to have ended > a week ago
        subscriptionService.updateStartDate(oldSubscription, DateTime.now().minusDays(512 + 90));

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        // create new subscription to pregnancy pack in Active state such that next OBD date falls on current date
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(90));
        subscriberDataService.update(mctsSubscriber);
        Subscription newSubscription = subscriptionService.createSubscription(
                9999911122L, rh.hindiLanguage(), sh.pregnancyPack(1), SubscriptionOrigin.MCTS_IMPORT);

        String expectedJsonResponse = "{\"inboxSubscriptionDetailList\":[{\"subscriptionId\":\""
                + newSubscription.getSubscriptionId()
                + "\",\"subscriptionPack\":\"pregnancyPack\",\"inboxWeekId\":\"w1_1\",\"contentFileName\":\"w1_1.wav\"}]}";

        HttpGet httpGet = createHttpGet(true, "9999911122", true, "123456789012345");
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_OK,
                expectedJsonResponse, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /**
     * To check anonymous user is able to access Kilkari with multiple states in
     * user's circle, given that service is deployed in at least one of these
     * states
     */
    @Test
    // TODO: https://applab.atlassian.net/browse/NMS-196
    @Ignore
    public void verifyFT124() throws IOException, InterruptedException {
        // setup state1 data
        Language language1 = new Language("Ur", "urdu");
        languageDataService.create(language1);

        District district1 = new District();
        district1.setName("Lucknow");
        district1.setRegionalName("Lucknow");
        district1.setLanguage(language1);
        district1.setCode(11L);

        State state1 = new State();
        state1.setName("UP");
        state1.setCode(11L);
        state1.getDistricts().add(district1);

        stateDataService.create(state1);

        // setup state2 data
        Language language2 = new Language("Br", "bhojpuri");
        languageDataService.create(language2);

        District district2 = new District();
        district2.setName("Bhopal");
        district2.setRegionalName("Bhopal");
        district2.setLanguage(language2);
        district2.setCode(21L);

        State state2 = new State();
        state2.setName("MP");
        state2.setCode(21L);
        state2.getDistricts().add(district2);

        stateDataService.create(state2);

        // deployed KILKARI service for state1 only
        deployedServiceDataService.create(new DeployedService(state1,
                Service.KILKARI));

        Language language3 = new Language("RJ", "Rajasthai");
        languageDataService.create(language3);

        // create circle and add states to it
        Circle circle = new Circle("NR");
        circle.getStates().add(state1);
        circle.getStates().add(state2);
        circleDataService.create(circle);

        // setup create subscription request
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
                9999911122L, rh.airtelOperator(), circle.getName(),
                123456789012545L, language3.getCode(),
                sh.childPack().getName());
        ObjectMapper mapper = new ObjectMapper();
        String subscriptionRequestJson = mapper
                .writeValueAsString(subscriptionRequest);
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/kilkari/subscription",
                TestContext.getJettyPort()));
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(subscriptionRequestJson));

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        Subscriber subscriber = subscriberDataService
                .findByCallingNumber(9999911122L);
        assertNotNull(subscriber);
        assertNotNull(subscriber.getSubscriptions());
    }

    /**
     * To check anonymous user is not able to access Kilkari with multiple
     * states in user's circle and service is not deployed in at least one of
     * these states.
     */
    @Test
    public void verifyFT126() throws IOException, InterruptedException {
        // setup state1 data
        Language language1 = new Language("Ur", "urdu");
        languageDataService.create(language1);

        District district1 = new District();
        district1.setName("Lucknow");
        district1.setRegionalName("Lucknow");
        district1.setLanguage(language1);
        district1.setCode(11L);

        State state1 = new State();
        state1.setName("UP");
        state1.setCode(11L);
        state1.getDistricts().add(district1);

        stateDataService.create(state1);

        // setup state2 data
        Language language2 = new Language("Br", "bhojpuri");
        languageDataService.create(language2);

        District district2 = new District();
        district2.setName("Bhopal");
        district2.setRegionalName("Bhopal");
        district2.setLanguage(language2);
        district2.setCode(21L);

        State state2 = new State();
        state2.setName("MP");
        state2.setCode(21L);
        state2.getDistricts().add(district2);

        stateDataService.create(state2);

        // Not deployed KILKARI service for state1 and state2

        Language language3 = new Language("RJ", "Rajasthai");
        languageDataService.create(language3);

        // create circle and add states to it
        Circle circle = new Circle("NR");
        circle.getStates().add(state1);
        circle.getStates().add(state2);
        circleDataService.create(circle);

        // setup create subscription request
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
                9999911122L, rh.airtelOperator(), circle.getName(),
                123456789012545L, language3.getCode(), sh.childPack().getName());
        ObjectMapper mapper = new ObjectMapper();
        String subscriptionRequestJson = mapper
                .writeValueAsString(subscriptionRequest);
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/kilkari/subscription",
                TestContext.getJettyPort()));
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(subscriptionRequestJson));

        String expectedJsonResponse = createFailureResponseJson("<KILKARI: Not Deployed In State>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine()
                .getStatusCode());
        Subscriber subscriber = subscriberDataService
                .findByCallingNumber(9999911122L);
        assertNull(subscriber);

    }

     /* NMS_FT_21 To verify the that Save Inbox call Details API request should
     * succeed for unsubscribed caller or caller with no active subscription
     * without any content being saved.
     */
    @Test
    public void verifyFT21() throws IOException, InterruptedException {
        // Test for unsubscribed caller
        HttpPost httpPost = createInboxCallDetailsRequestHttpPost(new InboxCallDetailsRequest(
                1234567899L, // unsubscribed callingNumber
                "A", // operator
                "AP", // circle
                123456789012345L, // callId
                123L, // callStartTime
                456L, // callEndTime
                123, // callDurationInPulses
                2, // callStatus
                4, // callDisconnectReason
                null)); // no content

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK,
                ADMIN_USERNAME, ADMIN_PASSWORD));
        // assert inboxCallDetailRecord
        InboxCallDetailRecord inboxCallDetailRecord = inboxCallDetailsDataService
                .retrieve("callingNumber", 1234567899L);
        assertTrue(1234567899L == inboxCallDetailRecord.getCallingNumber());
        assertTrue(123456789012345L == inboxCallDetailRecord.getCallId());
        assertEquals("A", inboxCallDetailRecord.getOperator());
        assertEquals("AP", inboxCallDetailRecord.getCircle());
        assertTrue(123 == inboxCallDetailRecord.getCallDurationInPulses());
        assertTrue(2 == inboxCallDetailRecord.getCallStatus());
        assertTrue(4 == inboxCallDetailRecord.getCallDisconnectReason());
        assertTrue(123L == inboxCallDetailRecord.getCallStartTime().getMillis());
        assertTrue(456L == inboxCallDetailRecord.getCallEndTime().getMillis());

        // subscribed caller with deactivated subscription i.e no active and
        // pending subscriptions
        Subscriber subscriber = subscriberDataService.create(new Subscriber(
                5000000000L));
        Subscription subscription1 = subscriptionService.createSubscription(
                subscriber.getCallingNumber(), rh.hindiLanguage(),
                sh.childPack(), SubscriptionOrigin.IVR);
        subscriptionService.deactivateSubscription(subscription1,
                DeactivationReason.DEACTIVATED_BY_USER);
        httpPost = createInboxCallDetailsRequestHttpPost(new InboxCallDetailsRequest(
                5000000000L, // callingNumber
                "A", // operator
                "AP", // circle
                123456789012345L, // callId
                123L, // callStartTime
                456L, // callEndTime
                123, // callDurationInPulses
                3, // callStatus
                5, // callDisconnectReason
                null)); // no content

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK,
                ADMIN_USERNAME, ADMIN_PASSWORD));
        // assert inboxCallDetailRecord
        InboxCallDetailRecord inboxCallDetailRecord2 = inboxCallDetailsDataService
                .retrieve("callingNumber", 5000000000L);
        assertEquals(5000000000L,
                (long) inboxCallDetailRecord2.getCallingNumber());
        assertEquals(123456789012345L,
                (long) inboxCallDetailRecord2.getCallId());
        assertEquals("A", inboxCallDetailRecord2.getOperator());
        assertEquals("AP", inboxCallDetailRecord2.getCircle());
        assertEquals(123,
                (int) inboxCallDetailRecord2.getCallDurationInPulses());
        assertEquals(3, (int) inboxCallDetailRecord2.getCallStatus());
        assertEquals(5, (int) inboxCallDetailRecord2.getCallDisconnectReason());
        assertEquals(123L, (long) inboxCallDetailRecord2.getCallStartTime()
                .getMillis());
        assertEquals(456L, (long) inboxCallDetailRecord2.getCallEndTime()
                .getMillis());
    }
    /**
     * To check NMS is able to make available a message corresponding to each
     * Pack of current week when user is subscribed to both pregnancy Pack and
     * child Pack with single message per week configuration .
     **/
    @Test
    public void verifyFT119() throws IOException, InterruptedException {
        // update 2 messages/week to 1 message/week configuration for pregnancy
        // pack
        sh.pregnancyPackFor1MessagePerWeek(subscriptionPackMessageDataService);
        
        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        // set DOB for child pack such that
        mctsSubscriber.setDateOfBirth(DateTime.now().minusDays(14));
        mctsSubscriber.setLastMenstrualPeriod(null);
        subscriberDataService.create(mctsSubscriber);

        // create subscription for child pack in Active state
        Subscription childPackSubscription = subscriptionService
                .createSubscription(9999911122L, rh.hindiLanguage(),
                        sh.childPack(), SubscriptionOrigin.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        // create new subscription for pregnancy pack in Active state
        mctsSubscriber.setDateOfBirth(null);
        mctsSubscriber.setLastMenstrualPeriod(DateTime.now().minusDays(90));
        subscriberDataService.update(mctsSubscriber);
        Subscription pregnancyPackSubscription = subscriptionService
                .createSubscription(9999911122L, rh.hindiLanguage(),
                        sh.pregnancyPack(), SubscriptionOrigin.MCTS_IMPORT);

        Pattern childPackJsonPattern = Pattern
                .compile(".*\"subscriptionId\":\""
                        + childPackSubscription.getSubscriptionId()
                        + "\",\"subscriptionPack\":\"childPack\",\"inboxWeekId\":\"w3_1\",\"contentFileName\":\"w3_1.wav.*");
        Pattern pregnancyPackJsonPattern = Pattern
                .compile(".*\"subscriptionId\":\""
                        + pregnancyPackSubscription.getSubscriptionId()
                        + "\",\"subscriptionPack\":\"pregnancyPack\",\"inboxWeekId\":\"w1_1\",\"contentFileName\":\"w1_1.wav.*");

        HttpGet httpGet = createHttpGet(true, "9999911122", true,
                "123456789012345");
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_OK,
                childPackJsonPattern, ADMIN_USERNAME, ADMIN_PASSWORD));
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_OK,
                pregnancyPackJsonPattern, ADMIN_USERNAME, ADMIN_PASSWORD));
    }
}
