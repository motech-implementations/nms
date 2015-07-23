package org.motechproject.nms.testing.it.imi;

import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.service.SmsNotificationService;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class ImiSMSNotificationServiceBundleIT extends BasePaxIT {

    private static final String SMS_NOTIFICATION_URL = "imi.sms.notification.url";
    private static final String MAX_NOTIFICATION_RETRY_COUNT = "imi.notification_retry_count";

    @Inject
    SettingsService settingsService;

    @Inject
    SmsNotificationService smsNotificationService;

    @Inject
    TestingService testingService;

    SettingsFacade settingsFacade;
    String oldEndpoint, oldRetryCount;

    @Before
    public void cleanupDatabase() {
        testingService.clearDatabase();

        settingsFacade = settingsService.getSettingsFacade();
        oldEndpoint = settingsFacade.getProperty(SMS_NOTIFICATION_URL);
        oldRetryCount = settingsFacade.getProperty(MAX_NOTIFICATION_RETRY_COUNT);
    }

    @After
    public void restore() {
        settingsFacade.setProperty(SMS_NOTIFICATION_URL,oldEndpoint);
        settingsFacade.setProperty(MAX_NOTIFICATION_RETRY_COUNT,oldRetryCount);
    }

    @Test
    public void verifySmsThreadNotBlocking() {
        // linking with an API which always returns 202
        settingsFacade.setProperty(SMS_NOTIFICATION_URL,
                "http://www.mocky.io/v2/55acd492052573e005262f0f");
        assertTrue(smsNotificationService.sendSms(1234567890l));
    }

    @Test
    public void verifyNonBlockingWithout202Response() {
        // linking with an API which always returns 400
        settingsFacade.setProperty(SMS_NOTIFICATION_URL,
                "http://www.mocky.io/v2/55af66c9e37b45d902e77b50");
        assertTrue(smsNotificationService.sendSms(1234567890l));
    }

    @Test
    public void testSmsWithIncreamentedRetryCounts() {
        // linking with an API which always returns 400
        settingsFacade.setProperty(SMS_NOTIFICATION_URL,
                "http://www.mocky.io/v2/55af66c9e37b45d902e77b50");
        // retry count to 20
        settingsFacade.setProperty(MAX_NOTIFICATION_RETRY_COUNT,
                "20");
        assertTrue(smsNotificationService.sendSms(1234567890l));
    }

    @Test
    public void testSmsWithTimeoutApi() {
        // linking with a fake API to check timeout
        settingsFacade.setProperty(SMS_NOTIFICATION_URL,
                "fakeAPI");
        // retry count to 10
        settingsFacade.setProperty(MAX_NOTIFICATION_RETRY_COUNT,
                "10");
        assertTrue(smsNotificationService.sendSms(1234567890l));
    }
}
