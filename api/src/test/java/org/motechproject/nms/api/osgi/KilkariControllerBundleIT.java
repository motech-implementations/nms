package org.motechproject.nms.api.osgi;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.repository.ServiceUsageDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
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

        Language ta = languageDataService.create(new Language("tamil", 10));

        SubscriptionPack pack1 = subscriptionPackDataService.create(new SubscriptionPack("pack1"));
        SubscriptionPack pack2 = subscriptionPackDataService.create(new SubscriptionPack("pack2"));
        List<SubscriptionPack> onePack = Arrays.asList(pack1);
        List<SubscriptionPack> twoPacks = Arrays.asList(pack1, pack2);

        Subscriber subscriber1 = subscriberDataService.create(new Subscriber("0000000000"));
        Subscriber subscriber2 = subscriberDataService.create(new Subscriber("0000000001"));

        Subscription subscription1 = subscriptionDataService.create(new Subscription(subscriber1, pack1, ta));
        Subscription subscription2 = subscriptionDataService.create(new Subscription(subscriber2, pack1, ta));
        Subscription subscription3 = subscriptionDataService.create(new Subscription(subscriber2, pack2, ta));
    }

    @Test
    public void testInboxRequest() throws IOException, InterruptedException {
        setupData();
        HttpGet httpGet = new HttpGet(String.format(
            "http://localhost:%d/api/kilkari/inbox?callingNumber=0000000000&callId=0123456789abcde",
            TestContext.getJettyPort()));

        Subscriber subscriber = subscriberDataService.findByCallingNumber("0000000000");
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
                "http://localhost:%d/api/kilkari/inbox?callingNumber=0000000009&callId=0123456789abcde",
                TestContext.getJettyPort()));

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_NOT_FOUND, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testInboxRequestNoSubscriber() throws IOException, InterruptedException {
        setupData();
        HttpGet httpGet = new HttpGet(String.format(
                "http://localhost:%d/api/kilkari/inbox?callId=0123456789abcde",
                TestContext.getJettyPort()));

        assertTrue(SimpleHttpClient.execHttpRequest(httpGet, HttpStatus.SC_BAD_REQUEST, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

}
