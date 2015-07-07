package org.motechproject.nms.testing.it.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.api.web.BaseController;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.SaveBookmarkRequest;
import org.motechproject.nms.api.web.contract.mobileAcademy.SmsStatusRequest;
import org.motechproject.nms.api.web.contract.mobileAcademy.sms.RequestData;
import org.motechproject.nms.mobileacademy.domain.NmsCourse;
import org.motechproject.nms.mobileacademy.dto.MaCourse;
import org.motechproject.nms.mobileacademy.repository.NmsCourseDataService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
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

/**
 * Integration tests for mobile academy controller
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class MobileAcademyControllerBundleIT extends BasePaxIT {

    @Inject
    MobileAcademyService mobileAcademyService;

    @Inject
    TestingService testingService;

    @Inject
    private NmsCourseDataService nmsCourseDataService;

    private static final String COURSE_NAME = "MobileAcademyCourse";

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
        bookmarkRequest.setCallId(BaseController.SMALLEST_15_DIGIT_NUMBER - 1);
        HttpPost request = RequestBuilder.createPostRequest(endpoint, bookmarkRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_BAD_REQUEST, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    @Test
    public void testBookmarkBadCallIdLargest() throws IOException, InterruptedException {

        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();
        bookmarkRequest.setCallId(BaseController.LARGEST_15_DIGIT_NUMBER + 1);
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
        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore?callingNumber=1234567890&callId=123456789012345",
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
        bookmark.setCallId(BaseController.SMALLEST_15_DIGIT_NUMBER);
        HttpPost request = RequestBuilder.createPostRequest(endpoint, bookmark);
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    @Test
    public void testTriggerNotification() throws IOException, InterruptedException {

        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmark = new SaveBookmarkRequest();
        bookmark.setCallingNumber(BaseController.SMALLEST_10_DIGIT_NUMBER);
        bookmark.setCallId(BaseController.SMALLEST_15_DIGIT_NUMBER);

        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 4);
        }
        bookmark.setScoresByChapter(scores);
        bookmark.setBookmark("Chapter11_Quiz");
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
        bookmark.setCallId(BaseController.SMALLEST_15_DIGIT_NUMBER);
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
    @Ignore
    public void testSmsStatusInvalidFormat() throws IOException, InterruptedException {
        String endpoint = String.format("http://localhost:%d/api/mobileacademy/smsdeliverystatus",
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
        String jsonText = IOUtils
                .toString(getFileInputStream("nmsCourse.json"));
        JSONObject jo = new JSONObject(jsonText);
        course.setName(jo.get("name").toString());
        course.setContent(jo.get("chapters").toString());
        nmsCourseDataService.create(new NmsCourse(course.getName(), course
                .getContent()));
        return jo;
    }

    private InputStream getFileInputStream(String fileName) {
        try {
            return new FileInputStream(new File(Thread.currentThread()
                    .getContextClassLoader().getResource(fileName).getPath()));
        } catch (IOException io) {
            return null;
        }
    }

    /**
     * To verify Get MA Course Version API is not returning course version when
     * MA course structure doesn't exist.
     */
    @Test
    public void verifyFT400() throws IOException, InterruptedException {
        String endpoint = String.format(
                "http://localhost:%d/api/mobileacademy/courseVersion",
                TestContext.getJettyPort());
        HttpGet request = RequestBuilder.createGetRequest(endpoint);
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);

        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, httpResponse
                .getStatusLine().getStatusCode());
    }

    /**
     * To verify Get MA Course Version API is returning correct course version
     * when MA course structure exist .
     */
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
                + course.getModificationDate().getMillis() + "}";

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
                .getMillis());
        courseResponseDTO.setChapters(jo.get("chapters").toString());

        ObjectMapper mapper = new ObjectMapper();
        String expectedJsonResponse = mapper
                .writeValueAsString(courseResponseDTO);

        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(httpResponse.getEntity()));
    }

}
