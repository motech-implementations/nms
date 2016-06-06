package org.motechproject.nms.testing.it.api;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.api.web.contract.AddFlwRequest;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.HealthFacilityTypeDataService;
import org.motechproject.nms.region.repository.HealthSubFacilityDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.repository.TalukaDataService;
import org.motechproject.nms.region.repository.VillageDataService;
import org.motechproject.nms.region.service.HealthBlockService;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.inject.Inject;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Integration tests for Ops controller
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class OpsControllerBundleIT extends BasePaxIT {

    private String addFlwEndpoint = String.format("http://localhost:%d/api/ops/addFlw",
            TestContext.getJettyPort());
    State state;
    District district;
    Taluka taluka;
    Village village;
    HealthBlock healthBlock;
    HealthFacilityType healthFacilityType;
    HealthFacility healthFacility;
    HealthSubFacility healthSubFacility;
    Language language;

    @Inject
    PlatformTransactionManager transactionManager;

    @Inject
    TestingService testingService;

    @Inject
    LanguageDataService languageDataService;

    @Inject
    StateDataService stateDataService;

    @Inject
    DistrictDataService districtDataService;

    @Inject
    TalukaDataService talukaDataService;

    @Inject
    VillageDataService villageDataService;

    @Inject
    HealthBlockDataService healthBlockDataService;

    @Inject
    HealthBlockService healthBlockService;

    @Inject
    HealthFacilityDataService healthFacilityDataService;

    @Inject
    HealthSubFacilityDataService healthSubFacilityDataService;

    @Inject
    FrontLineWorkerDataService frontLineWorkerDataService;

    @Before
    public void setupTestData() {
        testingService.clearDatabase();
        initializeLocationData();
    }

    // Test flw update with empty flw request
    @Test
    public void testEmptyAddFlwRequest() throws IOException, InterruptedException {
        HttpPost request = RequestBuilder.createPostRequest(addFlwEndpoint, new AddFlwRequest());
        assertTrue(SimpleHttpClient.execHttpRequest(request, HttpStatus.SC_BAD_REQUEST, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    // flw udpate failes with not all required fields present
    @Test
    public void testBadContactNumberAddFlwRequest() throws IOException, InterruptedException {
        AddFlwRequest addRequest = new AddFlwRequest();
        addRequest.setContactNumber(9876543210L);
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, addRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_BAD_REQUEST, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    // Create valid new flw
    @Test
    public void testCreateNewFlw() throws IOException, InterruptedException {
        AddFlwRequest addFlwRequest = getAddRequest();
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, addFlwRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    // Flw test name update
    @Test
    public void testUpdateFlwName() throws IOException, InterruptedException {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // create flw
        FrontLineWorker flw = new FrontLineWorker("Kookoo Devi" ,9876543210L);
        flw.setMctsFlwId("123");
        flw.setState(state);
        flw.setDistrict(district);
        flw.setLanguage(language);
        frontLineWorkerDataService.create(flw);
        transactionManager.commit(status);

        AddFlwRequest updateRequest = getAddRequest();
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, updateRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    // Flw update phone number
    @Test
    public void testUpdateFlwPhoneOpen() throws IOException, InterruptedException {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // create flw
        FrontLineWorker flw = new FrontLineWorker("Kookoo Devi" ,9876543210L);
        flw.setMctsFlwId("123");
        flw.setState(state);
        flw.setDistrict(district);
        flw.setLanguage(language);
        frontLineWorkerDataService.create(flw);
        transactionManager.commit(status);


        AddFlwRequest updateRequest = getAddRequest();
        updateRequest.setContactNumber(9876543211L);    // update
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, updateRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    // Test flw update to an existing used phone number by someone else
    @Test
    public void testUpdateFlwPhoneOccupied() throws IOException, InterruptedException {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // create flw
        FrontLineWorker flw = new FrontLineWorker("Kookoo Devi" ,9876543210L);
        flw.setMctsFlwId("456");
        flw.setState(state);
        flw.setDistrict(district);
        flw.setLanguage(language);
        frontLineWorkerDataService.create(flw);
        transactionManager.commit(status);

        AddFlwRequest updateRequest = getAddRequest();
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, updateRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));

        //TODO: check flw error records
    }

    // helper to create a valid flw add/update request
    private AddFlwRequest getAddRequest() {
        AddFlwRequest request = new AddFlwRequest();
        request.setContactNumber(9876543210L);
        request.setName("Chinkoo Devi");
        request.setMctsFlwId("123");
        request.setStateId(state.getCode());
        request.setDistrictId(district.getCode());
        request.setType("ANM");
        return request;
    }

    // helper to create location data
    private void initializeLocationData() {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        healthSubFacility = new HealthSubFacility();
        healthSubFacility.setName("Health Sub Facility 1");
        healthSubFacility.setRegionalName("Health Sub Facility 1");
        healthSubFacility.setCode(1L);

        healthFacilityType = new HealthFacilityType();
        healthFacilityType.setName("Health Facility Type 1");
        healthFacilityType.setCode(1L);

        healthFacility = new HealthFacility();
        healthFacility.setName("Health Facility 1");
        healthFacility.setRegionalName("Health Facility 1");
        healthFacility.setCode(1L);
        healthFacility.setHealthFacilityType(healthFacilityType);
        healthFacility.getHealthSubFacilities().add(healthSubFacility);

        healthBlock = new HealthBlock();
        healthBlock.setName("Health Block 1");
        healthBlock.setRegionalName("Health Block 1");
        healthBlock.setHq("Health Block 1 HQ");
        healthBlock.setCode(1L);
        healthBlock.getHealthFacilities().add(healthFacility);

        village = new Village();
        village.setName("Village 1");
        village.setRegionalName("Village 1");
        village.setVcode(1L);

        taluka = new Taluka();
        taluka.setName("Taluka 1");
        taluka.setRegionalName("Taluka 1");
        taluka.setIdentity(1);
        taluka.setCode("0004");
        taluka.getVillages().add(village);
        taluka.getHealthBlocks().add(healthBlock);

        district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);
        district.getTalukas().add(taluka);

        state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);

        language = languageDataService.create(new Language("15", "HINDI_DEFAULT"));
        district.setLanguage(language);
        stateDataService.create(state);

        transactionManager.commit(status);
    }

}
