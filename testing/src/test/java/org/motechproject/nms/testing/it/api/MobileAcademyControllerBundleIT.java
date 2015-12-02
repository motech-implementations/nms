package org.motechproject.nms.testing.it.api;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.mtraining.service.BookmarkService;
import org.motechproject.nms.api.web.BaseController;
import org.motechproject.nms.api.web.contract.BadRequest;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.SaveBookmarkRequest;
import org.motechproject.nms.api.web.contract.mobileAcademy.SmsStatusRequest;
import org.motechproject.nms.api.web.contract.mobileAcademy.sms.DeliveryStatus;
import org.motechproject.nms.api.web.contract.mobileAcademy.sms.RequestData;
import org.motechproject.nms.mobileacademy.domain.CompletionRecord;
import org.motechproject.nms.mobileacademy.domain.NmsCourse;
import org.motechproject.nms.mobileacademy.dto.MaCourse;
import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.repository.NmsCourseDataService;
import org.motechproject.nms.testing.it.api.utils.RequestBuilder;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for mobile academy controller
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class MobileAcademyControllerBundleIT extends BasePaxIT {

    @Inject
    TestingService testingService;

    @Inject
    private BookmarkService bookmarkService;

    @Inject
    private CompletionRecordDataService completionRecordDataService;

    @Inject
    private NmsCourseDataService nmsCourseDataService;

    private static final String COURSE_NAME = "MobileAcademyCourse";

    private static final String FINAL_BOOKMARK = "COURSE_COMPLETED";

    public static final int MILLISECONDS_PER_SECOND = 1000;

    public static final String VALID_CALL_ID = "1234567890123456789012345";

    @Before
    public void setupTestData() {
        testingService.clearDatabase();
        nmsCourseDataService.deleteAll();
    }

    @Test
    public void testBookmarkBadCallingNumber() throws IOException, InterruptedException {

        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());

        HttpPost request = RequestBuilder.createPostRequest(endpoint, new SaveBookmarkRequest());
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_BAD_REQUEST, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    @Test
    public void testBookmarkBadCallIdSmallest() throws IOException, InterruptedException {

        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();
        bookmarkRequest.setCallId(VALID_CALL_ID.substring(1));
        HttpPost request = RequestBuilder.createPostRequest(endpoint, bookmarkRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_BAD_REQUEST, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    @Test
    public void testBookmarkBadCallIdLargest() throws IOException, InterruptedException {

        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();
        bookmarkRequest.setCallId(VALID_CALL_ID + "1");
        HttpPost request = RequestBuilder.createPostRequest(endpoint, bookmarkRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_BAD_REQUEST, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    @Test
    public void testBookmarkNullCallId() throws IOException, InterruptedException {

        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmark = new SaveBookmarkRequest();
        bookmark.setCallingNumber(BaseController.SMALLEST_10_DIGIT_NUMBER);
        HttpPost request = RequestBuilder.createPostRequest(endpoint, bookmark);
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_BAD_REQUEST, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    @Test
    public void testGetBookmarkEmpty() throws IOException, InterruptedException {
        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore?callingNumber=1234567890&callId=1234567890123456789012345",
                TestContext.getJettyPort());
        HttpGet request = RequestBuilder.createGetRequest(endpoint);
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(request, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertNotNull(response);
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testSetValidBookmark() throws IOException, InterruptedException {

        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmark = new SaveBookmarkRequest();
        bookmark.setCallingNumber(BaseController.SMALLEST_10_DIGIT_NUMBER);
        bookmark.setCallId(VALID_CALL_ID);
        HttpPost request = RequestBuilder.createPostRequest(endpoint, bookmark);
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    @Test
    public void testTriggerNotification() throws IOException, InterruptedException {

        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmark = new SaveBookmarkRequest();
        bookmark.setCallingNumber(BaseController.SMALLEST_10_DIGIT_NUMBER);
        bookmark.setCallId(VALID_CALL_ID);

        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 4);
        }
        bookmark.setScoresByChapter(scores);
        bookmark.setBookmark(FINAL_BOOKMARK);
        HttpPost request = RequestBuilder.createPostRequest(endpoint, bookmark);
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));

        long callingNumber = BaseController.SMALLEST_10_DIGIT_NUMBER;
        endpoint = String.format("http://localhost:%d/api/mobileacademy/notify",
                TestContext.getJettyPort());
        request = RequestBuilder.createPostRequest(endpoint, callingNumber);
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));

        // removed the negative testing since there's not reliable way to clean the data for it to fail
        // after the first time. Debugged and verified that the negative works too and we have negative ITs
        // at the service layer.
    }

    @Test
    public void testSetValidExistingBookmark() throws IOException, InterruptedException {

        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmark = new SaveBookmarkRequest();
        bookmark.setCallingNumber(BaseController.SMALLEST_10_DIGIT_NUMBER);
        bookmark.setCallId(VALID_CALL_ID);
        HttpPost request = RequestBuilder.createPostRequest(endpoint, bookmark);
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));

        // Now, update the previous bookmark successfully
        bookmark.setBookmark("Chapter3_Lesson2");
        request = RequestBuilder.createPostRequest(endpoint, bookmark);
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    @Test
    public void testGetCourseValid() throws IOException, InterruptedException {
        setupMaCourse();
        String endpoint = String.format("http://localhost:%d/api/mobileacademy/course",
                TestContext.getJettyPort());
        HttpGet request = RequestBuilder.createGetRequest(endpoint);

        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(request, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        String body = IOUtils.toString(httpResponse.getEntity().getContent());
        assertNotNull(body);
        //TODO: figure out a way to automate the body comparison from the course json resource file
    }

    @Test
    public void testSmsStatusInvalidFormat() throws IOException, InterruptedException {
        String endpoint = String.format("http://localhost:%d/api/mobileacademy/sms/status/imi",
                TestContext.getJettyPort());
        SmsStatusRequest smsStatusRequest = new SmsStatusRequest();
        smsStatusRequest.setRequestData(new RequestData());
        HttpPost request = RequestBuilder.createPostRequest(endpoint, smsStatusRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_BAD_REQUEST, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }
    
    /**
     * setup MA course structure from nmsCourse.json file.
     */
    private JSONObject setupMaCourse() throws IOException {
        MaCourse course = new MaCourse();
        String jsonText = IOUtils.toString(new InputStreamReader(getClass().getClassLoader().
                getResourceAsStream("nmsCourse.json")));
        JSONObject jo = new JSONObject(jsonText);
        course.setName(jo.get("name").toString());
        course.setContent(jo.get("chapters").toString());
        nmsCourseDataService.create(new NmsCourse(course.getName(), course.getContent()));
        return jo;
    }

    /**
     * To verify Get MA Course Version API is not returning course version when
     * MA course structure doesn't exist.
     */
    @Test
    public void verifyFT400() throws IOException, InterruptedException {
        String endpoint = String.format("http://localhost:%d/api/mobileacademy/courseVersion", TestContext
                .getJettyPort());
        HttpGet request = RequestBuilder.createGetRequest(endpoint);
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(request, RequestBuilder
                .ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);

        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, httpResponse.getStatusLine().getStatusCode());
    }

    /**
     * To verify Get MA Course Version API is returning correct course version
     * when MA course structure exist .
     */
    // https://applab.atlassian.net/browse/NMS-226
    @Test
    public void verifyFT401() throws IOException, InterruptedException {
        setupMaCourse();

        String endpoint = String.format(
                "http://localhost:%d/api/mobileacademy/courseVersion",
                TestContext.getJettyPort());
        HttpGet request = RequestBuilder.createGetRequest(endpoint);
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);

        NmsCourse course = nmsCourseDataService.getCourseByName(COURSE_NAME);
        String expectedJsonResponse = "{\"courseVersion\":"
                + course.getModificationDate().getMillis()
                / MILLISECONDS_PER_SECOND + "}";

        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(httpResponse.getEntity()));

    }

    /**
     * To verify Get MA Course API is not returning course structure when MA
     * course structure doesn't exist.
     */
    @Test
    public void verifyFT402() throws IOException, InterruptedException {
        String endpoint = String.format(
                "http://localhost:%d/api/mobileacademy/course",
                TestContext.getJettyPort());
        HttpGet request = RequestBuilder.createGetRequest(endpoint);
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);

        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, httpResponse
                .getStatusLine().getStatusCode());
    }

    /**
     * To verify Get MA Course API is returning correct course structure when MA
     * course structure exist.
     */
    // https://applab.atlassian.net/browse/NMS-227
    @Test
    public void verifyFT403() throws IOException, InterruptedException {
        JSONObject jo = setupMaCourse();

        String endpoint = String.format(
                "http://localhost:%d/api/mobileacademy/course",
                TestContext.getJettyPort());
        HttpGet request = RequestBuilder.createGetRequest(endpoint);
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);

        NmsCourse course = nmsCourseDataService.getCourseByName(COURSE_NAME);
        CourseResponse courseResponseDTO = new CourseResponse();
        courseResponseDTO.setName(jo.get("name").toString());
        courseResponseDTO.setCourseVersion(course.getModificationDate()
                .getMillis() / MILLISECONDS_PER_SECOND);
        courseResponseDTO.setChapters(jo.get("chapters").toString());

        ObjectMapper mapper = new ObjectMapper();
        String expectedJsonResponse = mapper
                .writeValueAsString(courseResponseDTO);

        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(httpResponse.getEntity()));
    }

    HttpGet createHttpGetBookmarkWithScore(String callingNo, String callId) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort()));
        String seperator = "?";
        if (callingNo != null) {
            sb.append(seperator);
            sb.append("callingNumber=");
            sb.append(callingNo);
            seperator = "";
        }
        if (callId != null) {
            if (seperator.equals("")) {
                sb.append("&");
            } else {
                sb.append(seperator);
            }
            sb.append(String.format("callId=%s", callId));
        }
        // System.out.println("Request url:" + sb.toString());
        return RequestBuilder.createGetRequest(sb.toString());
    }

    private String createFailureResponseJson(String failureReason)
            throws IOException {
        BadRequest badRequest = new BadRequest(failureReason);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(badRequest);
    }

    /**
     * To verify Get Bookmark with Score API is returning correct bookmark and
     * score details.
     */
    @Test
    public void verifyFT404() throws IOException, InterruptedException {
        bookmarkService.deleteAllBookmarksForUser("1234567890");

        // Blank bookmark should come as request response, As there is no any
        // bookmark in the system for the user
        HttpGet getRequest = createHttpGetBookmarkWithScore("1234567890",
                VALID_CALL_ID);

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                getRequest, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine()
                .getStatusCode());
        String responseJson = EntityUtils.toString(response.getEntity());
        assertNotNull(responseJson);
        assertTrue("{\"bookmark\":null,\"scoresByChapter\":null}".equals(responseJson));
    }

    /**
     * To verify Get Bookmark with Score API is rejected when mandatory
     * parameter CallingNumber is missing
     */
    // TODO JIRA issue: https://applab.atlassian.net/browse/NMS-238
    @Test
    public void verifyFT405() throws IOException, InterruptedException {
        HttpGet request = createHttpGetBookmarkWithScore(null,
                VALID_CALL_ID);

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());

        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Not Present>");
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify Get Bookmark with Score API is rejected when mandatory
     * parameter CallId is missing.
     */
    // TODO JIRA issue: https://applab.atlassian.net/browse/NMS-238
    @Test
    public void verifyFT406() throws IOException, InterruptedException {
        HttpGet request = createHttpGetBookmarkWithScore("1234567890", null);

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());

        String expectedJsonResponse = createFailureResponseJson("<callId: Not Present>");
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify Get Bookmark with Score API is rejected when mandatory
     * parameter CallingNumber is having invalid value.
     */
    // TODO JIRA issue: https://applab.atlassian.net/browse/NMS-239
    @Test
    public void verifyFT407() throws IOException, InterruptedException {
        // 11 digit callingNumber
        HttpGet request = createHttpGetBookmarkWithScore("12345678901",
                VALID_CALL_ID);

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());

        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Invalid>");
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response
                .getEntity())));

        // 9 digit callingNumber
        request = createHttpGetBookmarkWithScore("123456789", VALID_CALL_ID);

        request.setHeader("Content-type", "application/json");
        response = SimpleHttpClient.httpRequestAndResponse(request,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response
                .getEntity())));

        // Alphanumeric callingNumber
        request = createHttpGetBookmarkWithScore("123456A789",
                VALID_CALL_ID);

        request.setHeader("Content-type", "application/json");
        response = SimpleHttpClient.httpRequestAndResponse(request,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
    }

    /**
     * To verify Get Bookmark with Score API is rejected when mandatory
     * parameter CallId is having invalid value.
     */
    // TODO JIRA issue: https://applab.atlassian.net/browse/NMS-239
    @Test
    public void verifyFT408() throws IOException, InterruptedException {
        // callId more than 25 digit
        HttpGet request = createHttpGetBookmarkWithScore("1234567890",
                VALID_CALL_ID.concat("14"));

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());

        String expectedJsonResponse = createFailureResponseJson("<callId: Invalid>");
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response
                .getEntity())));

        // callId alphanumeric
        request = createHttpGetBookmarkWithScore("1234567890",
                "12345678901234A");

        request.setHeader("Content-type", "application/json");
        response = SimpleHttpClient.httpRequestAndResponse(request,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
    }

    /**
     * To verify that bookmark with score are saved correctly using Save
     * bookmark with score API.
     */
    @Test
    public void verifyFT409() throws IOException, InterruptedException {

        String endpoint = String.format(
                "http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();
        bookmarkRequest.setCallId(VALID_CALL_ID);
        bookmarkRequest.setCallingNumber(1234567890l);
        bookmarkRequest.setBookmark("Chapter01_Lesson01");
        Map<String, Integer> scoreMap = new HashMap<String, Integer>();
        scoreMap.put("1", 2);
        scoreMap.put("2", 0);
        scoreMap.put("3", 3);
        bookmarkRequest.setScoresByChapter(scoreMap);
        HttpPost request = RequestBuilder.createPostRequest(endpoint,
                bookmarkRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_OK,
                RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD));
    }

    /**
     * To verify that bookmark with score are saved correctly using Save
     * bookmark with score API when optional parameter are missing.
     */
    @Test
    public void verifyFT410() throws IOException, InterruptedException {
        // Request without callingNumber and Bookmark

        String endpoint = String.format(
                "http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();
        bookmarkRequest.setCallId(VALID_CALL_ID);
        bookmarkRequest.setCallingNumber(1234567890l);
        HttpPost request = RequestBuilder.createPostRequest(endpoint,
                bookmarkRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_OK,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    /**
     * To verify Save bookmark with score API is rejected when mandatory
     * parameter "callingNumber" is missing.
     */
    // TODO NMS-219
    @Test
    public void verifyFT411() throws IOException, InterruptedException {
        // callingNumber missing in the request body

        String endpoint = String.format(
                "http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();
        bookmarkRequest.setCallId(VALID_CALL_ID);
        bookmarkRequest.setBookmark("Chapter01_Lesson01");
        Map<String, Integer> scoreMap = new HashMap<String, Integer>();
        scoreMap.put("1", 2);
        scoreMap.put("2", 0);
        scoreMap.put("3", 3);
        bookmarkRequest.setScoresByChapter(scoreMap);
        HttpPost request = RequestBuilder.createPostRequest(endpoint,
                bookmarkRequest);
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Not Present>");
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response
                .getEntity())));
    }

    /**
     * To verify Save bookmark with score API is rejected when mandatory
     * parameter "callId" is missing.
     */
    // TODO NMS-219
    @Test
    public void verifyFT412() throws IOException, InterruptedException {
        // callId missing in the request body

        String endpoint = String.format(
                "http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();
        bookmarkRequest.setCallingNumber(1234567890l);
        bookmarkRequest.setBookmark("Chapter01_Lesson01");
        Map<String, Integer> scoreMap = new HashMap<String, Integer>();
        scoreMap.put("1", 2);
        scoreMap.put("2", 0);
        scoreMap.put("3", 3);
        bookmarkRequest.setScoresByChapter(scoreMap);
        HttpPost request = RequestBuilder.createPostRequest(endpoint,
                bookmarkRequest);
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        String expectedJsonResponse = createFailureResponseJson("<callId: Not Present>");
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response
                .getEntity())));
    }

    /**
     * To verify Save bookmark with score API is rejected when mandatory
     * parameter "callId" is having invalid value.
     */
    @Test
    public void verifyFT413() throws IOException, InterruptedException {
        // callId more than 25 digit
        String endpoint = String.format(
                "http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();
        bookmarkRequest.setCallId(VALID_CALL_ID.concat("foo"));
        bookmarkRequest.setCallingNumber(1234567890l);
        bookmarkRequest.setBookmark("Chapter01_Lesson01");
        Map<String, Integer> scoreMap = new HashMap<String, Integer>();
        scoreMap.put("1", 2);
        scoreMap.put("2", 0);
        scoreMap.put("3", 3);
        bookmarkRequest.setScoresByChapter(scoreMap);
        HttpPost request = RequestBuilder.createPostRequest(endpoint,
                bookmarkRequest);
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        String expectedJsonResponse = createFailureResponseJson("<callId: Invalid>");
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response
                .getEntity())));

        // callId less than 25 digit
        bookmarkRequest.setCallId(VALID_CALL_ID.substring(2));
        request = RequestBuilder.createPostRequest(endpoint, bookmarkRequest);
        response = SimpleHttpClient.httpRequestAndResponse(request,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response
                .getEntity())));
    }

    /**
     * To verify Save bookmark with score API is rejected when mandatory
     * parameter "callingNumber" is having invalid value.
     */
    @Test
    public void verifyFT414() throws IOException, InterruptedException {
        // callingNumber less than 10 digit

        String endpoint = String.format(
                "http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();
        bookmarkRequest.setCallingNumber(123456789l);
        bookmarkRequest.setCallId(VALID_CALL_ID);
        bookmarkRequest.setBookmark("Chapter01_Lesson01");
        Map<String, Integer> scoreMap = new HashMap<String, Integer>();
        scoreMap.put("1", 2);
        scoreMap.put("2", 0);
        scoreMap.put("3", 3);
        bookmarkRequest.setScoresByChapter(scoreMap);
        HttpPost request = RequestBuilder.createPostRequest(endpoint,
                bookmarkRequest);
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Invalid>");
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response
                .getEntity())));

        // callingNumber more than 10 digit
        bookmarkRequest.setCallingNumber(12345678901l);
        request = RequestBuilder.createPostRequest(endpoint, bookmarkRequest);
        response = SimpleHttpClient.httpRequestAndResponse(request,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response
                .getEntity())));
    }

    /**
     * To verify Save bookmark with score API is rejected when parameter
     * scoresByChapter is having value greater than 4.
     */
    // TODO JIRA issue: https://applab.atlassian.net/browse/NMS-221
    @Test
    public void verifyFT415() throws IOException, InterruptedException {
        // Invalid scores should not be accepted

        String endpoint = String.format(
                "http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();
        bookmarkRequest.setCallingNumber(1234567890l);
        bookmarkRequest.setCallId(VALID_CALL_ID);
        bookmarkRequest.setBookmark("Chapter01_Lesson01");
        Map<String, Integer> scoreMap = new HashMap<String, Integer>();
        scoreMap.put("1", 2);
        scoreMap.put("2", 6); // Invalid score greater than 4
        scoreMap.put("3", 3);
        bookmarkRequest.setScoresByChapter(scoreMap);
        HttpPost request = RequestBuilder.createPostRequest(endpoint,
                bookmarkRequest);
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        String expectedJsonResponse = createFailureResponseJson("<scoresByChapter: Invalid>");
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response
                .getEntity())));
    }

    /**
     * To verify Save bookmark with score API is rejected when parameter
     * "bookmark" is having invalid value.
     */
    // TODO JIRA issue: https://applab.atlassian.net/browse/NMS-222
    @Test
    @Ignore
    public void verifyFT416() throws IOException, InterruptedException {
        // Request with invalid bookmark value
        String endpoint = String.format(
                "http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();
        bookmarkRequest.setCallingNumber(1234567890l);
        bookmarkRequest.setCallId(VALID_CALL_ID);
        bookmarkRequest.setBookmark("Abc_Abc"); // Invalid bookmark
        Map<String, Integer> scoreMap = new HashMap<String, Integer>();
        scoreMap.put("1", 2);
        scoreMap.put("2", 3);
        scoreMap.put("3", 3);
        bookmarkRequest.setScoresByChapter(scoreMap);
        HttpPost request = RequestBuilder.createPostRequest(endpoint,
                bookmarkRequest);
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        String expectedJsonResponse = createFailureResponseJson("<bookmark: Invalid>");
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response
                .getEntity())));
    }

    /**
     * To verify that any re-attempt of the quiz by user shall lead to
     * overwriting of the previous score(lower) with new higher score.
     */
    @Test
    public void verifyFT417() throws IOException, InterruptedException {
        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();

        // save bookmark first
        bookmarkRequest.setCallingNumber(1234567890l);
        bookmarkRequest.setCallId(VALID_CALL_ID);
        bookmarkRequest.setBookmark("Chapter03_Lesson01");
        Map<String, Integer> scoreMap = new HashMap<String, Integer>();
        scoreMap.put("1", 2);
        scoreMap.put("2", 1);
        scoreMap.put("3", 0);
        bookmarkRequest.setScoresByChapter(scoreMap);
        HttpPost postRequest = RequestBuilder.createPostRequest(endpoint, bookmarkRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(postRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD));

        // assert if bookmark has been saved
        HttpGet getRequest = createHttpGetBookmarkWithScore("1234567890", VALID_CALL_ID);
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(getRequest, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        String expectedJsonResponse = "{\"bookmark\":\"Chapter03_Lesson01\",\"scoresByChapter\":{\"1\":2,\"2\":1,\"3\":0}}";
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));

        // save new scores
        scoreMap.put("3", 3);
        bookmarkRequest.setScoresByChapter(scoreMap);
        postRequest = RequestBuilder.createPostRequest(endpoint, bookmarkRequest);
        response = SimpleHttpClient.httpRequestAndResponse(postRequest, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        // assert if the new score has been saved
        response = SimpleHttpClient.httpRequestAndResponse(getRequest,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        expectedJsonResponse = "{\"bookmark\":\"Chapter03_Lesson01\",\"scoresByChapter\":{\"1\":2,\"2\":1,\"3\":3}}";
        String actualResponse = EntityUtils.toString(response.getEntity());
        assertEquals(expectedJsonResponse, actualResponse);
    }

    /**
     * To verify that any re-attempt of the quiz by user shall lead to
     * overwriting of the previous score(higher) with new lower score.
     */
    @Test
    public void verifyFT418() throws IOException, InterruptedException {
        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore", TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();

        // save bookamark first
        bookmarkRequest.setCallingNumber(1234567890l);
        bookmarkRequest.setCallId(VALID_CALL_ID);
        bookmarkRequest.setBookmark("Chapter03_Lesson01");
        Map<String, Integer> scoreMap = new HashMap<String, Integer>();
        scoreMap.put("1", 2);
        scoreMap.put("2", 1);
        scoreMap.put("3", 4);
        bookmarkRequest.setScoresByChapter(scoreMap);
        HttpPost postRequest = RequestBuilder.createPostRequest(endpoint, bookmarkRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(postRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD));

        // assert if bookmark has been saved
        HttpGet getRequest = createHttpGetBookmarkWithScore("1234567890", VALID_CALL_ID);
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(getRequest, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        String expectedJsonResponse = "{\"bookmark\":\"Chapter03_Lesson01\",\"scoresByChapter\":{\"1\":2,\"2\":1,\"3\":4}}";
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));

        // save new scores(Lower)
        scoreMap.put("3", 3);
        bookmarkRequest.setScoresByChapter(scoreMap);
        postRequest = RequestBuilder.createPostRequest(endpoint, bookmarkRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(postRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD));

        // assert if the new score has been saved
        response = SimpleHttpClient.httpRequestAndResponse(getRequest, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        expectedJsonResponse = "{\"bookmark\":\"Chapter03_Lesson01\",\"scoresByChapter\":{\"1\":2,\"2\":1,\"3\":3}}";
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that any re-attempt of the quiz by user shall lead to no change
     * in score when same score is attained in the re-attempt.
     */
    @Test
    public void verifyFT419() throws IOException, InterruptedException {

        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore", TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();

        // save bookamark first
        bookmarkRequest.setCallingNumber(1234567890l);
        bookmarkRequest.setCallId(VALID_CALL_ID);
        bookmarkRequest.setBookmark("Chapter03_Lesson01");
        Map<String, Integer> scoreMap = new HashMap<String, Integer>();
        scoreMap.put("1", 2);
        scoreMap.put("2", 1);
        scoreMap.put("3", 4);
        bookmarkRequest.setScoresByChapter(scoreMap);
        HttpPost postRequest = RequestBuilder.createPostRequest(endpoint, bookmarkRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(postRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD));

        // assert if bookmark has been saved
        HttpGet getRequest = createHttpGetBookmarkWithScore("1234567890", VALID_CALL_ID);
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(getRequest, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        String expectedJsonResponse = "{\"bookmark\":\"Chapter03_Lesson01\",\"scoresByChapter\":{\"1\":2,\"2\":1,\"3\":4}}";
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));

        // save new scores(Lower)
        scoreMap.put("3", 4);
        bookmarkRequest.setScoresByChapter(scoreMap);
        postRequest = RequestBuilder.createPostRequest(endpoint, bookmarkRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(postRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD));

        // assert if the new score has been saved
        response = SimpleHttpClient.httpRequestAndResponse(getRequest, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify course is marked completed when user has listened all the
     * chapters,attempted all the quiz and total score should be greater than
     * 50%(i.e 22)
     */
    // TODO JIRA issue: https://applab.atlassian.net/browse/NMS-240
    @Test
    public void verifyFT508() throws IOException, InterruptedException {
        String endpoint = String.format(
                "http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();

        // save bookamark first
        bookmarkRequest.setCallingNumber(1234567890l);
        bookmarkRequest.setCallId(VALID_CALL_ID);
        bookmarkRequest.setBookmark("COURSE_COMPLETED");
        Map<String, Integer> scoreMap = new HashMap<String, Integer>();
        scoreMap.put("1", 2);
        scoreMap.put("2", 1);
        scoreMap.put("3", 4);
        scoreMap.put("4", 2);
        scoreMap.put("5", 1);
        scoreMap.put("6", 4);
        scoreMap.put("7", 2);
        scoreMap.put("8", 1);
        scoreMap.put("9", 4);
        scoreMap.put("10", 1);
        scoreMap.put("11", 4);
        bookmarkRequest.setScoresByChapter(scoreMap);
        HttpPost postRequest = RequestBuilder.createPostRequest(endpoint,
                bookmarkRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(postRequest,
                HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD));

        CompletionRecord cr = completionRecordDataService
                .findRecordByCallingNumber(1234567890l);
        assertNotNull(cr);
        assertEquals(cr.getCompletionCount(), 1);

        // assert if bookmark has been reset
        HttpGet getRequest = createHttpGetBookmarkWithScore("1234567890",
                VALID_CALL_ID);
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                getRequest, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        String expectedJsonResponse = "{\"bookmark\":null,\"scoresByChapter\":null}";
        assertTrue(expectedJsonResponse.equals(EntityUtils.toString(response
                .getEntity())));
    }

    /**
     * To verify Get Bookmark with Score API is returning correct bookmark and
     * score details.
     */
    @Test
    public void verifyFT532() throws IOException, InterruptedException {
        // create bookmark for the user
        String endpoint = String.format(
                "http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();
        bookmarkRequest.setCallId(VALID_CALL_ID);
        bookmarkRequest.setCallingNumber(1234567890l);
        bookmarkRequest.setBookmark("Chapter01_Lesson01");
        Map<String, Integer> scoreMap = new HashMap<String, Integer>();
        scoreMap.put("1", 2);
        bookmarkRequest.setScoresByChapter(scoreMap);
        HttpPost postRequest = RequestBuilder.createPostRequest(endpoint,
                bookmarkRequest);
        SimpleHttpClient.httpRequestAndResponse(postRequest,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);

        // fetch bookmark for the same user
        HttpGet getRequest = createHttpGetBookmarkWithScore("1234567890",
                VALID_CALL_ID);
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                getRequest, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertTrue("{\"bookmark\":\"Chapter01_Lesson01\",\"scoresByChapter\":{\"1\":2}}"
                .equals(EntityUtils.toString(response.getEntity())));
    }

    @Ignore
    @Test
    /**
     * Testing sms status updates. See https://applab.atlassian.net/browse/NMS-250
     */
    public void verifyFT564() throws IOException, InterruptedException {
        // create completion record for msisdn 1234567890l
        CompletionRecord cr = new CompletionRecord(1234567890l, 25, true, 1, 0);
        cr = completionRecordDataService.create(cr);
        assertNull(cr.getLastDeliveryStatus());
        // invoke delivery notification API
        String endpoint = String.format(
        "http://localhost:%d/api/mobileacademy/smsdeliverystatus",
        TestContext.getJettyPort());
        HttpPost postRequest = new HttpPost(endpoint);
        postRequest.setHeader("Content-type", "application/json");
        String inputJson = "{\"requestData\": {\"deliveryInfoNotification\": {\"clientCorrelator\": \"abc100\""
        + ",\"deliveryInfo\": {\"address\": \"tel: 1234567890\",\"deliveryStatus\": \"DeliveredToNetwork\"}}}}";
        postRequest.setEntity(new StringEntity(inputJson));

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                postRequest, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        // assert completion record
        cr = completionRecordDataService.findRecordByCallingNumber(1234567890l);
        assertEquals(DeliveryStatus.DeliveredToNetwork.toString(), cr.getLastDeliveryStatus());
    }
}

