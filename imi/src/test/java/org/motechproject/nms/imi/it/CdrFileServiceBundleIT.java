package org.motechproject.nms.imi.it;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.motechproject.alerts.contract.AlertCriteria;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.Alert;
import org.motechproject.event.MotechEvent;
import org.motechproject.nms.imi.exception.InvalidCdrFileException;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.dto.CallDetailRecordDto;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.CallSummaryRecordDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
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
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class CdrFileServiceBundleIT extends BasePaxIT {

    private static final String PROCESS_DETAIL_FILE_SUBJECT = "nms.imi.kk.process_detail_file";
    private static final String FILE_INFO_PARAM_KEY = "fileInfo";
    private static final String LOCAL_OBD_DIR = "imi.local_obd_dir";
    private static final String REMOTE_OBD_DIR = "imi.remote_obd_dir";
    private static final String LOCAL_CDR_DIR = "imi.local_cdr_dir";
    private static final String REMOTE_CDR_DIR = "imi.remote_cdr_dir";

    @Inject
    private SettingsService settingsService;

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private SubscriptionDataService subscriptionDataService;

    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;

    @Inject
    private SubscriberDataService subscriberDataService;

    @Inject
    private LanguageDataService languageDataService;

    @Inject
    private CallRetryDataService callRetryDataService;

    @Inject
    private CallSummaryRecordDataService callSummaryRecordDataService;

    @Inject
    private AlertService alertService;

    @Inject
    CdrFileService cdrFileService;

    @Inject
    private LanguageLocationDataService languageLocationDataService;

    @Inject
    private CircleDataService circleDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    private FileAuditRecordDataService fileAuditRecordDataService;

    @Before
    public void cleanupDatabase() {
        for (Subscription subscription: subscriptionDataService.retrieveAll()) {
            subscription.setStatus(SubscriptionStatus.COMPLETED);
            subscription.setEndDate(new DateTime().withDate(2011, 8, 1));

            subscriptionDataService.update(subscription);
        }

        subscriptionService.deleteAll();
        subscriberDataService.deleteAll();
        languageLocationDataService.deleteAll();
        languageDataService.deleteAll();
        districtDataService.deleteAll();
        stateDataService.deleteAll();
        circleDataService.deleteAll();
        callRetryDataService.deleteAll();
    }


    private String localCdrDirBackup;
    private String remoteCdrDirBackup;
    private String localObdDirBackup;
    private String remoteObdDirBackup;



    private String setupTestDir(String property, String dir) {
        String backup = settingsService.getSettingsFacade().getProperty(property);
        File directory = new File(System.getProperty("user.home"), dir);
        directory.mkdirs();
        settingsService.getSettingsFacade().setProperty(property, directory.getAbsolutePath());
        return backup;
    }


    @Before
    public void setupSettings() {
        localCdrDirBackup = setupTestDir(LOCAL_CDR_DIR, "cdr-local-dir-it");
        remoteCdrDirBackup = setupTestDir(REMOTE_CDR_DIR, "cdr-remote-dir-it");
        localObdDirBackup = setupTestDir(LOCAL_OBD_DIR, "obd-local-dir-it");
        remoteObdDirBackup = setupTestDir(REMOTE_OBD_DIR, "obd-remote-dir-it");
    }


    @After
    public void restoreSettings() {
        settingsService.getSettingsFacade().setProperty(REMOTE_OBD_DIR, remoteObdDirBackup);
        settingsService.getSettingsFacade().setProperty(LOCAL_OBD_DIR, localObdDirBackup);
        settingsService.getSettingsFacade().setProperty(REMOTE_CDR_DIR, remoteCdrDirBackup);
        settingsService.getSettingsFacade().setProperty(LOCAL_CDR_DIR, localCdrDirBackup);

    }


    @Test
    public void testServicePresent() {
        getLogger().debug("testServicePresent()");
        assertTrue(cdrFileService != null);
    }


    @Test
    public void testVerify() throws IOException, NoSuchAlgorithmException {
        getLogger().debug("testVerify()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, languageLocationDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,1,1,1);
        helper.makeLocalCdrFile();
        FileInfo fileInfo = new FileInfo(helper.cdr(), helper.cdrLocalChecksum(), helper.cdrCount());
        cdrFileService.verifyDetailFileChecksumAndCount(fileInfo);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testChecksumError() throws IOException, NoSuchAlgorithmException {
        getLogger().debug("testChecksumError()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, languageLocationDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1, 1, 1, 1);
        helper.makeLocalCdrFile();
        FileInfo fileInfo = new FileInfo(helper.cdr(), "invalid checksum", helper.cdrCount());

        exception.expect(IllegalStateException.class);
        cdrFileService.verifyDetailFileChecksumAndCount(fileInfo);
    }


    @Test
    public void testCsvErrors() throws IOException, NoSuchAlgorithmException {
        getLogger().debug("testCsvErrors()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, languageLocationDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1, 1, 1, 1);
        helper.makeLocalCdrFile(2);
        FileInfo fileInfo = new FileInfo(helper.cdr(), helper.cdrLocalChecksum(), helper.cdrCount());
        try {
            cdrFileService.verifyDetailFileChecksumAndCount(fileInfo);
        } catch (InvalidCdrFileException e) {
            assertEquals(2, e.getMessages().size());
        }
    }


    @Test
    public void testTooManyErrors() throws IOException, NoSuchAlgorithmException {
        getLogger().debug("testTooManyErrors()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, languageLocationDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(200,0,0,0);
        helper.makeLocalCdrFile(200);
        FileInfo fileInfo = new FileInfo(helper.cdr(), helper.cdrLocalChecksum(), helper.cdrCount());
        try {
            cdrFileService.verifyDetailFileChecksumAndCount(fileInfo);
        } catch (InvalidCdrFileException e) {
            List<String> errors = e.getMessages();
            assertEquals(101, errors.size());
            assertEquals("The maximum number of allowed errors", errors.get(errors.size() - 1).substring(0, 36));
        }
    }


    @Test
    public void testProcess() throws IOException, NoSuchAlgorithmException {
        getLogger().debug("testProcess()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, languageLocationDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,1,1,1);
        helper.makeRemoteCdrFile();
        helper.makeLocalCdrFile();
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(FILE_INFO_PARAM_KEY, helper.cdrLocalFileInfo());
        MotechEvent motechEvent = new MotechEvent(PROCESS_DETAIL_FILE_SUBJECT, eventParams);
        List<String> errors = cdrFileService.processDetailFile(motechEvent);
        assertEquals(0, errors.size());

        // This is going to try to send the file processed notification back to IMI, but will fail since we
        // didn't setup a server
        AlertCriteria criteria = new AlertCriteria().byExternalId(helper.cdrLocalFileInfo().getCdrFile());
        List<Alert> alerts = alertService.search(criteria);
        getLogger().debug("alerts={}", alerts);
        assertEquals(4, alerts.size()); //three warnings plus one error
    }


    @Test
    public void testAggregation() throws IOException, NoSuchAlgorithmException {
        getLogger().debug("testAggregation()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, languageLocationDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeSingleCallCdrs(3, true);
        List<CallDetailRecordDto> cdrs = helper.getCdrs();
        String sid1 = cdrs.get(cdrs.size()-1).getRequestId().getSubscriptionId();

        helper.makeSingleCallCdrs(4, false);
        String sid2 = cdrs.get(cdrs.size()-1).getRequestId().getSubscriptionId();

        File cdrFile = helper.makeLocalCdrFile();

        List<String> errors = cdrFileService.sendAggregatedRecords(cdrFile);

        assertEquals(0, errors.size());
    }
}
