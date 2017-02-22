package org.motechproject.nms.testing.it.api;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.regexp.RE;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.mtraining.domain.ActivityState;
import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.repository.ActivityDataService;
import org.motechproject.mtraining.service.BookmarkService;
import org.motechproject.nms.api.web.contract.AddFlwRequest;
import org.motechproject.nms.flw.domain.FlwError;
import org.motechproject.nms.flw.domain.FlwErrorReason;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.repository.FlwErrorDataService;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.kilkari.domain.*;
import org.motechproject.nms.kilkari.repository.*;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.mobileacademy.domain.CompletionRecord;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.*;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.LanguageService;
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
import org.osgi.service.dmt.Uri;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for Ops controller
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class OpsControllerBundleIT extends BasePaxIT {

    private String addFlwEndpoint = String.format("http://localhost:%d/api/ops/createUpdateFlw",
            TestContext.getJettyPort());
    private String deactivationRequest = String.format("http://localhost:%d/api/ops/deactivationRequest",
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
    FrontLineWorkerDataService frontLineWorkerDataService;

    @Inject
    FlwErrorDataService flwErrorDataService;


    @Inject
    SubscriberService subscriberService;
    @Inject
    SubscriptionService subscriptionService;
    @Inject
    SubscriberDataService subscriberDataService;
    @Inject
    SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    SubscriptionDataService subscriptionDataService;
    @Inject
    LanguageDataService languageDataService;
    @Inject
    LanguageService languageService;
    @Inject
    CircleDataService circleDataService;
    @Inject
    StateDataService stateDataService;
    @Inject
    DistrictDataService districtDataService;
    @Inject
    DistrictService districtService;
    @Inject
    MctsMotherDataService mctsMotherDataService;
    @Inject
    BlockedMsisdnRecordDataService blockedMsisdnRecordDataService;
    @Inject
    DeactivationSubscriptionAuditRecordDataService deactivationSubscriptionAuditRecordDataService;
    @Inject
    ActivityDataService activityDataService;
    @Inject
    MobileAcademyService maService;
    @Inject
    CompletionRecordDataService completionRecordDataService;


    private RegionHelper rh;
    private SubscriptionHelper sh;
    private static final String VALID_CALL_ID = "1234567890123456789012345";

    @Inject
    BookmarkService bookmarkService;

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
        AddFlwRequest addFlwRequest = getAddRequestASHA();
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, addFlwRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    // Validating whether the type is ASHA or not
    @Test
    public void testASHAValidation() throws IOException, InterruptedException {
        AddFlwRequest addFlwRequest = getAddRequestANM();
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, addFlwRequest);
        assertFalse(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    // Create valid new flw
    @Test
    public void testCreateNewFlwTalukaVillage() throws IOException, InterruptedException {

        createFlwHelper("Chinkoo Devi", 9876543210L, "123");
        FrontLineWorker flw = frontLineWorkerDataService.findByContactNumber(9876543210L);
        assertNotNull(flw.getState());
        assertNotNull(flw.getDistrict());
        assertNull(flw.getTaluka());    // null since we don't create it by default in helper
        assertNull(flw.getVillage());   // null since we don't create it by default in helper

        AddFlwRequest addFlwRequest = getAddRequestASHA();
        addFlwRequest.setTalukaId(taluka.getCode());
        addFlwRequest.setVillageId(village.getVcode());
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, addFlwRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));

        // refetch and check that taluka and village are set
        flw = frontLineWorkerDataService.findByContactNumber(9876543210L);
        assertNotNull(flw.getState());
        assertNotNull(flw.getDistrict());
        assertNotNull(flw.getTaluka());
        assertNotNull(flw.getVillage());
    }

    // Flw test name update
    @Test
    public void testUpdateFlwName() throws IOException, InterruptedException {

        // create flw
        createFlwHelper("Kookoo Devi", 9876543210L, "123");

        AddFlwRequest updateRequest = getAddRequestASHA();
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, updateRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
        FrontLineWorker flw = frontLineWorkerDataService.findByContactNumber(9876543210L);
        assertNotEquals(flw.getName(), "Kookoo Devi");
        assertEquals(flw.getName(), updateRequest.getName());
    }

    // Flw update phone number
    @Test
    public void testUpdateFlwPhoneOpen() throws IOException, InterruptedException {

        // create flw
        createFlwHelper("Chinkoo Devi", 9876543210L, "123");

        AddFlwRequest updateRequest = getAddRequestASHA();
        updateRequest.setContactNumber(9876543211L);    // update
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, updateRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    // Test flw update to an existing used phone number by someone else
    @Test
    public void testUpdateFlwPhoneOccupied() throws IOException, InterruptedException {

        // create flw
        createFlwHelper("Chinkoo Devi", 9876543210L, "456");

        long before = flwErrorDataService.count();

        AddFlwRequest updateRequest = getAddRequestASHA();
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, updateRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));

        long after = flwErrorDataService.count();

        assertEquals("No new expected flw error created", before + 1, after);

        List<FlwError> flwErrors = flwErrorDataService.findByMctsId(
                updateRequest.getMctsFlwId(),
                updateRequest.getStateId(),
                updateRequest.getDistrictId());

        // since we clear the db before each test, safe to assume that we will only have 1 item in list
        assertEquals(flwErrors.get(0).getReason(), FlwErrorReason.PHONE_NUMBER_IN_USE);
    }

    // Test flw update to an existing used phone number by someone else
    @Test
    public void testUpdateFlwAnonymousMctsMerge() throws IOException, InterruptedException {

        // create flw with null mcts id
        createFlwHelper("Chinkoo Devi", 9876543210L, null);

        AddFlwRequest updateRequest = getAddRequestASHA();
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, updateRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));

        FrontLineWorker flwByNumber = frontLineWorkerDataService.findByContactNumber(9876543210L);
        assertEquals("Anonymous user was not merged", updateRequest.getMctsFlwId(), flwByNumber.getMctsFlwId());
    }


    @Test
    public void testUpdateNoState() throws IOException, InterruptedException {

        // create flw
        createFlwHelper("State Singh", 9876543210L, "123");

        long before = flwErrorDataService.count();

        AddFlwRequest updateRequest = getAddRequestASHA();
        updateRequest.setStateId(5L);    // 5 doesn't exist since setup only creates state '1'
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, updateRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));

        long after = flwErrorDataService.count();

        assertEquals("No new expected flw error created", before + 1, after);

        List<FlwError> flwErrors = flwErrorDataService.findByMctsId(
                updateRequest.getMctsFlwId(),
                updateRequest.getStateId(),
                updateRequest.getDistrictId());

        // since we clear the db before each test, safe to assume that we will only have 1 item in list
        assertEquals(flwErrors.get(0).getReason(), FlwErrorReason.INVALID_LOCATION_STATE);
    }

    @Test
    public void testUpdateNoDistrict() throws IOException, InterruptedException {

        // create flw
        createFlwHelper("District Singh", 9876543210L, "123");

        long before = flwErrorDataService.count();

        AddFlwRequest updateRequest = getAddRequestASHA();
        updateRequest.setDistrictId(5L);    // 5 doesn't exist since setup only creates district '1'
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, updateRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));

        long after = flwErrorDataService.count();

        assertEquals("No new expected flw error created", before + 1, after);

        List<FlwError> flwErrors = flwErrorDataService.findByMctsId(
                updateRequest.getMctsFlwId(),
                updateRequest.getStateId(),
                updateRequest.getDistrictId());

        // since we clear the db before each test, safe to assume that we will only have 1 item in list
        assertEquals(flwErrors.get(0).getReason(), FlwErrorReason.INVALID_LOCATION_DISTRICT);
    }

    @Test
    public void testUpdateNoTaluka() throws IOException, InterruptedException {

        // create flw
        createFlwHelper("Taluka Singh", 9876543210L, "123");

        AddFlwRequest updateRequest = getAddRequestASHA();
        updateRequest.setTalukaId("999");   // taluka 999 doesn't exist. this shouldn't be updated
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, updateRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));

        FrontLineWorker flw = frontLineWorkerDataService.findByContactNumber(9876543210L);
        assertNull("Taluka update rejected", flw.getTaluka());
    }

    @Test
    public void testGetScoresForUser() throws IOException, InterruptedException {
        Long callingNumber = 9876543210L;
        String getScoresEndpoint = String.format("http://localhost:%d/api/ops/getScores?callingNumber=%d",
                TestContext.getJettyPort(), callingNumber);
        Map<String, Integer> scores = new HashMap<>();
        scores.put("1", 4);
        scores.put("2", 3);
        Map<String, Object> progress = new HashMap<>();
        progress.put("scoresByChapter", scores);
        Bookmark newBookmark = new Bookmark(callingNumber.toString(), null, null, null, progress);
        bookmarkService.createBookmark(newBookmark);

        HttpGet httpGet = RequestBuilder.createGetRequest(getScoresEndpoint);
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertNotNull(response);
        assertEquals(200, response.getStatusLine().getStatusCode());
        String body = IOUtils.toString(response.getEntity().getContent());
        assertTrue(body.contains("1=4"));
        assertTrue(body.contains("2=3"));
    }

    @Test
    public void testGetScoresForUserNoScores() throws IOException, InterruptedException {
        Long callingNumber = 9876543210L;
        String getScoresEndpoint = String.format("http://localhost:%d/api/ops/getScores?callingNumber=%d",
                TestContext.getJettyPort(), 9976543210L);
        Map<String, Integer> scores = new HashMap<>();
        scores.put("1", 4);
        scores.put("2", 3);
        Map<String, Object> progress = new HashMap<>();
        progress.put("scoresByChapter", scores);
        Bookmark newBookmark = new Bookmark(callingNumber.toString(), null, null, null, progress);
        bookmarkService.createBookmark(newBookmark);

        HttpGet httpGet = RequestBuilder.createGetRequest(getScoresEndpoint);
        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(httpGet, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD);
        assertNotNull(response);
        assertEquals(200, response.getStatusLine().getStatusCode());
        String body = IOUtils.toString(response.getEntity().getContent());
        assertEquals("{000000}", body);
    }

    private void createFlwHelper(String name, Long phoneNumber, String mctsFlwId) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        stateDataService.create(state);
        // create flw
        FrontLineWorker flw = new FrontLineWorker(name, phoneNumber);
        flw.setMctsFlwId(mctsFlwId);
        flw.setState(state);
        flw.setDistrict(district);
        flw.setLanguage(language);
        frontLineWorkerDataService.create(flw);
        transactionManager.commit(status);
    }

    // helper to create a valid flw add/update request with designation ASHA
    private AddFlwRequest getAddRequestASHA() {
        AddFlwRequest request = new AddFlwRequest();
        request.setContactNumber(9876543210L);
        request.setName("Chinkoo Devi");
        request.setMctsFlwId("123");
        request.setStateId(state.getCode());
        request.setDistrictId(district.getCode());
        request.setType("ASHA");
        return request;
    }

    // helper to create an flw add/update request with designation ANM
    private AddFlwRequest getAddRequestANM() {
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

        transactionManager.commit(status);
    }

    // create subscriber with many subscriptions helper
    private void createSubscriberHelper() {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // create subscription for a msisdn
        rh = new RegionHelper(languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, districtService);
        sh = new SubscriptionHelper(subscriptionService, subscriberDataService, subscriptionPackDataService,
                languageDataService, languageService, circleDataService, stateDataService, districtDataService,
                districtService);

        Subscriber subscriberIVR = subscriberDataService.create(new Subscriber(5000000000L));
        subscriberIVR.setLastMenstrualPeriod(DateTime.now().plusWeeks(70));
        subscriberIVR = subscriberDataService.update(subscriberIVR);

       subscriptionService.createSubscription(subscriberIVR, subscriberIVR.getCallingNumber(), rh.kannadaLanguage(), rh.karnatakaCircle(),
                sh.pregnancyPack(), SubscriptionOrigin.IVR);

        Subscriber subscriberMCTS = subscriberDataService.create(new Subscriber(6000000000L));
        subscriberMCTS.setLastMenstrualPeriod(DateTime.now().plusWeeks(70));
        subscriberMCTS = subscriberDataService.update(subscriberMCTS);
        subscriptionService.createSubscription(subscriberMCTS, subscriberMCTS.getCallingNumber(), rh.kannadaLanguage(), rh.karnatakaCircle(),
                sh.pregnancyPack(), SubscriptionOrigin.MCTS_IMPORT);
        transactionManager.commit(status);
    }


    public void testifAllSubscriberDectivated() {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        Subscriber subscriberIVR = subscriberDataService.findByNumber(5000000000L).get(0);
        Set<Subscription> subscriptionsIVR = ( Set<Subscription> ) subscriberDataService.getDetachedField(subscriberIVR, "subscriptions");
        for (Subscription subscriptionIVR : subscriptionsIVR) {
            Assert.assertTrue(subscriptionIVR.getDeactivationReason().equals(DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED));
            Assert.assertTrue(subscriptionIVR.getStatus().equals(SubscriptionStatus.DEACTIVATED));
        }

        Subscriber subscriberMCTS = subscriberDataService.findByNumber(6000000000L).get(0);
        Set<Subscription> subscriptionsMCTS = ( Set<Subscription> ) subscriberDataService.getDetachedField(subscriberMCTS, "subscriptions");
        for (Subscription subscriptionMCTS : subscriptionsMCTS) {
            Assert.assertTrue(subscriptionMCTS.getDeactivationReason().equals(DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED) || subscriptionMCTS.getDeactivationReason().equals(DeactivationReason.LOW_LISTENERSHIP));
            Assert.assertTrue(subscriptionMCTS.getStatus().equals(SubscriptionStatus.DEACTIVATED));
        }
        transactionManager.commit(status);

    }


    private void testDeactivationRequestByMsisdn(Long msisdn, String deactivationReason, int status) throws IOException, InterruptedException, URISyntaxException {
        StringBuilder sb = new StringBuilder(deactivationRequest);
        sb.append("?");
        sb.append(String.format("msisdn=%s", msisdn.toString()));
        sb.append("&");
        sb.append(String.format("deactivationReason=%s", deactivationReason));
        HttpDelete httpRequest = new HttpDelete(sb.toString());
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, status, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));
    }

    // Test audit trail of deactivation subscriptions
    public void testDeactivationSubscriptionAuditService(Long msisdn, SubscriptionOrigin origin, int testNumber) {
        List<DeactivationSubscriptionAuditRecord> auditRecords = deactivationSubscriptionAuditRecordDataService.retrieveAll();
        assertEquals(testNumber, auditRecords.size());
        assertEquals(msisdn, auditRecords.get(testNumber-1).getMsisdn());
        assertEquals(origin, auditRecords.get(testNumber-1).getSubscriptionOrigin());
        assertEquals(AuditStatus.SUCCESS, auditRecords.get(testNumber-1).getAuditStatus());
    }

    //Test deactivation of specific msisdn - 5000000000L as IVR and 6000000000L as MCTS import
    @Test
    public void testDeactivateSpecificValidMsisdn() throws IOException, InterruptedException, URISyntaxException {
        createSubscriberHelper();
        testDeactivationRequestByMsisdn(5000000000L, "WEEKLY_CALLS_NOT_ANSWERED", HttpStatus.SC_OK);
        testDeactivationRequestByMsisdn(5000000000L, "WEEKLY_CALLS_NOT_ANSWERED", HttpStatus.SC_OK);   // Test deactivation of same number again
        testDeactivationSubscriptionAuditService(5000000000L, SubscriptionOrigin.IVR, 1);
        testDeactivationRequestByMsisdn(6000000000L, "LOW_LISTENERSHIP", HttpStatus.SC_OK);
        testDeactivationSubscriptionAuditService(6000000000L, SubscriptionOrigin.MCTS_IMPORT, 2);
        testifAllSubscriberDectivated();
        testReactivationDisabledAfterDeactivation(5000000000L);
        testReactivationDisabledAfterDeactivation(6000000000L);
    }

    @Test
    public void testDeactivateSpecificValidNotInDatabaseMsisdn() throws IOException, InterruptedException, URISyntaxException {
        testDeactivationRequestByMsisdn(7000000000L, "LOW_LISTENERSHIP", HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testDeactivateSpecificInValidMsisdn() throws IOException, InterruptedException, URISyntaxException {
        testDeactivationRequestByMsisdn(1000-00L, "LOW_LISTENERSHIP", HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testDeactivateSpecificInValidDeactivationReason() throws IOException, InterruptedException, URISyntaxException {
        testDeactivationRequestByMsisdn(5000000000L, "DEACTIVATED_BY_USER", HttpStatus.SC_BAD_REQUEST);
    }

    private void testReactivationDisabledAfterDeactivation(long msisdn) throws IOException, InterruptedException, URISyntaxException {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // Testing weekly_calls_not_answered record with the given number
        BlockedMsisdnRecord blockedMsisdnRecord = blockedMsisdnRecordDataService.findByNumber(msisdn);
        assertNotNull(blockedMsisdnRecord);

        Subscriber subscriber = subscriberDataService.findByNumber(msisdn).get(0);
        Subscription subscription = subscriptionService.createSubscription(subscriber, subscriber.getCallingNumber(), rh.kannadaLanguage(), rh.karnatakaCircle(),
                sh.pregnancyPack(), SubscriptionOrigin.IVR);
        Assert.assertNull(subscription);
        transactionManager.commit(status);
    }

    // Test whether MSISDN is updated in Bookmark, Activity and Course Completion Records along with Flw
    @Test
    public void testMaMsisdnUpdate() throws IOException, InterruptedException {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        stateDataService.create(state);
        transactionManager.commit(status);
        AddFlwRequest addFlwRequest = getAddRequestASHA();
        HttpPost httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, addFlwRequest);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));

        MaBookmark bookmark = new MaBookmark(9876543210L, VALID_CALL_ID, null, null);
        maService.setBookmark(bookmark);
        assertNotNull(maService.getBookmark(9876543210L, VALID_CALL_ID));
        assertEquals(1, activityDataService.findRecordsForUserByState("9876543210", ActivityState.STARTED).size());

        bookmark.setBookmark("COURSE_COMPLETED");
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 3);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);
        CompletionRecord cr = completionRecordDataService.findRecordByCallingNumber(9876543210L);
        assertNotNull(cr);

        // Update Msisdn and verify MA records
        AddFlwRequest request = new AddFlwRequest();
        request.setContactNumber(7896543210L);
        request.setName("Chinkoo Devi");
        request.setMctsFlwId("123");
        request.setStateId(state.getCode());
        request.setDistrictId(district.getCode());
        request.setType("ASHA");
        httpRequest = RequestBuilder.createPostRequest(addFlwEndpoint, request);
        assertTrue(SimpleHttpClient.execHttpRequest(httpRequest, HttpStatus.SC_OK, RequestBuilder.ADMIN_USERNAME, RequestBuilder.ADMIN_PASSWORD));

        assertNull(maService.getBookmark(9876543210L, VALID_CALL_ID));
        assertNotNull(maService.getBookmark(7896543210L, VALID_CALL_ID));

        assertEquals(0, activityDataService.findRecordsForUserByState("9876543210", ActivityState.STARTED).size());
        assertEquals(1, activityDataService.findRecordsForUserByState("7896543210", ActivityState.STARTED).size());

        assertNull(completionRecordDataService.findRecordByCallingNumber(9876543210L));
        assertNotNull(completionRecordDataService.findRecordByCallingNumber(7896543210L));
    }
}
