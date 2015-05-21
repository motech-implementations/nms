package org.motechproject.nms.imi.it;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
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
import org.motechproject.nms.imi.web.contract.BadRequest;
import org.motechproject.nms.imi.web.contract.FileProcessedStatusRequest;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.http.SimpleHttpClient;
import org.motechproject.testing.utils.TestContext;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class ImiController_OBD_BundleIT extends BasePaxIT {
    private static final String ADMIN_USERNAME = "motech";
    private static final String ADMIN_PASSWORD = "motech";

    @Inject
    AlertService alertService;

    @Inject
    FileAuditRecordDataService fileAuditRecordDataService;

    @Before
    public void cleanupDatabase() {
        fileAuditRecordDataService.deleteAll();
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
        fileAuditRecordDataService.create(new FileAuditRecord(FileType.TARGET_FILE, "file.csv", "OK", 0, ""));
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testCreateFileProcessedStatusRequestNoStatusCode() throws IOException, InterruptedException {
        getLogger().debug("testCreateFileProcessedStatusRequestNoStatusCode()");
        HttpPost httpPost = createFileProcessedStatusHttpPost("file.csv", null);

        String expectedJsonResponse = createFailureResponseJson("<fileProcessedStatus: Not Present>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testCreateFileProcessedStatusRequestNoFileName() throws IOException, InterruptedException {
        getLogger().debug("testCreateFileProcessedStatusRequestNoFileName()");
        HttpPost httpPost = createFileProcessedStatusHttpPost(null,
                FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY);

        String expectedJsonResponse = createFailureResponseJson("<fileName: Not Present>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testCreateFileProcessedStatusRequestWithError() throws IOException, InterruptedException {
        getLogger().debug("testCreateFileProcessedStatusRequestWithError()");
        HttpPost httpPost = createFileProcessedStatusHttpPost("file.csv",
                FileProcessedStatus.FILE_ERROR_IN_FILE_FORMAT);

        fileAuditRecordDataService.create(new FileAuditRecord(FileType.TARGET_FILE, "file.csv", "ERROR", 0, ""));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK, ADMIN_USERNAME, ADMIN_PASSWORD));

        //check an alert was sent
        AlertCriteria criteria = new AlertCriteria().byExternalId("file.csv");
        List<Alert> alerts = alertService.search(criteria);
        assertEquals(1, alerts.size());
        assertEquals(AlertType.CRITICAL, alerts.get(0).getAlertType());
    }
}
