package org.motechproject.nms.api.osgi;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.api.web.contract.kilkari.InboxCallDetailsRequest;
import org.motechproject.nms.api.web.contract.kilkari.InboxCallDetailsRequestCallData;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.KilkariService;
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

    private void setupData() {
        subscriptionDataService.deleteAll();
        subscriptionPackDataService.deleteAll();
        subscriberDataService.deleteAll();

        SubscriptionPack pack1 = subscriptionPackDataService.create(new SubscriptionPack("pack1"));
        SubscriptionPack pack2 = subscriptionPackDataService.create(new SubscriptionPack("pack2"));
        List<SubscriptionPack> onePack = Arrays.asList(pack1);
        List<SubscriptionPack> twoPacks = Arrays.asList(pack1, pack2);

        Subscriber subscriber1 = subscriberDataService.create(new Subscriber("1000000000"));
        Subscriber subscriber2 = subscriberDataService.create(new Subscriber("2000000000"));

        Subscription subscription1 = subscriptionDataService.create(new Subscription("001", subscriber1, pack1));
        Subscription subscription2 = subscriptionDataService.create(new Subscription("002", subscriber2, pack1));
        Subscription subscription3 = subscriptionDataService.create(new Subscription("003", subscriber2, pack2));
    }

    @Test
    public void testInboxRequest() throws IOException, InterruptedException {
        setupData();
        HttpGet httpGet = new HttpGet(String.format(
            "http://localhost:%d/api/kilkari/inbox?callingNumber=1000000000&callId=123456789012345",
            TestContext.getJettyPort()));

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet,
            "{\"inboxSubscriptionDetailList\":[{\"subscriptionId\":\"001\",\"subscriptionPack\":\"pack1\",\"inboxWeekId\":\"10_1\",\"contentFileName\":\"xyz.wav\"}]}",
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
    public void testSaveInboxCallDetails() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/kilkari/inboxCallDetails",
                TestContext.getJettyPort()));
        InboxCallDetailsRequest request = new InboxCallDetailsRequest(
                "1234567890", //callingNumber
                "A", //operator
                "AP", //circle
                "123456789012345", //callId
                "123", //callStartTime
                "456", //callEndTime
                "123", //callDurationInPulses
                "1", //callStatus
                "1", //callDisconnectReason
                Arrays.asList(
                    new InboxCallDetailsRequestCallData(
                        "123", //subscriptionId
                        "123", //subscriptionPack
                        "123", //inboxWeekId
                        "foo", //contentFileName
                        "123", //startTime
                        "456"), //endTime
                    new InboxCallDetailsRequestCallData(
                        "123", //subscriptionId
                        "123", //subscriptionPack
                        "123", //inboxWeekId
                        "foo", //contentFileName
                        "123", //startTime
                        "456") //endTime
                ), //content
                null); //failureReason
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
                "1234567890", //callingNumber
                "A", //operator
                "AP", //circle
                "123456789012345", //callId
                "123", //callStartTime
                "456", //callEndTime
                "123", //callDurationInPulses
                "X", //callStatus
                "Y", //callDisconnectReason
                null, //content
                null); //failureReason
        String json = new ObjectMapper().writeValueAsString(request);
        StringEntity params = new StringEntity(json);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callStatus: Invalid><callDisconnectReason: Invalid>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }
}
