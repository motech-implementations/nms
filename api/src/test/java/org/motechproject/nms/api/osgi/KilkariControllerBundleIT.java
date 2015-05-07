package org.motechproject.nms.api.osgi;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.api.utils.HttpDeleteWithBody;
import org.motechproject.nms.api.web.contract.BadRequest;
import org.motechproject.nms.api.web.contract.kilkari.InboxCallDetailsRequest;
import org.motechproject.nms.api.web.contract.kilkari.CallDataRequest;
import org.motechproject.nms.api.web.contract.kilkari.InboxResponse;
import org.motechproject.nms.api.web.contract.kilkari.InboxSubscriptionDetailResponse;
import org.motechproject.nms.api.web.contract.kilkari.SubscriptionRequest;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.repository.ServiceUsageDataService;
import org.motechproject.nms.kilkari.domain.*;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackMessageDataService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.language.domain.Language;
import org.motechproject.nms.region.language.repository.CircleLanguageDataService;
import org.motechproject.nms.region.language.repository.LanguageDataService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.http.SimpleHttpClient;
import org.motechproject.testing.utils.TestContext;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

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
    private CircleLanguageDataService circleLanguageDataService;

    public KilkariControllerBundleIT() {
        System.setProperty("org.motechproject.testing.osgi.http.numTries", "1");
    }

    private Language gLanguage;
    private SubscriptionPack gPack1;
    private SubscriptionPack gPack2;

    private void cleanAllData() {
        subscriptionDataService.deleteAll();
        subscriptionPackDataService.deleteAll();
        subscriptionPackMessageDataService.deleteAll();
        subscriberDataService.deleteAll();
        serviceUsageCapDataService.deleteAll();
        serviceUsageDataService.deleteAll();
        frontLineWorkerDataService.deleteAll();
        circleLanguageDataService.deleteAll();
        languageDataService.deleteAll();
    }

    private void createLanguageAndSubscriptionPacks() {
        gLanguage = languageDataService.create(new Language("tamil", "10"));

        subscriptionService.createSubscriptionPacks();
        gPack1 = subscriptionPackDataService.byName("childPack"); // 48 weeks, 1 message per week
        gPack2 = subscriptionPackDataService.byName("pregnancyPack"); // 72 weeks, 2 messages per week
    }

    private void setupData() {
        cleanAllData();
        createLanguageAndSubscriptionPacks();

        Subscriber subscriber1 = subscriberDataService.create(new Subscriber(1000000000L));
        Subscriber subscriber2 = subscriberDataService.create(new Subscriber(2000000000L));

        subscriptionService.createSubscription(subscriber1.getCallingNumber(), gLanguage, gPack1,
                SubscriptionMode.IVR);
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), gLanguage, gPack1,
                SubscriptionMode.IVR);
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), gLanguage, gPack2,
                SubscriptionMode.IVR);
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
        subscription.setStartDate(LocalDate.now().minusDays(2));
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
        mctsSubscriber.setDateOfBirth(LocalDate.now().minusDays(250));
        subscriberDataService.create(mctsSubscriber);

        // create subscription to child pack
        subscriptionService.createSubscription(9999911122L, gLanguage, gPack1, SubscriptionMode.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        // due to subscription rules detailed in #157, we need to clear out the DOB and set an LMP in order to
        // create a second subscription for this MCTS subscriber
        mctsSubscriber.setDateOfBirth(null);
        mctsSubscriber.setLastMenstrualPeriod(LocalDate.now().minusDays(103));
        subscriberDataService.update(mctsSubscriber);

        // create subscription to pregnancy pack
        subscriptionService.createSubscription(9999911122L, gLanguage, gPack2, SubscriptionMode.MCTS_IMPORT);

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
        mctsSubscriber.setLastMenstrualPeriod(LocalDate.now().minusDays(30));
        subscriberDataService.create(mctsSubscriber);
        // create subscription to pregnancy pack, not due to start for 60 days
        subscriptionService.createSubscription(9999911122L, gLanguage, gPack2, SubscriptionMode.MCTS_IMPORT);

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
        subscription.setStartDate(LocalDate.now().minusDays(500));
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
        subscription.setStartDate(LocalDate.now().minusDays(340));
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscriptionDataService.update(subscription);

        Pattern expectedJsonPattern = Pattern.compile(".*\"subscriptionPack\":\"childPack\",\"inboxWeekId\":\"w48_1\",\"contentFileName\":\"w48_1\\.wav.*");

        HttpGet httpGet = createHttpGet(true, "1000000000", true, "123456789012345");
        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, expectedJsonPattern, ADMIN_USERNAME, ADMIN_PASSWORD));
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

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testCreateSubscriptionRequestInvalidPack() throws IOException, InterruptedException {
        setupData();
        HttpPost httpPost = createSubscriptionHttpPost(9999911122L, "pack99999");

        // Should return HTTP 404 (Not Found) because the subscription pack won't be found
        assertTrue(SimpleHttpClient
                .execHttpRequest(httpPost, HttpStatus.SC_NOT_FOUND, ADMIN_USERNAME, ADMIN_PASSWORD));
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
    public void testCreateSubscriptionViaMcts() {
        setupData();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(LocalDate.now().minusDays(14));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, gLanguage, gPack1, SubscriptionMode.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());
    }

    @Test
    public void testCreateDuplicateChildSubscriptionViaMcts() {
        setupData();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(LocalDate.now().minusDays(14));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, gLanguage, gPack1, SubscriptionMode.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());

        // attempt to create subscription to the same pack -- should fail
        subscriptionService.createSubscription(9999911122L, gLanguage, gPack1, SubscriptionMode.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());
    }

    @Test
    public void testCreateDuplicatePregnancySubscriptionViaMcts() {
        setupData();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(LocalDate.now().minusDays(28));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, gLanguage, gPack2, SubscriptionMode.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());

        // attempt to create subscription to the same pack -- should fail
        subscriptionService.createSubscription(9999911122L, gLanguage, gPack2, SubscriptionMode.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());
    }

    @Test
    public void testCreateSecondPregnancySubscriptionAfterDeactivationViaMcts() {
        setupData();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setLastMenstrualPeriod(LocalDate.now().minusDays(28));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, gLanguage, gPack2, SubscriptionMode.MCTS_IMPORT);
        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        Subscription pregnancySubscription = mctsSubscriber.getActiveSubscriptions().iterator().next();
        pregnancySubscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscriptionDataService.update(pregnancySubscription);

        // attempt to create subscription to the same pack -- should succeed
        subscriptionService.createSubscription(9999911122L, gLanguage, gPack2, SubscriptionMode.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(1, mctsSubscriber.getActiveSubscriptions().size());
    }

    @Test
    public void testCreateSubscriptionsToDifferentPacksViaMcts() {
        setupData();

        Subscriber mctsSubscriber = new Subscriber(9999911122L);
        mctsSubscriber.setDateOfBirth(LocalDate.now().minusDays(14));
        subscriberDataService.create(mctsSubscriber);

        subscriptionService.createSubscription(9999911122L, gLanguage, gPack1, SubscriptionMode.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);

        // due to subscription rules detailed in #157, we need to clear out the DOB and set an LMP in order to
        // create a second subscription for this MCTS subscriber
        mctsSubscriber.setDateOfBirth(null);
        mctsSubscriber.setLastMenstrualPeriod(LocalDate.now().minusDays(100));
        subscriberDataService.update(mctsSubscriber);

        // attempt to create subscription to a different pack
        subscriptionService.createSubscription(9999911122L, gLanguage, gPack2, SubscriptionMode.MCTS_IMPORT);

        mctsSubscriber = subscriberDataService.findByCallingNumber(9999911122L);
        assertEquals(2, mctsSubscriber.getActiveSubscriptions().size());
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
}
