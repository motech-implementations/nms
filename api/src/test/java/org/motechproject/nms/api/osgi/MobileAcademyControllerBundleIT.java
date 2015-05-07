package org.motechproject.nms.api.osgi;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.api.utils.CourseBuilder;
import org.motechproject.nms.api.utils.RequestBuilder;
import org.motechproject.nms.api.web.BaseController;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.SaveBookmarkRequest;
import org.motechproject.nms.api.web.converter.MobileAcademyConverter;
import org.motechproject.nms.mobileacademy.domain.Course;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Integration tests for mobile academy controller
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class MobileAcademyControllerBundleIT extends BasePaxIT {

    @Inject
    private MobileAcademyService mobileAcademyService;

    @Test
    public void testBookmarkBadCallingNumber() throws IOException, InterruptedException {

        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());

        HttpPost request = RequestBuilder.createPostRequest(endpoint, new SaveBookmarkRequest());
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_BAD_REQUEST, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    @Test
    public void testBookmarkBadCallId() throws IOException, InterruptedException {

        String endpoint = String.format("http://localhost:%d/api/mobileacademy/bookmarkWithScore",
                TestContext.getJettyPort());
        SaveBookmarkRequest bookmarkRequest = new SaveBookmarkRequest();
        bookmarkRequest.setCallId(BaseController.SMALLEST_15_DIGIT_NUMBER - 1);
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
    public void testGetCourseNotPresent() throws IOException, InterruptedException {

        String endpoint = String.format("http://localhost:%d/api/mobileacademy/course",
                TestContext.getJettyPort());
        HttpGet request = RequestBuilder.createGetRequest(endpoint);

        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_INTERNAL_SERVER_ERROR, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    @Test
    public void testGetCourseValid() throws IOException, InterruptedException {

        String endpoint = String.format("http://localhost:%d/api/mobileacademy/course",
                TestContext.getJettyPort());
        HttpGet request = RequestBuilder.createGetRequest(endpoint);

        CourseResponse response = CourseBuilder.generateValidCourseResponse();
        Course currentCourse = MobileAcademyConverter.convertCourseResponse(response);
        when(mobileAcademyService.getCourse()).thenReturn(currentCourse);

        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

}
