package org.motechproject.nms.api.osgi;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.http.SimpleHttpClient;
import org.motechproject.testing.utils.TestContext;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import java.io.IOException;


/**
 * Verify that LanguageService HTTP service is present and functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class UserControllerBundleIT extends BasePaxIT {
    private static final String ADMIN_USERNAME = "motech";
    private static final String ADMIN_PASSWORD = "motech";

    @Test
    public void testUserRequest() throws IOException, InterruptedException {
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/kilkari/user?callingNumber=0123456789&operator=OP&circle=AA&callId=0123456789abcde", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        SimpleHttpClient.execHttpRequest(httpGet, "{\"circle\":\"AA\",\"languageLocationCode\":\"??\"}");
    }

    @Test
    @Ignore
    public void testInvalidServiceName() throws IOException, InterruptedException {
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/NO_SERVICE/user?callingNumber=0123456789&operator=OP&circle=AA&callId=0123456789abcde", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        SimpleHttpClient.execHttpRequest(httpGet, "{\"failureReason\":\"<serviceName: Invalid>\"}");
    }

    @Test
    @Ignore
    public void testNoCallingNumber() throws IOException, InterruptedException {
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/kilkari/user?operator=OP&circle=AA&callId=0123456789abcde", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        SimpleHttpClient.execHttpRequest(httpGet, "{\"failureReason\":\"<callingNumber: Not Present>\"}");
    }

    @Test
    @Ignore
    public void testInvalidCallingNumber() throws IOException, InterruptedException {
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/kilkari/user?callingNumber=XXXXXXX&operator=OP&circle=AA&callId=0123456789abcde", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        SimpleHttpClient.execHttpRequest(httpGet, "{\"failureReason\":\"<callingNumber: Invalid>\"}");
    }

    @Test
    @Ignore
    public void testNoOperator() throws IOException, InterruptedException {
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/kilkari/user?callingNumber=XXXXXXX&circle=AA&callId=0123456789abcde", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        SimpleHttpClient.execHttpRequest(httpGet, "{\"failureReason\":\"<operator: Not Present>\"}");
    }

    @Test
    @Ignore
    public void testNoCircle() throws IOException, InterruptedException {
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/kilkari/user?callingNumber=XXXXXXX&operator=OP&callId=0123456789abcde", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        SimpleHttpClient.execHttpRequest(httpGet, "{\"failureReason\":\"<callId: Not Present>\"}");
    }

    @Test
    @Ignore
    public void testNoCallId() throws IOException, InterruptedException {
        HttpGet httpGet = new HttpGet(String.format("http://localhost:%d/api/kilkari/user?callingNumber=XXXXXXX&operator=OP&circle=AA", TestContext.getJettyPort()));

        httpGet.addHeader("Authorization",
                "Basic " + new String(Base64.encodeBase64((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes())));

        SimpleHttpClient.execHttpRequest(httpGet, "{\"failureReason\":\"<callId: Not Present>\"}");
    }
}
