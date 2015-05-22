package org.motechproject.nms.imi.it;

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
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.repository.StateDataService;
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
    private static final String SCP_USER = "imi.scp.user";
    private static final String SCP_HOST = "imi.scp.host";
    private static final String SCP_IDENTITY = "imi.scp.identity";
    private static final String LOCAL_OBD_DIR = "imi.local_obd_dir";
    private static final String REMOTE_OBD_DIR = "imi.remote_obd_dir";
    private static final String LOCAL_CDR_DIR = "imi.local_cdr_dir";
    private static final String REMOTE_CDR_DIR = "imi.remote_cdr_dir";


    @Inject
    private SettingsService settingsService;

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private SubscriberDataService subscriberDataService;

    @Inject
    private LanguageDataService languageDataService;

    @Inject
    private CircleDataService circleDataService;

    @Inject
    private LanguageLocationDataService languageLocationDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    private CallRetryDataService callRetryDataService;

    @Inject
    private FileAuditRecordDataService fileAuditRecordDataService;


    private String userBackup;
    private String hostBackup;
    private String identityBackup;
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
        userBackup = settingsService.getSettingsFacade().getProperty(SCP_USER);
        settingsService.getSettingsFacade().setProperty(SCP_USER, System.getProperty("user.name"));

        hostBackup = settingsService.getSettingsFacade().getProperty(SCP_HOST);
        settingsService.getSettingsFacade().setProperty(SCP_HOST, "localhost");

        identityBackup = settingsService.getSettingsFacade().getProperty(SCP_IDENTITY);
        settingsService.getSettingsFacade().setProperty(SCP_IDENTITY, "");

        localCdrDirBackup = setupTestDir(LOCAL_CDR_DIR, "cdr-local-dir-it");
        remoteCdrDirBackup = setupTestDir(REMOTE_CDR_DIR, "cdr-remote-dir-it");
        localObdDirBackup = setupTestDir(LOCAL_OBD_DIR, "obd-local-dir-it");
        remoteObdDirBackup = setupTestDir(REMOTE_OBD_DIR, "obd-remote-dir-it");
    }


    @After
    public void restoreSettings() {
        settingsService.getSettingsFacade().setProperty(SCP_USER, userBackup);
        settingsService.getSettingsFacade().setProperty(SCP_HOST, hostBackup);
        settingsService.getSettingsFacade().setProperty(SCP_IDENTITY, identityBackup);
        settingsService.getSettingsFacade().setProperty(REMOTE_OBD_DIR, remoteObdDirBackup);
        settingsService.getSettingsFacade().setProperty(LOCAL_OBD_DIR, localObdDirBackup);
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
                languageDataService, languageLocationDataService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService);

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
                languageDataService, languageLocationDataService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService);

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
                languageDataService, languageLocationDataService, circleDataService, stateDataService,
                districtDataService, fileAuditRecordDataService);
        HttpPost httpPost = createCdrFileNotificationHttpPost(helper, false, true, true);

        // All 3 filenames will be considered invalid because the target file is of invalid format, and the CDR
        // Summary and CDR Detail don't match it (even though their formats are technically valid on their own)
        String expectedJsonResponse =
                createFailureResponseJson("<fileName: Invalid><cdrSummary: Invalid><cdrDetail: Invalid>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }
}
