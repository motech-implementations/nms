package org.motechproject.nms.testing.it.imi;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.alerts.contract.AlertCriteria;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.Alert;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.nms.imi.domain.FileAuditRecord;
import org.motechproject.nms.imi.domain.FileProcessedStatus;
import org.motechproject.nms.imi.domain.FileType;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.web.contract.BadRequest;
import org.motechproject.nms.imi.web.contract.FileProcessedStatusRequest;
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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class ImiController_OBD_BundleIT extends BasePaxIT {

    private String localObdDirBackup;
    private String remoteObdDirBackup;

    @Inject
    AlertService alertService;

    @Inject
    SettingsService settingsService;

    @Inject
    FileAuditRecordDataService fileAuditRecordDataService;

    @Inject
    TestingService testingService;

    @Before
    public void cleanupDatabase() {
        testingService.clearDatabase();
    }

    @Before
    public void setupSettings() {
        localObdDirBackup = ImiTestHelper.setupTestDir(settingsService, ImiTestHelper.LOCAL_OBD_DIR, "obd-local-dir-it");
        remoteObdDirBackup = ImiTestHelper.setupTestDir(settingsService, ImiTestHelper.REMOTE_OBD_DIR, "obd-remote-dir-it");
    }


    @After
    public void restoreSettings() {
        settingsService.getSettingsFacade().setProperty(ImiTestHelper.REMOTE_OBD_DIR, remoteObdDirBackup);
        settingsService.getSettingsFacade().setProperty(ImiTestHelper.LOCAL_OBD_DIR, localObdDirBackup);
    }


    private HttpPost createFileProcessedStatusHttpPost(String fileName, FileProcessedStatus fileProcessedStatus)
        throws IOException {
        FileProcessedStatusRequest request = new FileProcessedStatusRequest();
        if (fileName != null) {
            request.setFileName(fileName);
        }
        if (fileProcessedStatus != null) {
            request.setFileProcessedStatus(fileProcessedStatus);
        }

        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(request);
        HttpPost httpPost = new HttpPost(String.format(
            "http://localhost:%d/imi/obdFileProcessedStatusNotification",
            TestContext.getJettyPort()));
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(requestJson));

        return httpPost;
    }


    private String createFailureResponseJson(String failureReason) throws IOException {
        BadRequest badRequest = new BadRequest(failureReason);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(badRequest);
    }


    @Test
    public void testCreateFileProcessedStatusRequest() throws IOException, InterruptedException {
        getLogger().debug("testCreateFileProcessedStatusRequest()");
        HttpPost httpPost = createFileProcessedStatusHttpPost("file.csv",
                FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY);
        fileAuditRecordDataService.create(new FileAuditRecord(FileType.TARGET_FILE, "file.csv", true, null, null,
                null));
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK, ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));
    }

    @Test
    public void testCreateFileProcessedStatusRequestNoStatusCode() throws IOException, InterruptedException {
        getLogger().debug("testCreateFileProcessedStatusRequestNoStatusCode()");
        HttpPost httpPost = createFileProcessedStatusHttpPost("file.csv", null);

        String expectedJsonResponse = createFailureResponseJson("<fileProcessedStatus: Not Present>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));
    }

    @Test
    public void testCreateFileProcessedStatusRequestNoFileName() throws IOException, InterruptedException {
        getLogger().debug("testCreateFileProcessedStatusRequestNoFileName()");
        HttpPost httpPost = createFileProcessedStatusHttpPost(null,
                FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY);

        String expectedJsonResponse = createFailureResponseJson("<fileName: Not Present>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));
    }

    @Test
    public void testCreateFileProcessedStatusRequestWithError() throws IOException, InterruptedException {
        getLogger().debug("testCreateFileProcessedStatusRequestWithError()");
        HttpPost httpPost = createFileProcessedStatusHttpPost("file.csv",
                FileProcessedStatus.FILE_ERROR_IN_FILE_FORMAT);

        fileAuditRecordDataService.create(new FileAuditRecord(FileType.TARGET_FILE, "file.csv", false, "ERROR",
                null, null));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK, ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));

        //check an alert was sent
        AlertCriteria criteria = new AlertCriteria().byExternalId("file.csv");
        List<Alert> alerts = alertService.search(criteria);
        assertEquals(1, alerts.size());
        assertEquals(AlertType.CRITICAL, alerts.get(0).getAlertType());
    }

    /*
    * NMS_FT_193 :To check "NotifyFileProcessedStatus" API is rejected in case the file could not
    * be copied or the file is not available.
    * NMS_FT_194 : To check "NotifyFileProcessedStatus" API is rejected in case there is an error in checksum
    * NMS_FT_195 : To check "NotifyFileProcessedStatus" API is rejected in case there is an error in records check.
    */
    @Test
    public void verifyFT193_194_195() throws IOException, InterruptedException {
        getLogger().debug("testCreateFileProcessedStatusRequestWithErrorFILE_NOT_ACCESSIBLE()");
        HttpPost httpPost = createFileProcessedStatusHttpPost("file.csv",
                FileProcessedStatus.FILE_NOT_ACCESSIBLE);

        fileAuditRecordDataService.create(new FileAuditRecord(FileType.TARGET_FILE, "file.csv", false, "ERROR",
                null, null));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK, ImiTestHelper.ADMIN_USERNAME, ImiTestHelper.ADMIN_PASSWORD));

        //check an alert was sent
        AlertCriteria criteria = new AlertCriteria().byExternalId("file.csv");
        List<Alert> alerts = alertService.search(criteria);
        assertEquals(1, alerts.size());
        assertEquals(AlertType.CRITICAL, alerts.get(0).getAlertType());
    }
}
