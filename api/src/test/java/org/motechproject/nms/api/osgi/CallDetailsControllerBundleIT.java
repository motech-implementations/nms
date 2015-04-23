package org.motechproject.nms.api.osgi;

import com.google.common.base.Joiner;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.flw.domain.CallDetailRecord;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.repository.CallContentDataService;
import org.motechproject.nms.flw.repository.CallDetailRecordDataService;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.repository.ServiceUsageDataService;
import org.motechproject.nms.flw.service.CallDetailRecordService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
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
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class CallDetailsControllerBundleIT extends BasePaxIT {
    private static final String ADMIN_USERNAME = "motech";
    private static final String ADMIN_PASSWORD = "motech";

    @Inject
    private CallDetailRecordService callDetailRecordService;

    @Inject
    private CallDetailRecordDataService callDetailRecordDataService;

    @Inject
    private CallContentDataService callContentDataService;

    @Inject
    private FrontLineWorkerService frontLineWorkerService;

    @Inject
    private FrontLineWorkerDataService frontLineWorkerDataService;

    @Inject
    private ServiceUsageDataService serviceUsageDataService;

    @Inject
    private ServiceUsageCapDataService serviceUsageCapDataService;

    private void cleanAllData() {
        serviceUsageCapDataService.deleteAll();
        serviceUsageDataService.deleteAll();
        callDetailRecordDataService.deleteAll();
        callContentDataService.deleteAll();
        frontLineWorkerDataService.deleteAll();
    }

    private String createCallDetailsJson(boolean includeCallingNumber, Long callingNumber,
                                         boolean includeCallId, Long callId,
                                         boolean includeOperator, String operator,
                                         boolean includeCircle, String circle,
                                         boolean includeCallStartTime, Long callStartTime,
                                         boolean includeCallEndTime, Long callEndTime,
                                         boolean includeCallDurationInPulses, Integer callDurationInPulses,
                                         boolean includeEndOfUsagePromptCounter, Integer endOfUsagePromptCounter,
                                         boolean includeWelcomeMessagePromptFlag, Boolean welcomeMessagePromptFlag,
                                         boolean includeCallStatus, Integer callStatus,
                                         boolean includeCallDisconnectReason, Integer callDisconnectReason,
                                         boolean includeContet, String content) {
        StringBuffer contentTemplate = new StringBuffer("{");
        ArrayList<String> array = new ArrayList<>();

        if (includeCallingNumber) {
            array.add(String.format("\"callingNumber\": %s", callingNumber));
        }

        if (includeCallId) {
            array.add(String.format("\"callId\": %s", callId));
        }

        if (includeOperator) {
            array.add(String.format("\"operator\": \"%s\"", operator));
        }

        if (includeCircle) {
            array.add(String.format("\"circle\": \"%s\"", circle));
        }

        if (includeCallStartTime) {
            array.add(String.format("\"callStartTime\": %s", callStartTime));
        }

        if (includeCallEndTime) {
            array.add(String.format("\"callEndTime\": %s", callEndTime));
        }

        if (includeCallDurationInPulses) {
            array.add(String.format("\"callDurationInPulses\": %s", callDurationInPulses));
        }

        if (includeEndOfUsagePromptCounter) {
            array.add(String.format("\"endOfUsagePromptCounter\": %s", endOfUsagePromptCounter));
        }

        if (includeWelcomeMessagePromptFlag) {
            array.add(String.format("\"welcomeMessagePromptFlag\": %s", welcomeMessagePromptFlag));
        }

        if (includeCallStatus) {
            array.add(String.format("\"callStatus\": %s", callStatus));
        }

        if (includeCallDisconnectReason) {
            array.add(String.format("\"callDisconnectReason\": %s", callDisconnectReason));
        }

        if (includeContet) {
            array.add(String.format("\"content\": [%s]", content));
        }

        contentTemplate.append(Joiner.on(",").join(array));
        contentTemplate.append("}");

        return contentTemplate.toString();
    }

    private String createContentJson(boolean includeType, String type,
                                     boolean includeMkCardNumber, String mkCardNumber,
                                     boolean includeContentName, String contentName,
                                     boolean includeContentFileName, String contentFileName,
                                     boolean includeStartTime, Long startTime,
                                     boolean includeEndTime, Long endTime,
                                     boolean includeCompletionFlag, Boolean completionFlag,
                                     boolean includeCorrectAnswerEntered, Boolean correctAnswerEntered) {
        StringBuffer contentTemplate = new StringBuffer("{");
        ArrayList<String> array = new ArrayList<>();

        if (includeType) {
            array.add(String.format("\"type\": \"%s\"", type));
        }

        if (includeMkCardNumber) {
            array.add(String.format("\"mkCardNumber\": \"%s\"", mkCardNumber));
        }

        if (includeContentName) {
            array.add(String.format("\"contentName\": \"%s\"", contentName));
        }

        if (includeContentFileName) {
            array.add(String.format("\"contentFileName\": \"%s\"", contentFileName));
        }

        if (includeStartTime) {
            array.add(String.format("\"startTime\": %s", startTime));
        }

        if (includeEndTime) {
            array.add(String.format("\"endTime\": %s", endTime));
        }

        if (includeCompletionFlag) {
            array.add(String.format("\"completionFlag\": %s", completionFlag));
        }

        if (includeCorrectAnswerEntered) {
            array.add(String.format("\"correctAnswerEntered\": %s", correctAnswerEntered));
        }

        contentTemplate.append(Joiner.on(",").join(array));
        contentTemplate.append("}");

        return contentTemplate.toString();
    }

    @Test
    public void testCallDetailsValidMobileKunji() throws IOException, InterruptedException {
        cleanAllData();

        FrontLineWorker flw = new FrontLineWorker("Frank Lloyd Wright", "9810320300");
        frontLineWorkerService.add(flw);

        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobilekunji/callDetails", TestContext.getJettyPort()));

        ArrayList<String> array = new ArrayList<>();
        array.add(createContentJson(false, null,                   // type
                true, "a",                     // mkCardNumber
                true, "YellowFever",           // contentName
                true, "Yellowfever.wav",       // contentFile
                true, 1200000000l,             // startTime
                true, 1222222221l,             // endTime
                false, null,                   // completionFlag
                false, null));                 // correctAnswerEntered
        array.add(createContentJson(false, null,                   // type
                true, "b",                     // mkCardNumber
                true, "Malaria",               // contentName
                true, "Malaria.wav",           // contentFile
                true, 1200000000l,             // startTime
                true, 1222222221l,             // endTime
                false, null,                   // completionFlag
                false, null));                 // correctAnswerEntered
        String callDetails = createCallDetailsJson(true, 9810320300l,       // callingNumber
                true, 234000011111111l,  // callId
                true, "A",               // operator
                true, "AP",              // circle
                true, 1422879843l,       // callStartTime
                true, 1422879903l,       // callEndTime
                true, 60,                // callDurationInPulses
                true, 0,                 // endOfUsagePromptCounter
                true, true,              // welcomeMessagePromptFlag
                true, 1,                 // callStatus
                true, 1,                 // callDisconnectReason
                true, Joiner.on(",").join(array));          // content
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK, ADMIN_USERNAME, ADMIN_PASSWORD));

        CallDetailRecord cdr = callDetailRecordService.getByCallingNumber(9810320300l);

        assertNotNull(cdr);
        assertEquals(234000011111111l, cdr.getCallId());
        assertEquals(2, cdr.getContent().size());

        assertEquals("YellowFever", cdr.getContent().get(0).getContentName());
        assertNull(cdr.getContent().get(0).getType());
        assertNull(cdr.getContent().get(0).getCompletionFlag());
        assertEquals("Malaria", cdr.getContent().get(1).getContentName());
        assertNull(cdr.getContent().get(1).getType());
        assertNull(cdr.getContent().get(1).getCompletionFlag());

        assertEquals(flw.getId(), cdr.getFrontLineWorker().getId());
    }

    @Test
    public void testCallDetailsValidMobileAcademy() throws IOException, InterruptedException {
        cleanAllData();

        FrontLineWorker flw = new FrontLineWorker("Frank Lloyd Wright", "9810320300");
        frontLineWorkerService.add(flw);

        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/callDetails",
                TestContext.getJettyPort()));

        ArrayList<String> array = new ArrayList<>();
        array.add(createContentJson(/* type */ true, "lesson",
                /* mkCardNumber */ false, null,
                /* contentName */ true, "Chapter-01lesson-04",
                /* contentFile */ true, "ch1_l4.wav",
                /* startTime */ true, 1200000000l,
                /* endTime */ true, 1222222221l,
                /* completionFlag */ true, true,
                /* correctAnswerEntered */ true, true));
        array.add(createContentJson(/* type */ true, "question",
                /* mkCardNumber */ false, null,
                /* contentName */ true, "chapter-01question-01",
                /* contentFile */ true, "ch1_q1.wav",
                /* startTime */ true, 1200000000l,
                /* endTime */ true, 1222222221l,
                /* completionFlag */ true, true,
                /* correctAnswerEntered */ true, true));
        String callDetails = createCallDetailsJson(/* callingNumber */ true, 9810320300l,
                /* callId */ true, 234000011111111l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879843l,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ true, 1,
                /* content */ true, Joiner.on(",").join(array));
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK, ADMIN_USERNAME, ADMIN_PASSWORD));

        CallDetailRecord cdr = callDetailRecordService.getByCallingNumber(9810320300l);

        assertNotNull(cdr);
        assertEquals(234000011111111l, cdr.getCallId());
        assertNull(cdr.getWelcomePrompt());
        assertEquals(2, cdr.getContent().size());
        assertEquals("Chapter-01lesson-04", cdr.getContent().get(0).getContentName());
        assertNull(cdr.getContent().get(0).getMobileKunjiCardNumber());
        assertEquals("chapter-01question-01", cdr.getContent().get(1).getContentName());
        assertNull(cdr.getContent().get(1).getMobileKunjiCardNumber());
        assertEquals(flw.getId(), cdr.getFrontLineWorker().getId());
    }

    @Test
    public void testCallDetailsValidNoContent() throws IOException, InterruptedException {
        cleanAllData();

        FrontLineWorker flw = new FrontLineWorker("Frank Lloyd Wright", "9810320300");
        frontLineWorkerService.add(flw);

        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/callDetails", TestContext.getJettyPort()));

        String callDetails = createCallDetailsJson(/* callingNumber */ true, 9810320300l,
                /* callId */ true, 234000011111111l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879843l,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ true, 1,
                /* content */ false, null);
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK));

        CallDetailRecord cdr = callDetailRecordService.getByCallingNumber(9810320300l);

        assertNotNull(cdr);
        assertEquals(234000011111111l, cdr.getCallId());
        assertEquals(0, cdr.getContent().size());
        assertEquals(flw.getId(), cdr.getFrontLineWorker().getId());
    }

    /*****************************************************************************************************************
     Test the existence and validity of elements common to MA/MK
     callingNumber, callId, operator, circle, callStartTime, callEndTime, callDurationInPulses, endOfUsagePromptCount,
     callStatus, callDisconnectReason
     ****************************************************************************************************************/
    @Test
    public void testCallDetailsFLWNotFound() throws IOException, InterruptedException {
        cleanAllData();

        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/callDetails", TestContext.getJettyPort()));

        String callDetails = createCallDetailsJson(/* callingNumber */ true, 9810320300l,
                /* callId */ true, 234000011111111l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879903l,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ true, 1,
                /* content */ false, null);
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_NOT_FOUND,
                "{\"failureReason\":\"<callingNumber: Not Found>\"}",
                (String) null, (String) null));
    }

    @Test
    public void testCallDetailsNullCallDisconnectReason() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/callDetails", TestContext.getJettyPort()));

        String callDetails = createCallDetailsJson(/* callingNumber */ true, 9810320300l,
                /* callId */ true, 234000011111111l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879903l,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ false, null,
                /* content */ false, null);
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callDisconnectReason: Not Present>\"}",
                (String) null, (String) null));
    }

    @Test
    public void testCallDetailsNullCallStatus() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/callDetails", TestContext.getJettyPort()));

        String callDetails = createCallDetailsJson(/* callingNumber */ true, 9810320300l,
                /* callId */ true, 234000011111111l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879903l,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ false, null,
                /* callDisconnectReason */ true, 1,
                /* content */ false, null);
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callStatus: Not Present>\"}",
                (String) null, (String) null));
    }

    @Test
    public void testCallDetailsNullEndOfUsagePromptCount() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/callDetails", TestContext.getJettyPort()));

        String callDetails = createCallDetailsJson(/* callingNumber */ true, 9810320300l,
                /* callId */ true, 234000011111111l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879903l,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ false, null,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ true, 1,
                /* content */ false, null);
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<endOfUsagePromptCount: Not Present>\"}",
                (String) null, (String) null));
    }

    @Test
    public void testCallDetailsNullCallDurationInPulses() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/callDetails", TestContext.getJettyPort()));

        String callDetails = createCallDetailsJson(/* callingNumber */ true, 9810320300l,
                /* callId */ true, 234000011111111l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879903l,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ false, null,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ true, 1,
                /* content */ false, null);
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callDurationInPulses: Not Present>\"}",
                (String) null, (String) null));
    }

    @Test
    public void testCallDetailsNullCallEndTime() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/callDetails", TestContext.getJettyPort()));

        String callDetails = createCallDetailsJson(/* callingNumber */ true, 9810320300l,
                /* callId */ true, 234000011111111l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879903l,
                /* callEndTime */ false, null,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ true, 1,
                /* content */ false, null);
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callEndTime: Not Present>\"}",
                (String) null, (String) null));
    }

    @Test
    public void testCallDetailsNullCallStartTime() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/callDetails", TestContext.getJettyPort()));

        String callDetails = createCallDetailsJson(/* callingNumber */ true, 9810320300l,
                /* callId */ true, 234000011111111l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ false, null,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ true, 1,
                /* content */ false, null);
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callStartTime: Not Present>\"}",
                (String) null, (String) null));
    }

    @Test
    public void testCallDetailsNullCallingNumber() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/callDetails", TestContext.getJettyPort()));

        String callDetails = createCallDetailsJson(/* callingNumber */ false, null,
                /* callId */ true, 234000011111111l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879843l,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ true, 1,
                /* content */ false, null);
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callingNumber: Not Present>\"}",
                (String) null, (String) null));
    }

    @Test
    public void testCallDetailsInvalidCallingNumber() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/callDetails", TestContext.getJettyPort()));

        String callDetails = createCallDetailsJson(/* callingNumber */ true, 1l,
                /* callId */ true, 234000011111111l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879843l,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ true, 1,
                /* content */ false, null);
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callingNumber: Invalid>\"}",
                (String) null, (String) null));
    }

    @Test
    public void testCallDetailsNullCallId() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/callDetails", TestContext.getJettyPort()));

        String callDetails = createCallDetailsJson(/* callingNumber */ true, 9810320300l,
                /* callId */ false, null,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879843l,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ true, 1,
                /* content */ false, null);
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callId: Not Present>\"}",
                (String) null, (String) null));
    }

    @Test
    public void testCallDetailsInvalidCallId() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/callDetails", TestContext.getJettyPort()));

        String callDetails = createCallDetailsJson(/* callingNumber */ true, 9810320300l,
                /* callId */ true, 1l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879843l,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ true, 1,
                /* content */ false, null);
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callId: Invalid>\"}",
                (String) null, (String) null));
    }

    /*****************************************************************************************************************
     Test the existence and validity of elements specific to MA
     content.type, content.completionFlag
     *****************************************************************************************************************/
    @Test
    public void testCallDetailsNullContentType() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/callDetails", TestContext.getJettyPort()));

        ArrayList<String> array = new ArrayList<>();
        array.add(createContentJson(/* type */ false, null,
                /* mkCardNumber */ false, null,
                /* contentName */ true, "Chapter-01lesson-04",
                /* contentFile */ true, "ch1_l4.wav",
                /* startTime */ true, 1200000000l,
                /* endTime */ true, 1222222221l,
                /* completionFlag */ true, true,
                /* correctAnswerEntered */ true, true));
        array.add(createContentJson(/* type */ true, "question",
                /* mkCardNumber */ false, null,
                /* contentName */ true, "chapter-01question-01",
                /* contentFile */ true, "ch1_q1.wav",
                /* startTime */ true, 1200000000l,
                /* endTime */ true, 1222222221l,
                /* completionFlag */ true, true,
                /* correctAnswerEntered */ true, true));
        String callDetails = createCallDetailsJson(/* callingNumber */ true, 9810320300l,
                /* callId */ true, 234000011111111l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879843l,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ true, 1,
                /* content */ true, Joiner.on(",").join(array));
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<type: Not Present>\"}",
                (String) null, (String) null));
    }

    @Test
    public void testCallDetailsNullContentCompletionFlag() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/callDetails", TestContext.getJettyPort()));

        ArrayList<String> array = new ArrayList<>();
        array.add(createContentJson(/* type */ true, "lesson",
                /* mkCardNumber */ false, null,
                /* contentName */ true, "Chapter-01lesson-04",
                /* contentFile */ true, "ch1_l4.wav",
                /* startTime */ true, 1200000000l,
                /* endTime */ true, 1222222221l,
                /* completionFlag */ false, null,
                /* correctAnswerEntered */ false, null));
        array.add(createContentJson(/* type */ true, "question",
                /* mkCardNumber */ false, null,
                /* contentName */ true, "chapter-01question-01",
                /* contentFile */ true, "ch1_q1.wav",
                /* startTime */ true, 1200000000l,
                /* endTime */ true, 1222222221l,
                /* completionFlag */ false, null,
                /* correctAnswerEntered */ false, null));
        String callDetails = createCallDetailsJson(/* callingNumber */ true, 9810320300l,
                /* callId */ true, 234000011111111l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879843l,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ true, 1,
                /* content */ true, Joiner.on(",").join(array));
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<completionFlag: Not Present><completionFlag: Not Present>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /*****************************************************************************************************************
     Test the existence and validity of elements specific to MK
     welcomeMessagePromptFlag, content.mkCardNumber
     *****************************************************************************************************************/
    @Test
    public void testCallDetailsNullWelcomeMessagePromptFlag() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobilekunji/callDetails", TestContext.getJettyPort()));

        String callDetails = createCallDetailsJson(/* callingNumber */ true, 9810320300l,
                /* callId */ true, 234000011111111l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879903l,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ false, null,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ true, 1,
                /* content */ false, null);
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<welcomeMessagePromptFlag: Not Present>\"}",
                (String) null, (String) null));
    }

    @Test
    public void testCallDetailsNullContentMkCardNumber() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobilekunji/callDetails", TestContext.getJettyPort()));

        ArrayList<String> array = new ArrayList<>();
        array.add(createContentJson(/* type */ false, null,
                /* mkCardNumber */ false, null,
                /* contentName */ true, "Chapter-01lesson-04",
                /* contentFile */ true, "ch1_l4.wav",
                /* startTime */ true, 1200000000l,
                /* endTime */ true, 1222222221l,
                /* completionFlag */ false, null,
                /* correctAnswerEntered */ false, null));
        String callDetails = createCallDetailsJson(/* callingNumber */ true, 9810320300l,
                /* callId */ true, 234000011111111l,
                /* operator */ true, "A",
                /* circle */ true, "AP",
                /* callStartTime */ true, 1422879843l,
                /* callEndTime */ true, 1422879903l,
                /* callDurationInPulses */ true, 60,
                /* endOfUsagePromptCounter */ true, 0,
                /* welcomeMessagePromptFlag */ true, false,
                /* callStatus */ true, 1,
                /* callDisconnectReason */ true, 1,
                /* content */ true, Joiner.on(",").join(array));
        StringEntity params = new StringEntity(callDetails);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<mkCardNumber: Not Present>\"}",
                (String) null, (String) null));
    }

    // Test with no content
    // Test with empty content
    // Test witn invalid callingNumber
    // Test with null callingNumber
    // Test with no flw for callingNumber
    // Test with invalid callId
    // Test with missing callId
}
