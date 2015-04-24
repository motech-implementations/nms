package org.motechproject.nms.api.osgi;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.api.utils.HttpDeleteWithBody;
import org.motechproject.nms.api.web.contract.kilkari.InboxCallDetailsRequest;
import org.motechproject.nms.api.web.contract.kilkari.InboxCallDetailsRequestCallData;
import org.motechproject.nms.api.web.contract.kilkari.SubscriptionRequest;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.repository.ServiceUsageDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.KilkariService;
import org.motechproject.nms.language.domain.Language;
import org.motechproject.nms.language.repository.CircleLanguageDataService;
import org.motechproject.nms.language.repository.LanguageDataService;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;


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
    private KilkariService kilkariService;

    @Inject
    private SubscriberDataService subscriberDataService;

    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;

    @Inject
    private SubscriptionDataService subscriptionDataService;

    @Inject
    private FrontLineWorkerService frontLineWorkerService;

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

    private void cleanAllData() {
        subscriptionDataService.deleteAll();
        subscriptionPackDataService.deleteAll();
        subscriberDataService.deleteAll();
        serviceUsageCapDataService.deleteAll();
        serviceUsageDataService.deleteAll();
        frontLineWorkerDataService.deleteAll();
        circleLanguageDataService.deleteAll();
        languageDataService.deleteAll();
    }

    private void setupData() {
        cleanAllData();

        Language ta = languageDataService.create(new Language("tamil", "10"));

        SubscriptionPack pack1 = subscriptionPackDataService.create(new SubscriptionPack("pack1"));
        SubscriptionPack pack2 = subscriptionPackDataService.create(new SubscriptionPack("pack2"));
        List<SubscriptionPack> onePack = Arrays.asList(pack1);
        List<SubscriptionPack> twoPacks = Arrays.asList(pack1, pack2);

        Subscriber subscriber1 = subscriberDataService.create(new Subscriber(1000000000L));
        Subscriber subscriber2 = subscriberDataService.create(new Subscriber(2000000000L));

        Subscription subscription1 = subscriptionDataService.create(new Subscription(subscriber1, pack1, ta));
        Subscription subscription2 = subscriptionDataService.create(new Subscription(subscriber2, pack1, ta));
        Subscription subscription3 = subscriptionDataService.create(new Subscription(subscriber2, pack2, ta));
    }

    @Test
    public void testInboxRequest() throws IOException, InterruptedException {
        setupData();
        HttpGet httpGet = new HttpGet(String.format(
            "http://localhost:%d/api/kilkari/inbox?callingNumber=1000000000&callId=123456789012345",
            TestContext.getJettyPort()));

        Subscriber subscriber = subscriberDataService.findByCallingNumber(1000000000L);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet,
            "{\"inboxSubscriptionDetailList\":[{\"subscriptionId\":\"" + subscription.getSubscriptionId().toString() +
            "\",\"subscriptionPack\":\"pack1\",\"inboxWeekId\":\"10_1\",\"contentFileName\":\"xyz.wav\"}]}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testInboxRequestBadSubscriber() throws IOException, InterruptedException {
        setupData();
        HttpGet httpGet = new HttpGet(String.format(
                "http://localhost:%d/api/kilkari/inbox?callingNumber=3000000009&callId=123456789012345",
                TestContext.getJettyPort()));

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_NOT_FOUND, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testInboxRequestNoSubscriber() throws IOException, InterruptedException {
        setupData();
        HttpGet httpGet = new HttpGet(String.format(
                "http://localhost:%d/api/kilkari/inbox?callId=123456789012345",
                TestContext.getJettyPort()));

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_BAD_REQUEST, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testCreateSubscriptionRequest() throws IOException, InterruptedException {
        setupData();

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(9999911122L, "A", "AP",
                123456789012545L, "10", "pack1");
        ObjectMapper mapper = new ObjectMapper();
        String subscriptionRequestJson = mapper.writeValueAsString(subscriptionRequest);

        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/kilkari/subscription", TestContext.getJettyPort()));
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(subscriptionRequestJson));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testCreateSubscriptionRequestInvalidPack() throws IOException, InterruptedException {
        setupData();

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(9999911122L, "A", "AP",
                123456789012545L, "10", "pack99999");
        ObjectMapper mapper = new ObjectMapper();
        String subscriptionRequestJson = mapper.writeValueAsString(subscriptionRequest);

        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/kilkari/subscription", TestContext.getJettyPort()));
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(subscriptionRequestJson));

        // Should return HTTP 404 (Not Found) because the subscription pack won't be found
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_NOT_FOUND, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testDeactivateSubscriptionRequest() throws IOException, InterruptedException {
        setupData();

        Subscriber subscriber = kilkariService.getSubscriber(1000000000L);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
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

        subscription = kilkariService.getSubscription(subscriptionId);
        assertTrue(subscription.getStatus().equals(SubscriptionStatus.DEACTIVATED));
    }

    @Test
    public void testDeactivateSubscriptionRequestAlreadyInactive() throws IOException, InterruptedException {
        setupData();

        Subscriber subscriber = kilkariService.getSubscriber(1000000000L);
        Subscription subscription = subscriber.getSubscriptions().iterator().next();
        String subscriptionId = subscription.getSubscriptionId();
        kilkariService.deactivateSubscription(subscription);

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

        subscription = kilkariService.getSubscription(subscriptionId);
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

    @Test
    public void testSaveInboxCallDetails() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/kilkari/inboxCallDetails",
                TestContext.getJettyPort()));
        InboxCallDetailsRequest request = new InboxCallDetailsRequest(
                1234567890L, //callingNumber
                "A", //operator
                "AP", //circle
                123456789012345L, //callId
                123L, //callStartTime
                456L, //callEndTime
                123, //callDurationInPulses
                1, //callStatus
                1, //callDisconnectReason
                Arrays.asList(
                    new InboxCallDetailsRequestCallData(
                        "123", //subscriptionId
                        "123", //subscriptionPack
                        "123", //inboxWeekId
                        "foo", //contentFileName
                        123L, //startTime
                        456L), //endTime
                    new InboxCallDetailsRequestCallData(
                        "123", //subscriptionId
                        "123", //subscriptionPack
                        "123", //inboxWeekId
                        "foo", //contentFileName
                        123L, //startTime
                        456L) //endTime
                )); //content

        String json = new ObjectMapper().writeValueAsString(request);
        StringEntity params = new StringEntity(json);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testSaveInboxCallDetailsInvalidParams() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/kilkari/inboxCallDetails",
                TestContext.getJettyPort()));
        InboxCallDetailsRequest request = new InboxCallDetailsRequest(
                1234567890L, //callingNumber
                "A", //operator
                "AP", //circle
                123456789012345L, //callId
                123L, //callStartTime
                456L, //callEndTime
                123, //callDurationInPulses
                9, //callStatus
                9, //callDisconnectReason
                null); //content
        String json = new ObjectMapper().writeValueAsString(request);
        StringEntity params = new StringEntity(json);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callStatus: Invalid><callDisconnectReason: Invalid>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }
}
