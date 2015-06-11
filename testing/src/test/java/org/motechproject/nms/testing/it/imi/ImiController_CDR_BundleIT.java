package org.motechproject.nms.testing.it.imi;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.web.contract.BadRequest;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.it.utils.CdrHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.http.SimpleHttpClient;
import org.motechproject.testing.utils.TestContext;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class ImiController_CDR_BundleIT extends BasePaxIT {

    private static final String ADMIN_USERNAME = "motech";
    private static final String ADMIN_PASSWORD = "motech";
    private static final String LOCAL_CDR_DIR = "imi.local_cdr_dir";
    private static final String REMOTE_CDR_DIR = "imi.remote_cdr_dir";


    @Inject
    private SettingsService settingsService;

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private SubscriberDataService subscriberDataService;

    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;

    @Inject
    private LanguageDataService languageDataService;

    @Inject
    private CircleDataService circleDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    private CallRetryDataService callRetryDataService;

    @Inject
    private FileAuditRecordDataService fileAuditRecordDataService;

    @Inject
    private TestingService testingService;

    private String localCdrDirBackup;
    private String remoteCdrDirBackup;


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
    }


    @Before
    public void setupDatabase() {
        testingService.clearDatabase();
    }


    @After
    public void restoreSettings() {
        settingsService.getSettingsFacade().setProperty(REMOTE_CDR_DIR, remoteCdrDirBackup);
        settingsService.getSettingsFacade().setProperty(LOCAL_CDR_DIR, localCdrDirBackup);
    }


    private String createFailureResponseJson(String failureReason) throws IOException {
        BadRequest badRequest = new BadRequest(failureReason);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(badRequest);
    }


    private HttpPost createCdrFileNotificationHttpPost(CdrHelper helper, boolean useValidTargetFile,
                                                       boolean useValidSummaryFile, boolean useValidDetailFile)
            throws IOException, NoSuchAlgorithmException {
        String targetFile = useValidTargetFile ? helper.obd() : helper.obd() + "xxx";
        String summaryFile = useValidSummaryFile ? helper.csr() : helper.csr() + "xxx";
        String detailFile = useValidDetailFile ? helper.cdr() : helper.cdr() + "xxx";

        FileInfo cdrSummary;
        FileInfo cdrDetail;
        if (useValidTargetFile && useValidSummaryFile && useValidDetailFile) {
            cdrSummary = new FileInfo(summaryFile, helper.csrRemoteChecksum(), 0);
            cdrDetail = new FileInfo(detailFile, helper.cdrRemoteChecksum(), 1);
        } else {
            cdrSummary = new FileInfo(summaryFile, "", 0);
            cdrDetail = new FileInfo(detailFile, "", 0);

        }
        return createHttpPost(targetFile, cdrSummary, cdrDetail);
    }

    private HttpPost createHttpPost(String targetFile, FileInfo cdrSummary, FileInfo cdrDetail)
            throws IOException, NoSuchAlgorithmException {
        CdrFileNotificationRequest cdrFileNotificationRequest =
                new CdrFileNotificationRequest(targetFile, cdrSummary, cdrDetail);


        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(cdrFileNotificationRequest);
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/imi/cdrFileNotification",
                TestContext.getJettyPort()));
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(requestJson));

        return httpPost;
    }

    @Test
    public void testCreateCdrFileNotificationRequest() throws IOException, InterruptedException,
            NoSuchAlgorithmException {
        getLogger().debug("testCreateCdrFileNotificationRequest()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,0,0,0);
        helper.makeRemoteCsrFile();
        helper.makeRemoteCdrFile();
        helper.createObdFileAuditRecord(true, true);

        HttpPost httpPost = createCdrFileNotificationHttpPost(helper, true, true, true);

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_ACCEPTED, response.getStatusLine().getStatusCode());
    }


    @Test
    public void testCreateCdrFileNotificationRequestBadCdrSummaryFileName() throws IOException,
            InterruptedException, NoSuchAlgorithmException {
        getLogger().debug("testCreateCdrFileNotificationRequestBadCdrSummaryFileName()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,0,0,0);
        helper.makeRemoteCdrFile();

        HttpPost httpPost = createCdrFileNotificationHttpPost(helper, true, false, true);

        String expectedJsonResponse = createFailureResponseJson("<cdrSummary: Invalid>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }


    @Test
    public void testCreateCdrFileNotificationRequestBadFileNames() throws IOException,
            InterruptedException, NoSuchAlgorithmException {
        getLogger().debug("testCreateCdrFileNotificationRequestBadFileNames()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);
        HttpPost httpPost = createCdrFileNotificationHttpPost(helper, false, true, true);

        // All 3 filenames will be considered invalid because the target file is of invalid format, and the CDR
        // Summary and CDR Detail don't match it (even though their formats are technically valid on their own)
        String expectedJsonResponse =
                createFailureResponseJson("<fileName: Invalid><cdrSummary: Invalid><cdrDetail: Invalid>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case mandatory parameter filename is missing.
    */
    @Test
    public void verifyFT201() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfOBDFileMissing()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,0,0,0);
        helper.makeRemoteCsrFile();
        helper.makeRemoteCdrFile();
        helper.createObdFileAuditRecord(true, true);

        String summaryFile = helper.csr();
        String detailFile = helper.cdr();

        FileInfo cdrSummary = new FileInfo(summaryFile, helper.csrRemoteChecksum(), 0);
        FileInfo cdrDetail = new FileInfo(detailFile, helper.cdrRemoteChecksum(), 1);

        HttpPost httpPost = createHttpPost(null, cdrSummary, cdrDetail);
        String expectedJsonResponse =
                createFailureResponseJson("<fileName: Not Present><cdrSummary: Invalid><cdrDetail: Invalid>");
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case mandatory parameter cdrSummary is missing.
    */
    @Test
    public void verifyFT202() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfCdrSummaryMissing()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,0,0,0);
        helper.makeRemoteCsrFile();
        helper.makeRemoteCdrFile();
        helper.createObdFileAuditRecord(true, true);

        String targetFile = helper.obd();
        String detailFile = helper.cdr();

        FileInfo cdrDetail = new FileInfo(detailFile, helper.cdrRemoteChecksum(), 1);

        HttpPost httpPost = createHttpPost(targetFile, null, cdrDetail);
        String expectedJsonResponse =
                createFailureResponseJson("<cdrSummary: Not Present>");
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case mandatory parameter cdrDetail is missing.
    */
    @Test
    public void verifyFT203() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfCdrDetailMissing()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,0,0,0);
        helper.makeRemoteCsrFile();
        helper.makeRemoteCdrFile();
        helper.createObdFileAuditRecord(true, true);

        String targetFile = helper.obd();
        String summaryFile = helper.csr();

        FileInfo cdrSummary = new FileInfo(summaryFile, helper.csrRemoteChecksum(), 0);

        HttpPost httpPost = createHttpPost(targetFile, cdrSummary, null);
        String expectedJsonResponse =
                createFailureResponseJson("<cdrDetail: Not Present>");
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case filename is  not found in audit records.
    */
    @Test
    public void verifyFT204() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfOBDFileMissingINAuditRecord()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,0,0,0);
        helper.makeRemoteCsrFile();
        helper.makeRemoteCdrFile();

        String targetFile = helper.obd();
        String summaryFile = helper.csr();
        String detailFile = helper.cdr();

        FileInfo cdrSummary = new FileInfo(summaryFile, helper.csrRemoteChecksum(), 0);
        FileInfo cdrDetail = new FileInfo(detailFile, helper.cdrRemoteChecksum(), 1);

        HttpPost httpPost = createHttpPost(targetFile, cdrSummary, cdrDetail);
        String expectedJsonResponse =
                createFailureResponseJson("<" +targetFile +": Not Found>");
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_NOT_FOUND, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case cdrFile is missing inside cdrSummary.
    */
    @Test
    public void verifyFT205() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfCdrFileMissingInsideCdrSummary()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,0,0,0);
        helper.makeRemoteCsrFile();
        helper.makeRemoteCdrFile();
        helper.createObdFileAuditRecord(true, true);

        String targetFile = helper.obd();
        String detailFile = helper.cdr();

        FileInfo cdrSummary = new FileInfo(null, helper.csrRemoteChecksum(), 0);
        FileInfo cdrDetail = new FileInfo(detailFile, helper.cdrRemoteChecksum(), 1);

        HttpPost httpPost = createHttpPost(targetFile, cdrSummary, cdrDetail);
        String expectedJsonResponse =
                createFailureResponseJson("<cdrFile: Not Present>");
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case checksum is missing inside cdrSummary.
    */
    @Test
    public void verifyFT206() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfChecksumMissingInsideCdrSummary()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,0,0,0);
        helper.makeRemoteCsrFile();
        helper.makeRemoteCdrFile();
        helper.createObdFileAuditRecord(true, true);

        String targetFile = helper.obd();
        String summaryFile = helper.csr();
        String detailFile = helper.cdr();

        FileInfo cdrSummary = new FileInfo(summaryFile, null, 0);
        FileInfo cdrDetail = new FileInfo(detailFile, helper.cdrRemoteChecksum(), 1);

        HttpPost httpPost = createHttpPost(targetFile, cdrSummary, cdrDetail);
        String expectedJsonResponse =
                createFailureResponseJson("<checksum: Not Present>");
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case recordsCount is missing inside cdrSummary.
    */
    @Test
    public void verifyFT207() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfRecordsCountMissingInsideCdrSummary()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,0,0,0);
        helper.makeRemoteCsrFile();
        helper.makeRemoteCdrFile();
        helper.createObdFileAuditRecord(true, true);

        String targetFile = helper.obd();
        String summaryFile = helper.csr();
        String detailFile = helper.cdr();

        FileInfo cdrSummary = new FileInfo(summaryFile, helper.csrRemoteChecksum(), -1);
        FileInfo cdrDetail = new FileInfo(detailFile, helper.cdrRemoteChecksum(), 1);

        HttpPost httpPost = createHttpPost(targetFile, cdrSummary, cdrDetail);
        String expectedJsonResponse =
                createFailureResponseJson("<recordsCount: Invalid>");
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case cdrFile is missing inside cdrDetail
    */
    @Test
    public void verifyFT208() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfCdrFileMissingInsideCdrDetail()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,0,0,0);
        helper.makeRemoteCsrFile();
        helper.makeRemoteCdrFile();
        helper.createObdFileAuditRecord(true, true);

        String targetFile = helper.obd();
        String summaryFile = helper.csr();

        FileInfo cdrSummary = new FileInfo(summaryFile, helper.csrRemoteChecksum(), 0);
        FileInfo cdrDetail = new FileInfo(null, helper.cdrRemoteChecksum(), 1);

        HttpPost httpPost = createHttpPost(targetFile, cdrSummary, cdrDetail);
        String expectedJsonResponse =
                createFailureResponseJson("<cdrFile: Not Present>");
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case checksum is missing inside cdrDetail.
    */
    @Test
    public void verifyFT209() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfChecksumMissingInsideCdrDetail()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,0,0,0);
        helper.makeRemoteCsrFile();
        helper.makeRemoteCdrFile();
        helper.createObdFileAuditRecord(true, true);

        String targetFile = helper.obd();
        String summaryFile = helper.csr();
        String detailFile = helper.cdr();

        FileInfo cdrSummary = new FileInfo(summaryFile, helper.csrRemoteChecksum(), 0);
        FileInfo cdrDetail = new FileInfo(detailFile, null, 1);

        HttpPost httpPost = createHttpPost(targetFile, cdrSummary, cdrDetail);
        String expectedJsonResponse =
                createFailureResponseJson("<checksum: Not Present>");
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case recordsCount is missing inside cdrDetail.
    */
    @Test
    public void verifyFT210() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfRecordsCountMissingInsideCdrDetail()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService, fileAuditRecordDataService);

        helper.makeCdrs(1,0,0,0);
        helper.makeRemoteCsrFile();
        helper.makeRemoteCdrFile();
        helper.createObdFileAuditRecord(true, true);

        String targetFile = helper.obd();
        String summaryFile = helper.csr();
        String detailFile = helper.cdr();

        FileInfo cdrSummary = new FileInfo(summaryFile, helper.csrRemoteChecksum(), 0);
        FileInfo cdrDetail = new FileInfo(detailFile, helper.cdrRemoteChecksum(), -1);

        HttpPost httpPost = createHttpPost(targetFile, cdrSummary, cdrDetail);
        String expectedJsonResponse =
                createFailureResponseJson("<recordsCount: Invalid>");
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }
}
