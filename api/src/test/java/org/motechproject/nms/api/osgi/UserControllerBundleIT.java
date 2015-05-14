package org.motechproject.nms.api.osgi;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.api.web.contract.BadRequest;
import org.motechproject.nms.api.web.contract.FlwUserResponse;
import org.motechproject.nms.api.web.contract.UserLanguageRequest;
import org.motechproject.nms.api.web.contract.kilkari.KilkariUserResponse;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.Service;
import org.motechproject.nms.flw.domain.ServiceUsage;
import org.motechproject.nms.flw.domain.ServiceUsageCap;
import org.motechproject.nms.flw.domain.WhitelistEntry;
import org.motechproject.nms.flw.domain.WhitelistState;
import org.motechproject.nms.flw.repository.CallDetailRecordDataService;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.repository.ServiceUsageDataService;
import org.motechproject.nms.flw.repository.WhitelistEntryDataService;
import org.motechproject.nms.flw.repository.WhitelistStateDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;


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
    private SubscriptionService subscriptionService;

    @Inject
    private SubscriberDataService subscriberDataService;

    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;

    @Inject
    private SubscriptionDataService subscriptionDataService;

    @Inject
    private FrontLineWorkerService frontLineWorkerService;

    @Inject
    private FrontLineWorkerDataService frontLineWorkerDataService;

    @Inject
    private ServiceUsageDataService serviceUsageDataService;

    @Inject
    private ServiceUsageCapDataService serviceUsageCapDataService;

    @Inject
    private LanguageDataService languageDataService;

    @Inject
    private LanguageLocationDataService languageLocationDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private WhitelistEntryDataService whitelistEntryDataService;

    @Inject
    private WhitelistStateDataService whitelistStateDataService;

    @Inject
    private CallDetailRecordDataService callDetailRecordDataService;

    @Inject
    private CircleDataService circleDataService;

    @Inject
    private DistrictDataService districtDataService;

    public UserControllerBundleIT() {
        System.setProperty("org.motechproject.testing.osgi.http.numTries", "1");
    }

    // TODO: Clean up data creation and cleanup
    private void cleanAllData() {
        whitelistEntryDataService.deleteAll();
        whitelistStateDataService.deleteAll();
        serviceUsageCapDataService.deleteAll();
        serviceUsageDataService.deleteAll();
        callDetailRecordDataService.deleteAll();
        frontLineWorkerDataService.deleteAll();
        subscriptionDataService.deleteAll();
        subscriptionPackDataService.deleteAll();
        subscriberDataService.deleteAll();
        languageLocationDataService.deleteAll();
        languageDataService.deleteAll();
        districtDataService.deleteAll();
        stateDataService.deleteAll();
        circleDataService.deleteAll();
    }

    /*
    Creates two subscription packs ('pack1' and 'pack2')
    Create two subscribers:
        Subscriber 1000000000L is subscribed to pack 'pack1'
        Subscriber 2000000000L is subscribed to packs 'pack1' and 'pack2'
     */
    private void createKilkariTestData() {
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

        Language ta = languageDataService.create(new Language("tamil"));

        Circle circle = new Circle("AA");

        LanguageLocation languageLocation = new LanguageLocation("50", circle, ta, true);
        languageLocation.getDistrictSet().add(district);
        languageLocationDataService.create(languageLocation);

        SubscriptionPack pack1 = subscriptionPackDataService.create(new SubscriptionPack("pack1",
                SubscriptionPackType.CHILD, 48, 1, null));
        SubscriptionPack pack2 = subscriptionPackDataService.create(new SubscriptionPack("pack2",
                SubscriptionPackType.PREGNANCY, 72, 2, null));
        List<SubscriptionPack> onePack = Arrays.asList(pack1);
        List<SubscriptionPack> twoPacks = Arrays.asList(pack1, pack2);

        Subscriber subscriber1 = subscriberDataService.create(new Subscriber(1000000000L, languageLocation));
        Subscriber subscriber2 = subscriberDataService.create(new Subscriber(2000000000L, languageLocation));
        Subscriber subscriber3 = subscriberDataService.create(new Subscriber(3000000000L));

        subscriptionService.createSubscription(subscriber1.getCallingNumber(), languageLocation, pack1,
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), languageLocation, pack1,
                SubscriptionOrigin.IVR);
        subscriptionService.createSubscription(subscriber2.getCallingNumber(), languageLocation, pack2,
                SubscriptionOrigin.IVR);
    }

    private void createFlwCappedServiceNoUsageNoLocationNoLanguage() {
        cleanAllData();

        FrontLineWorker flw = new FrontLineWorker("Frank Lloyd Wright", 1111111111L);
        frontLineWorkerService.add(flw);

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);

        stateDataService.create(state);

        Language language = new Language("Papiamento");
        languageDataService.create(language);

        LanguageLocation languageLocation = new LanguageLocation("99", new Circle("AA"), language, true);
        languageLocation.getDistrictSet().add(district);
        languageLocationDataService.create(languageLocation);

        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(null, Service.MOBILE_KUNJI, 3600);
        serviceUsageCapDataService.create(serviceUsageCap);
    }

    private void createFlwWithLanguageServiceUsageAndCappedService() {
        cleanAllData();

        Language language = new Language("English");
        languageDataService.create(language);

        Circle circle = new Circle("AA");

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);

        District district2 = new District();
        district2.setName("District 2");
        district2.setRegionalName("District 2");
        district2.setCode(2L);

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);
        state.getDistricts().add(district2);

        stateDataService.create(state);

        LanguageLocation languageLocation = new LanguageLocation("10", circle, language, false);
        languageLocation.getDistrictSet().add(district);
        languageLocationDataService.create(languageLocation);

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguageLocation(languageLocation);
        frontLineWorkerService.add(flw);

        language = new Language("Papiamento");
        languageDataService.create(language);

        languageLocation = new LanguageLocation("99", circle, language, true);
        languageLocation.getDistrictSet().add(district2);
        languageLocationDataService.create(languageLocation);

        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(null, Service.MOBILE_KUNJI, 3600);
        serviceUsageCapDataService.create(serviceUsageCap);

        // A service record without endOfService and WelcomePrompt played
        ServiceUsage serviceUsage = new ServiceUsage(flw, Service.MOBILE_KUNJI, 1, 0, 0, DateTime.now());
        serviceUsageDataService.create(serviceUsage);
    }

    private void createFlwWithLanguageFullServiceUsageAndCappedService() {
        cleanAllData();

        Circle circle = new Circle("AA");

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);

        District district2 = new District();
        district2.setName("District 2");
        district2.setRegionalName("District 2");
        district2.setCode(2L);

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);
        state.getDistricts().add(district2);

        stateDataService.create(state);

        Language language = new Language("English");
        languageDataService.create(language);

        LanguageLocation languageLocation = new LanguageLocation("10", circle, language, false);
        languageLocation.getDistrictSet().add(district);
        languageLocationDataService.create(languageLocation);

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguageLocation(languageLocation);
        frontLineWorkerService.add(flw);

        language = new Language("Papiamento");
        languageDataService.create(language);

        languageLocation = new LanguageLocation("99", circle, language, true);
        languageLocation.getDistrictSet().add(district2);
        languageLocationDataService.create(languageLocation);

        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(null, Service.MOBILE_KUNJI, 3600);
        serviceUsageCapDataService.create(serviceUsageCap);

        ServiceUsage serviceUsage = new ServiceUsage(flw, Service.MOBILE_KUNJI, 1, 1, 1, DateTime.now());
        serviceUsageDataService.create(serviceUsage);
    }

    private void createFlwWithLanguageFullUsageOfBothServiceUncapped() {
        cleanAllData();

        Circle circle = new Circle("AA");

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);

        District district2 = new District();
        district2.setName("District 2");
        district2.setRegionalName("District 2");
        district2.setCode(2L);

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);
        state.getDistricts().add(district2);

        stateDataService.create(state);

        Language language = new Language("English");
        languageDataService.create(language);

        LanguageLocation languageLocation = new LanguageLocation("10", circle, language, false);
        languageLocation.getDistrictSet().add(district);
        languageLocationDataService.create(languageLocation);

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111L);
        flw.setLanguageLocation(languageLocation);
        frontLineWorkerService.add(flw);

        language = new Language("Papiamento");
        languageDataService.create(language);

        languageLocation = new LanguageLocation("99", circle, language, true);
        languageLocation.getDistrictSet().add(district2);
        languageLocationDataService.create(languageLocation);

        ServiceUsage serviceUsage = new ServiceUsage(flw, Service.MOBILE_KUNJI, 1, 1, 1, DateTime.now());
        serviceUsageDataService.create(serviceUsage);

        // Academy doesn't have a welcome prompt
        serviceUsage = new ServiceUsage(flw, Service.MOBILE_ACADEMY, 1, 1, 0, DateTime.now());
        serviceUsageDataService.create(serviceUsage);

        ServiceUsageCap serviceUsageCap = new ServiceUsageCap(null, Service.MOBILE_KUNJI, 10);
        serviceUsageCapDataService.create(serviceUsageCap);
    }

    private void createFlwWithStateNotInWhitelist() {
        cleanAllData();

        circleDataService.create(new Circle("AA"));

        District district = new District();
        district.setName("9");
        district.setRegionalName("9");
        district.setCode(9l);

        State whitelistState = new State("WhitelistState", 1l);
        whitelistState.getDistricts().add(district);
        stateDataService.create(whitelistState);

        whitelistStateDataService.create(new WhitelistState(whitelistState));

        WhitelistEntry entry = new WhitelistEntry(0000000000l, whitelistState);
        whitelistEntryDataService.create(entry);

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111l);
        flw.setDistrict(district);
        frontLineWorkerService.add(flw);
    }

    private void createFlwWithLanguageLocationCodeNotInWhitelist() {
        cleanAllData();

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);

        State whitelist = new State();
        whitelist.setName("Whitelist");
        whitelist.setCode(1L);
        whitelist.getDistricts().add(district);

        stateDataService.create(whitelist);

        // Currently the code to get a state from a languageLocationCode is stubbed out.
        // llc 34 returns the state "Whitelist".  There is a todo tracking this.
        Language language = new Language("Language From Whitelisted State");
        languageDataService.create(language);

        LanguageLocation languageLocation = new LanguageLocation("34", new Circle("AA"), language, false);
        languageLocation.getDistrictSet().add(district);
        languageLocationDataService.create(languageLocation);

        whitelistStateDataService.create(new WhitelistState(whitelist));

        WhitelistEntry entry = new WhitelistEntry(0000000000l, whitelist);
        whitelistEntryDataService.create(entry);

        FrontLineWorker flw = new FrontLineWorker("Frank Llyod Wright", 1111111111l);
        flw.setLanguageLocation(languageLocation);
        frontLineWorkerService.add(flw);
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

        Language language = new Language("Papiamento");
        languageDataService.create(language);

        LanguageLocation languageLocation = new LanguageLocation("99", new Circle("AA"), language, true);
        languageLocation.getDistrictSet().add(district);
        languageLocationDataService.create(languageLocation);
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
                                                 Set<String> subscriptionPackList) throws IOException {
        KilkariUserResponse kilkariUserResponse = new KilkariUserResponse();
        if (defaultLanguageLocationCode != null) {
            kilkariUserResponse.setDefaultLanguageLocationCode(defaultLanguageLocationCode);
        }
        if (locationCode != null) {
            kilkariUserResponse.setLanguageLocationCode(locationCode);
        }
        if (subscriptionPackList != null) {
            kilkariUserResponse.setSubscriptionPackList(subscriptionPackList);
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(kilkariUserResponse);
    }

    private String createFlwUserResponseJson(String defaultLanguageLocationCode, String locationCode,
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


    @Test
    public void testKilkariUserRequestNoLanguage() throws IOException, InterruptedException {
        createKilkariTestData();

        HttpGet httpGet = createHttpGet(
                true, "kilkari",        //service
                true, "3000000000",     //callingNumber
                true, "OP",             //operator
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createKilkariUserResponseJson(
                "50", //defaultLanguageLocationCode
                null, //locationCode
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
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createKilkariUserResponseJson(
                "50", //defaultLanguageLocationCode
                null, //locationCode
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
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(
                "99",  //defaultLanguageLocationCode
                "10",  //locationCode
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
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(
                "99",  //defaultLanguageLocationCode
                "10",  //locationCode
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
        HttpGet httpGet = createHttpGet(
                true, "kilkari",        //service
                true, "1111111111",     //callingNumber
                false, null,            //operator
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFailureResponseJson("<operator: Not Present>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertEquals(expectedJsonResponse, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testNoCircle() throws IOException, InterruptedException {
        HttpGet httpGet = createHttpGet(
                true, "kilkari",        //service
                true, "1111111111",     //callingNumber
                true, "OP",             //operator
                false, null,            //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFailureResponseJson("<circle: Not Present>");

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
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
                "99",  //defaultLanguageLocationCode
                null,  //locationCode
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
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(
                "99",  //defaultLanguageLocationCode
                "10",  //locationCode
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
                true, "AA",             //circle
                true, "123456789012345" //callId
        );

        String expectedJsonResponse = createFlwUserResponseJson(
                "99",  //defaultLanguageLocationCode
                "10",  //locationCode
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

        HttpPost httpPost = createHttpPost("mobilekunji", new UserLanguageRequest(1111111111L, 123456789012345L, "99"));

        assertTrue(SimpleHttpClient.execHttpRequest(httpPost));

        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(1111111111l);
        assertNotNull(flw);
        LanguageLocation languageLocation = flw.getLanguageLocation();
        assertNotNull(languageLocation);
        assertEquals("FLW Language Code", "99", languageLocation.getCode());
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
        LanguageLocation languageLocation = flw.getLanguageLocation();
        assertNotNull(languageLocation);
        assertEquals("FLW Language Code", "99", languageLocation.getCode());
    }
}
