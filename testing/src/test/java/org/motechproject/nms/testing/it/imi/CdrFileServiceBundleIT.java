package org.motechproject.nms.testing.it.imi;

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
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.imi.exception.InvalidCdrFileException;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.nms.kilkari.domain.CallRetry;
import org.motechproject.nms.kilkari.dto.CallDetailRecordDto;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.testing.it.utils.CdrHelper;
import org.motechproject.nms.testing.service.TestingService;
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
    private static final String PROCESS_SUMMARY_RECORD_SUBJECT = "nms.imi.kk.process_summary_record";
    private static final int MAX_MILLISECOND_WAIT = 5;

    private static final String INITIAL_RETRY_DELAY = "imi.initial_retry_delay";
    private static final String MAX_CDR_ERROR_COUNT = "imi.max_cdr_error_count";

    @Inject
    SettingsService settingsService;
    @Inject
    SubscriptionService subscriptionService;
    @Inject
    SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    SubscriberDataService subscriberDataService;
    @Inject
    LanguageDataService languageDataService;
    @Inject
    AlertService alertService;
    @Inject
    CdrFileService cdrFileService;
    @Inject
    CircleDataService circleDataService;
    @Inject
    StateDataService stateDataService;
    @Inject
    DistrictDataService districtDataService;
    @Inject
    DistrictService districtService;
    @Inject
    FileAuditRecordDataService fileAuditRecordDataService;
    @Inject
    CallRetryDataService callRetryDataService;

    private int eventCount;


    @Inject
    TestingService testingService;

    @Before
    public void cleanupDatabase() {
        testingService.clearDatabase();
    }


    private String localCdrDirBackup;
    private String remoteCdrDirBackup;
    private String localObdDirBackup;
    private String remoteObdDirBackup;
    private String initialRetryDelay;
    private String maxErrorCountBackup;

    @Before
    public void setupSettings() {
        localCdrDirBackup = ImiTestHelper.setupTestDir(settingsService, ImiTestHelper.LOCAL_CDR_DIR, "cdr-local-dir-it");
        remoteCdrDirBackup = ImiTestHelper.setupTestDir(settingsService, ImiTestHelper.REMOTE_CDR_DIR, "cdr-remote-dir-it");
        localObdDirBackup = ImiTestHelper.setupTestDir(settingsService, ImiTestHelper.LOCAL_OBD_DIR, "obd-local-dir-it");
        remoteObdDirBackup = ImiTestHelper.setupTestDir(settingsService, ImiTestHelper.REMOTE_OBD_DIR, "obd-remote-dir-it");
        initialRetryDelay = settingsService.getSettingsFacade().getProperty(INITIAL_RETRY_DELAY);
        settingsService.getSettingsFacade().setProperty(INITIAL_RETRY_DELAY, "0");
        settingsService.getSettingsFacade().setProperty(INITIAL_RETRY_DELAY, "0");
        maxErrorCountBackup = settingsService.getSettingsFacade().getProperty(MAX_CDR_ERROR_COUNT);
        settingsService.getSettingsFacade().setProperty(MAX_CDR_ERROR_COUNT, "3");
    }


    @After
    public void restoreSettings() {
        settingsService.getSettingsFacade().setProperty(ImiTestHelper.REMOTE_OBD_DIR, remoteObdDirBackup);
        settingsService.getSettingsFacade().setProperty(ImiTestHelper.LOCAL_OBD_DIR, localObdDirBackup);
        settingsService.getSettingsFacade().setProperty(ImiTestHelper.REMOTE_CDR_DIR, remoteCdrDirBackup);
        settingsService.getSettingsFacade().setProperty(ImiTestHelper.LOCAL_CDR_DIR, localCdrDirBackup);
        settingsService.getSettingsFacade().setProperty(INITIAL_RETRY_DELAY, initialRetryDelay);
        settingsService.getSettingsFacade().setProperty(MAX_CDR_ERROR_COUNT, maxErrorCountBackup);
    }


    @Test
    public void testServicePresent() {
        assertTrue(cdrFileService != null);
    }


    @Test
    public void testVerify() throws IOException, NoSuchAlgorithmException {

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService, districtService);

        helper.makeCdrs(1,1,1,1);
        helper.makeLocalCdrFile();
        FileInfo fileInfo = new FileInfo(helper.cdr(), helper.cdrLocalChecksum(), helper.cdrCount());
        cdrFileService.verifyDetailFileChecksumAndCount(fileInfo);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testChecksumError() throws IOException, NoSuchAlgorithmException {

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService, districtService);

        helper.makeCdrs(1, 1, 1, 1);
        helper.makeLocalCdrFile();
        FileInfo fileInfo = new FileInfo(helper.cdr(), "invalid checksum", helper.cdrCount());

        exception.expect(IllegalStateException.class);
        cdrFileService.verifyDetailFileChecksumAndCount(fileInfo);
    }


    @Test
    public void testCsvErrors() throws IOException, NoSuchAlgorithmException {

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService, districtService);

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

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService, districtService);

        helper.makeCdrs(5,0,0,0);
        helper.makeLocalCdrFile(5);
        FileInfo fileInfo = new FileInfo(helper.cdr(), helper.cdrLocalChecksum(), helper.cdrCount());
        try {
            cdrFileService.verifyDetailFileChecksumAndCount(fileInfo);
        } catch (InvalidCdrFileException e) {
            List<String> errors = e.getMessages();
            assertEquals(4, errors.size());
            assertEquals("The maximum number of allowed errors", errors.get(errors.size() - 1).substring(0, 36));
        }
    }


    @MotechListener(subjects = { PROCESS_SUMMARY_RECORD_SUBJECT })
    public void processCallSummaryRecord(MotechEvent event) {
        eventCount++;
    }

    @Test
    public void testProcess() throws IOException, NoSuchAlgorithmException, InterruptedException {

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService, districtService);

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
        assertEquals(4, alerts.size()); //three warnings plus one error

        //Fancy code that waits for all 4 CDRs to be processed
        eventCount = 0;
        long start = System.currentTimeMillis();
        while (eventCount < 4) {
            Thread.sleep(100L);
            if (System.currentTimeMillis() - start > MAX_MILLISECOND_WAIT) {
                assertTrue("Timeout while waiting for message processing", false);
            }
        }

        //Now verify that we should be rescheduling one call (the failed one)
        List<CallRetry> callRetries = callRetryDataService.retrieveAll();
        assertEquals(1, callRetries.size());
    }


    @Test
    public void testAggregation() throws IOException, NoSuchAlgorithmException {

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService, districtService);

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
