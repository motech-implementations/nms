package org.motechproject.nms.imi.it;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.alerts.contract.AlertCriteria;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.Alert;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.nms.imi.domain.FileProcessedStatus;
import org.motechproject.nms.imi.web.contract.BadRequest;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.imi.web.contract.FileInfo;
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
public class ImiControllerBundleIT extends BasePaxIT {
    private static final String ADMIN_USERNAME = "motech";
    private static final String ADMIN_PASSWORD = "motech";
    private static final String VALID_TARGET_FILE_NAME = "OBD_NMS1_20150127090000.csv";
    private static final String INVALID_TARGET_FILE_NAME = "OBD_NMS_2015012709000.csv";
    private static final String VALID_CDR_SUMMARY_FILE_NAME = "cdrSummary_OBD_NMS1_20150127090000.csv";
    private static final String INVALID_CDR_SUMMARY_FILE_NAME = "cdrSummary_OBD_NMS1_20150127091111.csv";
    private static final String VALID_CDR_DETAIL_FILE_NAME = "cdrDetail_OBD_NMS1_20150127090000.csv";
    private static final String INVALID_CDR_DETAIL_FILE_NAME = "cdrDetail_NMS1_20150127090000.csv";

    @Inject
    AlertService alertService;

    private String createFailureResponseJson(String failureReason) throws IOException {
        BadRequest badRequest = new BadRequest(failureReason);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(badRequest);
    }

    private HttpPost createCdrFileNotificationHttpPost(boolean useValidTargetFile, boolean useValidSummaryFile,
        boolean useValidDetailFile) throws IOException {
        String targetFile = useValidTargetFile ? VALID_TARGET_FILE_NAME : INVALID_TARGET_FILE_NAME;
        String summaryFile = useValidSummaryFile ? VALID_CDR_SUMMARY_FILE_NAME : INVALID_CDR_SUMMARY_FILE_NAME;
        String detailFile = useValidDetailFile ? VALID_CDR_DETAIL_FILE_NAME : INVALID_CDR_DETAIL_FILE_NAME;

        FileInfo cdrSummary =
            new FileInfo(summaryFile, "xxxx", 5000);
        FileInfo cdrDetail =
            new FileInfo(detailFile, "xxxx", 9900);
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
    public void testCreateCdrFileNotificationRequest() throws IOException, InterruptedException {
        HttpPost httpPost = createCdrFileNotificationHttpPost(true, true, true);
        
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_ACCEPTED, ADMIN_USERNAME,
                ADMIN_PASSWORD));
    }

    @Test
    public void testCreateCdrFileNotificationRequestBadCdrSummaryFileName() throws IOException,
        InterruptedException {
        HttpPost httpPost = createCdrFileNotificationHttpPost(true, false, true);

        String expectedJsonResponse = createFailureResponseJson("<cdrSummary: Invalid>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testCreateCdrFileNotificationRequestBadFileNames() throws IOException,
        InterruptedException {
        HttpPost httpPost = createCdrFileNotificationHttpPost(false, true, true);

        // All 3 filenames will be considered invalid because the target file is of invalid format, and the CDR
        // Summary and CDR Detail don't match it (even though their formats are technically valid on their own)
        String expectedJsonResponse =
            createFailureResponseJson("<fileName: Invalid><cdrSummary: Invalid><cdrDetail: Invalid>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
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


    @Test
    public void testCreateFileProcessedStatusRequest() throws IOException, InterruptedException {
        HttpPost httpPost = createFileProcessedStatusHttpPost("file.csv",
                FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY);
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK, ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testCreateFileProcessedStatusRequestNoStatusCode() throws IOException, InterruptedException {
        HttpPost httpPost = createFileProcessedStatusHttpPost("file.csv", null);

        String expectedJsonResponse = createFailureResponseJson("<fileProcessedStatus: Not Present>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testCreateFileProcessedStatusRequestNoFileName() throws IOException, InterruptedException {
        HttpPost httpPost = createFileProcessedStatusHttpPost(null,
                FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY);

        String expectedJsonResponse = createFailureResponseJson("<fileName: Not Present>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testCreateFileProcessedStatusRequestWithError() throws IOException, InterruptedException {
        HttpPost httpPost = createFileProcessedStatusHttpPost("file.csv",
                FileProcessedStatus.FILE_ERROR_IN_FILE_FORMAT);

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK, ADMIN_USERNAME, ADMIN_PASSWORD));

        //check an alert was sent
        AlertCriteria criteria = new AlertCriteria().byExternalId("file.csv");
        List<Alert> alerts = alertService.search(criteria);
        assertEquals(1, alerts.size());
        assertEquals(AlertType.CRITICAL, alerts.get(0).getAlertType());
    }
}
