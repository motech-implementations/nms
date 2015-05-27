package org.motechproject.nms.testing.it.api;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.api.web.contract.UserLanguageRequest;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.domain.ServiceUsageCap;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.props.domain.DeployedService;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.props.repository.DeployedServiceDataService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.repository.StateDataService;
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

    @Inject
    private FrontLineWorkerService frontLineWorkerService;

    @Inject
    private LanguageDataService languageDataService;

    @Inject
    private LanguageLocationDataService languageLocationDataService;

    @Inject
    private ServiceUsageCapDataService serviceUsageCapDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DeployedServiceDataService deployedServiceDataService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    private CircleDataService circleDataService;

    @Inject
    private FrontLineWorkerDataService frontLineWorkerDataService;

    private void cleanAllData() {
        for (FrontLineWorker flw: frontLineWorkerDataService.retrieveAll()) {
            flw.setStatus(FrontLineWorkerStatus.INVALID);
            flw.setInvalidationDate(new DateTime().withDate(2011, 8, 1));

            frontLineWorkerDataService.update(flw);
        }

        frontLineWorkerDataService.deleteAll();
        serviceUsageCapDataService.deleteAll();
        languageLocationDataService.deleteAll();
        languageDataService.deleteAll();
        deployedServiceDataService.deleteAll();
        stateDataService.deleteAll();
        districtDataService.deleteAll();
        circleDataService.deleteAll();
    }

    private void createCircleWithLanguage() {
        cleanAllData();

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);

        stateDataService.create(state);

        deployedServiceDataService.create(new DeployedService(state, Service.KILKARI));
        deployedServiceDataService.create(new DeployedService(state, Service.MOBILE_KUNJI));

        Language language = new Language("Papiamento");
        languageDataService.create(language);

        LanguageLocation languageLocation = new LanguageLocation("99", new Circle("AA"), language, false);
        languageLocation.getDistrictSet().add(district);
        languageLocationDataService.create(languageLocation);
    }

    private void createLanguageLocationForUndeployedState() {
        cleanAllData();

        District district = new District();
        district.setName("District 2");
        district.setRegionalName("District 2");
        district.setCode(2L);

        State state = new State();
        state.setName("State 2");
        state.setCode(2L);
        state.getDistricts().add(district);

        stateDataService.create(state);

        Language language = new Language("malayalam");
        languageDataService.create(language);

        LanguageLocation undeployedLanguageLocation = new LanguageLocation("88", new Circle("BB"), language, true);
        undeployedLanguageLocation.getDistrictSet().add(district);
        languageLocationDataService.create(undeployedLanguageLocation);
    }


    private void createFlwCappedServiceNoUsageNoLocationNoLanguage() {
        createCircleWithLanguage();

        FrontLineWorker flw = new FrontLineWorker("Frank Lloyd Wright", 1111111111l);
        frontLineWorkerService.add(flw);

        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(null, Service.MOBILE_KUNJI, 3600);
        serviceUsageCapDataService.create(serviceUsageCap);
    }

    @Test
    public void testSetLanguageInvalidService() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/NO_SERVICE/languageLocationCode", TestContext.getJettyPort()));
        StringEntity params = new StringEntity("{\"callingNumber\":1111111111,\"callId\":123456789012345,\"languageLocationCode\":10}");
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<serviceName: Invalid>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testSetLanguageMissingCallingNumber() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobilekunji/languageLocationCode", TestContext.getJettyPort()));
        StringEntity params = new StringEntity("{\"callId\":123456789012345,\"languageLocationCode\":10}");
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callingNumber: Not Present>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testSetLanguageInvalidCallingNumber() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobilekunji/languageLocationCode", TestContext.getJettyPort()));

        UserLanguageRequest request = new UserLanguageRequest(
                123L, //callingNumber
                123456789012345l, //callId
                "123"); //languageLocationCode
        String json = new ObjectMapper().writeValueAsString(request);
        StringEntity params = new StringEntity(json);
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callingNumber: Invalid>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testSetLanguageMissingCallId() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(
                String.format("http://localhost:%d/api/mobilekunji/languageLocationCode",
                        TestContext.getJettyPort()));
        StringEntity params = new StringEntity("{\"callingNumber\":1111111111,\"languageLocationCode\":10}");
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST,
                "{\"failureReason\":\"<callId: Not Present>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testSetLanguageInvalidCallId() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(
                String.format("http://localhost:%d/api/mobilekunji/languageLocationCode",
                        TestContext.getJettyPort()));
        StringEntity params = new StringEntity(
                "{\"callingNumber\":abcdef,\"callId\":\"123456789012345\",\"languageLocationCode\":\"10\"}");
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, ADMIN_USERNAME,
                ADMIN_PASSWORD));
    }

    @Test
    public void testSetLanguageMissingLanguageLocationCode() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobilekunji/languageLocationCode", TestContext.getJettyPort()));
        StringEntity params = new StringEntity("{\"callingNumber\":abcdef,\"callId\":123456789012345}");
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, ADMIN_USERNAME,
                ADMIN_PASSWORD));
    }

    @Test
    public void testSetLanguageInvalidLanguageLocationCode() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobilekunji/languageLocationCode", TestContext.getJettyPort()));
        StringEntity params = new StringEntity("{\"callingNumber\":abcdef,\"callId\":123456789012345,\"languageLocationCode\":\"AA\"}");
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, ADMIN_USERNAME,
                ADMIN_PASSWORD));
    }

    @Test
    public void testSetLanguageNoFLW() throws IOException, InterruptedException {
        createCircleWithLanguage();

        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobilekunji/languageLocationCode", TestContext.getJettyPort()));
        StringEntity params = new StringEntity("{\"callingNumber\":1111111111,\"callId\":123456789012345,\"languageLocationCode\":99}");
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK));

        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(1111111111l);
        assertNotNull(flw);
        assertEquals(FrontLineWorkerStatus.ANONYMOUS, flw.getStatus());
        LanguageLocation languageLocation = flw.getLanguageLocation();
        assertNotNull(languageLocation);
        assertEquals("FLW Language Code", "99", languageLocation.getCode());
    }

    @Test
    public void testSetLanguageLanguageNotFound() throws IOException, InterruptedException {
        createFlwCappedServiceNoUsageNoLocationNoLanguage();

        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobilekunji/languageLocationCode", TestContext.getJettyPort()));
        StringEntity params = new StringEntity("{\"callingNumber\":1111111111,\"callId\":123456789012345,\"languageLocationCode\":77}");
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_NOT_FOUND,
                "{\"failureReason\":\"<languageLocationCode: Not Found>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testSetLanguageUndeployedState() throws IOException, InterruptedException {
        createFlwCappedServiceNoUsageNoLocationNoLanguage();

        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobileacademy/languageLocationCode", TestContext.getJettyPort()));
        StringEntity params = new StringEntity("{\"callingNumber\":1111111111,\"callId\":123456789012345,\"languageLocationCode\":99}");
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        // the MOBILE_ACADEMY service hasn't been deployed for State 1, so this request should fail
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_NOT_IMPLEMENTED,
                "{\"failureReason\":\"<MOBILE_ACADEMY: Not Deployed In State>\"}",
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testSetLanguageValid() throws IOException, InterruptedException {
        createFlwCappedServiceNoUsageNoLocationNoLanguage();

        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobilekunji/languageLocationCode", TestContext.getJettyPort()));
        StringEntity params = new StringEntity("{\"callingNumber\":1111111111,\"callId\":123456789012345,\"languageLocationCode\":99}");
        httpPost.setEntity(params);

        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK));

        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(1111111111l);
        LanguageLocation languageLocation = flw.getLanguageLocation();
        assertNotNull(languageLocation);
        assertEquals(FrontLineWorkerStatus.ANONYMOUS, flw.getStatus());
        assertEquals("FLW Language Code", "99", languageLocation.getCode());
    }
}
