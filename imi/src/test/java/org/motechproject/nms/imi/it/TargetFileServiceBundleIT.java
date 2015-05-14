package org.motechproject.nms.imi.it;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.kilkari.domain.CallRetry;
import org.motechproject.nms.kilkari.domain.CallStage;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.service.contract.TargetFileNotification;
import org.motechproject.nms.imi.service.TargetFileService;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.repository.StateDataService;
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

import static org.junit.Assert.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class TargetFileServiceBundleIT extends BasePaxIT {

    @Inject
    TargetFileService targetFileService;

    @Inject
    SubscriptionService subscriptionService;

    @Inject
    SubscriberDataService subscriberDataService;

    @Inject
    SubscriptionPackDataService subscriptionPackDataService;

    @Inject
    CallRetryDataService callRetryDataService;

    @Inject
    LanguageDataService languageDataService;

    @Inject
    LanguageLocationDataService languageLocationDataService;

    @Inject
    private CircleDataService circleDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    SettingsService settingsService;

    private void setupDatabase() {
        subscriptionService.deleteAll();
        subscriptionPackDataService.deleteAll();
        subscriberDataService.deleteAll();
        languageLocationDataService.deleteAll();
        languageDataService.deleteAll();
        districtDataService.deleteAll();
        stateDataService.deleteAll();
        circleDataService.deleteAll();
        callRetryDataService.deleteAll();

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);

        stateDataService.create(state);

        Circle aa = new Circle("AA");
        Circle bb = new Circle("BB");

        LanguageLocation hindi = new LanguageLocation("HI", aa, new Language("Hindi"), false);
        hindi.getDistrictSet().add(district);
        languageLocationDataService.create(hindi);

        LanguageLocation urdu = new LanguageLocation("UR", aa, new Language("Urdu"), false);
        urdu.getDistrictSet().add(district);
        languageLocationDataService.create(urdu);

        SubscriptionPack pack1 = subscriptionPackDataService.create(new SubscriptionPack("one",
                SubscriptionPackType.CHILD, 48, 1, null));
        SubscriptionPack pack2 = subscriptionPackDataService.create(new SubscriptionPack("two",
                SubscriptionPackType.PREGNANCY, 72, 2, null));

        Subscriber subscriber1 = subscriberDataService.create(new Subscriber(1111111111L, hindi, aa));
        Subscriber subscriber2 = subscriberDataService.create(new Subscriber(2222222222L, urdu, bb));

        Subscription s = new Subscription(subscriber1, pack1, SubscriptionOrigin.IVR);
        s.setStatus(SubscriptionStatus.ACTIVE);
        Subscription subscription11 = subscriptionService.create(s);

        s = new Subscription(subscriber1, pack2, SubscriptionOrigin.IVR);
        s.setStatus(SubscriptionStatus.ACTIVE);
        Subscription subscription12 = subscriptionService.create(s);

        s = new Subscription(subscriber2, pack1, SubscriptionOrigin.IVR);
        s.setStatus(SubscriptionStatus.ACTIVE);
        Subscription subscription21 = subscriptionService.create(s);

        s = new Subscription(subscriber2, pack2, SubscriptionOrigin.IVR);
        s.setStatus(SubscriptionStatus.COMPLETED);
        Subscription subscription22 = subscriptionService.create(s);

        CallRetry callRetry1 = callRetryDataService.create(new CallRetry("123", 3333333333L, DayOfTheWeek.today(),
                CallStage.RETRY_1, hindi.getCode(), aa.getName(), "I"));
        CallRetry callRetry2 = callRetryDataService.create(new CallRetry("546", 4444444444L, DayOfTheWeek.today(),
                CallStage.RETRY_1, hindi.getCode(), bb.getName(), "M"));
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
