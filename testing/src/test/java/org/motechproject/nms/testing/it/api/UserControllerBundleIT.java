package org.motechproject.nms.testing.it.api;

import com.google.common.collect.Sets;
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
import org.motechproject.nms.flw.domain.CallDetailRecord;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.domain.ServiceUsageCap;
import org.motechproject.nms.flw.domain.WhitelistEntry;
import org.motechproject.nms.flw.domain.WhitelistState;
import org.motechproject.nms.flw.repository.CallDetailRecordDataService;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.repository.WhitelistEntryDataService;
import org.motechproject.nms.flw.repository.WhitelistStateDataService;
import org.motechproject.nms.flw.service.CallDetailRecordService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.service.ServiceUsageService;
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
import org.motechproject.nms.region.service.LanguageService;
import org.motechproject.nms.testing.it.api.utils.RequestBuilder;
import org.motechproject.nms.testing.it.utils.ApiRequestHelper;
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

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


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
    CallDetailRecordDataService callDetailRecordDataService;
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
    LanguageService languageService;

    @Inject
    FrontLineWorkerDataService frontLineWorkerDataService;

    @Inject
    WhitelistService whitelistService;

    @Inject
    ServiceUsageService serviceUsageService;

    @Inject
    CallDetailRecordService callDetailRecordService;

    public static final Long WHITELIST_CONTACT_NUMBER = 1111111111l;
    public static final Long NOT_WHITELIST_CONTACT_NUMBER = 9000000000l;

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
        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(1111111111l);
        cdr.setService(Service.MOBILE_KUNJI);
        cdr.setCallDurationInPulses(1);
        cdr.setEndOfUsagePromptCounter(0);
        cdr.setWelcomePrompt(false);
        cdr.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(cdr);
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

        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(1111111111l);
        cdr.setService(Service.MOBILE_KUNJI);
        cdr.setCallDurationInPulses(1);
        cdr.setEndOfUsagePromptCounter(1);
        cdr.setWelcomePrompt(true);
        cdr.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(cdr);
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

        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(1111111111l);
        cdr.setService(Service.MOBILE_KUNJI);
        cdr.setCallDurationInPulses(1);
        cdr.setEndOfUsagePromptCounter(1);
        cdr.setWelcomePrompt(true);
        cdr.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(cdr);

        // Academy doesn't have a welcome prompt
        cdr = new CallDetailRecord();
        cdr.setCallingNumber(1111111111l);
        cdr.setFrontLineWorker(flw);
        cdr.setService(Service.MOBILE_ACADEMY);
        cdr.setCallDurationInPulses(1);
        cdr.setEndOfUsagePromptCounter(1);
        cdr.setWelcomePrompt(false);
        cdr.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(cdr);

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

    private KilkariUserResponse createKilkariUserResponse(String defaultLanguageLocationCode, String locationCode,
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
            kilkariUserResponse.setAllowedLanguageLocationCodes(Sets.newTreeSet(allowedLanguageLocations));
        }
        if (subscriptionPackList != null) {
            kilkariUserResponse.setSubscriptionPackList(subscriptionPackList);
        }

        return kilkariUserResponse;
    }

    private String createKilkariUserResponseJson(String defaultLanguageLocationCode, String locationCode,
                                                 List<String> allowedLanguageLocations,
                                                 Set<String> subscriptionPackList) throws IOException {
        KilkariUserResponse kilkariUserResponse;
        kilkariUserResponse = createKilkariUserResponse(defaultLanguageLocationCode, locationCode,
                allowedLanguageLocations, subscriptionPackList);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(kilkariUserResponse);
    }

    private FlwUserResponse createFlwUserResponse(String defaultLanguageLocationCode, String locationCode,
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
            userResponse.setAllowedLanguageLocationCodes(Sets.newTreeSet(allowedLanguageLocations));
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

        return userResponse;
    }

    private String createFlwUserResponseJson(String defaultLanguageLocationCode, String locationCode,
                                             List<String> allowedLanguageLocations,
                                             Long currentUsageInPulses, Long endOfUsagePromptCounter,
                                             Boolean welcomePromptFlag, Integer maxAllowedUsageInPulses,
                                             Integer maxAllowedEndOfUsagePrompt) throws IOException {
        FlwUserResponse userResponse = createFlwUserResponse(defaultLanguageLocationCode, locationCode,
                                                             allowedLanguageLocations, currentUsageInPulses,
                endOfUsagePromptCounter, welcomePromptFlag, maxAllowedUsageInPulses, maxAllowedEndOfUsagePrompt);

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
        HttpPost httpPost = createHttpPost("INVALID_SERVICE", new UserLanguageRequest(1111111111L, 123456789012345L, "10"));

        String expectedJsonResponse = createFailureResponseJson("<serviceName: Invalid>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testSetLanguageMissingCallingNumber() throws IOException, InterruptedException {
        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(null, 123456789012345L, "10"));

        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Not Present>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testSetLanguageInvalidCallingNumber() throws IOException, InterruptedException {
        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(123L, 123456789012345L, "10"));

        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Invalid>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testSetLanguageMissingCallId() throws IOException, InterruptedException {
        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(1111111111L, null, "10"));

        String expectedJsonResponse = createFailureResponseJson("<callId: Not Present>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testSetLanguageInvalidCallId() throws IOException, InterruptedException {
        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(1111111111L, 123L, "10"));

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
                new HashSet<>(Collections.singletonList(sh.pregnancyPack().getName()))
        );
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }


    private void createNationalDefaultLanguageForKilkari() {
        nationalDefaultLanguageDataService.create(new NationalDefaultLanguage(rh.hindiLanguage()));
    }


    private void createCircleWithMultipleLanguages() {

        rh.bangaloreDistrict();
        rh.karnatakaState();
        rh.mysuruDistrict();

        Circle c = rh.karnatakaCircle();
        c.setDefaultLanguage(rh.kannadaLanguage());
        circleDataService.update(c);

        deployedServiceDataService.create(new DeployedService(rh.karnatakaState(), Service.KILKARI));
        deployedServiceDataService.create(new DeployedService(rh.karnatakaState(), Service.MOBILE_ACADEMY));
        deployedServiceDataService.create(new DeployedService(rh.karnatakaState(), Service.MOBILE_KUNJI));
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
                Collections.singletonList(rh.hindiLanguage().getCode()), // allowedLanguageLocationCodes
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
                Collections.singletonList(rh.hindiLanguage().getCode()), // allowedLanguageLocationCodes
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
        KilkariUserResponse expectedResponse = createKilkariUserResponse(
                rh.kannadaLanguage().getCode(), // defaultLanguageLocationCode
                null, // locationCode
                // allowedLanguageLocationCodes
                Arrays.asList(rh.kannadaLanguage().getCode(), rh.tamilLanguage().getCode()),
                new HashSet<String>() // subscriptionPackList
        );
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        ObjectMapper mapper = new ObjectMapper();
        KilkariUserResponse actual = mapper.readValue(EntityUtils.toString(response.getEntity()), KilkariUserResponse.class);
        assertEquals(expectedResponse, actual);
    }



    /**
     * To verify the behavior of Get Subscriber Details API if the service is
     * not deployed in provided Subscriber's state.
     */
    @Test
    // https://applab.atlassian.net/browse/NMS-181
    public void verifyFT16() throws IOException, InterruptedException {
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
     * Verifies that a circle with multiple states (mix of deployed & undeployed) with no mcts data for
     * subscriber always returns true
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testCircleMixedDeployedMutipleStates() throws IOException, InterruptedException {

        // create non-whitelist state
        State nws = new State();
        nws.setName("Non-whitelist state in delhi");
        nws.setCode(7L);
        stateDataService.create(nws);

        // create whitelist state
        State ws = new State();
        ws.setName("Whitelist state in delhi");
        ws.setCode(8L);
        stateDataService.create(ws);

        // create and update circle
        Circle delhiCircle = circleDataService.create(new Circle("DE"));
        delhiCircle.setStates(new ArrayList<>(Arrays.asList(nws, ws)));
        circleDataService.update(delhiCircle);

        // update deployment
        deployedServiceDataService.create(new DeployedService(ws, Service.KILKARI));

        // create subscriber
        Subscriber subscriber1 = subscriberDataService.create(new Subscriber(1000000000L, rh.hindiLanguage()));
        subscriptionService.createSubscription(subscriber1.getCallingNumber(), rh.hindiLanguage(), delhiCircle,
                sh.childPack(), SubscriptionOrigin.IVR);

        HttpGet httpGet = createHttpGet(true, "kilkari", // service
                true, "1000000000", // callingNumber
                true, "OP", // operator
                true, delhiCircle.getName(), // circle
                true, "123456789012345" // callId
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }

    /**
     * Verifies that a circle with multiple states (all undeployed) with no mcts data for
     * subscriber always returns false
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testUndeployedCircleMutipleStates() throws IOException, InterruptedException {

        // create non-whitelist state
        State nws = new State();
        nws.setName("Non-whitelist state in delhi");
        nws.setCode(7L);
        stateDataService.create(nws);

        // create whitelist state
        State ws = new State();
        ws.setName("Whitelist state in delhi");
        ws.setCode(8L);
        stateDataService.create(ws);

        // create and update circle
        Circle delhiCircle = circleDataService.create(new Circle("DE"));
        delhiCircle.setStates(new ArrayList<>(Arrays.asList(nws, ws)));
        circleDataService.update(delhiCircle);

        // no deployment whitelist created

        // create subscriber
        Subscriber subscriber1 = subscriberDataService.create(new Subscriber(1000000000L, rh.hindiLanguage()));
        subscriptionService.createSubscription(subscriber1.getCallingNumber(), rh.hindiLanguage(), delhiCircle,
                sh.childPack(), SubscriptionOrigin.IVR);

        HttpGet httpGet = createHttpGet(true, "kilkari", // service
                true, "1000000000", // callingNumber
                true, "OP", // operator
                true, delhiCircle.getName(), // circle
                true, "123456789012345" // callId
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());
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
     * To verify anonymous User belongs to a circle that has multiple states,
     * should be able to access MK Service content, if user's callingNumber is
     * in whitelist and whitelist is set to Enabled for user's state
     */
    @Test
    public void verifyFT341() throws InterruptedException, IOException {
        setupWhiteListData();

        // Delhi circle has a state already, add one more
        Circle delhiCircle = rh.delhiCircle();
        State s = new State();
        s.setName("New State in delhi");
        s.setCode(7L);
        stateDataService.create(s);
        delhiCircle.getStates().add(s);
        circleDataService.update(delhiCircle);

        // Create the whitelist number entry in whitelist table
        WhitelistEntry entry = new WhitelistEntry(WHITELIST_CONTACT_NUMBER,
                whitelistState);
        whitelistEntryDataService.create(entry);

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(whitelistState,
                Service.MOBILE_KUNJI));

        // Check the response
        HttpGet request = createHttpGet(true, "mobilekunji", true,
                String.valueOf(WHITELIST_CONTACT_NUMBER), false, "", false, "",
                true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());

        Set<State> states = languageService.getAllStatesForLanguage(rh
                .hindiLanguage());
        assertEquals(1, states.size());

        // create set Language location code request and check the response
        HttpPost postRequest = createHttpPost("mobilekunji",
                new UserLanguageRequest(WHITELIST_CONTACT_NUMBER,
                        123456789012345l, rh.hindiLanguage().getCode()));
        httpResponse = SimpleHttpClient.httpRequestAndResponse(postRequest,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());

        // assert user's status
        FrontLineWorker whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.ANONYMOUS,
                whitelistWorker.getStatus());
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
     * To verify anonymous User belongs to a circle that has multiple states,
     * shouldn't be able to access MK Service content, if user's callingNumber
     * is not in whitelist and whitelist is set to Enabled for user's state.
     */
    @Test
    public void verifyFT343() throws InterruptedException, IOException {
        setupWhiteListData();

        // Delhi circle has a state already, add one more
        Circle delhiCircle = rh.delhiCircle();
        State s = new State();
        s.setName("New State in delhi");
        s.setCode(7L);
        stateDataService.create(s);
        delhiCircle.getStates().add(s);
        circleDataService.update(delhiCircle);

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(whitelistState,
                Service.MOBILE_KUNJI));

        // Check the response
        HttpGet request = createHttpGet(true, "mobilekunji", true,
                String.valueOf(NOT_WHITELIST_CONTACT_NUMBER), false, "", false,
                "", true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());

        Set<State> states = languageService.getAllStatesForLanguage(rh
                .hindiLanguage());
        assertEquals(1, states.size());

        // create set Language location code request and check the response
        HttpPost postRequest = createHttpPost("mobilekunji",
                new UserLanguageRequest(NOT_WHITELIST_CONTACT_NUMBER,
                        123456789012345l, rh.hindiLanguage().getCode()));
        httpResponse = SimpleHttpClient.httpRequestAndResponse(postRequest,
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
     * To verify anonymous User belongs to a circle that has multiple states,
     * should be able to access MK Service content, if user's callingNumber is
     * in whitelist and whitelist is set to disabled for user's state.
     */
    @Test
    public void verifyFT345() throws InterruptedException, IOException {
        setupWhiteListData();

        // karnataka circle has a state already, add one more

        Circle karnatakaCircle = rh.karnatakaCircle();
        State s = new State();
        s.setName("New State in Karnataka");
        s.setCode(7L);
        stateDataService.create(s);
        karnatakaCircle.getStates().add(s);
        circleDataService.update(karnatakaCircle);

        // Create the whitelist number entry in whitelist table
        WhitelistEntry entry = new WhitelistEntry(WHITELIST_CONTACT_NUMBER,
                whitelistState);
        whitelistEntryDataService.create(entry);

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(
                nonWhitelistState, Service.MOBILE_KUNJI));

        // Check the response
        HttpGet request = createHttpGet(true, "mobilekunji", true,
                String.valueOf(WHITELIST_CONTACT_NUMBER), false, "", false, "",
                true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());

        Set<State> states = languageService.getAllStatesForLanguage(rh
                .tamilLanguage());
        assertEquals(1, states.size());

        // create set Language location code request and check the response
        HttpPost postRequest = createHttpPost("mobilekunji",
                new UserLanguageRequest(WHITELIST_CONTACT_NUMBER,
                        123456789012345l, rh.tamilLanguage().getCode()));
        httpResponse = SimpleHttpClient.httpRequestAndResponse(postRequest,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());

        // assert user's status
        FrontLineWorker whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.ANONYMOUS,
                whitelistWorker.getStatus());
    }

    /**
     * To verify anonymous User belongs to a circle that has single state,
     * should be able to access MK Service content, if user's callingNumber is
     * in whitelist and whitelist is set to Enabled for user's state.
     */
    @Test
    public void verifyFT346() throws InterruptedException, IOException {
        setupWhiteListData();

        Circle delhiCircle = circleDataService.findByName(rh.delhiCircle()
                .getName());
        assertNotNull(delhiCircle);
        assertNotNull(delhiCircle.getStates());
        assertEquals(delhiCircle.getStates().size(), 1);

        // Create the whitelist number entry in whitelist table
        WhitelistEntry entry = new WhitelistEntry(WHITELIST_CONTACT_NUMBER,
                whitelistState);
        whitelistEntryDataService.create(entry);

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(whitelistState,
                Service.MOBILE_KUNJI));

        // Check the response
        HttpGet request = createHttpGet(true, "mobilekunji", true,
                String.valueOf(WHITELIST_CONTACT_NUMBER), false, "", true, rh
                        .delhiCircle().getName(), true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());
    }

    /**
     * To verify anonymous User belongs to a circle that has single state,
     * shouldn't be able to access MK Service content, if user's callingNumber
     * is not in whitelist and whitelist is set to Enabled for user's state.
     */
    @Test
    public void verifyFT347() throws InterruptedException, IOException {
        setupWhiteListData();

        Circle delhiCircle = circleDataService.findByName(rh.delhiCircle()
                .getName());
        assertNotNull(delhiCircle);
        assertNotNull(delhiCircle.getStates());
        assertEquals(delhiCircle.getStates().size(), 1);

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(whitelistState,
                Service.MOBILE_KUNJI));

        // Check the response
        HttpGet request = createHttpGet(true, "mobilekunji", true,
                String.valueOf(NOT_WHITELIST_CONTACT_NUMBER), false, "", true,
                rh.delhiCircle().getName(), true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());

        Set<State> states = languageService.getAllStatesForLanguage(rh
                .hindiLanguage());
        assertEquals(1, states.size());

        // create set Language location code request and check the response
        HttpPost postRequest = createHttpPost("mobilekunji",
                new UserLanguageRequest(NOT_WHITELIST_CONTACT_NUMBER,
                        123456789012345l, rh.hindiLanguage().getCode()));
        httpResponse = SimpleHttpClient.httpRequestAndResponse(postRequest,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_FORBIDDEN, httpResponse.getStatusLine()
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
     * To verify anonymous User belongs to a circle that has multiple states,
     * should be able to access MA Service content, if user's callingNumber is
     * in whitelist and whitelist is set to Enabled for user's state.
     */
    @Test
    public void verifyFT441() throws InterruptedException, IOException {
        setupWhiteListData();

        // Delhi circle has a state already, add one more
        Circle delhiCircle = rh.delhiCircle();
        State s = new State();
        s.setName("New State in delhi");
        s.setCode(7L);
        stateDataService.create(s);
        delhiCircle.getStates().add(s);
        circleDataService.update(delhiCircle);

        // Create the whitelist number entry in whitelist table
        WhitelistEntry entry = new WhitelistEntry(WHITELIST_CONTACT_NUMBER,
                whitelistState);
        whitelistEntryDataService.create(entry);

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(whitelistState,
                Service.MOBILE_ACADEMY));

        // Check the response
        HttpGet request = createHttpGet(true, "mobileacademy", true,
                String.valueOf(WHITELIST_CONTACT_NUMBER), false, "", false, "",
                true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());

        Set<State> states = languageService.getAllStatesForLanguage(rh
                .hindiLanguage());
        assertEquals(1, states.size());

        // create set Language location code request and check the response
        HttpPost postRequest = createHttpPost("mobileacademy",
                new UserLanguageRequest(WHITELIST_CONTACT_NUMBER,
                        123456789012345l, rh.hindiLanguage().getCode()));
        httpResponse = SimpleHttpClient.httpRequestAndResponse(postRequest,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());

        // assert user's status
        FrontLineWorker whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.ANONYMOUS,
                whitelistWorker.getStatus());
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
     * To verify anonymous User belongs to a circle that has multiple states,
     * shouldn't be able to access MA Service content, if user's callingNumber
     * is not in whitelist and whitelist is set to Enabled for user's state.
     */
    @Test
    public void verifyFT445() throws InterruptedException, IOException {
        setupWhiteListData();

        // Delhi circle has a state already, add one more
        Circle delhiCircle = rh.delhiCircle();
        State s = new State();
        s.setName("New State in delhi");
        s.setCode(7L);
        stateDataService.create(s);
        delhiCircle.getStates().add(s);
        circleDataService.update(delhiCircle);

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(whitelistState,
                Service.MOBILE_ACADEMY));

        // Check the response
        HttpGet request = createHttpGet(true, "mobileacademy", true,
                String.valueOf(NOT_WHITELIST_CONTACT_NUMBER), false, "", false,
                "",
                true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());

        Set<State> states = languageService.getAllStatesForLanguage(rh
                .hindiLanguage());
        assertEquals(1, states.size());

        // create set Language location code request and check the response
        HttpPost postRequest = createHttpPost("mobileacademy",
                new UserLanguageRequest(NOT_WHITELIST_CONTACT_NUMBER,
                        123456789012345l, rh.hindiLanguage().getCode()));
        httpResponse = SimpleHttpClient.httpRequestAndResponse(postRequest,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
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
     * To verify anonymous User belongs to a circle that has multiple states,
     * should be able to access MA Service content, if user's callingNumber is
     * in whitelist and whitelist is set to disabled for user's state.
     */
    @Test
    public void verifyFT449() throws InterruptedException, IOException {
        setupWhiteListData();

        // karnataka circle has a state already, add one more

        Circle karnatakaCircle = rh.karnatakaCircle();
        State s = new State();
        s.setName("New State in Karnataka");
        s.setCode(7L);
        stateDataService.create(s);
        karnatakaCircle.getStates().add(s);
        circleDataService.update(karnatakaCircle);

        // Create the whitelist number entry in whitelist table
        WhitelistEntry entry = new WhitelistEntry(WHITELIST_CONTACT_NUMBER,
                whitelistState);
        whitelistEntryDataService.create(entry);

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(
                nonWhitelistState, Service.MOBILE_ACADEMY));

        // Check the response
        HttpGet request = createHttpGet(true, "mobileacademy", true,
                String.valueOf(WHITELIST_CONTACT_NUMBER), false, "", false, "",
                true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());

        Set<State> states = languageService.getAllStatesForLanguage(rh
                .tamilLanguage());
        assertEquals(1, states.size());

        // create set Language location code request and check the response
        HttpPost postRequest = createHttpPost("mobileacademy",
                new UserLanguageRequest(WHITELIST_CONTACT_NUMBER,
                        123456789012345l, rh.tamilLanguage().getCode()));
        httpResponse = SimpleHttpClient.httpRequestAndResponse(postRequest,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());

        // assert user's status
        FrontLineWorker whitelistWorker = frontLineWorkerService
                .getByContactNumber(WHITELIST_CONTACT_NUMBER);
        assertEquals(FrontLineWorkerStatus.ANONYMOUS,
                whitelistWorker.getStatus());
    }

    /**
     * To verify anonymous User belongs to a circle that has single state,
     * should be able to access MA Service content, if user's callingNumber is
     * in whitelist and whitelist is set to Enabled for user's state.
     */
    @Test
    public void verifyFT451() throws InterruptedException, IOException {
        setupWhiteListData();

        Circle delhiCircle = circleDataService.findByName(rh.delhiCircle()
                .getName());
        assertNotNull(delhiCircle);
        assertNotNull(delhiCircle.getStates());
        assertEquals(delhiCircle.getStates().size(), 1);

        // Create the whitelist number entry in whitelist table
        WhitelistEntry entry = new WhitelistEntry(WHITELIST_CONTACT_NUMBER,
                whitelistState);
        whitelistEntryDataService.create(entry);

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(whitelistState,
                Service.MOBILE_ACADEMY));

        // Check the response
        HttpGet request = createHttpGet(true, "mobileacademy", true,
                String.valueOf(WHITELIST_CONTACT_NUMBER), false, "", true, rh
                        .delhiCircle().getName(), true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());
    }

    /**
     * To verify anonymous User belongs to a circle that has single state,
     * shouldn't be able to access MA Service content, if user's callingNumber
     * is not in whitelist and whitelist is set to Enabled for user's state.
     */
    @Test
    public void verifyFT452() throws InterruptedException, IOException {
        setupWhiteListData();

        Circle delhiCircle = circleDataService.findByName(rh.delhiCircle()
                .getName());
        assertNotNull(delhiCircle);
        assertNotNull(delhiCircle.getStates());
        assertEquals(delhiCircle.getStates().size(), 1);

        // Deploy the service in user's state
        deployedServiceDataService.create(new DeployedService(whitelistState,
                Service.MOBILE_ACADEMY));

        // Check the response
        HttpGet request = createHttpGet(true, "mobileacademy", true,
                String.valueOf(NOT_WHITELIST_CONTACT_NUMBER), false, "", true,
                rh.delhiCircle().getName(), true, "123456789012345");
        HttpResponse httpResponse = SimpleHttpClient.httpRequestAndResponse(
                request, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine()
                .getStatusCode());

        Set<State> states = languageService.getAllStatesForLanguage(rh
                .hindiLanguage());
        assertEquals(1, states.size());

        // create set Language location code request and check the response
        HttpPost postRequest = createHttpPost("mobileacademy",
                new UserLanguageRequest(NOT_WHITELIST_CONTACT_NUMBER,
                        123456789012345l, rh.hindiLanguage().getCode()));
        httpResponse = SimpleHttpClient.httpRequestAndResponse(postRequest,
                RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_FORBIDDEN, httpResponse.getStatusLine()
                .getStatusCode());
    }

    /**
     * This case has been added to test the functionality mentioned in
     * NMS.GEN.FLW.008. The NMS system shall provide means to mark an FLW as
     * invalid using CSV upload. Once an FLW is marked invalid, any incoming
     * call with MSISDN that is same as that of invalid FLW shall be treated as
     * that of an anonymous caller.
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

    /**
     * To verify that MK service shall be allowed when cappingType is set to
     * "State Capping" having usage pluses remaining.
     * <p>
     * To verify that MK maxallowedUsageInPulses counter is set successfully.
     */
    @Test
    public void verifyFT329_427() throws IOException, InterruptedException {
        State s = rh.delhiState();
        rh.delhiCircle();

        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.MOBILE_KUNJI));

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

        // Set maxallowedUsageInPulses to 3800
        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(s,
                Service.MOBILE_KUNJI, 3800);
        serviceUsageCapDataService.create(serviceUsageCap);

        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(1111111111l);
        cdr.setService(Service.MOBILE_KUNJI);
        cdr.setCallDurationInPulses(1);
        cdr.setEndOfUsagePromptCounter(0);
        cdr.setWelcomePrompt(false);
        cdr.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(cdr);

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
                3800, // maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that MK service  shall allow unlimited usage when cappingType is set to "No Capping"  for
     * user who has not listened  welcome message completely.
     */
    @Test
    public void verifyFT332() throws IOException, InterruptedException {
        rh.newDelhiDistrict();
        rh.southDelhiDistrict();
        rh.delhiCircle();

        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.MOBILE_KUNJI));

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

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
                0L,    //currentUsageInPulses
                0L,    //endOfUsagePromptCounter
                false,  //welcomePromptFlag
                -1,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that MK service  shall allow unlimited usage when cappingType is set to "No Capping"  for
     * user who has listened  welcome message completely earlier.
     */
    @Test
    public void verifyFT333() throws IOException, InterruptedException {
        rh.newDelhiDistrict();
        rh.southDelhiDistrict();
        rh.delhiCircle();

        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.MOBILE_KUNJI));

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(1111111111l);
        cdr.setService(Service.MOBILE_KUNJI);
        cdr.setCallDurationInPulses(1);
        cdr.setEndOfUsagePromptCounter(1);
        cdr.setWelcomePrompt(true);
        cdr.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(cdr);

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
                -1,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that Anonymous user belongs to circle having one state should  be able to listen MK content and
     * service deployment status is set to deploy in that particular state.
     */
    @Test
    public void verifyFT334() throws IOException, InterruptedException {
        rh.delhiCircle();

        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.MOBILE_KUNJI));

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

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
                0L,    //currentUsageInPulses
                0L,    //endOfUsagePromptCounter
                false,  //welcomePromptFlag
                -1,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that Anonymous user belongs to a circle having multiple states should  be able to listen
     * MK content and  service deploy status is set to deploy in that particular state.
     */
    @Test
    public void verifyFT335() throws IOException, InterruptedException {
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        frontLineWorkerService.add(flw);

        createCircleWithLanguage();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        List<String> allowedLLCCodes = new ArrayList<>();
        allowedLLCCodes.add(rh.hindiLanguage().getCode());
        allowedLLCCodes.add(rh.kannadaLanguage().getCode());

        String expectedJsonResponse = createFlwUserResponseJson(
                rh.hindiLanguage().getCode(),  //defaultLanguageLocationCode
                null,  //locationCode
                allowedLLCCodes, // allowedLanguageLocationCodes
                0L,    //currentUsageInPulses
                0L,    //endOfUsagePromptCounter
                false,  //welcomePromptFlag
                -1,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));

        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(1111111111L, 123456789012345L,
                rh.hindiLanguage().getCode()));

        response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }

    /**
     * To verify that Inactive user should  be able to listen MK content if service
     * deploy status is set to deploy in a particular state.
     */
    @Test
    public void verifyFT336_1() throws IOException, InterruptedException {
        rh.delhiCircle();

        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.MOBILE_KUNJI));

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguage(rh.hindiLanguage());
        flw.setStatus(FrontLineWorkerStatus.INACTIVE);
        frontLineWorkerService.add(flw);

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
                0L,    //currentUsageInPulses
                0L,    //endOfUsagePromptCounter
                false,  //welcomePromptFlag
                -1,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that Active user should  be able to listen MK content if service
     * deploy status is set to deploy in a particular state.
     */
    @Test
    public void verifyFT336_2() throws IOException, InterruptedException {
        rh.delhiCircle();

        deployedServiceDataService.create(new DeployedService(rh.delhiState(), Service.MOBILE_KUNJI));

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguage(rh.hindiLanguage());
        flw.setStatus(FrontLineWorkerStatus.ACTIVE);
        frontLineWorkerService.add(flw);

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
                0L,    //currentUsageInPulses
                0L,    //endOfUsagePromptCounter
                false,  //welcomePromptFlag
                -1,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that Anonymous user belonging to circle having one state should not be able to listen MK
     * content if service deploy status is set to not deploy in a particular state.
     */
    @Test
    public void verifyFT337() throws IOException, InterruptedException {
        rh.delhiCircle();

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, rh.delhiCircle().getName(),             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = "{\"failureReason\":\"<MOBILE_KUNJI: Not Deployed In State>\"}";

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that Anonymous user belonging to circle having multiple state should not  be able to
     * listen MK content if service deploy status is set to not deploy in a particular state.
     */
    @Test
    public void verifyFT338() throws IOException, InterruptedException {
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        frontLineWorkerService.add(flw);

        createCircleWithLanguage();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        List<String> allowedLLCCodes = new ArrayList<>();
        allowedLLCCodes.add(rh.hindiLanguage().getCode());
        allowedLLCCodes.add(rh.kannadaLanguage().getCode());

        String expectedJsonResponse = createFlwUserResponseJson(
            rh.hindiLanguage().getCode(),  //defaultLanguageLocationCode
            null,  //locationCode
            allowedLLCCodes, // allowedLanguageLocationCodes
            0L,    //currentUsageInPulses
            0L,    //endOfUsagePromptCounter
            false,  //welcomePromptFlag
            -1,  //maxAllowedUsageInPulses
            2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));

        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(1111111111L, 123456789012345L,
                rh.kannadaLanguage().getCode()));

        response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);

        expectedJsonResponse = "{\"failureReason\":\"<MOBILE_KUNJI: Not Deployed In State>\"}";

        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that Inactive user should not  be able to listen MK content if service
     * deploy status is set to not deploy in a particular state.
     */
    @Test
    public void verifyFT339_1() throws IOException, InterruptedException {
        rh.delhiCircle();

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguage(rh.hindiLanguage());
        flw.setStatus(FrontLineWorkerStatus.INACTIVE);
        frontLineWorkerService.add(flw);

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, rh.delhiCircle().getName(),             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = "{\"failureReason\":\"<MOBILE_KUNJI: Not Deployed In State>\"}";

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that Active user should not  be able to listen MK content if service
     * deploy status is set to not deploy in a particular state.
     */
    @Test
    public void verifyFT339_2() throws IOException, InterruptedException {
        rh.delhiCircle();

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguage(rh.hindiLanguage());
        flw.setStatus(FrontLineWorkerStatus.ACTIVE);
        frontLineWorkerService.add(flw);

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, rh.delhiCircle().getName(),             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = "{\"failureReason\":\"<MOBILE_KUNJI: Not Deployed In State>\"}";

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To get the details of the Anonymous user using get user details API when
     * circle sent in request is not mapped to any languageLocation.
     */
    @Test
    public void verifyFT453() throws IOException, InterruptedException {
        // create languages
        rh.hindiLanguage();
        rh.kannadaLanguage();

        // set national default language
        nationalDefaultLanguageDataService.create(new NationalDefaultLanguage(
                rh.hindiLanguage()));
        // create circle not mapped to any language
        Circle circle = RegionHelper.createCircle("BH");

        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                true, "OP", // operator
                true, circle.getName(),// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(
                rh.hindiLanguage().getCode(),  //defaultLanguageLocationCode=national
                null,  //locationCode
                Arrays.asList(rh.hindiLanguage().getCode(), rh.kannadaLanguage().getCode()), // allowedLanguageLocationCodes
                0L,    //currentUsageInPulses
                0L,    //endOfUsagePromptCounter
                false, //welcomePromptFlag
                -1,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that getuserdetails API is rejected when mandatory parameter
     * callingNumber is missing.
     */
    @Test
    public void verifyFT456() throws IOException, InterruptedException {
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                false, null, // callingNumber missing
                true, "OP", // operator
                true, rh.delhiCircle().getName(),// circle
                true, "123456789012345" // callId
        );
        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Not Present>");
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that getuserdetails API is rejected when mandatory parameter
     * callId is missing.
     */
    @Test
    public void verifyFT457() throws IOException, InterruptedException {
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                true, "OP", // operator
                true, rh.delhiCircle().getName(),// circle
                false, null // callId Missing
        );
        String expectedJsonResponse = createFailureResponseJson("<callId: Not Present>");
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that getuserdetails API is rejected when mandatory parameter
     * callingNumber is having invalid value
     */
    @Test
    public void verifyFT458() throws IOException, InterruptedException {
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "123456789", // callingNumber Invalid
                true, "OP", // operator
                true, rh.delhiCircle().getName(),// circle
                true, "123456789012345" // callId
        );
        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Invalid>");
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that getuserdetails API is rejected when mandatory parameter
     * callId is having invalid value
     */
    @Test
    public void verifyFT460() throws IOException, InterruptedException {
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1234567890", // callingNumber Invalid
                true, "OP", // operator
                true, rh.delhiCircle().getName(),// circle
                true, "1234567890123456" // callId
        );
        String expectedJsonResponse = createFailureResponseJson("<callId: Invalid>");
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To get the details of the Anonymous user using get user details API when
     * circle sent in request is mapped to multiple languageLocationCodes
     */
    @Test
    public void verifyFT454() throws IOException, InterruptedException {
        // create KARNATAKA circle with two languages i.e TAMIL, KANNADA and set
        // KANNADA as default
        createCircleWithMultipleLanguages();

        // set national default language
        nationalDefaultLanguageDataService.create(new NationalDefaultLanguage(
                rh.hindiLanguage()));

        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                true, "OP", // operator
                true, rh.karnatakaCircle().getName(),// circle
                true, "123456789012345" // callId
        );

        FlwUserResponse expectedResponse = createFlwUserResponse(rh
                        .kannadaLanguage().getCode(), // defaultLanguageLocationCode=circle default
                null, // locationCode
                Arrays.asList(rh.kannadaLanguage().getCode(), rh
                        .tamilLanguage().getCode()), // allowedLanguageLocationCodes
                0L, // currentUsageInPulses
                0L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        ObjectMapper mapper = new ObjectMapper();
        FlwUserResponse actual = mapper.readValue(EntityUtils
                .toString(response.getEntity()), FlwUserResponse.class);
        assertEquals(expectedResponse, actual);
    }

    /**
     * To get the details of the Anonymous user using get user details API when
     * circle and operator are missing
     */
    @Test
    public void verifyFT455() throws IOException, InterruptedException {
        rh.kannadaLanguage();
        rh.tamilLanguage();
        rh.hindiLanguage();
        // set national default language
        nationalDefaultLanguageDataService.create(new NationalDefaultLanguage(
                rh.hindiLanguage()));

        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        FlwUserResponse expectedResponse = createFlwUserResponse(rh
                        .hindiLanguage().getCode(), // defaultLanguageLocationCode=national default
                null, // locationCode
                Arrays.asList(rh.kannadaLanguage().getCode(), rh.tamilLanguage().getCode(), rh
                        .hindiLanguage().getCode()), // allowedLanguageLocationCodes
                0L, // currentUsageInPulses
                0L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        ObjectMapper mapper = new ObjectMapper();
        FlwUserResponse actual = mapper.readValue(EntityUtils
                .toString(response.getEntity()), FlwUserResponse.class);
        assertEquals(expectedResponse, actual);
    }

    /**
     * To get the details of the inactive user using get user details API when
     * languageLocation code is retrieved based on state and district. FLW must
     * exist in system with status as Inactive
     */
    @Test
    public void verifyFT461() throws IOException, InterruptedException {
        // create Invalid FLW record
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1200000001l);
        flw.setLanguage(rh.tamilLanguage());
        flw.setDistrict(rh.bangaloreDistrict());
        flw.setState(rh.karnatakaState());
        frontLineWorkerService.add(flw);

        // assert for FLW status
        flw = frontLineWorkerService.getByContactNumber(1200000001l);
        assertTrue(FrontLineWorkerStatus.INACTIVE == flw.getStatus());
        
        Circle circle = rh.karnatakaCircle();
        circle.setDefaultLanguage(rh.kannadaLanguage());
        circleDataService.update(circle);
        
        deployedServiceDataService.create(new
        DeployedService(rh.karnatakaState(), Service.MOBILE_ACADEMY));

        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000001", // callingNumber
                true, "OP", // operator
                true, circle.getName(),// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(rh
                .kannadaLanguage().getCode(), // defaultLanguageLocationCode=circle default
                rh.tamilLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                0L, // currentUsageInPulses
                0L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    /**
     * To get the details of the active user using get user details API when
     * languageLocation code is retrieved based on state and district. FLW must
     * exist in system with status as active.
     */
    @Test
    public void verifyFT462() throws IOException, InterruptedException {
        // create anonymous FLW record
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000001l);
        frontLineWorkerService.add(flw);

        // update FLW status to ACTIVE
        flw = frontLineWorkerService.getByContactNumber(1200000001l);
        flw.setLanguage(rh.tamilLanguage());
        flw.setDistrict(rh.bangaloreDistrict());
        flw.setState(rh.karnatakaState());
        frontLineWorkerService.update(flw);

        // assert for FLW status
        flw = frontLineWorkerService.getByContactNumber(1200000001l);
        assertTrue(FrontLineWorkerStatus.ACTIVE == flw.getStatus());

        Circle circle = rh.karnatakaCircle();
        circle.setDefaultLanguage(rh.kannadaLanguage());
        circleDataService.update(circle);

        deployedServiceDataService.create(new DeployedService(rh
                .karnatakaState(), Service.MOBILE_ACADEMY));

        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000001", // callingNumber
                true, "OP", // operator
                true, circle.getName(),// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(rh
                .kannadaLanguage().getCode(), // defaultLanguageLocationCode=circle
                                              // default
                rh.tamilLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                0L, // currentUsageInPulses
                0L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }
    
    private void createCircleWithNoLanguage(){
    	Circle circle = new Circle("AA");
        circle.setDefaultLanguage(rh.hindiLanguage());
        circleDataService.create(circle);
        
        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(null, Service.MOBILE_KUNJI, 3600);
        serviceUsageCapDataService.create(serviceUsageCap);
        
    }
    
    private void createFlwWithStatusActive(){
    	// create anonymous FLW record
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        frontLineWorkerService.add(flw);

        // update FLW status to ACTIVE
        flw = frontLineWorkerService.getByContactNumber(1111111111L);
        flw.setLanguage(rh.tamilLanguage());
        flw.setDistrict(rh.bangaloreDistrict());
        flw.setState(rh.karnatakaState());
        frontLineWorkerService.update(flw);

        Circle circle = rh.karnatakaCircle();
        circle.setDefaultLanguage(rh.kannadaLanguage());
        circleDataService.update(circle);

        deployedServiceDataService.create(new DeployedService(rh
                .karnatakaState(), Service.MOBILE_KUNJI));
    }
    
    private void createFlwWithStatusInactive(){
    	// create Invalid FLW record
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguage(rh.tamilLanguage());
        flw.setDistrict(rh.bangaloreDistrict());
        flw.setState(rh.karnatakaState());
        frontLineWorkerService.add(flw);

        Circle circle = rh.karnatakaCircle();
        circle.setDefaultLanguage(rh.kannadaLanguage());
        circleDataService.update(circle);
        
        deployedServiceDataService.create(new
        DeployedService(rh.karnatakaState(), Service.MOBILE_KUNJI));
    }
    
    /*
     * To get the details of the Anonymous user using getuserdetails API 
     * when circle sent in request is not mapped to any languageLocation.
     */
    @Test
    public void verifyFT349() throws IOException, InterruptedException {
        createCircleWithNoLanguage();

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
                new ArrayList<String>(), // allowedLanguageLocationCodes
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
    
    /*
     * To get the details of the Anonymous user using getuserdetails API 
     * when circle sent in request is mapped to multiple languageLocationCodes
     */
    @Test
    public void verifyFT350() throws IOException, InterruptedException {
        createCircleWithMultipleLanguages();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1200000000",     //callingNumber
                true, "OP",             //operator
                true, "KA",             //circle
                true, "123456789012345" //callId
        );

        FlwUserResponse expectedResponse = createFlwUserResponse(
                rh.kannadaLanguage().getCode(),  //defaultLanguageLocationCode
                null,  //locationCode
                Arrays.asList(rh.kannadaLanguage().getCode(), rh.tamilLanguage().getCode()), // allowedLanguageLocationCodes
                0L,    //currentUsageInPulses
                0L,    //endOfUsagePromptCounter
                false, //welcomePromptFlag
                -1,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        ObjectMapper mapper = new ObjectMapper();
        FlwUserResponse actual = mapper.readValue(EntityUtils.toString(response.getEntity()), FlwUserResponse.class);
        assertEquals(expectedResponse, actual);
    }
    
    /*
     * To get the details of the Anonymous user using getuserdetails API 
     * when circle and operator are missing.
     */
    @Test
    public void verifyFT351() throws IOException, InterruptedException {
    	//Used this method to set up mobile_kunji environment 
    	createCircleWithLanguage();
    	
        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",      //service
                true, "1111111112",       //callingNumber
                false, null,              //operator
                false, null,			  //circle
                true, "123456789012345"   //callId
        );

        FlwUserResponse expectedResponse = createFlwUserResponse(
                rh.hindiLanguage().getCode(),  //defaultLanguageLocationCode
                null,  //locationCode
                Arrays.asList(rh.hindiLanguage().getCode(), rh.kannadaLanguage().getCode(), rh.tamilLanguage()
                        .getCode()), // allowedLanguageLocationCodes
                0L,    //currentUsageInPulses
                0L,    //endOfUsagePromptCounter
                false, //welcomePromptFlag
                -1,  //maxAllowedUsageInPulses
                2      //maxAllowedEndOfUsagePrompt
        );
        
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        ObjectMapper mapper = new ObjectMapper();
        FlwUserResponse actual = mapper.readValue(EntityUtils
                .toString(response.getEntity()), FlwUserResponse.class);
        assertEquals(expectedResponse, actual);
    }
    
    /*
     * To verify that getuserdetails API is rejected when mandatory parameter 
     * callingNumber is missing.
     */
    @Test
    public void verifyFT352() throws IOException, InterruptedException {
        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
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
    
    /*
     * To verify that getuserdetails API is rejected when mandatory parameter callId is missing.
     */
    @Test
    public void verifyFT353() throws IOException, InterruptedException {
    	//Used this method to set up mobile_kunji environment
    	createCircleWithLanguage();
    	
        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
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
    
    /*
     * To verify that getuserdetails API is rejected when mandatory parameter 
     * callingNumber is having invalid value
     */
    @Test
    public void verifyFT354() throws IOException, InterruptedException {
        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111",       //callingNumber
                true, "OP",             //operator
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFailureResponseJson("<callingNumber: Invalid>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }
    
    /*
     * To verify that getuserdetails API is rejected when optional parameter circle is having invalid value
     */
    @Test
    public void verifyFT355() throws IOException, InterruptedException {
    	//Used this method to set up mobile_kunji environment
    	createCircleWithLanguage();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, "Invalid",       //circle
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
    
    /*
     * To verify that getuserdetails API is rejected when mandatory parameter 
     * callId is having invalid value
     */
    @Test
    public void verifyFT356() throws IOException, InterruptedException {
    	//Used this method to set up mobile_kunji environment
    	createCircleWithLanguage();

        HttpGet httpGet = createHttpGet(
                true, "mobilekunji",    //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                true, "AP",       		//circle
                true, "22222222222222222" //callId
        );

        
        String expectedJsonResponse = createFailureResponseJson("<callId: Invalid>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }
    
    /*
     * To get the details of the inactive user using getuserdetails API 
     * when languageLocation code is retrieved based on state and district.
     */
    @Test
    public void verifyFT357() throws IOException, InterruptedException {
    	createFlwWithStatusInactive();

        HttpGet httpGet = createHttpGet(true, "mobilekunji", // service
                true, "1111111111", // callingNumber
                true, "OP", // operator
                true, "KA",// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(rh
                .kannadaLanguage().getCode(), // defaultLanguageLocationCode=circle default
                rh.tamilLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                0L, // currentUsageInPulses
                0L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));

    }
    
    /*
     * To get the details of the active user using getuserdetails API 
     * when languageLocation code is retrieved based on state and district.
     */
    @Test
    public void verifyFT358() throws IOException, InterruptedException {
    	createFlwWithStatusActive();
    	
        HttpGet httpGet = createHttpGet(true, "mobilekunji", // service
                true, "1111111111", // callingNumber
                true, "OP", // operator
                true, "KA",// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(rh
                .kannadaLanguage().getCode(), // defaultLanguageLocationCode=circle
                                              // default
                rh.tamilLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                0L, // currentUsageInPulses
                0L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
        
    }

    /**
     * To verify that Anonymous user belonging to a circle having single state
     * should be able to listen MA content if service deploy status is set to
     * deploy in a particular state.
     */
    @Test
    public void verifyFT428() throws IOException, InterruptedException {

        rh.newDelhiDistrict();
        rh.delhiCircle();
        deployedServiceDataService.create(new DeployedService(rh.delhiState(),
                Service.MOBILE_ACADEMY));

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                true, "OP", // operator
                true, rh.delhiCircle().getName(),// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(rh
                .hindiLanguage().getCode(), // defaultLanguageLocationCode=circle
                                            // default
                null, // locationCode
                Collections.singletonList(rh.hindiLanguage().getCode()), // allowedLanguageLocationCodes
                0L, // currentUsageInPulses
                0L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));

        // Invoke set LLC API
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/mobileacademy/languageLocationCode",
                TestContext.getJettyPort()));
        StringEntity params = new StringEntity(
                "{\"callingNumber\":1200000000,\"callId\":123456789012345,\"languageLocationCode\":\""
                        + rh.hindiLanguage().getCode() + "\"}");
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK));
        FrontLineWorker flw = frontLineWorkerService
                .getByContactNumber(1200000000l);
        assertEquals(FrontLineWorkerStatus.ANONYMOUS, flw.getStatus());
        assertEquals(rh.hindiLanguage().getCode(), flw.getLanguage().getCode());
    }

    /**
     * To verify that Anonymous user belongs to circle having one state
     * shouldn't be able to listen MA content when service deploy status is set
     * to not deploy in that particular state.
     */
    @Test
    public void verifyFT437() throws IOException, InterruptedException {

        rh.newDelhiDistrict();
        rh.delhiCircle();
        // service not deployed in delhi state

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                true, "OP", // operator
                true, rh.delhiCircle().getName(),// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFailureResponseJson("<MOBILE_ACADEMY: Not Deployed In State>");
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine()
                .getStatusCode());
    }

    /**
     * To verify that Anonymous user belongs to a circle having multiple states
     * should be able to listen MA content when service deployment status is set
     * to deploy in that particular state.
     */
    @Test
    public void verifyFT429() throws IOException, InterruptedException {
        // setup delhi circle with two states delhi and karnataka

        rh.newDelhiDistrict();
        Circle c = rh.delhiCircle();
        rh.bangaloreDistrict();
        c.getStates().add(rh.karnatakaState());
        circleDataService.update(c);
        // service deployed only in delhi state
        deployedServiceDataService.create(new DeployedService(rh.delhiState(),
                Service.MOBILE_ACADEMY));

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                true, "OP", // operator
                true, rh.delhiCircle().getName(),// circle
                true, "123456789012345" // callId
        );

        FlwUserResponse expectedResponse = createFlwUserResponse(rh
                        .hindiLanguage().getCode(), // defaultLanguageLocationCode=circle
                // default
                null, // locationCode
                Arrays.asList(rh.hindiLanguage().getCode(), rh.tamilLanguage()
                        .getCode()), // allowedLanguageLocationCodes
                0L, // currentUsageInPulses
                0L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        ObjectMapper mapper = new ObjectMapper();
        FlwUserResponse actual = mapper.readValue(EntityUtils
                .toString(response.getEntity()), FlwUserResponse.class);
        assertEquals(expectedResponse, actual);

        // Invoke set LLC API
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/mobileacademy/languageLocationCode",
                TestContext.getJettyPort()));
        StringEntity params = new StringEntity(
                "{\"callingNumber\":1200000000,\"callId\":123456789012345,\"languageLocationCode\":\""
                        + rh.hindiLanguage().getCode() + "\"}");
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK));
        FrontLineWorker flw = frontLineWorkerService
                .getByContactNumber(1200000000l);
        assertEquals(FrontLineWorkerStatus.ANONYMOUS, flw.getStatus());
        assertEquals(rh.hindiLanguage().getCode(), flw.getLanguage().getCode());
    }

    /**
     * To verify that Anonymous user belongs to a circle having multiple states
     * shouldn't be able to listen MA content when service deploy status is set
     * to not deploy in that particular state.
     */
    @Test
    public void verifyFT438() throws IOException, InterruptedException {
        // setup delhi circle with two states delhi and karnataka

        rh.newDelhiDistrict();
        Circle c = rh.delhiCircle();
        rh.bangaloreDistrict();
        c.getStates().add(rh.karnatakaState());
        circleDataService.update(c);
        // service deployed only in delhi state
        deployedServiceDataService.create(new DeployedService(rh.delhiState(),
                Service.MOBILE_ACADEMY));

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                true, "OP", // operator
                true, rh.delhiCircle().getName(),// circle
                true, "123456789012345" // callId
        );

        FlwUserResponse expectedResponse = createFlwUserResponse(rh
                        .hindiLanguage().getCode(), // defaultLanguageLocationCode=circle
                // default
                null, // locationCode
                Arrays.asList(rh.hindiLanguage().getCode(), rh.tamilLanguage()
                        .getCode()), // allowedLanguageLocationCodes
                0L, // currentUsageInPulses
                0L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        ObjectMapper mapper = new ObjectMapper();
        FlwUserResponse actual = mapper.readValue(EntityUtils
                .toString(response.getEntity()), FlwUserResponse.class);
        assertEquals(expectedResponse, actual);

        // Invoke set LLC API
        // Set LLC for which service is not deployed i.e karnataka
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/mobileacademy/languageLocationCode",
                TestContext.getJettyPort()));
        StringEntity params = new StringEntity(
                "{\"callingNumber\":1200000000,\"callId\":123456789012345,\"languageLocationCode\":\""
                        + rh.tamilLanguage().getCode() + "\"}");
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");

        String expectedJsonResponse = createFailureResponseJson("<MOBILE_ACADEMY: Not Deployed In State>");
        response = SimpleHttpClient.httpRequestAndResponse(httpPost,
                ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine()
                .getStatusCode());
    }

    /**
     * To verify that Anonymous user should not be able to listen MA content if
     * service deploy status is set to not deploy in a particular state.
     */
    @Test
    public void verifyFT433() throws IOException, InterruptedException {
        // setup karnataka state for which service is not deployed
        rh.bangaloreDistrict();

        // invoke get user detail API without circle and operator
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                null, // locationCode
                Collections.singletonList(rh.tamilLanguage().getCode()), // allowedLanguageLocationCodes
                0L, // currentUsageInPulses
                0L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));

        // Invoke set LLC API
        // Set LLC as per allowedLanguageLocationCodes response field
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/mobileacademy/languageLocationCode",
                TestContext.getJettyPort()));
        StringEntity params = new StringEntity(
                "{\"callingNumber\":1200000000,\"callId\":123456789012345,\"languageLocationCode\":\""
                        + rh.tamilLanguage().getCode() + "\"}");
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");

        expectedJsonResponse = createFailureResponseJson("<MOBILE_ACADEMY: Not Deployed In State>");
        response = SimpleHttpClient.httpRequestAndResponse(httpPost,
                ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine()
                .getStatusCode());
    }

    /**
     * To verify that Active user should be able to listen MA content if service
     * deploy status is set to deploy in a particular state.
     */
    @Test
    public void verifyFT430() throws IOException, InterruptedException {
        // add FLW with active status
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        flw.setLanguage(rh.tamilLanguage());
        flw.setDistrict(rh.bangaloreDistrict());
        flw.setState(rh.karnatakaState());
        flw.setStatus(FrontLineWorkerStatus.ACTIVE);
        frontLineWorkerDataService.create(flw);

        // service deployed in Karnataka State
        deployedServiceDataService.create(new DeployedService(rh
                .karnatakaState(), Service.MOBILE_ACADEMY));

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.tamilLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                0L, // currentUsageInPulses
                0L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that Inactive user should be able to listen MA content if
     * service deploy status is set to deploy in a particular state.
     */
    @Test
    public void verifyFT431() throws IOException, InterruptedException {
        // add FLW with In active status
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        flw.setLanguage(rh.tamilLanguage());
        flw.setDistrict(rh.bangaloreDistrict());
        flw.setState(rh.karnatakaState());
        flw.setStatus(FrontLineWorkerStatus.INACTIVE);
        frontLineWorkerDataService.create(flw);

        // service deployed in Karnataka State
        deployedServiceDataService.create(new DeployedService(rh
                .karnatakaState(), Service.MOBILE_ACADEMY));

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.tamilLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                0L, // currentUsageInPulses
                0L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that Invalid user should be able to listen MA content if
     * service deploy status is set to deploy in a particular state.
     *
     * RL: Invalid users are now handled the same as anonymous
     *     https://applab.atlassian.net/browse/NMS-236
     */
    @Test
    public void verifyFT432() throws IOException, InterruptedException {
        // add FLW with Invalid status
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        flw.setLanguage(rh.tamilLanguage());
        flw.setDistrict(rh.bangaloreDistrict());
        flw.setState(rh.karnatakaState());
        flw.setStatus(FrontLineWorkerStatus.INVALID);
        flw.setInvalidationDate(DateTime.now().minusDays(50));
        frontLineWorkerDataService.create(flw);

        // service deployed in Karnataka State
        deployedServiceDataService.create(new DeployedService(rh
                .karnatakaState(), Service.MOBILE_ACADEMY));

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                null, // locationCode
                Collections.singletonList(rh.tamilLanguage().getCode()), // allowedLanguageLocationCodes
                0L, // currentUsageInPulses
                0L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that Active user should not be able to listen MA content if
     * service deploy status is set to not deploy in a particular state.
     */
    @Test
    public void verifyFT434() throws IOException, InterruptedException {
        // add FLW with active status
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        flw.setLanguage(rh.tamilLanguage());
        flw.setDistrict(rh.bangaloreDistrict());
        flw.setState(rh.karnatakaState());
        flw.setStatus(FrontLineWorkerStatus.ACTIVE);
        frontLineWorkerDataService.create(flw);

        // service not deployed in Karnataka State

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFailureResponseJson("<MOBILE_ACADEMY: Not Deployed In State>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine()
                .getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that Inactive user should not be able to listen MA content if
     * service deploy status is set to not deploy in a particular state.
     */
    @Test
    public void verifyFT435() throws IOException, InterruptedException {
        // add FLW with In active status
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        flw.setLanguage(rh.tamilLanguage());
        flw.setDistrict(rh.bangaloreDistrict());
        flw.setState(rh.karnatakaState());
        flw.setStatus(FrontLineWorkerStatus.INACTIVE);
        frontLineWorkerDataService.create(flw);

        // service not deployed in Karnataka State

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFailureResponseJson("<MOBILE_ACADEMY: Not Deployed In State>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine()
                .getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that Invalid user should not be able to listen MA content if
     * service deploy status is set to not deploy in a particular state.
     *
     * RL: Invalid users are now treated the same as non-existent or Anonymous
     *     https://applab.atlassian.net/browse/NMS-236
     */
    @Test
    public void verifyFT436() throws IOException, InterruptedException {
        // add FLW with Invalid status
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1200000000l);
        flw.setLanguage(rh.tamilLanguage());
        flw.setDistrict(rh.bangaloreDistrict());
        flw.setState(rh.karnatakaState());
        flw.setStatus(FrontLineWorkerStatus.INVALID);
        flw.setInvalidationDate(DateTime.now().minusDays(50));
        frontLineWorkerDataService.create(flw);

        // service not deployed in Karnataka State

        // invoke get user detail API without circle and operator
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                null, // locationCode
                Collections.singletonList(rh.tamilLanguage().getCode()), // allowedLanguageLocationCodes
                0L, // currentUsageInPulses
                0L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));

        // Invoke set LLC API
        // Set LLC as per allowedLanguageLocationCodes response field
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/mobileacademy/languageLocationCode",
                TestContext.getJettyPort()));
        StringEntity params = new StringEntity(
                "{\"callingNumber\":1200000000,\"callId\":123456789012345,\"languageLocationCode\":\""
                        + rh.tamilLanguage().getCode() + "\"}");
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");

        expectedJsonResponse = createFailureResponseJson("<MOBILE_ACADEMY: Not Deployed In State>");
        response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());
    }

    /**
     * To verify that MA service is accessible usage when cappingType is set to
     * "National Capping" having usage pulses remaining.
     * <p>
     * To verify that MA maxallowedUsageInPulses counter is set successfully.
     */
    @Test
    public void verifyFT421_480() throws IOException, InterruptedException {
        rh.newDelhiDistrict();

        // National Capping set maxallowedUsageInPulses
        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(null,
                Service.MOBILE_ACADEMY, 5000);
        serviceUsageCapDataService.create(serviceUsageCap);

        // FLW usage
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        frontLineWorkerService.add(flw);
        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(1111111111l);
        cdr.setService(Service.MOBILE_ACADEMY);
        cdr.setCallDurationInPulses(80);
        cdr.setEndOfUsagePromptCounter(1);
        cdr.setWelcomePrompt(false);
        cdr.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(cdr);

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                null, // locationCode
                Collections.singletonList(rh.hindiLanguage().getCode()), // allowedLanguageLocationCodes
                80L, // currentUsageInPulses
                1L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                5000, // maxAllowedUsageInPulses=National cap
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that MA service is accessible usage when cappingType is set to
     * "State Capping" having usage pulses remaining.
     */
    @Test
    public void verifyFT423() throws IOException, InterruptedException {
        rh.newDelhiDistrict();
        deployedServiceDataService.create(new DeployedService(rh
                .delhiState(), Service.MOBILE_ACADEMY));
        
        // State Capping
        ServiceUsageCap stateUsageCap = new ServiceUsageCap(rh.delhiState(),
                Service.MOBILE_ACADEMY, 6000);
        serviceUsageCapDataService.create(stateUsageCap);
        
        //national capping
        ServiceUsageCap nationalUsageCap = new ServiceUsageCap(null,
                Service.MOBILE_ACADEMY, 5000);
        serviceUsageCapDataService.create(nationalUsageCap);

        // FLW usage
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(1111111111l);
        cdr.setService(Service.MOBILE_ACADEMY);
        cdr.setCallDurationInPulses(80);
        cdr.setEndOfUsagePromptCounter(1);
        cdr.setWelcomePrompt(false);
        cdr.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(cdr);

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                80L, // currentUsageInPulses
                1L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                6000, // maxAllowedUsageInPulses=State cap
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that MA service shall maintain the pulses consumed by FLW for
     * MA usage.
     */
    // TODO https://applab.atlassian.net/browse/NMS-241
    @Test
    public void verifyFT523() throws IOException, InterruptedException {
        rh.newDelhiDistrict();
        rh.delhiCircle();
        deployedServiceDataService.create(new DeployedService(rh.delhiState(),
                Service.MOBILE_ACADEMY));
        
        // Create FLW with no usage
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);
        
        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                0L, // currentUsageInPulses
                0L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses=No capping
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));

        // Invoke Save call Detail

        HttpPost httpPost = ApiRequestHelper.createCallDetailsPost(
                "mobileacademy",
        /* callingNumber */true, 1200000000l,
        /* callId */true, 234000011111111l,
        /* operator */false, null,
        /* circle */false, null,

        // This test will fail if run within 5 minutes of midnight on the first of the month.  I'm ok with that.
        /* callStartTime */true, (DateTime.now().minusMinutes(5).getMillis() / 1000),
        /* callEndTime */true, (DateTime.now().getMillis() / 1000),

        /* callDurationInPulses */true, 60,
        /* endOfUsagePromptCounter */true, 1,
        /* welcomeMessagePromptFlag */false, null,
        /* callStatus */true, 1,
        /* callDisconnectReason */true, 2,
        /* content */false, null);

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK,
                ADMIN_USERNAME, ADMIN_PASSWORD));

        CallDetailRecord cdr = callDetailRecordService
                .getByCallingNumber(1200000000l);

        // assert call detail record
        assertNotNull(cdr);
        assertEquals(1200000000l, cdr.getCallingNumber());
        assertEquals(60, cdr.getCallDurationInPulses());
        assertEquals(1, cdr.getEndOfUsagePromptCounter());

        // invoke get user detail API To check updated usage and prompt
        httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012346" // callId
        );

        expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                60L, // currentUsageInPulses=updated
                1L, // endOfUsagePromptCounter=updated
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses=No capping
                2 // maxAllowedEndOfUsagePrompt
        );

        response = SimpleHttpClient.httpRequestAndResponse(httpGet,
                ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }
    
    /**
     * To verify that current usage pulses is resetted after the end of month.
     * For national capping
     */
    @Test
    public void verifyFT498() throws IOException, InterruptedException {
        rh.newDelhiDistrict();
        rh.delhiCircle();
        deployedServiceDataService.create(new DeployedService(rh.delhiState(),
                Service.MOBILE_ACADEMY));
        
        // national capping
        ServiceUsageCap nationalUsageCap = new ServiceUsageCap(null,
                Service.MOBILE_ACADEMY, 500);
        serviceUsageCapDataService.create(nationalUsageCap);

        // FLW usage
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(1111111111l);
        cdr.setService(Service.MOBILE_ACADEMY);
        cdr.setCallDurationInPulses(80);
        cdr.setEndOfUsagePromptCounter(1);
        cdr.setWelcomePrompt(false);
        cdr.setCallStartTime(DateTime.now().withDayOfMonth(1).withTimeAtStartOfDay());
        callDetailRecordDataService.create(cdr);

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                80L, // currentUsageInPulses
                1L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                500, // maxAllowedUsageInPulses=National capping
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));

        // Update FLW usage to previous month last day time such that it is resetted now

        cdr.setCallStartTime(DateTime.now().withDayOfMonth(1).withTimeAtStartOfDay().minusMinutes(1));
        callDetailRecordDataService.update(cdr);

        // invoke get user detail API To check updated usage and prompt
        httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012346" // callId
        );

        expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                0L, // currentUsageInPulses=updated
                0L, // endOfUsagePromptCounter=updated
                false, // welcomePromptFlag
                500, // maxAllowedUsageInPulses=National capping
                2 // maxAllowedEndOfUsagePrompt
        );

        response = SimpleHttpClient.httpRequestAndResponse(httpGet,
                ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that endOfusagePrompt counter incremented when cappingType is
     * set to "National Capping" having usage pulses exhausted.
     */
    @Test
    public void verifyFT422() throws IOException, InterruptedException {
        rh.newDelhiDistrict();
        deployedServiceDataService.create(new DeployedService(rh.delhiState(),
                Service.MOBILE_ACADEMY));

        // national capping
        ServiceUsageCap nationalUsageCap = new ServiceUsageCap(null,
                Service.MOBILE_ACADEMY, 100);
        serviceUsageCapDataService.create(nationalUsageCap);

        // FLW usage
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

        // Assume IVR already played endo of usage 1 time.
        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(1200000000l);
        cdr.setService(Service.MOBILE_ACADEMY);
        cdr.setCallDurationInPulses(110);// greater than max allowed pulses
        cdr.setEndOfUsagePromptCounter(1);
        cdr.setWelcomePrompt(false);
        cdr.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(cdr);

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                110L, // currentUsageInPulses
                1L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                100, // maxAllowedUsageInPulses=National capping
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));

        // Invoke Save call Detail by assuming IVR increment
        // endOfUsagePromptCounter 1

        HttpPost httpPost = ApiRequestHelper.createCallDetailsPost(
                "mobileacademy",
                /* callingNumber */true, 1200000000l,
                /* callId */true, 234000011111111l,
                /* operator */false, null,
                /* circle */false, null,

                // This test will fail if run within 5 minutes of midnight on
                // the first of the month. I'm ok with that.
                /* callStartTime */true, (DateTime.now().minusMinutes(5)
                        .getMillis() / 1000),
                /* callEndTime */true, (DateTime.now().getMillis() / 1000),

                /* callDurationInPulses */true, 40,
                /* endOfUsagePromptCounter */true, 1,
                /* welcomeMessagePromptFlag */false, null,
                /* callStatus */true, 1,
                /* callDisconnectReason */true, 2,
                /* content */false, null);

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK,
                ADMIN_USERNAME, ADMIN_PASSWORD));

        // invoke get user detail API To check updated usage and prompt
        httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012346" // callId
        );

        expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                150L, // currentUsageInPulses=updated
                2L, // endOfUsagePromptCounter=updated
                false, // welcomePromptFlag
                100, // maxAllowedUsageInPulses=National capping
                2 // maxAllowedEndOfUsagePrompt
        );

        response = SimpleHttpClient.httpRequestAndResponse(httpGet,
                ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that endOfUsagePromptCounter is incremented when cappingType is
     * set to "State Capping" having usage pulses exhausted.
     */
    @Test
    public void verifyFT425() throws IOException, InterruptedException {
        rh.newDelhiDistrict();
        deployedServiceDataService.create(new DeployedService(rh.delhiState(),
                Service.MOBILE_ACADEMY));

        // State Capping
        ServiceUsageCap stateUsageCap = new ServiceUsageCap(rh.delhiState(),
                Service.MOBILE_ACADEMY, 150);
        serviceUsageCapDataService.create(stateUsageCap);

        // FLW usage
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

        // Assume IVR already played endo of usage 1 time.
        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(1200000000l);
        cdr.setService(Service.MOBILE_ACADEMY);
        cdr.setCallDurationInPulses(160);// greater than max allowed pulses
        cdr.setEndOfUsagePromptCounter(1);
        cdr.setWelcomePrompt(false);
        cdr.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(cdr);

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                160L, // currentUsageInPulses
                1L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                150, // maxAllowedUsageInPulses=State capping
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));

        // Invoke Save call Detail by assuming IVR increment
        // endOfUsagePromptCounter 1

        HttpPost httpPost = ApiRequestHelper.createCallDetailsPost(
                "mobileacademy",
                /* callingNumber */true, 1200000000l,
                /* callId */true, 234000011111111l,
                /* operator */false, null,
                /* circle */false, null,

                // This test will fail if run within 5 minutes of midnight on
                // the first of the month. I'm ok with that.
                /* callStartTime */true, (DateTime.now().minusMinutes(5)
                        .getMillis() / 1000),
                /* callEndTime */true, (DateTime.now().getMillis() / 1000),

                /* callDurationInPulses */true, 40,
                /* endOfUsagePromptCounter */true, 1,
                /* welcomeMessagePromptFlag */false, null,
                /* callStatus */true, 1,
                /* callDisconnectReason */true, 2,
                /* content */false, null);

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK,
                ADMIN_USERNAME, ADMIN_PASSWORD));

        // invoke get user detail API To check updated usage and prompt
        httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012346" // callId
        );

        expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                200L, // currentUsageInPulses=updated
                2L, // endOfUsagePromptCounter=updated
                false, // welcomePromptFlag
                150, // maxAllowedUsageInPulses=state capping
                2 // maxAllowedEndOfUsagePrompt
        );

        response = SimpleHttpClient.httpRequestAndResponse(httpGet,
                ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that endOfusagePrompt counter incremented when cappingType is
     * set to "National Capping" having usage pulses exhausted.
     */
    @Test
    public void verifyFT327() throws IOException, InterruptedException {
        rh.newDelhiDistrict();
        deployedServiceDataService.create(new DeployedService(rh.delhiState(),
                Service.MOBILE_KUNJI));

        // national capping for MOBILEKUNJI
        ServiceUsageCap nationalUsageCap = new ServiceUsageCap(null, Service.MOBILE_KUNJI, 100);
        serviceUsageCapDataService.create(nationalUsageCap);

        // FLW usage
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

        // Assume IVR already played end of usage 1 time.
        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(1200000000l);
        cdr.setService(Service.MOBILE_KUNJI);
        cdr.setCallDurationInPulses(110);// greater than max allowed pulses
        cdr.setEndOfUsagePromptCounter(1);
        cdr.setWelcomePrompt(true);
        cdr.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(cdr);

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobilekunji", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                110L, // currentUsageInPulses
                1L, // endOfUsagePromptCounter
                true, // welcomePromptFlag
                100, // maxAllowedUsageInPulses=National capping
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));

        // Invoke Save call Detail by assuming IVR increment
        // endOfUsagePromptCounter 1

        HttpPost httpPost = ApiRequestHelper.createCallDetailsPost(
                "mobilekunji",
                /* callingNumber */true, 1200000000l,
                /* callId */true, 234000011111111l,
                /* operator */false, null,
                /* circle */false, null,

                // This test will fail if run within 5 minutes of midnight on
                // the first of the month. I'm ok with that.
                /* callStartTime */true, (DateTime.now().minusMinutes(5)
                        .getMillis() / 1000),
                /* callEndTime */true, (DateTime.now().getMillis() / 1000),

                /* callDurationInPulses */true, 40,
                /* endOfUsagePromptCounter */true, 1,
                /* welcomeMessagePromptFlag */true, true,
                /* callStatus */true, 1,
                /* callDisconnectReason */true, 2,
                /* content */false, null);

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK,
                ADMIN_USERNAME, ADMIN_PASSWORD));

        // invoke get user detail API To check updated usage and prompt
        httpGet = createHttpGet(true, "mobilekunji", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012346" // callId
        );

        expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                150L, // currentUsageInPulses=updated
                2L, // endOfUsagePromptCounter=updated
                true, // welcomePromptFlag
                100, // maxAllowedUsageInPulses=National capping
                2 // maxAllowedEndOfUsagePrompt
        );

        response = SimpleHttpClient.httpRequestAndResponse(httpGet,
                ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that endOfUsagePromptCounter is incremented when cappingType is
     * set to "State Capping" having usage pulses exhausted.
     */
    @Test
    public void verifyFT330() throws IOException, InterruptedException {
        rh.newDelhiDistrict();
        deployedServiceDataService.create(new DeployedService(rh.delhiState(),
                Service.MOBILE_KUNJI));

        // State Capping
        ServiceUsageCap stateUsageCap = new ServiceUsageCap(rh.delhiState(),
                Service.MOBILE_KUNJI, 150);
        serviceUsageCapDataService.create(stateUsageCap);

        // FLW usage
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

        // Assume IVR already played endo of usage 1 time.
        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(1200000000l);
        cdr.setService(Service.MOBILE_KUNJI);
        cdr.setCallDurationInPulses(160);// greater than max allowed pulses
        cdr.setEndOfUsagePromptCounter(1);
        cdr.setWelcomePrompt(true);
        cdr.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(cdr);

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobilekunji", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                160L, // currentUsageInPulses
                1L, // endOfUsagePromptCounter
                true, // welcomePromptFlag
                150, // maxAllowedUsageInPulses=State capping
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));

        // Invoke Save call Detail by assuming IVR increment
        // endOfUsagePromptCounter 1

        HttpPost httpPost = ApiRequestHelper.createCallDetailsPost(
                "mobilekunji",
                /* callingNumber */true, 1200000000l,
                /* callId */true, 234000011111111l,
                /* operator */false, null,
                /* circle */false, null,

                // This test will fail if run within 5 minutes of midnight on
                // the first of the month. I'm ok with that.
                /* callStartTime */true, (DateTime.now().minusMinutes(5)
                        .getMillis() / 1000),
                /* callEndTime */true, (DateTime.now().getMillis() / 1000),

                /* callDurationInPulses */true, 40,
                /* endOfUsagePromptCounter */true, 1,
                /* welcomeMessagePromptFlag */true, true,
                /* callStatus */true, 1,
                /* callDisconnectReason */true, 2,
                /* content */false, null);

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost, HttpStatus.SC_OK,
                ADMIN_USERNAME, ADMIN_PASSWORD));

        // invoke get user detail API To check updated usage and prompt
        httpGet = createHttpGet(true, "mobilekunji", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012346" // callId
        );

        expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                200L, // currentUsageInPulses=updated
                2L, // endOfUsagePromptCounter=updated
                true, // welcomePromptFlag
                150, // maxAllowedUsageInPulses=state capping
                2 // maxAllowedEndOfUsagePrompt
        );

        response = SimpleHttpClient.httpRequestAndResponse(httpGet,
                ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that current usage pulses is resetted after the end of month.
     */
    @Test
    public void verifyFT328() throws IOException, InterruptedException {
        rh.newDelhiDistrict();
        rh.delhiCircle();
        deployedServiceDataService.create(new DeployedService(rh.delhiState(),
                Service.MOBILE_KUNJI));

        // State Capping
        ServiceUsageCap stateUsageCap = new ServiceUsageCap(rh.delhiState(),
                Service.MOBILE_KUNJI, 150);
        serviceUsageCapDataService.create(stateUsageCap);

        // FLW usage
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(1111111111l);
        cdr.setService(Service.MOBILE_KUNJI);
        cdr.setCallDurationInPulses(80);
        cdr.setEndOfUsagePromptCounter(1);
        cdr.setWelcomePrompt(true);
        cdr.setCallStartTime(DateTime.now().withDayOfMonth(1).withTimeAtStartOfDay());
        callDetailRecordDataService.create(cdr);

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobilekunji", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                80L, // currentUsageInPulses
                1L, // endOfUsagePromptCounter
                true, // welcomePromptFlag
                150, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));

        // Update FLW usage to previous month last day time such that it is resetted now

        cdr.setCallStartTime(DateTime.now().withDayOfMonth(1).withTimeAtStartOfDay().minusMinutes(1));
        callDetailRecordDataService.update(cdr);

        // invoke get user detail API To check updated usage and prompt
        httpGet = createHttpGet(true, "mobilekunji", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012346" // callId
        );

        expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                0L, // currentUsageInPulses=updated
                0L, // endOfUsagePromptCounter=updated
                false, // welcomePromptFlag
                150, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        response = SimpleHttpClient.httpRequestAndResponse(httpGet,
                ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that current usage pulses is resetted after the end of month.
     */
    @Test
    public void verifyFT331() throws IOException, InterruptedException {
        rh.newDelhiDistrict();
        rh.delhiCircle();
        deployedServiceDataService.create(new DeployedService(rh.delhiState(),
                Service.MOBILE_KUNJI));

        // National Capping
        ServiceUsageCap stateUsageCap = new ServiceUsageCap(null,
                Service.MOBILE_KUNJI, 150);
        serviceUsageCapDataService.create(stateUsageCap);

        // FLW usage
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(1111111111l);
        cdr.setService(Service.MOBILE_KUNJI);
        cdr.setCallDurationInPulses(80);
        cdr.setEndOfUsagePromptCounter(1);
        cdr.setWelcomePrompt(true);
        cdr.setCallStartTime(DateTime.now().withDayOfMonth(1).withTimeAtStartOfDay());
        callDetailRecordDataService.create(cdr);

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobilekunji", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                80L, // currentUsageInPulses
                1L, // endOfUsagePromptCounter
                true, // welcomePromptFlag
                150, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));

        // Update FLW usage to previous month last day time such that it is resetted now

        cdr.setCallStartTime(DateTime.now().withDayOfMonth(1).withTimeAtStartOfDay().minusMinutes(1));
        callDetailRecordDataService.update(cdr);

        // invoke get user detail API To check updated usage and prompt
        httpGet = createHttpGet(true, "mobilekunji", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012346" // callId
        );

        expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                0L, // currentUsageInPulses=updated
                0L, // endOfUsagePromptCounter=updated
                false, // welcomePromptFlag
                150, // maxAllowedUsageInPulses
                2 // maxAllowedEndOfUsagePrompt
        );

        response = SimpleHttpClient.httpRequestAndResponse(httpGet,
                ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that current usage pulses is resetted after the end of month.
     * For state capping.
     */
    @Test
    public void verifyFT534() throws IOException, InterruptedException {
        rh.newDelhiDistrict();
        rh.delhiCircle();
        deployedServiceDataService.create(new DeployedService(rh.delhiState(),
                Service.MOBILE_ACADEMY));

        // national capping
        ServiceUsageCap nationalUsageCap = new ServiceUsageCap(null,
                Service.MOBILE_ACADEMY, 500);
        serviceUsageCapDataService.create(nationalUsageCap);

        // State capping
        ServiceUsageCap stateUsageCap = new ServiceUsageCap(rh.delhiState(),
                Service.MOBILE_ACADEMY, 250);
        serviceUsageCapDataService.create(stateUsageCap);

        // FLW usage
        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright",
                1200000000l);
        flw.setLanguage(rh.hindiLanguage());
        frontLineWorkerService.add(flw);

        CallDetailRecord cdr = new CallDetailRecord();
        cdr.setFrontLineWorker(flw);
        cdr.setCallingNumber(1111111111l);
        cdr.setService(Service.MOBILE_ACADEMY);
        cdr.setCallDurationInPulses(80);
        cdr.setEndOfUsagePromptCounter(1);
        cdr.setWelcomePrompt(false);
        cdr.setCallStartTime(DateTime.now().withDayOfMonth(1)
                .withTimeAtStartOfDay());
        callDetailRecordDataService.create(cdr);

        // invoke get user detail API
        HttpGet httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012345" // callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                80L, // currentUsageInPulses
                1L, // endOfUsagePromptCounter
                false, // welcomePromptFlag
                250, // maxAllowedUsageInPulses=State capping
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));

        // Update FLW usage to previous month last day time such that it is
        // resetted now

        cdr.setCallStartTime(DateTime.now().withDayOfMonth(1)
                .withTimeAtStartOfDay().minusMinutes(1));
        callDetailRecordDataService.update(cdr);

        // invoke get user detail API To check updated usage and prompt
        httpGet = createHttpGet(true, "mobileacademy", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                false, null,// circle
                true, "123456789012346" // callId
        );

        expectedJsonResponse = createFlwUserResponseJson(null, // defaultLanguageLocationCode
                rh.hindiLanguage().getCode(), // locationCode
                null, // allowedLanguageLocationCodes
                0L, // currentUsageInPulses=updated
                0L, // endOfUsagePromptCounter=updated
                false, // welcomePromptFlag
                250, // maxAllowedUsageInPulses=State capping
                2 // maxAllowedEndOfUsagePrompt
        );

        response = SimpleHttpClient.httpRequestAndResponse(httpGet,
                ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));
    }

    /**
     * To verify that status of flw must be set to "Anonymous" when user call first time
     * and its information does not exist in NMS DB.
     */
    @Test
    public void verifyFT511() throws IOException, InterruptedException {
        rh.newDelhiDistrict();
        rh.delhiCircle();
        deployedServiceDataService.create(new DeployedService(rh.delhiState(),
                Service.MOBILE_KUNJI));

        // invoke get user detail API To check updated usage and prompt
        HttpGet httpGet = createHttpGet(true, "mobilekunji", // service
                true, "1200000000", // callingNumber
                false, null, // operator
                true, rh.delhiCircle().getName(),// circle
                true, "123456789012346" // callId
        );
        List<String> allowedLLCCodes = new ArrayList<>();
        allowedLLCCodes.add(rh.hindiLanguage().getCode());

        String expectedJsonResponse = createFlwUserResponseJson(rh.hindiLanguage().getCode(), // defaultLanguageLocationCode
                null, // locationCode
                allowedLLCCodes, // allowedLanguageLocationCodes
                0L, // currentUsageInPulses=updated
                0L, // endOfUsagePromptCounter=updated
                false, // welcomePromptFlag
                -1, // maxAllowedUsageInPulses=State capping
                2 // maxAllowedEndOfUsagePrompt
        );

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet,
                ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse,
                EntityUtils.toString(response.getEntity()));

        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(1200000000L, 123456789012346L,
                rh.hindiLanguage().getCode()));

        response = SimpleHttpClient.httpRequestAndResponse(httpPost, ADMIN_USERNAME, ADMIN_PASSWORD);

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(1200000000l);
        assertEquals(FrontLineWorkerStatus.ANONYMOUS, flw.getStatus());
    }

}