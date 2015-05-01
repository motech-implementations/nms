package org.motechproject.nms.outbounddialer.it;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionMode;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.language.domain.Language;
import org.motechproject.nms.language.repository.LanguageDataService;
import org.motechproject.nms.outbounddialer.domain.CallRetry;
import org.motechproject.nms.outbounddialer.domain.CallStage;
import org.motechproject.nms.outbounddialer.domain.DayOfTheWeek;
import org.motechproject.nms.outbounddialer.repository.CallRetryDataService;
import org.motechproject.nms.outbounddialer.service.SettingsService;
import org.motechproject.nms.outbounddialer.service.TargetFileNotification;
import org.motechproject.nms.outbounddialer.service.TargetFileService;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class TargetFileServiceBundleIT extends BasePaxIT {

    @Inject
    TargetFileService targetFileService;

    @Inject
    SubscriptionDataService subscriptionDataService;

    @Inject
    SubscriberDataService subscriberDataService;

    @Inject
    SubscriptionPackDataService subscriptionPackDataService;

    @Inject
    CallRetryDataService callRetryDataService;

    @Inject
    LanguageDataService languageDataService;

    @Inject
    SettingsService settingsService;

    private void setupDatabase() {
        subscriptionDataService.deleteAll();
        subscriptionPackDataService.deleteAll();
        subscriberDataService.deleteAll();
        languageDataService.deleteAll();
        callRetryDataService.deleteAll();

        Language hindi = languageDataService.create(new Language("Hindi", "HI"));
        Language urdu = languageDataService.create(new Language("Urdu", "UR"));

        SubscriptionPack pack1 = subscriptionPackDataService.create(new SubscriptionPack("one",
                SubscriptionPackType.CHILD));
        SubscriptionPack pack2 = subscriptionPackDataService.create(new SubscriptionPack("two",
                SubscriptionPackType.PREGNANCY));

        Subscriber subscriber1 = subscriberDataService.create(new Subscriber(1111111111L, hindi));
        Subscriber subscriber2 = subscriberDataService.create(new Subscriber(2222222222L, urdu));

        Subscription s = new Subscription(subscriber1, pack1, SubscriptionMode.IVR);
        s.setStatus(SubscriptionStatus.ACTIVE);
        Subscription subscription11 = subscriptionDataService.create(s);

        s = new Subscription(subscriber1, pack2, SubscriptionMode.IVR);
        s.setStatus(SubscriptionStatus.ACTIVE);
        Subscription subscription12 = subscriptionDataService.create(s);

        s = new Subscription(subscriber2, pack1, SubscriptionMode.IVR);
        s.setStatus(SubscriptionStatus.ACTIVE);
        Subscription subscription21 = subscriptionDataService.create(s);

        s = new Subscription(subscriber2, pack2, SubscriptionMode.IVR);
        s.setStatus(SubscriptionStatus.COMPLETED);
        Subscription subscription22 = subscriptionDataService.create(s);

        CallRetry callRetry1 = callRetryDataService.create(new CallRetry("123", 3333333333L, DayOfTheWeek.today(),
                CallStage.Retry1, "HI"));
        CallRetry callRetry2 = callRetryDataService.create(new CallRetry("546", 4444444444L, DayOfTheWeek.today(),
                CallStage.Retry1, "HI"));
    }


    @Test
    public void testTargetFileGeneration() {
        SettingsFacade settingsFacade = settingsService.getSettingsFacade();

        String oldNotificationUrl = settingsFacade.getProperty("outbound-dialer.target_file_notification_url");
        settingsFacade.setProperty("outbound-dialer.target_file_notification_url", "http://xxx.yyy/zzz");
        setupDatabase();
        TargetFileNotification tfn = targetFileService.generateTargetFile();
        settingsFacade.setProperty("outbound-dialer.target_file_notification_url", oldNotificationUrl);
        assertNotNull(tfn);

        // Should not pickup subscription22 because its status is COMPLETED
        assertEquals(5, (int) tfn.getRecordCount());

        //todo: verify tfn data actually matches created file
    }


    @Test
    public void testServicePresent() {
        assertTrue(targetFileService != null);
    }
}
