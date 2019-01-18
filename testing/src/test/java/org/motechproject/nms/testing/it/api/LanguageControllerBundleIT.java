package org.motechproject.nms.testing.it.api;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.api.web.contract.UserLanguageRequest;
import org.motechproject.nms.flw.domain.FlwJobStatus;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.domain.ServiceUsageCap;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.props.domain.DeployedService;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.props.repository.DeployedServiceDataService;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.NationalDefaultLanguage;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.NationalDefaultLanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.LanguageService;
import org.motechproject.nms.testing.it.utils.RegionHelper;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class LanguageControllerBundleIT extends BasePaxIT {
    private static final String ADMIN_USERNAME = "motech";
    private static final String ADMIN_PASSWORD = "motech";

    private static final String VALID_CALL_ID = "1234567890123456789012345";

    @Inject
    FrontLineWorkerService frontLineWorkerService;

    @Inject
    LanguageDataService languageDataService;

    @Inject
    LanguageService languageService;

    @Inject
    ServiceUsageCapDataService serviceUsageCapDataService;

    @Inject
    StateDataService stateDataService;

    @Inject
    DeployedServiceDataService deployedServiceDataService;

    @Inject
    CircleDataService circleDataService;

    @Inject
    TestingService testingService;

    @Inject
    DistrictDataService districtDataService;

    @Inject
    DistrictService districtService;
    
    @Inject
    NationalDefaultLanguageDataService nationalDefaultLanguageDataService;

    private RegionHelper rh;

    @Before
    public void setupTestData() {

        testingService.clearDatabase();

        rh = new RegionHelper(languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, districtService);
//        rh.newDelhiDistrict();  delhiCircle also creates the district
        rh.delhiCircle();

        // All 3 services deployed in DELHI
        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.KILKARI));
        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.MOBILE_KUNJI));
        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.MOBILE_ACADEMY));

        // Services not deployed in KARNATAKA
