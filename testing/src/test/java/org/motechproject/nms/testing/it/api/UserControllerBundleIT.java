package org.motechproject.nms.testing.it.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.api.web.contract.BadRequest;
import org.motechproject.nms.api.web.contract.FlwUserResponse;
import org.motechproject.nms.api.web.contract.UserLanguageRequest;
import org.motechproject.nms.api.web.contract.kilkari.KilkariUserResponse;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.domain.ServiceUsage;
import org.motechproject.nms.flw.domain.ServiceUsageCap;
import org.motechproject.nms.flw.domain.WhitelistEntry;
import org.motechproject.nms.flw.domain.WhitelistState;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.repository.ServiceUsageDataService;
import org.motechproject.nms.flw.repository.WhitelistEntryDataService;
import org.motechproject.nms.flw.repository.WhitelistStateDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.service.WhitelistService;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.DeployedService;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.props.repository.DeployedServiceDataService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.NationalDefaultLanguage;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.NationalDefaultLanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.testing.it.api.utils.RequestBuilder;
import org.motechproject.nms.testing.it.utils.RegionHelper;
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.http.SimpleHttpClient;
import org.motechproject.testing.utils.TestContext;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;


/**
 * Verify that User API is present and functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class UserControllerBundleIT extends BasePaxIT {
    private static final String ADMIN_USERNAME = "motech";
    private static final String ADMIN_PASSWORD = "motech";

    @Inject
    SubscriptionService subscriptionService;
    @Inject
    SubscriberDataService subscriberDataService;
    @Inject
    SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    FrontLineWorkerService frontLineWorkerService;
    @Inject
    ServiceUsageDataService serviceUsageDataService;
    @Inject
    ServiceUsageCapDataService serviceUsageCapDataService;
    @Inject
    LanguageDataService languageDataService;
    @Inject
    StateDataService stateDataService;
    @Inject
    WhitelistEntryDataService whitelistEntryDataService;
    @Inject
    WhitelistStateDataService whitelistStateDataService;
    @Inject
    CircleDataService circleDataService;
    @Inject
    DistrictDataService districtDataService;
    @Inject
    DistrictService districtService;
    @Inject
    DeployedServiceDataService deployedServiceDataService;
    @Inject
    NationalDefaultLanguageDataService nationalDefaultLanguageDataService;
    @Inject
    TestingService testingService;

    @Inject
    FrontLineWorkerDataService frontLineWorkerDataService;

    @Inject
    WhitelistService whitelistService;

    public static final Long WHITELIST_CONTACT_NUMBER = 1111111111l;
    public static final Long NOT_WHITELIST_CONTACT_NUMBER = 9000000000l;
    public Subscriber whitelistSubscriber;
    public Subscriber notWhiteListSubscriber;

    private State whitelistState;
    private State nonWhitelistState;

    private RegionHelper rh;
    private SubscriptionHelper sh;


    @Before
    public void setupTestData() {
        testingService.clearDatabase();

        rh = new RegionHelper(languageDataService, circleDataService, stateDataService, districtDataService,
                districtService);

        sh = new SubscriptionHelper(subscriptionService, subscriberDataService, subscriptionPackDataService,
                languageDataService, circleDataService, stateDataService, districtDataService, districtService);
    }


    private void createKilkariTestData() {

        rh.newDelhiDistrict();
        rh.delhiCircle();

            
        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.KILKARI));

        Subscriber subscriber1 = subscriberDataService.create(new Subscriber(1000000000L, rh.hindiLanguage()));
        subscriptionService.createSubscription(subscriber1.getCallingNumber(), rh.hindiLanguage(),
                sh.childPack(), SubscriptionOrigin.IVR);

        Subscriber subscriber2 = subscriberDataService.create(new Subscriber(2000000000L, rh.hindiLanguage()));
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), rh.hindiLanguage(),
                sh.childPack(), SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), rh.hindiLanguage(),
                sh.pregnancyPack(), SubscriptionOrigin.IVR);

        Subscriber subscriber3 = subscriberDataService.create(new Subscriber(3000000000L, rh.hindiLanguage()));
        subscriptionService.createSubscription(subscriber3.getCallingNumber(), rh.hindiLanguage(),
                sh.pregnancyPack(), SubscriptionOrigin.IVR);
    }

    private void createKilkariUndeployTestData() {

        rh.delhiState();
        rh.delhiCircle();

        Subscriber subscriber1 = subscriberDataService.create(new Subscriber(1000000000L, rh.hindiLanguage()));
        subscriptionService.createSubscription(subscriber1.getCallingNumber(), rh.hindiLanguage(),
                sh.childPack(), SubscriptionOrigin.IVR);
    }



    private void createFlwCappedServiceNoUsageNoLocationNoLanguage() {

        Language language = new Language("99", "Papiamento");
        languageDataService.create(language);

        FrontLineWorker flw = new FrontLineWorker("Frank Lloyd Wright", 1111111111L);
        frontLineWorkerService.add(flw);

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setLanguage(language);
        district.setCode(1L);

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);

        stateDataService.create(state);

        deployedServiceDataService.create(new DeployedService(state, Service.MOBILE_ACADEMY));
        deployedServiceDataService.create(new DeployedService(state, Service.MOBILE_KUNJI));

        Circle circle = new Circle("AA");
        circle.getStates().add(state);
        circle.setDefaultLanguage(language);
        circleDataService.create(circle);

        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(null, Service.MOBILE_KUNJI, 3600);
        serviceUsageCapDataService.create(serviceUsageCap);
    }

    private void createFlwWithLanguageServiceUsageAndCappedService() {

        rh.delhiState();
        rh.delhiCircle();

        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.MOBILE_ACADEMY));
        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.MOBILE_KUNJI));

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(null, Service.MOBILE_KUNJI, 3600);
        serviceUsageCapDataService.create(serviceUsageCap);

        // A service record without endOfService and WelcomePrompt played
        ServiceUsage serviceUsage = new ServiceUsage(flw, Service.MOBILE_KUNJI, 1, 0, 0, DateTime.now());
        serviceUsageDataService.create(serviceUsage);
    }

    private void createFlwWithLanguageFullServiceUsageAndCappedService() {

        rh.newDelhiDistrict();
        rh.southDelhiDistrict();
        rh.delhiCircle();

        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.MOBILE_ACADEMY));
        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.MOBILE_KUNJI));

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(null, Service.MOBILE_KUNJI, 3600);
        serviceUsageCapDataService.create(serviceUsageCap);

        ServiceUsage serviceUsage = new ServiceUsage(flw, Service.MOBILE_KUNJI, 1, 1, 1, DateTime.now());
        serviceUsageDataService.create(serviceUsage);
    }

    private void createFlwWithLanguageFullUsageOfBothServiceUncapped() {

        // Make sure to create two districts (with two languages) for the delhi state
        rh.newDelhiDistrict();
        rh.southDelhiDistrict();
        // And a circle
        rh.delhiCircle();

        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.MOBILE_ACADEMY));
        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.MOBILE_KUNJI));

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

        ServiceUsage serviceUsage = new ServiceUsage(flw, Service.MOBILE_KUNJI, 1, 1, 1, DateTime.now());
        serviceUsageDataService.create(serviceUsage);

        // Academy doesn't have a welcome prompt
        serviceUsage = new ServiceUsage(flw, Service.MOBILE_ACADEMY, 1, 1, 0, DateTime.now());
        serviceUsageDataService.create(serviceUsage);

        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(null, Service.MOBILE_KUNJI, 10);
        serviceUsageCapDataService.create(serviceUsageCap);
    }

    private void createFlwWithStateNotInWhitelist() {

        circleDataService.create(new Circle("AA"));

        District district = new District();
        district.setName("9");
        district.setRegionalName("9");
        district.setCode(9l);

        State whitelistState = new State("WhitelistState", 1l);
        whitelistState.getDistricts().add(district);

        district.setState(whitelistState);

        stateDataService.create(whitelistState);

        deployedServiceDataService.create(new DeployedService(whitelistState, Service.MOBILE_ACADEMY));
        deployedServiceDataService.create(new DeployedService(whitelistState, Service.MOBILE_KUNJI));

        whitelistStateDataService.create(new WhitelistState(whitelistState));

        WhitelistEntry entry = new WhitelistEntry(0000000000l, whitelistState);
        whitelistEntryDataService.create(entry);

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111l);
        flw.setState(whitelistState);
        flw.setDistrict(district);
        frontLineWorkerService.add(flw);
    }

    private void createFlwWithLanguageLocationCodeNotInWhitelist() {

        Language language = new Language("34", "Language From Whitelisted State");
        languageDataService.create(language);

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setLanguage(language);
        district.setCode(1L);

        State whitelist = new State();
        whitelist.setName("Whitelist");
        whitelist.setCode(1L);
        whitelist.getDistricts().add(district);

        stateDataService.create(whitelist);

        deployedServiceDataService.create(new DeployedService(whitelist, Service.MOBILE_ACADEMY));
        deployedServiceDataService.create(new DeployedService(whitelist, Service.MOBILE_KUNJI));

        Circle circle = new Circle("AA");
        circle.getStates().add(whitelist);
        circleDataService.create(circle);

        whitelistStateDataService.create(new WhitelistState(whitelist));

        WhitelistEntry entry = new WhitelistEntry(0000000000l, whitelist);
        whitelistEntryDataService.create(entry);

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111l);
        flw.setLanguage(language);
        frontLineWorkerService.add(flw);
    }

    private void createCircleWithLanguage() {

        // Let's create a pretend circle with two states

        final Circle circle = new Circle("AA");
        circle.setDefaultLanguage(rh.hindiLanguage());
        circleDataService.create(circle);

        // Calling these will make sure the districts exist and will map the districts' language to their state
        rh.newDelhiDistrict();
        rh.mysuruDistrict();

        //TODO: remove this when https://applab.atlassian.net/browse/MOTECH-1679 is fixed
        circleDataService.doInTransaction(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                circle.getStates().add(rh.delhiState());
                circle.getStates().add(rh.karnatakaState());
                circleDataService.update(circle);
            }
        });

        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.MOBILE_KUNJI));

        nationalDefaultLanguageDataService.create(new NationalDefaultLanguage(rh.hindiLanguage()));
    }

    private void createCircleWithSingleLanguage() {

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);

        stateDataService.create(state);

        Language language = new Language("99", "Papiamento");
        languageDataService.create(language);

        Circle circle = new Circle("AA");
        circle.setDefaultLanguage(language);
        circle.getStates().add(state);
        circleDataService.create(circle);

        nationalDefaultLanguageDataService.create(new NationalDefaultLanguage(language));
    }

    private HttpGet createHttpGet(boolean includeService, String service,
                                  boolean includeCallingNumber, String callingNumber,
                                  boolean includeOperator, String operator,
                                  boolean includeCircle, String circle,
                                  boolean includeCallId, String callId) {

        StringBuilder sb = new StringBuilder(String.format("http://localhost:%d/api/", TestContext.getJettyPort()));
        String sep = "";
        if (includeService) {
            sb.append(String.format("%s/", service));
        }
        sb.append("user?");
        if (includeCallingNumber) {
            sb.append(String.format("callingNumber=%s", callingNumber));
            sep = "&";
        }
        if (includeOperator) {
            sb.append(String.format("%soperator=%s", sep, operator));
            sep = "&";
        }
        if (includeCircle) {
            sb.append(String.format("%scircle=%s", sep, circle));
            sep = "&";
        }
        if (includeCallId) {
            sb.append(String.format("%scallId=%s", sep, callId));
        }

        return new HttpGet(sb.toString());
    }

    private HttpPost createHttpPost(String service, UserLanguageRequest request) throws IOException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/%s/languageLocationCode",
                TestContext.getJettyPort(), service));
        ObjectMapper mapper = new ObjectMapper();
        StringEntity params = new StringEntity(mapper.writeValueAsString(request));
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");
        return httpPost;
    }

    private String createKilkariUserResponseJson(String defaultLanguageLocationCode, String locationCode,
                                                 List<String> allowedLanguageLocations,
                                                 Set<String> subscriptionPackList) throws IOException {
        KilkariUserResponse kilkariUserResponse = new KilkariUserResponse();
        if (defaultLanguageLocationCode != null) {
            kilkariUserResponse.setDefaultLanguageLocationCode(defaultLanguageLocationCode);
        }
        if (locationCode != null) {
            kilkariUserResponse.setLanguageLocationCode(locationCode);
        }
        if (allowedLanguageLocations != null) {
            kilkariUserResponse.setAllowedLanguageLocationCodes(allowedLanguageLocations);
        }
        if (subscriptionPackList != null) {
            kilkariUserResponse.setSubscriptionPackList(subscriptionPackList);
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(kilkariUserResponse);
    }

    private String createFlwUserResponseJson(String defaultLanguageLocationCode, String locationCode,
                                             List<String> allowedLanguageLocations,
                                             Long currentUsageInPulses, Long endOfUsagePromptCounter,
                                             Boolean welcomePromptFlag, Integer maxAllowedUsageInPulses,
                                             Integer maxAllowedEndOfUsagePrompt) throws IOException {
        FlwUserResponse userResponse = new FlwUserResponse();
        if (defaultLanguageLocationCode != null) {
            userResponse.setDefaultLanguageLocationCode(defaultLanguageLocationCode);
        }
        if (locationCode != null) {
            userResponse.setLanguageLocationCode(locationCode);
        }
        if (allowedLanguageLocations != null) {
            userResponse.setAllowedLanguageLocationCodes(allowedLanguageLocations);
        }
        if (currentUsageInPulses != null) {
            userResponse.setCurrentUsageInPulses(currentUsageInPulses);
        }
        if (endOfUsagePromptCounter != null) {
            userResponse.setEndOfUsagePromptCounter(endOfUsagePromptCounter);
        }
        if (welcomePromptFlag != null) {
            userResponse.setWelcomePromptFlag(welcomePromptFlag);
        }
        if (maxAllowedUsageInPulses != null) {
            userResponse.setMaxAllowedUsageInPulses(maxAllowedUsageInPulses);
        }
        if (maxAllowedEndOfUsagePrompt != null) {
            userResponse.setMaxAllowedEndOfUsagePrompt(maxAllowedEndOfUsagePrompt);
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(userResponse);
    }

    private String createFailureResponseJson(String failureReason)throws IOException {
        BadRequest badRequest = new BadRequest(failureReason);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(badRequest);
    }

    private void createFlwWithLanguageNoDeployedServices() {

        Language language = new Language("10", "English");
        languageDataService.create(language);

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setLanguage(language);
        district.setCode(1L);

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);

        stateDataService.create(state);

        Circle circle = new Circle("AA");
        circle.getStates().add(state);
        circleDataService.create(circle);

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguage(language);
        frontLineWorkerService.add(flw);
    }

    // Request undeployed service by language location
    @Test
    public void testUndeployedServiceByLanguageLocation() throws IOException, InterruptedException {
        createFlwWithLanguageNoDeployedServices();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                false, null,             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFailureResponseJson("<MOBILE_KUNJI: Not Deployed In State>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());
    }

    private void createFlwWithLocationNoLanguageNoDeployedServices() {

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);

        stateDataService.create(state);

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setState(state);
        flw.setDistrict(district);
        frontLineWorkerService.add(flw);
    }

    // Request undeployed service by flw location
    @Test
    public void testUndeployedServiceByFLWLocation() throws IOException, InterruptedException {
        createFlwWithLocationNoLanguageNoDeployedServices();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                false, null,             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFailureResponseJson("<MOBILE_KUNJI: Not Deployed In State>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());
    }

    // Request undeployed service by circle
    private void createFlwWithNoLocationNoLanguageNoDeployedServices() {

        rh.newDelhiDistrict();
        rh.delhiCircle();

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        frontLineWorkerService.add(flw);
    }

    // Request undeployed service by flw location
    @Test
    public void testUndeployedServiceByCircleLocation() throws IOException, InterruptedException {
        createFlwWithNoLocationNoLanguageNoDeployedServices();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, rh.delhiCircle().getName(),//circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFailureResponseJson("<MOBILE_KUNJI: Not Deployed In State>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());
    }

    // Request undeployed service by flw location
    @Test
    public void testKilkariUndeployedServiceByCircleLocation() throws IOException, InterruptedException {
        createFlwWithNoLocationNoLanguageNoDeployedServices();

        HttpGet httpGet = createHttpGet(
                true, "kilkari",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, rh.delhiCircle().getName(),//circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFailureResponseJson("<KILKARI: Not Deployed In State>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testKilkariUserRequestNoLanguage() throws IOException, InterruptedException {

        nationalDefaultLanguageDataService.create(new NationalDefaultLanguage(rh.hindiLanguage()));
        subscriberDataService.create(new Subscriber(1000000000L));

        rh.newDelhiDistrict();

        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.KILKARI));

        HttpGet httpGet = createHttpGet(
                true, "kilkari",        //service
                true, "1000000000",     //callingNumber
                true, "OP",             //operator
                true, rh.delhiCircle().getName(),//circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createKilkariUserResponseJson(
                rh.hindiLanguage().getCode(), //defaultLanguageLocationCode
                null, //locationCode
                Collections.singletonList(rh.hindiLanguage().getCode()), // allowedLanguageLocationCodes
                new HashSet<String>() //subscriptionPackList
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testKilkariNonexistentUserRequest() throws IOException, InterruptedException {
        createKilkariTestData();

        HttpGet httpGet = createHttpGet(
                true, "kilkari",        //service
                true, "9999999999",     //callingNumber
                true, "OP",             //operator
                true, rh.delhiCircle().getName(),//circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createKilkariUserResponseJson(
                rh.hindiLanguage().getCode(), //defaultLanguageLocationCode
                null, //locationCode
                Collections.singletonList(rh.hindiLanguage().getCode()), // allowedLanguageLocationCodes
                new HashSet<String>() //subscriptionPackList
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testFlwUserRequestWithoutServiceUsage() throws IOException, InterruptedException {
        createFlwCappedServiceNoUsageNoLocationNoLanguage();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(
                "99",  //defaultLanguageLocationCode
                null,  //locationCode
                Collections.singletonList("99"), // allowedLanguageLocationCodes
                0L,    //currentUsageInPulses
                0L,    //endOfUsagePromptCounter
                false, //welcomePromptFlag
                3600,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testFlwUserRequestWithServiceUsageOnly() throws IOException, InterruptedException {
        createFlwWithLanguageServiceUsageAndCappedService();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, rh.delhiCircle().getName(),//circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(
                rh.hindiLanguage().getCode(),  //defaultLanguageLocationCode
                rh.hindiLanguage().getCode(),  //locationCode
                new ArrayList<String>(), // allowedLanguageLocationCodes
                1L,    //currentUsageInPulses
                0L,    //endOfUsagePromptCounter
                false, //welcomePromptFlag
                3600,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testFlwUserRequestWithServiceUsageAndEndOfUsageAndWelcomeMsg() throws IOException, InterruptedException {
        createFlwWithLanguageFullServiceUsageAndCappedService();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, rh.delhiCircle().getName(),             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(
                rh.hindiLanguage().getCode(),  //defaultLanguageLocationCode
                rh.hindiLanguage().getCode(),  //locationCode
                new ArrayList<String>(), // allowedLanguageLocationCodes
                1L,    //currentUsageInPulses
                1L,    //endOfUsagePromptCounter
                true,  //welcomePromptFlag
                3600,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testInvalidServiceName() throws IOException, InterruptedException {
        HttpGet httpGet = createHttpGet(
                true, "INVALID!!!!",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFailureResponseJson("<serviceName: Invalid>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testNoCallingNumber() throws IOException, InterruptedException {
        HttpGet httpGet = createHttpGet(
                true, "kilkari",        //service
                false, null,            //callingNumber
                true, "OP",             //operator
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Not Present>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testInvalidCallingNumber() throws IOException, InterruptedException {
        HttpGet httpGet = createHttpGet(
                true, "kilkari",        //service
                true, "123",            //callingNumber
                true, "OP",             //operator
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Invalid>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testNoOperator() throws IOException, InterruptedException {


        createKilkariTestData();

        HttpGet httpGet = createHttpGet(
                true, "kilkari",        //service
                true, "9999999999",     //callingNumber
                false, null,             //operator
                true, rh.delhiCircle().getName(),//circle
                true, "123456789012345" //callId
        );



        String expectedJsonResponse = createKilkariUserResponseJson(
                rh.hindiLanguage().getCode(), //defaultLanguageLocationCode
                null, //locationCode
                Collections.singletonList(rh.hindiLanguage().getCode()), // allowedLanguageLocationCodes
                new HashSet<String>() //subscriptionPackList
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));

    }

    @Test
    public void testNoCircle() throws IOException, InterruptedException {
        createCircleWithLanguage();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",        //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                false, null,            //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(
                rh.hindiLanguage().getCode(),  //defaultLanguageLocationCode
                null,  //locationCode
                Arrays.asList(rh.hindiLanguage().getCode(), rh.kannadaLanguage().getCode()), // allowedLanguageLocationCodes
                0L,    //currentUsageInPulses
                0L,    //endOfUsagePromptCounter
                false, //welcomePromptFlag
                -1,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testNoCallId() throws IOException, InterruptedException {
        HttpGet httpGet = createHttpGet(
                true, "kilkari",    //service
                true, "1111111111", //callingNumber
                true, "OP",         //operator
                true, "AA",         //circle
                false, null         //callId
        );

        String expectedJsonResponse = createFailureResponseJson("<callId: Not Present>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    // An FLW that does not exist
    @Test
    public void testGetUserDetailsUnknownUser() throws IOException, InterruptedException {
        createCircleWithLanguage();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111112",     //callingNumber
                true, "OP",             //operator
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(
                rh.hindiLanguage().getCode(),  //defaultLanguageLocationCode
                null,  //locationCode
                Arrays.asList(rh.hindiLanguage().getCode(), rh.kannadaLanguage().getCode()), // allowedLanguageLocationCodes
                0L,    //currentUsageInPulses
                0L,    //endOfUsagePromptCounter
                false, //welcomePromptFlag
                -1,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }


    @Test
    @Ignore  // Currently under discussion with IMI.  My preference would be for them to handle this case
    public void testGetUserDetailsUnknownUserCircleSingleLanguage() throws IOException, InterruptedException {
        createCircleWithSingleLanguage();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111112",     //callingNumber
                true, "OP",             //operator
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(
                "99",  //defaultLanguageLocationCode
                "99",  //locationCode
                null, // allowedLanguageLocationCodes
                0L,    //currentUsageInPulses
                0L,    //endOfUsagePromptCounter
                false, //welcomePromptFlag
                -1,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));

        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(1111111112l);
        assertNotNull(flw);
        Language language = flw.getLanguage();
        assertNotNull(language);
        assertEquals("FLW Language Code", rh.hindiLanguage().getCode(), language.getCode());
    }

    @Test
    public void testGetUserDetailsUnknownUserUnknownCircle() throws IOException, InterruptedException {
        createCircleWithLanguage();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111112",     //callingNumber
                true, "OP",             //operator
                false, "",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(
                rh.hindiLanguage().getCode(),  //defaultLanguageLocationCode
                null,  //locationCode
                Arrays.asList(rh.hindiLanguage().getCode(), rh.kannadaLanguage().getCode()), // allowedLanguageLocationCodes
                0L,    //currentUsageInPulses
                0L,    //endOfUsagePromptCounter
                false, //welcomePromptFlag
                -1,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    // An FLW with usage for both MA and MK
    @Test
    public void testGetUserDetailsUserOfBothServices() throws IOException, InterruptedException {
        createFlwWithLanguageFullUsageOfBothServiceUncapped();

        HttpGet httpGet = createHttpGet(
                true, "mobileacademy",  //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, rh.delhiCircle().getName(),//circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(
                rh.hindiLanguage().getCode(),  //defaultLanguageLocationCode
                rh.hindiLanguage().getCode(),  //locationCode
                new ArrayList<String>(),
                1L,    //currentUsageInPulses
                1L,    //endOfUsagePromptCounter
                false, //welcomePromptFlag
                -1,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    // An FLW with usage and a service with a cap
    @Test
    public void testGetUserDetailsServiceCapped() throws IOException, InterruptedException {
        createFlwWithLanguageFullUsageOfBothServiceUncapped();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, rh.delhiCircle().getName(),//circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(
                rh.hindiLanguage().getCode(),  //defaultLanguageLocationCode
                rh.hindiLanguage().getCode(),  //locationCode
                new ArrayList<String>(),
                1L,    //currentUsageInPulses
                1L,    //endOfUsagePromptCounter
                true,  //welcomePromptFlag
                10,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testGetUserNotInWhitelistByState() throws IOException, InterruptedException {
        createFlwWithStateNotInWhitelist();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Not Authorized>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testGetUserNotInWhitelistByLanguageLocationCode() throws IOException, InterruptedException {
        createFlwWithLanguageLocationCodeNotInWhitelist();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Not Authorized>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testSetLanguageInvalidService() throws IOException, InterruptedException {
        HttpPost httpPost = createHttpPost("INVALID_SERVICE", new UserLanguageRequest(1111111111L, 123456789012345L,"10"));

        String expectedJsonResponse = createFailureResponseJson("<serviceName: Invalid>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testSetLanguageMissingCallingNumber() throws IOException, InterruptedException {
        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(null, 123456789012345L,"10"));

        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Not Present>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testSetLanguageInvalidCallingNumber() throws IOException, InterruptedException {
        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(123L, 123456789012345L,"10"));

        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Invalid>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testSetLanguageMissingCallId() throws IOException, InterruptedException {
        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(1111111111L, null,"10"));

        String expectedJsonResponse = createFailureResponseJson("<callId: Not Present>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testSetLanguageInvalidCallId() throws IOException, InterruptedException {
        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(1111111111L, 123L,"10"));

        String expectedJsonResponse = createFailureResponseJson("<callId: Invalid>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testSetLanguageMissingLanguageLocationCode() throws IOException, InterruptedException {
        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(1111111111L, 123456789012345L, null));

        String expectedJsonResponse = createFailureResponseJson("<languageLocationCode: Not Present>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testSetLanguageInvalidJson() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/api/mobilekunji/languageLocationCode",
                TestContext.getJettyPort()));
        StringEntity params = new StringEntity(
                "{\"callingNumber\":invalid,\"callId\":123456789012345,\"languageLocationCode\":\"10\"}");
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");

        Pattern expectedJsonPattern = Pattern.compile(".*Could not read JSON.*");
        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_BAD_REQUEST, expectedJsonPattern,
                ADMIN_USERNAME, ADMIN_PASSWORD));
    }

    @Test
    public void testSetLanguageNoFLW() throws IOException, InterruptedException {
        createCircleWithLanguage();

        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(1111111111L, 123456789012345L,
                rh.hindiLanguage().getCode()));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost));

        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(1111111111l);
        assertNotNull(flw);
        Language language = flw.getLanguage();
        assertNotNull(language);
        assertEquals("FLW Language Code", rh.hindiLanguage().getCode(), language.getCode());
    }

    @Test
    public void testSetLanguageLanguageNotFound() throws IOException, InterruptedException {
        createFlwCappedServiceNoUsageNoLocationNoLanguage();

        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(1111111111L, 123456789012345L, "77"));

        String expectedJsonResponse = createFailureResponseJson("<languageLocationCode: Not Found>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testSetLanguageValid() throws IOException, InterruptedException {
        createFlwCappedServiceNoUsageNoLocationNoLanguage();

        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(1111111111L, 123456789012345L, "99"));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK));

        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(1111111111L);
        Language language = flw.getLanguage();
        assertNotNull(language);
        assertEquals("FLW Language Code", "99", language.getCode());
    }


    /**
     * To check that Get Subscriber Details API is returning correct
     * information of subscriber subscribed to both Packs.
     */
    @Test
    public void verifyFT1() throws IOException, InterruptedException {
        createKilkariTestData();
        HttpGet httpGet = createHttpGet(
                true, "kilkari", // service
                true, "2000000000", // callingNumber- subscribed to both packs
                true, "OP", // operator
                true, rh.delhiCircle().getName(), // circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createKilkariUserResponseJson(
                rh.hindiLanguage().getCode(), // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                // subscriptionPackList
                new HashSet<>(Arrays.asList(sh.childPack().getName(), sh.pregnancyPack().getName()))
        );
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }


    /**
     * To check that Get Subscriber Details API is returning correct information of subscriber subscribed to
     * pregnancy pack.
     */
    @Test
    public void verifyFT2() throws IOException, InterruptedException {
        createKilkariTestData();
        HttpGet httpGet = createHttpGet(
                true, "kilkari", // service
                true, "3000000000", // callingNumber- subscribed to pregnancyPack
                true, "OP", // operator
                true, rh.delhiCircle().getName(), // circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createKilkariUserResponseJson(
                rh.hindiLanguage().getCode(), // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                // subscriptionPackList
                new HashSet<>(Arrays.asList(sh.pregnancyPack().getName()))
        );
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }


    private void createNationalDefaultLanguageForKilkari() {
        nationalDefaultLanguageDataService.create(new NationalDefaultLanguage(rh.hindiLanguage()));
    }


    private void createCircleWithMultipleLanguages() {

        rh.karnatakaState();
        rh.mysuruDistrict();
        rh.bangaloreDistrict();

        Circle c = rh.karnatakaCircle();
        c.setDefaultLanguage(rh.kannadaLanguage());
        circleDataService.update(c);

        deployedServiceDataService.create(new DeployedService(rh.karnatakaState(), Service.KILKARI));
    }


    @Test
    public void verifyFT3() throws IOException, InterruptedException {
        /**
         * To get the details of the Subscriber identified by the callingNumber
         * when Subscriber has called first time to subscribe and LLC for
         * identified circle is not available.
         */
        createKilkariTestData();
        createNationalDefaultLanguageForKilkari();
        HttpGet httpGet = createHttpGet(
                true, "kilkari", // service
                true, "1000000011", // callingNumber- first time caller
                true, "OP", // operator
                true, rh.delhiCircle().getName(), // circle
                true, "123456789012345" // callId
        );
        // defaultLanguageLocationCode== national default language location
        String expectedJsonResponse = createKilkariUserResponseJson(
                rh.hindiLanguage().getCode(), // defaultLanguageLocationCode
                null, // locationCode
                Arrays.asList(rh.hindiLanguage().getCode()), // allowedLanguageLocationCodes
                new HashSet<String>() // subscriptionPackList
        );
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }


    /**
     * To get the details of the Subscriber identified by the callingNumber
     * when Subscriber has called first time to subscribe and circle is not
     * available or not identified.
     */
    @Test
    public void verifyFT4() throws IOException, InterruptedException {
        createKilkariTestData();
        createNationalDefaultLanguageForKilkari();
        HttpGet httpGet = createHttpGet(
                true, "kilkari", // service
                true, "1000000012", // callingNumber- first time caller
                true, "OP", // operator
                true, "99", // Unknown circle
                true, "123456789012345" // callId
        );
        // defaultLanguageLocationCode== national default language location
        String expectedJsonResponse = createKilkariUserResponseJson(
                rh.hindiLanguage().getCode(), // defaultLanguageLocationCode
                null, // locationCode
                Arrays.asList(rh.hindiLanguage().getCode()), // allowedLanguageLocationCodes
                new HashSet<String>() // subscriptionPackList
        );
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }


    /**
     * To get the details of the Subscriber identified by the callingNumber
     * when Subscriber has called first time to subscribe and identified
     * circle has multiple states.
     */
    @Test
    public void verifyFT5() throws IOException, InterruptedException {
        createCircleWithMultipleLanguages();
        HttpGet httpGet = createHttpGet(
                true, "kilkari", // service
                true, "1000000013", // callingNumber- first time caller
                true, "OP", // operator
                true, rh.karnatakaCircle().getName(), // circle have multiple LLC
                true, "123456789012345" // callId
        );
        // defaultLanguageLocationCode== circle default language location
        String expectedJsonResponse = createKilkariUserResponseJson(
                rh.kannadaLanguage().getCode(), // defaultLanguageLocationCode
                null, // locationCode
                // allowedLanguageLocationCodes
                Arrays.asList(rh.kannadaLanguage().getCode(), rh.tamilLanguage().getCode()),
                new HashSet<String>() // subscriptionPackList
        );
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        String jsonResponse = EntityUtils.toString(response.getEntity());
        getLogger().debug("expectedJsonResponse: {}", expectedJsonResponse);
        getLogger().debug("        jsonResponse: {}", jsonResponse);
        assertEquals(expectedJsonResponse, jsonResponse);
    }



    /**
     * To verify the behavior of Get Subscriber Details API if the service is
     * not deployed in provided Subscriber's state.
     */
    @Test
    // https://applab.atlassian.net/browse/NMS-181
    public void verifyFT16() throws IOException,
            InterruptedException {
        createKilkariUndeployTestData();

                HttpGet httpGet = createHttpGet(true, "kilkari", // service
                true, "1000000000", // callingNumber
                        true, "OP", // operator
                        true, rh.delhiCircle().getName(), // circle
                        true, "123456789012345" // callId
                );

        // Should return HTTP 501 because the service is not
        // deployed for the specified state
        String expectedJsonResponse = createFailureResponseJson("<KILKARI: Not Deployed In State>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());

        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that any DEACTIVATED subscription is not returned in get
     * subscriber details.
     */
    @Test
    public void verifyFT183() throws IOException,
            InterruptedException {
        createKilkariTestData();
        // subscriber 4000000000L subscribed to both pack and Pregnancy pack is
        // deactivated
        Subscriber subscriber = subscriberDataService.create(new Subscriber(4000000000L, rh.hindiLanguage()));
        subscriptionService.createSubscription(subscriber.getCallingNumber(),
                rh.hindiLanguage(), sh.childPack(), SubscriptionOrigin.IVR);
        
        Subscription pregnancyPack = subscriptionService.createSubscription(
                subscriber.getCallingNumber(), rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.IVR);
        subscriptionService.deactivateSubscription(pregnancyPack,
                DeactivationReason.DEACTIVATED_BY_USER);
        
        Set<String> expectedPacks = new HashSet<>();
        expectedPacks.add("childPack");

        HttpGet httpGet = createHttpGet(true, "kilkari", // service
                true, "4000000000", // callingNumber
                true, "OP", // operator
                true, rh.delhiCircle().getName(), // circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createKilkariUserResponseJson(rh.hindiLanguage().getCode(), // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                expectedPacks // subscriptionPackList
        );
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * NMS_FT_184 To verify that any COMPLETED subscription is not returned in
     * get subscriber details.
     */
    @Test
    public void verifyFT184() throws IOException,
            InterruptedException {
        createKilkariTestData();
        // subscriber subscribed to both packs and Pregnancy pack is completed
        Subscriber subscriber = subscriberDataService.create(new Subscriber(5000000000L, rh.hindiLanguage()));
        
        subscriptionService.createSubscription(subscriber.getCallingNumber(),
                rh.hindiLanguage(), sh.childPack(), SubscriptionOrigin.IVR);
        
        Subscription pregnancyPack=subscriptionService.createSubscription(
                subscriber.getCallingNumber(), rh.hindiLanguage(), sh.pregnancyPack(),
                SubscriptionOrigin.IVR);
        subscriptionService.updateStartDate(pregnancyPack, DateTime.now()
                .minusDays(505 + 90));
 
        Set<String> expectedPacks = new HashSet<>();
        expectedPacks.add("childPack");

        HttpGet httpGet = createHttpGet(true, "kilkari", // service
                true, "5000000000", // callingNumber
                true, "OP", // operator
                true, rh.delhiCircle().getName(), // circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createKilkariUserResponseJson(rh.hindiLanguage().getCode(), // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                expectedPacks // subscriptionPackList
        );
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    private void setupWhiteListData() {
        rh = new RegionHelper(languageDataService, circleDataService,
                stateDataService, districtDataService, districtService);
        rh.newDelhiDistrict();
        rh.bangaloreDistrict();

        whitelistState = rh.delhiState();
        stateDataService.create(whitelistState);

        whitelistStateDataService.create(new WhitelistState(whitelistState));

        nonWhitelistState = rh.karnatakaState();
        stateDataService.create(nonWhitelistState);
    }

    /**
     * To verify Active/Inactive User should be able to access MK Service
     * content, if user's callingNumber is in whitelist and whitelist is set to
     * Enabled for user's state.
     */
    @Test
    public void verifyFT340() throws InterruptedException, IOException {
        setupWhiteListData();

        // create a FLW with whitelist number and whitelist state
        FrontLineWorker whitelistWorker = new FrontLineWorker("Test",
                WHITELIST_CONTACT_NUMBER);
        whitelistWorker.setState(whitelistState);
        whitelistWorker.setDistrict(rh.newDelhiDistrict());
        whitelistWorker.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(whitelistWorker);

        // assert user's status
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.INACTIVE,
                whitelistWorker.getStatus());

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(whitelistState,
                Service.MOBILE_KUNJI));

        // Create the whitelist number entry in whitelist table
        WhitelistEntry entry = new WhitelistEntry(WHITELIST_CONTACT_NUMBER,
                whitelistState);
        whitelistEntryDataService.create(entry);

        // Check the response
        HttpGet request = createHttpGet(true, "mobilekunji",
                true, String.valueOf(WHITELIST_CONTACT_NUMBER), true, "A",
                true, rh.delhiCircle().getName(), true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());

        // Update user's status to active
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        whitelistWorker.setStatus(FrontLineWorkerStatus.ACTIVE);
        frontLineWorkerService.update(whitelistWorker);

        // assert user's status
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.ACTIVE, whitelistWorker.getStatus());

        // Check the response
        request = createHttpGet(true, "mobilekunji", true,
                String.valueOf(WHITELIST_CONTACT_NUMBER), true, "A", true, rh
                        .delhiCircle().getName(), true, "123456789012345");
        httpResponse = SimpleHttpClient.httpRequestAndResponse(request,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());
    }

    /**
     * To verify Active/Inactive User shouldn't be able to access MK Service
     * content, if user's callingNumber is not in whitelist and whitelist is set
     * to Enabled for user's state.
     */
    @Test
    public void verifyFT342() throws InterruptedException, IOException {
        setupWhiteListData();

        // create a FLW with non-whitelist number and whitelist state
        FrontLineWorker notWhitelistWorker = new FrontLineWorker("Test",
                NOT_WHITELIST_CONTACT_NUMBER);
        notWhitelistWorker.setState(whitelistState);
        notWhitelistWorker.setDistrict(rh.newDelhiDistrict());
        notWhitelistWorker.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(notWhitelistWorker);

        // assert user's status
        notWhitelistWorker = frontLineWorkerService
                .getByContactNumber(NOT_WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.INACTIVE,
                notWhitelistWorker.getStatus());

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(whitelistState,
                Service.MOBILE_KUNJI));

        // Check the response
        HttpGet request = createHttpGet(true, "mobilekunji",
                true, String.valueOf(NOT_WHITELIST_CONTACT_NUMBER), true, "A",
                true, rh.delhiCircle().getName(), true, "123456789012345");

        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_FORBIDDEN, httpResponse.getStatusLine()
                .getStatusCode());

        // Update user's status
        notWhitelistWorker = frontLineWorkerService
                .getByContactNumber(NOT_WHITELIST_CONTACT_NUMBER);
        notWhitelistWorker.setStatus(FrontLineWorkerStatus.ACTIVE);
        frontLineWorkerService.update(notWhitelistWorker);

        // assert user's status
        notWhitelistWorker = frontLineWorkerService
                .getByContactNumber(NOT_WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.ACTIVE,
                notWhitelistWorker.getStatus());

        // Check the response
        request = createHttpGet(true, "mobilekunji", true,
                String.valueOf(NOT_WHITELIST_CONTACT_NUMBER), true, "A", true,
                rh.delhiCircle().getName(), true, "123456789012345");
        httpResponse = SimpleHttpClient.httpRequestAndResponse(request,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_FORBIDDEN, httpResponse.getStatusLine()
                .getStatusCode());
    }

    /**
     * To verify Active/Inactive User should be able to access MK Service
     * content, if user's callingNumber is in whitelist and whitelist is set to
     * disabled for user's state.
     */
    @Test
    public void verifyFT344() throws InterruptedException, IOException {
        setupWhiteListData();
        // user's number in whitelist, but state not whitelisted
        FrontLineWorker whitelistWorker = new FrontLineWorker("Test",
                WHITELIST_CONTACT_NUMBER);
        whitelistWorker.setDistrict(rh.bangaloreDistrict());
        whitelistWorker.setLanguage(rh.kannadaLanguage());
        whitelistWorker.setState(nonWhitelistState);
        frontLineWorkerService.add(whitelistWorker);

        // assert user's status
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.INACTIVE,
                whitelistWorker.getStatus());

        // create user's number in whitelist entry table
        whitelistEntryDataService.create(new WhitelistEntry(
                WHITELIST_CONTACT_NUMBER, nonWhitelistState));

        // service deployed in user's state
        deployedServiceDataService.create(new DeployedService(
                nonWhitelistState, Service.MOBILE_KUNJI));

        // Check the response
        HttpGet request = createHttpGet(true, "mobilekunji",
                true, String.valueOf(WHITELIST_CONTACT_NUMBER), true, "A",
                true, rh.karnatakaCircle().getName(), true, "123456789012345");

        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());

        // Update user's status
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        whitelistWorker.setStatus(FrontLineWorkerStatus.ACTIVE);
        frontLineWorkerService.update(whitelistWorker);

        // assert user's status
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.ACTIVE, whitelistWorker.getStatus());

        // Check the response
        request = createHttpGet(true, "mobilekunji", true,
                String.valueOf(WHITELIST_CONTACT_NUMBER), true, "A", true, rh
                        .karnatakaCircle().getName(), true, "123456789012345");

        httpResponse = SimpleHttpClient.httpRequestAndResponse(request,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());
    }

    /**
     * To verify Active User should be able to access MA Service content, if
     * user's callingNumber is in whitelist and whitelist is set to Enabled for
     * user's state.
     */
    @Test
    public void verifyFT439() throws InterruptedException, IOException {
        setupWhiteListData();

        // create a FLW with whitelist number and whitelist state
        FrontLineWorker whitelistWorker = new FrontLineWorker("Test",
                WHITELIST_CONTACT_NUMBER);
        whitelistWorker.setState(whitelistState);
        whitelistWorker.setDistrict(rh.newDelhiDistrict());
        whitelistWorker.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(whitelistWorker);

        // Update user's status to active
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        whitelistWorker.setStatus(FrontLineWorkerStatus.ACTIVE);
        frontLineWorkerService.update(whitelistWorker);

        // assert user's status
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.ACTIVE, whitelistWorker.getStatus());

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(whitelistState,
                Service.MOBILE_ACADEMY));

        // Create the whitelist number entry in whitelist table
        WhitelistEntry entry = new WhitelistEntry(WHITELIST_CONTACT_NUMBER,
                whitelistState);
        whitelistEntryDataService.create(entry);

        // Check the response
        HttpGet request = createHttpGet(true, "mobileacademy", true,
                String.valueOf(WHITELIST_CONTACT_NUMBER), true, "A", true, rh
                        .delhiCircle().getName(), true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());
    }

    /**
     * To verify Inactive User, should be able to access MA Service content, if
     * user's callingNumber is in whitelist and whitelist is set to Enabled for
     * user's state.
     */
    @Test
    public void verifyFT440() throws InterruptedException, IOException {
        setupWhiteListData();

        // create a FLW with whitelist number and whitelist state
        FrontLineWorker whitelistWorker = new FrontLineWorker("Test",
                WHITELIST_CONTACT_NUMBER);
        whitelistWorker.setStatus(FrontLineWorkerStatus.INACTIVE);
        whitelistWorker.setState(whitelistState);
        whitelistWorker.setDistrict(rh.newDelhiDistrict());
        whitelistWorker.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(whitelistWorker);

        // assert user's status
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.INACTIVE,
                whitelistWorker.getStatus());

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(whitelistState,
                Service.MOBILE_ACADEMY));

        // Create the whitelist number entry in whitelist table
        WhitelistEntry entry = new WhitelistEntry(WHITELIST_CONTACT_NUMBER,
                whitelistState);
        whitelistEntryDataService.create(entry);

        // Check the response
        HttpGet getRequest = createHttpGet(true, "mobileacademy", true,
                String.valueOf(WHITELIST_CONTACT_NUMBER), true, "A", true, rh
                        .delhiCircle().getName(), true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                getRequest, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());
    }

    /**
     * To verify Active User shouldn't be able to access MA Service content, if
     * user's callingNumber is not is whitelist and whitelist is set to Enabled
     * for user's state.
     */
    @Test
    public void verifyFT443() throws InterruptedException, IOException {
        setupWhiteListData();

        // create a FLW with non-whitelist number and whitelist state
        FrontLineWorker notWhitelistWorker = new FrontLineWorker("Test",
                NOT_WHITELIST_CONTACT_NUMBER);
        notWhitelistWorker.setState(whitelistState);
        notWhitelistWorker.setDistrict(rh.newDelhiDistrict());
        notWhitelistWorker.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(notWhitelistWorker);

        // Update user's status
        notWhitelistWorker = frontLineWorkerService
                .getByContactNumber(NOT_WHITELIST_CONTACT_NUMBER);
        notWhitelistWorker.setStatus(FrontLineWorkerStatus.ACTIVE);
        frontLineWorkerService.update(notWhitelistWorker);

        // assert user's status
        notWhitelistWorker = frontLineWorkerService
                .getByContactNumber(NOT_WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.ACTIVE,
                notWhitelistWorker.getStatus());

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(whitelistState,
                Service.MOBILE_ACADEMY));

        // Check the response
        HttpGet request = createHttpGet(true, "mobileacademy", true,
                String.valueOf(NOT_WHITELIST_CONTACT_NUMBER), true, "A", true,
                rh.delhiCircle().getName(), true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_FORBIDDEN, httpResponse.getStatusLine()
                .getStatusCode());
    }

    /**
     * To verify Inactive User shouldn't be able to access MA Service content,
     * if user's callingNumber is not in whitelist and whitelist is set to
     * Enabled for user's state.
     */
    @Test
    public void verifyFT444() throws InterruptedException, IOException {
        setupWhiteListData();

        // create a FLW with non-whitelist number and whitelist state
        FrontLineWorker notWhitelistWorker = new FrontLineWorker("Test",
                NOT_WHITELIST_CONTACT_NUMBER);
        notWhitelistWorker.setState(whitelistState);
        notWhitelistWorker.setDistrict(rh.newDelhiDistrict());
        notWhitelistWorker.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(notWhitelistWorker);

        // assert user's status
        notWhitelistWorker = frontLineWorkerService
                .getByContactNumber(NOT_WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.INACTIVE,
                notWhitelistWorker.getStatus());

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(whitelistState,
                Service.MOBILE_ACADEMY));

        // Check the response
        HttpGet request = createHttpGet(true, "mobileacademy", true,
                String.valueOf(NOT_WHITELIST_CONTACT_NUMBER), true, "A", true,
                rh.delhiCircle().getName(), true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_FORBIDDEN, httpResponse.getStatusLine()
                .getStatusCode());
    }

    /**
     * To verify Active User should be able to access MA Service content, if
     * user's callingNumber is in whitelist and whitelist is set to disabled for
     * user's state.
     */
    @Test
    public void verifyFT447() throws InterruptedException, IOException {
        setupWhiteListData();
        // user's no in whitelist, but state not whitelisted
        FrontLineWorker whitelistWorker = new FrontLineWorker("Test",
                WHITELIST_CONTACT_NUMBER);
        whitelistWorker.setDistrict(rh.bangaloreDistrict());
        whitelistWorker.setLanguage(rh.kannadaLanguage());
        whitelistWorker.setState(nonWhitelistState);
        frontLineWorkerService.add(whitelistWorker);

        // Update user's status to active
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        whitelistWorker.setStatus(FrontLineWorkerStatus.ACTIVE);
        frontLineWorkerService.update(whitelistWorker);

        // assert user's status
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.ACTIVE, whitelistWorker.getStatus());

        // create user's number in whitelist entry table
        whitelistEntryDataService.create(new WhitelistEntry(
                WHITELIST_CONTACT_NUMBER, nonWhitelistState));

        // service deployed in user's state
        deployedServiceDataService.create(new DeployedService(
                nonWhitelistState, Service.MOBILE_ACADEMY));

        // Check the response
        HttpGet request = createHttpGet(true, "mobileacademy", true,
                String.valueOf(WHITELIST_CONTACT_NUMBER), true, "A", true, rh
                        .karnatakaCircle().getName(), true, "123456789012345");

        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());
    }

    /**
     * To verify Inactive User should be able to access MA Service content, if
     * user's callingNumber is in whitelist and whitelist is set to disabled for
     * user's state.
     */
    @Test
    public void verifyFT448() throws InterruptedException, IOException {
        setupWhiteListData();
        // user's no in whitelist, but state not whitelisted
        FrontLineWorker whitelistWorker = new FrontLineWorker("Test",
                WHITELIST_CONTACT_NUMBER);
        whitelistWorker.setDistrict(rh.bangaloreDistrict());
        whitelistWorker.setLanguage(rh.kannadaLanguage());
        whitelistWorker.setState(nonWhitelistState);
        frontLineWorkerService.add(whitelistWorker);

        // assert user's status
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.INACTIVE,
                whitelistWorker.getStatus());

        // create user's number in whitelist entry table
        whitelistEntryDataService.create(new WhitelistEntry(
                WHITELIST_CONTACT_NUMBER, nonWhitelistState));

        // service deployed in user's state
        deployedServiceDataService.create(new DeployedService(
                nonWhitelistState, Service.MOBILE_ACADEMY));

        // Check the response
        HttpGet request = createHttpGet(true, "mobileacademy", true,
                String.valueOf(WHITELIST_CONTACT_NUMBER), true, "A", true, rh
                        .karnatakaCircle().getName(), true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());
    }

    /**
     * This case has been added to test the functionality mentioned in
     * NMS.GEN.FLW.008.
     * 
     * The NMS system shall provide means to mark an FLW as invalid using CSV
     * upload. Once an FLW is marked invalid, any incoming call with MSISDN that
     * is same as that of invalid FLW shall be treated as that of an anonymous
     * caller.
     */
    // TODO JIRA issue: https://applab.atlassian.net/browse/NMS-236
    @Ignore
    @Test
    public void verifyStatusChangeFromInvalidToAnonymous()
            throws InterruptedException, IOException {
        setupWhiteListData();
        // user's no in whitelist, but state not whitelisted
        FrontLineWorker whitelistWorker = new FrontLineWorker("Test",
                WHITELIST_CONTACT_NUMBER);
        whitelistWorker.setDistrict(rh.newDelhiDistrict());
        whitelistWorker.setLanguage(rh.hindiLanguage());
        whitelistWorker.setState(whitelistState);
        frontLineWorkerService.add(whitelistWorker);

        // assert user's status
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.INACTIVE,
                whitelistWorker.getStatus());

        // Update user's status to active
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        whitelistWorker.setStatus(FrontLineWorkerStatus.INVALID);
        frontLineWorkerService.update(whitelistWorker);

        // assert user's status
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.INVALID, whitelistWorker.getStatus());

        // create user's number in whitelist entry table
        whitelistEntryDataService.create(new WhitelistEntry(
                WHITELIST_CONTACT_NUMBER, whitelistState));

        // service deployed in user's state
        deployedServiceDataService.create(new DeployedService(whitelistState,
                Service.MOBILE_ACADEMY));

        // Check the response
        HttpGet request = createHttpGet(true, "mobileacademy", true,
                String.valueOf(WHITELIST_CONTACT_NUMBER), true, "A", true, rh
                        .delhiCircle().getName(), true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());

        // assert user's status, On issuing the getUserDetails API to a invalid
        // user, its status should get changed to anonymous
        whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.ANONYMOUS,
                whitelistWorker.getStatus());
    }
}
