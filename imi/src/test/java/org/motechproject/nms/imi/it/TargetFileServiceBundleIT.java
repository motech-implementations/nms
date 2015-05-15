package org.motechproject.nms.imi.it;

import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.service.TargetFileService;
import org.motechproject.nms.imi.service.contract.TargetFileNotification;
import org.motechproject.nms.kilkari.domain.CallRetry;
import org.motechproject.nms.kilkari.domain.CallStage;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
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
        hindi = languageLocationDataService.create(hindi);

        LanguageLocation urdu = new LanguageLocation("UR", aa, new Language("Urdu"), false);
        urdu.getDistrictSet().add(district);
        urdu = languageLocationDataService.create(urdu);

        SubscriptionPack childPack = subscriptionPackDataService.byName("childPack");
        SubscriptionPack pregnancyPack = subscriptionPackDataService.byName("pregnancyPack");

        Subscriber subscriber1 = new Subscriber(1111111111L, hindi, aa);
        subscriber1.setLastMenstrualPeriod(DateTime.now().minusDays(90)); // startDate will be today
        subscriberDataService.create(subscriber1);

        subscriptionService.createSubscription(1111111111L, hindi, pregnancyPack, SubscriptionOrigin.MCTS_IMPORT);

        Subscriber subscriber2 = new Subscriber(2222222222L, urdu, bb);
        subscriber2.setDateOfBirth(DateTime.now()); // startDate will be today
        subscriberDataService.create(subscriber2);

        Subscription s = subscriptionService.createSubscription(2222222222L, urdu, childPack,
                SubscriptionOrigin.MCTS_IMPORT);
        subscriptionService.deactivateSubscription(s, DeactivationReason.CHILD_DEATH);

        callRetryDataService.create(new CallRetry("123", 3333333333L, DayOfTheWeek.today(), CallStage.RETRY_1,
                "w1_m1.wav", "w1_1", hindi.getCode(), aa.getName(), "I"));
        callRetryDataService.create(new CallRetry("546", 4444444444L, DayOfTheWeek.today().nextDay(),
                CallStage.RETRY_1, "w1_m1.wav", "w1_1", hindi.getCode(), bb.getName(), "M"));
    }


    @Test
    public void testTargetFileGeneration() throws NoSuchAlgorithmException, IOException {
        setupDatabase();
        TargetFileNotification tfn = targetFileService.generateTargetFile();
        assertNotNull(tfn);

        // Should not pickup subscription2 because its status is COMPLETED nor callRetry 546 because it's for
        // tomorrow
        assertEquals(2, (int) tfn.getRecordCount());

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
