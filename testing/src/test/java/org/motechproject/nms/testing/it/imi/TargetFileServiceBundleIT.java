package org.motechproject.nms.testing.it.imi;

import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.it.api.utils.SubscriptionPackBuilder;
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
import org.motechproject.nms.testing.service.TestingService;
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

    private static final String LOCAL_OBD_DIR = "imi.local_obd_dir";
    private static final String REMOTE_OBD_DIR = "imi.remote_obd_dir";


    private String localObdDirBackup;
    private String remoteObdDirBackup;

    @Inject
    TargetFileService targetFileService;

    @Inject
    SubscriptionService subscriptionService;

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
    private CircleDataService circleDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    SettingsService settingsService;

    @Inject
    TestingService testingService;


    Circle aa;
    Circle bb;
    Language hindi;
    Language urdu;
    SubscriptionPack childPack;
    SubscriptionPack pregnancyPack;


    private String setupTestDir(String property, String dir) {
        String backup = settingsService.getSettingsFacade().getProperty(property);
        File directory = new File(System.getProperty("user.home"), dir);
        directory.mkdirs();
        settingsService.getSettingsFacade().setProperty(property, directory.getAbsolutePath());
        return backup;
    }


    @Before
    public void setupDatabase() {

        testingService.clearDatabase();

        hindi = new Language("HI", "Hindi");
        languageDataService.create(hindi);

        urdu = new Language("UR", "Urdu");
        languageDataService.create(urdu);

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setLanguage(hindi);
        district.setCode(1L);

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);

        stateDataService.create(state);

        aa = new Circle("AA");
        aa.getStates().add(state);
        circleDataService.create(aa);

        bb = new Circle("BB");
        bb.getStates().add(state);
        circleDataService.create(bb);

        childPack = subscriptionPackDataService.create(
                SubscriptionPackBuilder.createSubscriptionPack(
                        "childPack",
                        SubscriptionPackType.CHILD,
                        SubscriptionPackBuilder.CHILD_PACK_WEEKS,
                        1));

        pregnancyPack = subscriptionPackDataService.create(
                SubscriptionPackBuilder.createSubscriptionPack(
                        "pregnancyPack",
                        SubscriptionPackType.PREGNANCY,
                        SubscriptionPackBuilder.PREGNANCY_PACK_WEEKS,
                        2));
    }


    @Before
    public void setupSettings() {
        localObdDirBackup = setupTestDir(LOCAL_OBD_DIR, "obd-local-dir-it");
        remoteObdDirBackup = setupTestDir(REMOTE_OBD_DIR, "obd-remote-dir-it");
    }


    @After
    public void restoreSettings() {
        settingsService.getSettingsFacade().setProperty(REMOTE_OBD_DIR, remoteObdDirBackup);
        settingsService.getSettingsFacade().setProperty(LOCAL_OBD_DIR, localObdDirBackup);
    }


    @Test
    public void testTargetFileGeneration() throws NoSuchAlgorithmException, IOException {

        Subscriber subscriber1 = new Subscriber(1111111111L, hindi, aa);
        subscriber1.setLastMenstrualPeriod(DateTime.now().minusDays(90)); // startDate will be today
        subscriberDataService.create(subscriber1);
        subscriptionService.createSubscription(1111111111L, hindi, pregnancyPack, SubscriptionOrigin.MCTS_IMPORT);


        // Should not be picked up because it's been deactivated
        Subscriber subscriber2 = new Subscriber(2222222222L, urdu, bb);
        subscriber2.setLastMenstrualPeriod(DateTime.now().minusDays(90)); // startDate will be today
        subscriberDataService.create(subscriber2);
        Subscription subscription2 = subscriptionService.createSubscription(2222222222L, urdu, pregnancyPack,
                SubscriptionOrigin.MCTS_IMPORT);
        subscriptionService.deactivateSubscription(subscription2, DeactivationReason.CHILD_DEATH);

        //Should not be picked up because it's not for today
        Subscriber subscriber3 = new Subscriber(6666666666L, urdu, bb);
        subscriber3.setDateOfBirth(DateTime.now().plusDays(1)); // startDate is DOB + 1 for child packs,
                                                    // so setting the DOB tomorrow this should be picked up
                                                    // the day after tomorrow
        subscriberDataService.create(subscriber3);
        subscriptionService.createSubscription(6666666666L, urdu, childPack, SubscriptionOrigin.IVR);


        // Should not be picked up because it's not for today
        callRetryDataService.create(new CallRetry("11111111-1111-1111-1111-111111111111", 3333333333L,
                DayOfTheWeek.today().nextDay(), CallStage.RETRY_1, "w1_m1.wav", "w1_1", hindi.getCode(),
                aa.getName(), SubscriptionOrigin.IVR));


        TargetFileNotification tfn = targetFileService.generateTargetFile();
        assertNotNull(tfn);

        // Should not pickup subscription2 because its status is not ACTIVE
        // Should not pickup subscription3 because it's for tomorrow
        // Should not pickup call retry record because it's for tomorrow also
        assertEquals(1, (int) tfn.getRecordCount());

        //read the file to get checksum & record count
        File targetDir = new File(settingsService.getSettingsFacade().getProperty("imi.local_obd_dir"));
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


    // un-ignore to create a large sample OBD file
    @Ignore
    public void createLargeFile() {
        SubscriptionHelper sh = new SubscriptionHelper(subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService);

        subscriptionService.deleteAll();
        subscriberDataService.deleteAll();
        languageDataService.deleteAll();
        districtDataService.deleteAll();
        stateDataService.deleteAll();
        circleDataService.deleteAll();
        callRetryDataService.deleteAll();

        for (int i=0 ; i<1000 ; i++) {
            sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now());
        }

        for (int i=0 ; i<1000 ; i++) {

            int randomWeek = (int) (Math.random() * sh.childPack().getWeeks());
            Subscription sub = sh.mksub(
                    SubscriptionOrigin.MCTS_IMPORT,
                    DateTime.now().minusDays(7 * randomWeek - 1)
            );
            callRetryDataService.create(new CallRetry(
                    sub.getSubscriptionId(),
                    sub.getSubscriber().getCallingNumber(),
                    DayOfTheWeek.today(),
                    CallStage.RETRY_1,
                    sh.getContentMessageFile(sub, randomWeek),
                    sh.getWeekId(sub, randomWeek),
                    sh.getLanguageLocationCode(sub),
                    sh.getCircle(sub),
                    SubscriptionOrigin.MCTS_IMPORT
            ));
        }

        TargetFileNotification tfn = targetFileService.generateTargetFile();
        assertNotNull(tfn);
        getLogger().debug("Generated {}", tfn.getFileName());
    }


    //todo: test success notification is sent to the IVR system
}
