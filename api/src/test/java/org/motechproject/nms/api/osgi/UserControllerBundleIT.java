package org.motechproject.nms.api.osgi;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.Service;
import org.motechproject.nms.flw.domain.ServiceUsage;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
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
 * Verify that LanguageService HTTP service is present and functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class UserControllerBundleIT extends BasePaxIT {
    private static final String ADMIN_USERNAME = "motech";
    private static final String ADMIN_PASSWORD = "motech";

    @Inject
    private KilkariService kilkariService;
    @Inject
    private SubscriberDataService subscriberDataService;
    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    private FrontLineWorkerService frontLineWorkerService;
    @Inject
    private FrontLineWorkerDataService frontLineWorkerDataService;
    @Inject
    private ServiceUsageDataService serviceUsageDataService;

    private void setupData() {
        subscriptionPackDataService.deleteAll();
        SubscriptionPack pack1 = subscriptionPackDataService.create(new SubscriptionPack("pack1"));
        SubscriptionPack pack2 = subscriptionPackDataService.create(new SubscriptionPack("pack2"));
        List<SubscriptionPack> onePack = Arrays.asList(pack1);
        List<SubscriptionPack> twoPacks = Arrays.asList(pack1, pack2);

        subscriberDataService.deleteAll();
        Subscriber subscriber1 = subscriberDataService.create(new Subscriber("0000000000", onePack));
        Subscriber subscriber2 = subscriberDataService.create(new Subscriber("0000000001", twoPacks));
    }

    private void setupFLWData() {
        frontLineWorkerDataService.deleteAll();

        FrontLineWorker flwNoService = new FrontLineWorker("No Service Worker", "0000000000");
        frontLineWorkerService.add(flwNoService);

        FrontLineWorker flwService1 = new FrontLineWorker("No Service Worker", "1111111111");
        frontLineWorkerService.add(flwService1);

        FrontLineWorker flwService2 = new FrontLineWorker("No Service Worker", "2222222222");
        frontLineWorkerService.add(flwService2);

        // A service record with endOfService and WelcomePrompt played
        ServiceUsage serviceUsage1 = new ServiceUsage(flwService1, Service.MOBILE_KUNJI, 1, 1, 1, DateTime.now());
        serviceUsageDataService.create(serviceUsage1);

        // A service record without endOfService and WelcomePrompt played
        ServiceUsage serviceUsage2 = new ServiceUsage(flwService2, Service.MOBILE_KUNJI, 1, 0, 0, DateTime.now());
        serviceUsageDataService.create(serviceUsage2);
    }

    @Test
    public void testKilkariUserRequest() throws IOException, InterruptedException {
        setupData();
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/kilkari/user?callingNumber=0000000001&operator=OP&circle=AA&callId=0123456789abcde", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, "{\"languageLocationCode\":null,\"defaultLanguageLocationCode\":null,\"subscriptionPackList\":[\"pack2\",\"pack1\"]}"));
    }

    @Test
    public void testFLWUserRequestWithoutServiceUsage() throws IOException, InterruptedException {
        setupFLWData();
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/mobilekunji/user?callingNumber=0000000000&operator=OP&circle=AA&callId=0123456789abcde", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, "{\"languageLocationCode\":10,\"defaultLanguageLocationCode\":10,\"currentUsageInPulses\":0,\"maxAllowedUsageInPulses\":3600,\"endOfUsagePromptCounter\":0,\"maxAllowedEndOfUsagePrompt\":2,\"welcomePromptFlag\":false}"));
    }

    @Test
    public void testFLWUserRequestWithServiceUsageOnly() throws IOException, InterruptedException {
        setupFLWData();
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/mobilekunji/user?callingNumber=1111111111&operator=OP&circle=AA&callId=0123456789abcde", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, "{\"languageLocationCode\":10,\"defaultLanguageLocationCode\":10,\"currentUsageInPulses\":1,\"maxAllowedUsageInPulses\":3600,\"endOfUsagePromptCounter\":0,\"maxAllowedEndOfUsagePrompt\":2,\"welcomePromptFlag\":false}"));
    }

    @Test
    public void testFLWUserRequestWithServiceUsageAndEndOfUsageAndWelcomeMsg() throws IOException, InterruptedException {
        setupFLWData();
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/mobilekunji/user?callingNumber=2222222222&operator=OP&circle=AA&callId=0123456789abcde", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, "{\"languageLocationCode\":10,\"defaultLanguageLocationCode\":10,\"currentUsageInPulses\":1,\"maxAllowedUsageInPulses\":3600,\"endOfUsagePromptCounter\":1,\"maxAllowedEndOfUsagePrompt\":2,\"welcomePromptFlag\":true}"));
    }

    @Test
    public void testInvalidServiceName() throws IOException, InterruptedException {
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/NO_SERVICE/user?callingNumber=0123456789&operator=OP&circle=AA&callId=0123456789abcde", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        //todo: replace with execHttpRequest method that also tests response body (in addition to status code)
        //todo: when it's available in platform: org.motechproject.testing.osgi.http.SimpleHttpClient
        SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @Ignore
    public void testNoCallingNumber() throws IOException, InterruptedException {
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/kilkari/user?operator=OP&circle=AA&callId=0123456789abcde", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        SimpleHttpClient.execHttpRequest(httpGet, "{\"failureReason\":\"<callingNumber: Not Present>\"}");
    }

    @Test
    @Ignore
    public void testInvalidCallingNumber() throws IOException, InterruptedException {
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/kilkari/user?callingNumber=XXXXXXX&operator=OP&circle=AA&callId=0123456789abcde", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        SimpleHttpClient.execHttpRequest(httpGet, "{\"failureReason\":\"<callingNumber: Invalid>\"}");
    }

    @Test
    @Ignore
    public void testNoOperator() throws IOException, InterruptedException {
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/kilkari/user?callingNumber=XXXXXXX&circle=AA&callId=0123456789abcde", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        SimpleHttpClient.execHttpRequest(httpGet, "{\"failureReason\":\"<operator: Not Present>\"}");
    }

    @Test
    @Ignore
    public void testNoCircle() throws IOException, InterruptedException {
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/kilkari/user?callingNumber=XXXXXXX&operator=OP&callId=0123456789abcde", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        SimpleHttpClient.execHttpRequest(httpGet, "{\"failureReason\":\"<callId: Not Present>\"}");
    }

    @Test
    @Ignore
    public void testNoCallId() throws IOException, InterruptedException {
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/kilkari/user?callingNumber=XXXXXXX&operator=OP&circle=AA", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        SimpleHttpClient.execHttpRequest(httpGet, "{\"failureReason\":\"<callId: Not Present>\"}");
    }
}
