package org.motechproject.nms.imi.it;

import org.apache.commons.codec.binary.Hex;
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
import org.motechproject.nms.region.language.domain.Language;
import org.motechproject.nms.region.language.repository.LanguageDataService;
import org.motechproject.nms.imi.domain.CallRetry;
import org.motechproject.nms.imi.domain.CallStage;
import org.motechproject.nms.imi.domain.DayOfTheWeek;
import org.motechproject.nms.imi.repository.CallRetryDataService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.service.TargetFileNotification;
import org.motechproject.nms.imi.service.TargetFileService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
                SubscriptionPackType.CHILD, 1, null));
        SubscriptionPack pack2 = subscriptionPackDataService.create(new SubscriptionPack("two",
                SubscriptionPackType.PREGNANCY, 2, null));

        Subscriber subscriber1 = subscriberDataService.create(new Subscriber(1111111111L, hindi, "AA"));
        Subscriber subscriber2 = subscriberDataService.create(new Subscriber(2222222222L, urdu, "BB"));

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
                CallStage.RETRY_1, "HI", "AA", "I"));
        CallRetry callRetry2 = callRetryDataService.create(new CallRetry("546", 4444444444L, DayOfTheWeek.today(),
                CallStage.RETRY_1, "HI", "BB", "M"));
    }


    @Test
    public void testTargetFileGeneration() throws NoSuchAlgorithmException, IOException {
        setupDatabase();
        TargetFileNotification tfn = targetFileService.generateTargetFile();
        assertNotNull(tfn);

        // Should not pickup subscription22 because its status is COMPLETED
        assertEquals(5, (int) tfn.getRecordCount());

        //read the file to get checksum & record count
        File homeDir = new File(System.getProperty("user.home"));
        File targetDir = new File(homeDir,
                settingsService.getSettingsFacade().getProperty("imi.target_file_directory"));
        File targetFile = new File(targetDir, tfn.getFileName());
        MessageDigest md = MessageDigest.getInstance("MD5");
        int recordCount = 0;
        try (InputStream is = Files.newInputStream(targetFile.toPath());
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            DigestInputStream dis = new DigestInputStream(is, md);
            while ((reader.readLine()) != null) {
                recordCount++;
            }
        }
        String md5Checksum = new String(Hex.encodeHex(md.digest()));

        assertEquals((int)tfn.getRecordCount(), recordCount);

        assertEquals(tfn.getChecksum(), md5Checksum);
    }


    @Test
    public void testServicePresent() {
        assertTrue(targetFileService != null);
    }

    //todo: test success notification is sent to the IVR system
}