//        rh.bangaloreDistrict();  karnatakaCircle creates bangalore district
        rh.karnatakaCircle();
    }

    private void createFlwCappedServiceNoUsageNoLocationNoLanguage() {
        FrontLineWorker flw = new FrontLineWorker("Frank Lloyd Wright", 1111111111l);
        frontLineWorkerService.add(flw);

        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(null, Service.MOBILE_KUNJI, 3600);
        serviceUsageCapDataService.create(serviceUsageCap);
    }

    @Test
    public void testSetLanguageInvalidService() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/NO_SERVICE/languageLocationCode", TestContext.getJettyPort()));
        StringEntity params = new StringEntity("{\"callingNumber\":1111111111,\"callId\":"+ VALID_CALL_ID + ",\"languageLocationCode\":10}");
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<serviceName: Invalid>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testSetLanguageUndeployedState() throws IOException, InterruptedException {
        createFlwCappedServiceNoUsageNoLocationNoLanguage();

        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/languageLocationCode", TestContext.getJettyPort()));
        StringEntity params = new StringEntity(
                "{\"callingNumber\":1111111111,\"callId\":"+ VALID_CALL_ID + ",\"languageLocationCode\":\""
                        + rh.tamilLanguage().getCode() + "\"}");
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        // the MOBILE_ACADEMY service hasn't been deployed for KARNATAKA, so this request should fail
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());
        assertEquals("{\"failureReason\":\"<MOBILE_ACADEMY: Not Deployed In State>\"}", EntityUtils.toString(response.getEntity()));

    }

    /**
     * To set the LanguageLocationCode of the user using Set User Language
     * Location Code API.
     */
    @Test
    public void verifyFT463() throws IOException, InterruptedException {
        // create FLW record
        FrontLineWorker flw = new FrontLineWorker(1111111112l);
        flw.setJobStatus(FlwJobStatus.ACTIVE);
        frontLineWorkerService.add(flw);
        flw = frontLineWorkerService
                .getByContactNumber(1111111112l);
        assertEquals(null, flw.getLanguage());// No Language

        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/mobileacademy/languageLocationCode",
                TestContext.getJettyPort()));
        StringEntity params = new StringEntity(
                "{\"callingNumber\":1111111112,\"callId\":"+ VALID_CALL_ID + ",\"languageLocationCode\":\""
                        + rh.hindiLanguage().getCode() + "\"}");
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");
        
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        
        flw = frontLineWorkerService
                .getByContactNumber(1111111112l);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(rh.hindiLanguage().getCode(), flw.getLanguage().getCode());
    }

    /**
     * To check that Set User Language Location Code API is rejected when
     * mandatory parameter callingNumber is missing.
     */
    @Test
    public void verifyFT464() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/mobileacademy/languageLocationCode",
                TestContext.getJettyPort()));
        StringEntity params = new StringEntity(
                "{\"callId\":"+ VALID_CALL_ID + ",\"languageLocationCode\":\""
                        + rh.hindiLanguage().getCode() + "\"}");
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callingNumber: Not Present>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /**
     * To check that Set User Language Location Code API is rejected when
     * mandatory parameter callId is missing.
     */
    @Test
    public void verifyFT465() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/mobileacademy/languageLocationCode",
                TestContext.getJettyPort()));
        StringEntity params = new StringEntity(
                "{\"callingNumber\":1111111112,\"languageLocationCode\":\""
                        + rh.hindiLanguage().getCode() + "\"}");
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost,
                HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callId: Not Present>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /**
     * To check that Set User Language Location Code API is rejected when
     * mandatory parameter languageLocationCode is missing.
     */
    @Test
    public void verifyFT466() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/mobileacademy/languageLocationCode",
                TestContext.getJettyPort()));
        StringEntity params = new StringEntity(
                "{\"callingNumber\":1111111112,\"callId\":"+ VALID_CALL_ID + "}");
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost,
                HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<languageLocationCode: Not Present>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /**
     * To check that Set User Language Location Code API is rejected when
     * mandatory parameter callingNumber is having invalid value
     * i.e.callingNumber value less than 10 digit
     */
    @Test
    public void verifyFT467() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/mobileacademy/languageLocationCode",
                TestContext.getJettyPort()));
        StringEntity params = new StringEntity(
                "{\"callingNumber\":123456789,\"callId\":"+ VALID_CALL_ID + ",\"languageLocationCode\":\""
                        + rh.hindiLanguage().getCode() + "\"}");
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callingNumber: Invalid>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /**
     * To check that Set User Language Location Code API is rejected when
     * mandatory parameter callId is having invalid value. i.e.callId value
     * greater than 15 digit
     */
    @Test
    public void verifyFT468() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/mobileacademy/languageLocationCode",
                TestContext.getJettyPort()));
        StringEntity params = new StringEntity(
                "{\"callingNumber\":1234567890,\"callId\":1234567890123456,\"languageLocationCode\":\""
                        + rh.hindiLanguage().getCode() + "\"}");
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");
        
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost,
                HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callId: Invalid>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    /**
     * To check that Set User Language Location Code API is rejected when
     * mandatory parameter languageLocationCode is having invalid value. i.e.
     * Language Location Code value languageLocationCode value that doesn’t
     * exist in NMS DB.
     */
    @Test
    public void verifyFT469() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/mobileacademy/languageLocationCode",
                TestContext.getJettyPort()));
        StringEntity params = new StringEntity(
                "{\"callingNumber\":1234567890,\"callId\":"+ VALID_CALL_ID + ",\"languageLocationCode\":\"TT\"}");
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost,
                HttpStatus.SC_NOT_FOUND,
                "{\"failureReason\":\"<languageLocationCode: Not Found>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }
    
    private void createFlwWithStatusAnonymous(){
    	Language language = new Language("99", "Papiamento");
        languageDataService.create(language);
        
    	// create anonymous FLW record
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        frontLineWorkerService.add(flw);
        
        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(null, Service.MOBILE_KUNJI, 3600);
        serviceUsageCapDataService.create(serviceUsageCap);
        
        nationalDefaultLanguageDataService.create(new NationalDefaultLanguage(language));
        
    }
}
