package org.motechproject.nms.imi.it;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.imi.domain.CallDetailRecord;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.web.contract.BadRequest;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.language.repository.CircleLanguageDataService;
import org.motechproject.nms.region.language.repository.LanguageDataService;
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
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class ImiController_CDR_BundleIT extends BasePaxIT {

    private static final String ADMIN_USERNAME = "motech";
    private static final String ADMIN_PASSWORD = "motech";


    @Inject
    private SettingsService settingsService;

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private SubscriberDataService subscriberDataService;

    @Inject
    private LanguageDataService languageDataService;

    @Inject
    private CircleLanguageDataService circleLanguageDataService;


    private String createFailureResponseJson(String failureReason) throws IOException {
        BadRequest badRequest = new BadRequest(failureReason);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(badRequest);
    }


    private HttpPost createCdrFileNotificationHttpPost(CdrHelper helper, boolean useValidTargetFile,
                                                       boolean useValidSummaryFile, boolean useValidDetailFile)
            throws IOException, NoSuchAlgorithmException {
        String targetFile = useValidTargetFile ? helper.obdFileName() : helper.obdFileName() + "xxx";
        String summaryFile = useValidSummaryFile ? helper.cdrSummaryFileName() : helper.cdrSummaryFileName() +
                "xxx";
        String detailFile = useValidDetailFile ? helper.cdrDetailFileName() : helper.cdrDetailFileName() + "xxx";

        FileInfo cdrSummary = new FileInfo(summaryFile, helper.detailFileChecksum(), 5000);
        FileInfo cdrDetail = new FileInfo(detailFile, helper.detailFileChecksum(), 9900);
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
        getLogger().info("testCreateCdrFileNotificationRequest()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                languageDataService, circleLanguageDataService);

        List<CallDetailRecord> cdrs = helper.makeCdrs(5);
        helper.setCrds(cdrs);
        helper.makeCdrSummaryFile();
        helper.makeCdrDetailFile();

        HttpPost httpPost = createCdrFileNotificationHttpPost(helper, true, true, true);

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_ACCEPTED, ADMIN_USERNAME,
                ADMIN_PASSWORD));
    }


    @Test
    public void testCreateCdrFileNotificationRequestBadCdrSummaryFileName() throws IOException,
            InterruptedException, NoSuchAlgorithmException {
        getLogger().info("testCreateCdrFileNotificationRequestBadCdrSummaryFileName()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                languageDataService, circleLanguageDataService);

        helper.makeCdrDetailFile();

        HttpPost httpPost = createCdrFileNotificationHttpPost(helper, true, false, true);

        String expectedJsonResponse = createFailureResponseJson("<cdrSummary: Invalid>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }


    @Test
    public void testCreateCdrFileNotificationRequestBadFileNames() throws IOException,
            InterruptedException, NoSuchAlgorithmException {
        getLogger().info("testCreateCdrFileNotificationRequestBadFileNames()");

        CdrHelper helper = new CdrHelper(settingsService, subscriptionService, subscriberDataService,
                languageDataService, circleLanguageDataService);
        HttpPost httpPost = createCdrFileNotificationHttpPost(helper, false, true, true);

        // All 3 filenames will be considered invalid because the target file is of invalid format, and the CDR
        // Summary and CDR Detail don't match it (even though their formats are technically valid on their own)
        String expectedJsonResponse =
                createFailureResponseJson("<fileName: Invalid><cdrSummary: Invalid><cdrDetail: Invalid>");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonResponse,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }
}
