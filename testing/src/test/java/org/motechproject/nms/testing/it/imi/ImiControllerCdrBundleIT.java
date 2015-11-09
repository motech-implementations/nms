package org.motechproject.nms.testing.it.imi;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.service.TargetFileService;
import org.motechproject.nms.imi.service.contract.TargetFileNotification;
import org.motechproject.nms.imi.web.contract.BadRequest;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.LanguageService;
import org.motechproject.nms.testing.it.utils.CdrHelper;
import org.motechproject.nms.testing.it.utils.RegionHelper;
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
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
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class ImiControllerCdrBundleIT extends BasePaxIT {

    @Inject
    SettingsService settingsService;
    @Inject
    SubscriptionService subscriptionService;
    @Inject
    SubscriberDataService subscriberDataService;
    @Inject
    SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    LanguageDataService languageDataService;
    @Inject
    LanguageService languageService;
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
    TestingService testingService;
    @Inject
    TargetFileService targetFileService;


    private String localCdrDirBackup;
    private String remoteCdrDirBackup;
    private String localObdDirBackup;
    private String remoteObdDirBackup;
    private CdrHelper helper;


    @Before
    public void setupSettings() {
        localCdrDirBackup = ImiTestHelper.setupTestDir(settingsService, ImiTestHelper.LOCAL_CDR_DIR,
                "cdr-local-dir-it");
        remoteCdrDirBackup = ImiTestHelper.setupTestDir(settingsService, ImiTestHelper.REMOTE_CDR_DIR,
                "cdr-remote-dir-it");
        localObdDirBackup = ImiTestHelper.setupTestDir(settingsService, ImiTestHelper.LOCAL_OBD_DIR,
                "obd-local-dir-it");
        remoteObdDirBackup = ImiTestHelper.setupTestDir(settingsService, ImiTestHelper.REMOTE_OBD_DIR,
                "obd-remote-dir-it");
    }


    @Before
    public void setupDatabase() {
        testingService.clearDatabase();
    }


    @Before
    public void setupCdrHelper() throws IOException {
        helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService, districtService);
    }


    @After
    public void restoreSettings() {
        settingsService.getSettingsFacade().setProperty(ImiTestHelper.REMOTE_CDR_DIR, remoteCdrDirBackup);
        settingsService.getSettingsFacade().setProperty(ImiTestHelper.LOCAL_CDR_DIR, localCdrDirBackup);
        settingsService.getSettingsFacade().setProperty(ImiTestHelper.REMOTE_OBD_DIR, remoteObdDirBackup);
        settingsService.getSettingsFacade().setProperty(ImiTestHelper.LOCAL_OBD_DIR, localObdDirBackup);
    }


    private String createFailureResponseJson(String failureReason) throws IOException {
        BadRequest badRequest = new BadRequest(failureReason);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(badRequest);
    }


    private HttpPost createCdrFileNotificationHttpPost(boolean useValidTargetFile, boolean useValidSummaryFile,
                                                       boolean useValidDetailFile)
            throws IOException, NoSuchAlgorithmException {
        String targetFile = useValidTargetFile ? helper.obd() : helper.obd() + "xxx";
        String summaryFile = useValidSummaryFile ? helper.csr() : helper.csr() + "xxx";
        String detailFile = useValidDetailFile ? helper.cdr() : helper.cdr() + "xxx";

        FileInfo cdrSummary;
        FileInfo cdrDetail;
        if (useValidTargetFile && useValidSummaryFile && useValidDetailFile) {
            cdrSummary = new FileInfo(summaryFile, helper.csrRemoteChecksum(), helper.csrRemoteRecordCount());
            cdrDetail = new FileInfo(detailFile, helper.cdrRemoteChecksum(), helper.cdrRemoteRecordCount());
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

        helper.makeCdrs(1,0,0,0);
        helper.makeRemoteCsrFile();
        helper.makeRemoteCdrFile();
        helper.createObdFileAuditRecord(true, true);

        HttpPost httpPost = createCdrFileNotificationHttpPost(true, true, true);

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ImiTestHelper.ADMIN_USERNAME,
                ImiTestHelper.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_ACCEPTED, response.getStatusLine().getStatusCode());
        assertEquals("", EntityUtils.toString(response.getEntity()));

    }


    @Test
    public void testCreateCdrFileNotificationRequestBadCdrSummaryFileName() throws IOException,
            InterruptedException, NoSuchAlgorithmException {
        getLogger().debug("testCreateCdrFileNotificationRequestBadCdrSummaryFileName()");

        helper.makeCdrs(1,0,0,0);
        helper.makeRemoteCdrFile();

        HttpPost httpPost = createCdrFileNotificationHttpPost(true, false, true);

        String expectedJsonResponse = createFailureResponseJson("<cdrSummary: Invalid>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));
    }


    @Test
    public void testCreateCdrFileNotificationRequestBadFileNames() throws IOException,
            InterruptedException, NoSuchAlgorithmException {
        getLogger().debug("testCreateCdrFileNotificationRequestBadFileNames()");

        HttpPost httpPost = createCdrFileNotificationHttpPost(false, true, true);

        // All 3 filenames will be considered invalid because the target file is of invalid format, and the CDR
        // Summary and CDR Detail don't match it (even though their formats are technically valid on their own)
        String expectedJsonResponse =
                createFailureResponseJson("<fileName: Invalid><cdrSummary: Invalid><cdrDetail: Invalid>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case mandatory parameter filename is missing.
    */
    @Test
    public void verifyFT201() throws IOException, InterruptedException, NoSuchAlgorithmException {
        getLogger().debug("cdrFileNotificationAPIRejectedIfOBDFileMissing()");

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
                ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case mandatory parameter cdrSummary is missing.
    */
    @Test
    public void verifyFT202() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfCdrSummaryMissing()");

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
                ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case mandatory parameter cdrDetail is missing.
    */
    @Test
    public void verifyFT203() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfCdrDetailMissing()");

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
                ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case filename is  not found in audit records.
    */
    @Test
    public void verifyFT204() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfOBDFileMissingINAuditRecord()");

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
                ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case cdrFile is missing inside cdrSummary.
    */
    @Test
    public void verifyFT205() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfCdrFileMissingInsideCdrSummary()");

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
                ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case checksum is missing inside cdrSummary.
    */
    @Test
    public void verifyFT206() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfChecksumMissingInsideCdrSummary()");

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
                ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case recordsCount is missing inside cdrSummary.
    */
    @Test
    public void verifyFT207() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfRecordsCountMissingInsideCdrSummary()");

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
                ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case cdrFile is missing inside cdrDetail
    */
    @Test
    public void verifyFT208() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfCdrFileMissingInsideCdrDetail()");

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
                ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case checksum is missing inside cdrDetail.
    */
    @Test
    public void verifyFT209() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfChecksumMissingInsideCdrDetail()");

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
                ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));
    }

    /*
    * To check cdrFileNotification API is rejected in case recordsCount is missing inside cdrDetail.
    */
    @Test
    public void verifyFT210() throws IOException, InterruptedException, NoSuchAlgorithmException{
        getLogger().debug("cdrFileNotificationAPIRejectedIfRecordsCountMissingInsideCdrDetail()");

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
                ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));
    }

    /*
    * Verify that the filename returned by generateTargetFile can be passed back to us
    */
    @Test
    public void verifyTargetFileNameRoundTrip() throws IOException, InterruptedException, NoSuchAlgorithmException{

        RegionHelper rh = new RegionHelper(languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, districtService);

        SubscriptionHelper sh = new SubscriptionHelper(subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, districtService);

        Subscriber subscriber1 = new Subscriber(1111111111L, rh.hindiLanguage(), rh.delhiCircle());
        subscriber1.setLastMenstrualPeriod(DateTime.now().minusDays(90)); // startDate will be today
        subscriberDataService.create(subscriber1);
        subscriptionService.createSubscription(1111111111L, rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.MCTS_IMPORT);

        TargetFileNotification tfn = targetFileService.generateTargetFile();
        assertNotNull(tfn);

        CdrHelper cdrHelper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                subscriptionPackDataService, languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService, districtService, tfn.getFileName());

        helper.makeCdrs(1, 0, 0, 0);
        File remoteCdrFile = helper.makeRemoteCdrFile();
        Files.deleteIfExists(remoteCdrFile.toPath());
        String targetFile = cdrHelper.obd();
        String summaryFile = cdrHelper.csr();
        String detailFile = cdrHelper.cdr();

        FileInfo cdrSummary = new FileInfo(summaryFile, "checksum", 1);
        FileInfo cdrDetail = new FileInfo(detailFile, "checksum", 1);

        HttpPost httpPost = createHttpPost(targetFile, cdrSummary, cdrDetail);
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ImiTestHelper.ADMIN_USERNAME,
                ImiTestHelper.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());

        //We're expecting for the file copy to fail, what we wanted to check here is that the given file name
        //is valid
        String expectedJsonFailure1 = "{\"failureReason\":\"Error 1 running";
        String responseBody = EntityUtils.toString(response.getEntity());
        assertEquals(expectedJsonFailure1,  responseBody.substring(0, expectedJsonFailure1.length()));
    }
}
