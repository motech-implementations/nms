package org.motechproject.nms.testing.it.rch;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.event.MotechEvent;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.kilkari.domain.*;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.rch.utils.Constants;
import org.motechproject.nms.rch.service.RchWebServiceFacade;
import org.motechproject.nms.rch.service.RchWsImportService;
import org.motechproject.nms.region.domain.*;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;
import org.motechproject.nms.rejectionhandler.domain.FlwImportRejection;
import org.motechproject.nms.rejectionhandler.repository.ChildRejectionDataService;
import org.motechproject.nms.rejectionhandler.repository.FlwImportRejectionDataService;
import org.motechproject.nms.rejectionhandler.repository.MotherRejectionDataService;
import org.motechproject.nms.testing.it.helperUtils.HelperUtils;
import org.motechproject.nms.testing.it.rch.util.*;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.http.SimpleHttpServer;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.http.HttpService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static java.util.Collections.singletonList;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createDistrict;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createState;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.motechproject.nms.kilkari.repository.*;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;


@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class RchWebServiceFacadeBundleIT extends BasePaxIT {

    @Inject
    private HttpService httpService;

    @Inject
    private RchWebServiceFacade rchWebServiceFacade;

    @Inject
    private RchWsImportService rchWsImportService;

    @Inject
    private MotechSchedulerService schedulerService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;

    @Inject
    private TestingService testingService;

    @Inject
    private SettingsService settingsService;

    @Inject
    private MotherRejectionDataService motherRejectionDataService;

    @Inject
    private ChildRejectionDataService childRejectionDataService;

    @Inject
    private MctsMotherDataService mctsMotherDataService;

    @Inject
    private FlwImportRejectionDataService flwImportRejectionDataService;

    @Inject
    private FrontLineWorkerDataService frontLineWorkerDataService;

    @Inject
    SubscriberDataService subscriberDataService;

    @Inject
    SubscriptionService subscriptionService;

    @Inject
    SubscriptionDataService subscriptionDataService;

    @Inject
    PlatformTransactionManager transactionManager;

    @Inject
    SubscriberService subscriberService;

    @Inject
    SubscriberMsisdnTrackerDataService subscriberMsisdnTrackerDataService;


    @Before
    public void setUp() throws ServletException, NamespaceException {
        testingService.clearDatabase();
        State state = stateDataService.create(new State("My State", 21L));

        District district = new District();
        district.setCode(4L);
        district.setState(state);
        district.setName("District_Name 4");
        district.setRegionalName("Regional Name 4");

        Taluka taluka = new Taluka();
        taluka.setRegionalName("Taluka Regional");
        taluka.setName("Taluka_Name 1");
        taluka.setCode("0046");
        taluka.setIdentity(55);

        taluka.setDistrict(district);
        district.setTalukas(new ArrayList<>(singletonList(taluka)));

        HealthBlock healthBlock = new HealthBlock();
        healthBlock.setCode(113L);
        healthBlock.setName("HealthBlock_Name 1");
        healthBlock.setRegionalName("HB1");
        healthBlock.setHq("An HQ");
        healthBlock.setDistrict(district);
        //TODO HARITHA commented 2 lines m-n taluka hb
//        healthBlock.addTaluka(taluka);
//
//        taluka.addHealthBlock(healthBlock);

        HealthFacilityType phcType = new HealthFacilityType();
        phcType.setCode(11L);
        phcType.setName("PHC TYPE 111");

        HealthFacility facility = new HealthFacility();
        facility.setName("PHC_NAME 3");
        facility.setRegionalName("Regional PHC 3");
        facility.setCode(111L);
        facility.setHealthFacilityType(phcType);

        healthBlock.setHealthFacilities(new ArrayList<>(singletonList(facility)));
        facility.setHealthBlock(healthBlock);

        HealthSubFacility subcentre = new HealthSubFacility();
        subcentre.setName("SubCentre_Name 1");
        subcentre.setRegionalName("Regional sub name");
        subcentre.setCode(333L);

        subcentre.setHealthFacility(facility);
        facility.setHealthSubFacilities(new ArrayList<>(singletonList(subcentre)));

        districtDataService.create(district);

        SubscriptionPack pregnancyPack = new SubscriptionPack("prg", SubscriptionPackType.PREGNANCY, 70, 10,
                Collections.<SubscriptionPackMessage>emptyList());
        SubscriptionPack childPack = new SubscriptionPack("child", SubscriptionPackType.CHILD, 5000, 6,
                Collections.<SubscriptionPackMessage>emptyList());


        subscriptionPackDataService.create(pregnancyPack);
        subscriptionPackDataService.create(childPack);

    }

    public void SetupImportNewChild() throws IOException {
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_StateID_21_Child_Import_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);

        MotechEvent event = new MotechEvent(Constants.RCH_CHILD_READ, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
    }

    public void SetupImportNewChild2() throws IOException {
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_StateID_21_Child_Import_2_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);

        MotechEvent event = new MotechEvent(Constants.RCH_CHILD_READ, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
    }

    @Test
    @Ignore
    public void shouldSerializeMothersDataFromSoapResponse() throws IOException {
        String response = RchImportTestHelper.getRchMothersResponseData();

        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);

        URL endpoint = new URL(url);
        LocalDate referenceDate = LocalDate.now().minusDays(1);

        //TODO resolve problem with class loader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWebServiceFacade.getClass().getClassLoader());
        boolean status = rchWebServiceFacade.getMothersData(referenceDate, referenceDate, endpoint, 21l);
        Thread.currentThread().setContextClassLoader(cl);

        assertTrue(status);
    }

    @Test
    @Ignore
    public void shouldSerializeChildrenDataFromSoapResponse() throws IOException {
        String response = RchImportTestHelper.getRchChildrenResponseData();

        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);

        URL endpoint = new URL(url);
        LocalDate referenceDate = LocalDate.now().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWebServiceFacade.getClass().getClassLoader());
        boolean status = rchWebServiceFacade.getChildrenData(referenceDate, referenceDate, endpoint, 21l);
        Thread.currentThread().setContextClassLoader(cl);

        assertTrue(status);
    }

    @Test
    @Ignore
    public void shouldSerializeAshaDataFromSoapResponse() throws IOException {
        String response = RchImportTestHelper.getAnmAshaResponseData();

        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);

        URL endpoint = new URL(url);
        LocalDate referenceDate = LocalDate.now().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWebServiceFacade.getClass().getClassLoader());
        boolean status = rchWebServiceFacade.getAnmAshaData(referenceDate, referenceDate, endpoint, 21l);
        Thread.currentThread().setContextClassLoader(cl);

        assertTrue(status);
    }

    @Test
    public void checkForZeroMother() throws IOException {
        String response = RchImportTestHelper.getRchChildrenResponseDataForZeroMother();
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);

        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(7);
        LocalDate yesterday = DateUtil.today().minusDays(1);

        // this CL workaround is for an issue with PAX IT logging messing things up
        // shouldn't affect production
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());

        // setup motech event
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        List<Long> a = new ArrayList<>();
        a.add(21L);
        MotechEvent event = new MotechEvent("foobar", params);
        rchWsImportService.importFromRch(a, yesterday, endpoint);
//        rchWsImportService.importRchMothersData(event);
        Thread.currentThread().setContextClassLoader(rchWebServiceFacade.getClass().getClassLoader());

        MotechEvent event1 = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_CHILD_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread.currentThread().setContextClassLoader(cl);

//        Should reject non ASHA FLWs

        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        assertEquals(0, childImportRejections.size());
        List<MctsMother> mothers = HelperUtils.retrieveAllMothers(mctsMotherDataService);
        assertEquals(0, mothers.size());
    }

    @Test
    public void testChildDeactivatedWithDeath() throws IOException {
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_StateID_21_Child_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);
        List<Long> a = new ArrayList<>();
        a.add(21L);
        // MotechEvent event = new MotechEvent("foobar", params);
        rchWsImportService.importChildFromRch(a, yesterday, endpoint);
        MotechEvent event1 = new MotechEvent(Constants.RCH_CHILD_READ, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<ChildImportRejection> childImportRejectionList = childRejectionDataService.retrieveAll();
        assertEquals(1, childImportRejectionList.size());

    }

    @Test
    public void testAshaRCHImport() throws IOException{
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName =  "rch-anm-asha-data.xml"; //done by vishnu
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("ashaendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);
        List<Long> a = new ArrayList<>();
        a.add(21L);
        // MotechEvent event = new MotechEvent("foobar", params);
        rchWsImportService.importAshaFromRch(a, yesterday, endpoint);
        MotechEvent event1 = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_ASHA_READ, params);
        try {
            rchWebServiceFacade.readAshaResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<FlwImportRejection> flwImportRejectionList = flwImportRejectionDataService.retrieveAll();
        assertEquals(1, flwImportRejectionList.size());
        List<FrontLineWorker> frontLineWorkers = frontLineWorkerDataService.retrieveAll();
        assertEquals(2, frontLineWorkers.size());
    }

    /*
     * To verify import new child subscriber via xml with MotherID
     */
    @Test
    public void testChildRCHImportNewSubscriberViaXml() throws IOException {
        SetupImportNewChild();
        List<Subscription> subscriptions = subscriptionDataService.retrieveAll();
        Assert.assertEquals(1, subscriptions.size());
        Assert.assertEquals(SubscriptionPackType.CHILD, subscriptions.get(0).getSubscriptionPack().getType());
    }

    /*
     * To verify import child via xml with no MotherID
     */
    @Test
    public void testImportChildNewSubscriberNoMotherIdViaXml() throws IOException {
        SetupImportNewChild2();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscriber = subscriberService.getSubscriber(8206996122L);
        assertEquals(1, subscriber.size());
        Set<Subscription> subscriptions = subscriber.get(0).getActiveAndPendingSubscriptions();
        assertEquals(1, subscriptions.size());
        transactionManager.commit(status);
    }

    /*
     * To verify import child via xml with same MSISDN and matching MotherID
     */
    @Test
    public void testImportMotherAndChildSameMsisdnViaXml() throws IOException {
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_StateID_21_Mother_Import_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);

        MotechEvent event = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscriber = subscriberService.getSubscriber(9876543212L);
        assertEquals(1, subscriber.size());
        Set<Subscription> subscriptions = subscriber.get(0).getActiveAndPendingSubscriptions();
        assertEquals(1, subscriptions.size());

        List<Subscription> subscriptions1 = subscriptionDataService.retrieveAll();
        Assert.assertEquals(SubscriptionStatus.ACTIVE, subscriptions1.get(0).getStatus());
        transactionManager.commit(status);

        //Import child xml file with same msisdn as mother

        SetupImportNewChild();
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9876543212L);
        assertEquals(1, subscriber.size());
        Subscription childSubscription = subscriptionService
                .getActiveSubscription(subscriber.get(0), SubscriptionPackType.CHILD);
        Subscription pregnancySubscription = subscriptionService
                .getActiveSubscription(subscriber.get(0), SubscriptionPackType.PREGNANCY);
        assertEquals(1, subscriptions.size());

        // the pregnancy subscription should have been deactivated
        assertEquals(1, subscriptions.size());
        assertNotNull(childSubscription);
        assertNull(pregnancySubscription);
        transactionManager.commit(status);
    }

    /*
     * To verify child subscriber is rejected when future DOB is provided.
     */
    @Test
    public void verifyChildImportWithFutureDOBViaXml() throws Exception {
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_StateID_21_Child_Import_Future_DOB_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);

        MotechEvent event = new MotechEvent(Constants.RCH_CHILD_READ, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        Assert.assertEquals(1, childImportRejections.size());
        Assert.assertEquals("8206996121", childImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.INVALID_DOB.toString(), childImportRejections.get(0).getRejectionReason());
        Assert.assertEquals("225893460704", childImportRejections.get(0).getRegistrationNo());

    }

    /*
     * To verify child subscription is rejected when DOB provided is 48 weeks back.
     */
    @Test
    public void verifyChildImportWithOldDOBViaXml() throws Exception {
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_StateID_21_Child_Import_Old_DOB_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);

        MotechEvent event = new MotechEvent(Constants.RCH_CHILD_READ, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        Assert.assertEquals(1, childImportRejections.size());
        Assert.assertEquals("8206996121", childImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.INVALID_DOB.toString(), childImportRejections.get(0).getRejectionReason());
        Assert.assertEquals("225893460704", childImportRejections.get(0).getRegistrationNo());
    }

    /*
     * To verify child subscription is deactivated when
     * entry type given as '9'.
     */
    @Test
    public void testDeactivateChildSubscriptionDueToDeathViaXml() throws Exception {
        // import Child
        SetupImportNewChild();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9876543212L).get(0);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        transactionManager.commit(status);

        // import record for same child with Entry_Type set to 9 -- her subscription should be deactivated
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName1 = "RCH_StateID_21_Child_Death_9_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName1);

        MotechEvent event = new MotechEvent(Constants.RCH_CHILD_READ, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9876543212L).get(0);
        subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.CHILD_DEATH, subscription.getDeactivationReason());
        transactionManager.commit(status);
    }

    /*
     * To verify RCH upload is rejected when MSISDN number already exist
     * for subscriber with new rch id.
     */
    @Test
    public void testChildImportSameMsisdnViaXml() throws Exception {
        // import Child
        SetupImportNewChild();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9876543212L).get(0);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        transactionManager.commit(status);

        // attempt to create subscriber with same msisdn but different rch id.
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_Child_Same_Msisdn_Diff_RchId_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);

        MotechEvent event = new MotechEvent(Constants.RCH_CHILD_READ, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);

        //second subscriber should have been rejected
        List<Subscriber> subscribersByMsisdn = subscriberService.getSubscriber(9876543212L);
        assertEquals(1, subscribersByMsisdn.size());
        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        Assert.assertEquals(1, childImportRejections.size());
        Assert.assertEquals("9876543212", childImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.MOBILE_NUMBER_ALREADY_SUBSCRIBED.toString(), childImportRejections.get(0).getRejectionReason());
        Assert.assertEquals("245893460721", childImportRejections.get(0).getRegistrationNo());
    }

    /*
     * To verify RCH upload is rejected when data doesn’t contain DOB.
     */
    @Test
    public void testChildImportWithoutDOBViaXml() throws Exception {
        //DOB is missing
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_StateID_21_Child_DOB_missing_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);

        MotechEvent event = new MotechEvent(Constants.RCH_CHILD_READ, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);

        //subscriber should not be created and rejected entry should be in nms_child_rejects with reason 'MISSING_DOB'.
        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        Assert.assertEquals(1, childImportRejections.size());
        Assert.assertEquals("9856852245", childImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.INVALID_DOB.toString(), childImportRejections.get(0).getRejectionReason());
        Assert.assertEquals("245893460722", childImportRejections.get(0).getRegistrationNo());
    }

    /*
     * To verify MSISDN is sucessfully updated for
     * for existing child subscriber.
     */
    @Test
    public void testUpdateMsisdnForChildRecordViaXml() throws Exception {
        // import Child
        SetupImportNewChild();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9876543212L).get(0);
        assertNotNull(subscriber);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        transactionManager.commit(status);

        //update msisdn of the existing child
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_StateID_21_Child_With_Diff_Msisdn_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);

        MotechEvent event = new MotechEvent(Constants.RCH_CHILD_READ, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<Subscriber> subscribersByMsisdn = subscriberService.getSubscriber(9856852178L);
        assertEquals(1, subscribersByMsisdn.size());
        assertNotNull(subscriber);
    }

    /*
     * To verify RCH upload is rejected when MSISDN number already exist
     * for subscriber with new rch id.
     */
    @Test
    public void verifyChildImportWithSameMsisdnDifferentStateViaXml() throws Exception {
        // import Child
        SetupImportNewChild();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9876543212L).get(0);
        assertNotNull(subscriber);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriptions.iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        transactionManager.commit(status);

        // attempt to create subscriber with same msisdn but different rch id.
        State state20 = createState(20L, "State 20");
        stateDataService.create(state20);
        District district = createDistrict(state20, 3L, "EXAMPLE DISTRICT");
        districtDataService.create(district);

        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_StateID_20_Child_With_Same_Msisdn_Diff_State_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 20L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);

        MotechEvent event = new MotechEvent(Constants.RCH_CHILD_READ, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);

        //second subscriber should have been rejected

        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        Assert.assertEquals(1, childImportRejections.size());
        Assert.assertEquals("9876543212", childImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.MOBILE_NUMBER_ALREADY_SUBSCRIBED.toString(), childImportRejections.get(0).getRejectionReason());
        Assert.assertEquals("245893460721", childImportRejections.get(0).getRegistrationNo());
    }

    /*
     * To verify DOB is changed successfully via XML when subscription
     * already exist for childPack having status as "Active"
     */
    @Test
    public void verifyChildDOBUpdateViaXml() throws Exception {
        // import Child
        SetupImportNewChild2();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(8206996122L).get(0);
        assertNotNull(subscriber);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        transactionManager.commit(status);

        // attempt to update dob through rch upload
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_StateID_21_Child_With_DOB_Update_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);

        MotechEvent event = new MotechEvent(Constants.RCH_CHILD_READ, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread.currentThread().setContextClassLoader(cl);
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscribersByMsisdn = subscriberService.getSubscriber(8206996122L);
        assertEquals(1, subscribersByMsisdn.size());
        assertNotNull(subscriber);
        transactionManager.commit(status);
    }

    /*
     * To verify when subscription
     * already exists for childPack having status as "Deactivated" received with DOB update is rejected.
     */
    @Test
    public void verifyD0bUpdateForDeactivatedChildViaXml() throws Exception {
        // import Child
        SetupImportNewChild2();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(8206996122L).get(0);
        assertNotNull(subscriber);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        //Mark subscription deactivate
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.STILL_BIRTH);
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        transactionManager.commit(status);

        //Update DOB for subscriber whose subscription is deactivated.
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_StateID_21_Child_With_DOB_Update_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);

        MotechEvent event = new MotechEvent(Constants.RCH_CHILD_READ, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        //Record should get rejected when existed record with status "Deactivated" comes with DOB update.
        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        Assert.assertEquals(1, childImportRejections.size());
        Assert.assertEquals("8206996122", childImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.UPDATED_RECORD_ALREADY_EXISTS.toString(), childImportRejections.get(0).getRejectionReason());
        Assert.assertEquals("245893460788", childImportRejections.get(0).getRegistrationNo());
        transactionManager.commit(status);
    }

    /*
     * To verify when DOB is changed successfully via xml when subscription
     * already exist for childPack having status as "Completed"
     */
    @Test
    public void verifyD0bUpdateForCompletedChildViaXml() throws Exception {
        // import Child
        DateTime dob = DateTime.now();
        SetupImportNewChild2();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(8206996122L).get(0);
        assertNotNull(subscriber);
        Set<Subscription> subscriptions = subscriber.getAllSubscriptions();
        assertEquals(1, subscriptions.size());

        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        //Make subscription completed
        subscriber.setDateOfBirth(dob.minusDays(35000));
        subscriberService.updateStartDate(subscriber);
        transactionManager.commit(status);
        assertEquals(SubscriptionStatus.COMPLETED, subscription.getStatus());
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        //Update DOB for subscriber whose subscription is completed.
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_StateID_21_Child_With_DOB_Update_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);

        MotechEvent event = new MotechEvent(Constants.RCH_CHILD_READ, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);

        subscriber = subscriberService.getSubscriber(8206996122L).get(0);
        Assert.assertEquals(2, subscriber.getAllSubscriptions().size());
        Assert.assertEquals(1, subscriber.getActiveAndPendingSubscriptions().size());
        transactionManager.commit(status);
    }

    /*
     * To verify that NMS shall deactivate pregancyPack if childPack uploads
     * for updation which contains motherId for an active mother beneficiary.
     */
    @Test
    public void testDeactivateMotherWhenChildUploadsViaXml() throws Exception {
        // import mother
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_StateID_21_Mother_Import_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);

        MotechEvent event = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9876543212L).get(0);
        assertNotNull(subscriber);
        Set<Subscription> subscriptions = subscriber.getActiveAndPendingSubscriptions();
        Assert.assertEquals(1, subscriptions.size());
        transactionManager.commit(status);

        // import child with same MSISDN and above MotherID --> child should be updated and mother be deactivated

        SetupImportNewChild();
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9876543212L).get(0);
        subscriptions = subscriber.getActiveAndPendingSubscriptions();
        Subscription childSubscription = subscriptionService.getActiveSubscription(subscriber, SubscriptionPackType.CHILD);
        Subscription pregnancySubscription = subscriptionService
                .getActiveSubscription(subscriber, SubscriptionPackType.PREGNANCY);


        //only child subscription should be activated
        Assert.assertEquals(1, subscriptions.size());
        assertNotNull(childSubscription);
        assertNull(pregnancySubscription);
        transactionManager.commit(status);
    }

    // Test SubscriberMsisdnTracker in Child Import
    @Test
    public void testChildMsisdnTrackerViaXml() throws Exception {
        SetupImportNewChild();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscribers = subscriberService.getSubscriber(9876543212L);
        Assert.assertEquals(1, subscribers.size());
        transactionManager.commit(status);

        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_Child_With_Same_Mother_Diff_Msisdn_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);

        MotechEvent event = new MotechEvent(Constants.RCH_CHILD_READ, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscribers = subscriberService.getSubscriber(9876543212L);
        Assert.assertEquals(0, subscribers.size());
        subscribers = subscriberService.getSubscriber(9856852235L);
        Assert.assertEquals(1, subscribers.size());

        List<SubscriberMsisdnTracker> msisdnTrackers = subscriberMsisdnTrackerDataService.retrieveAll();
        Assert.assertEquals(1, msisdnTrackers.size());
        SubscriberMsisdnTracker msisdnTracker = msisdnTrackers.get(0);
        Assert.assertEquals(9876543212L, msisdnTracker.getOldCallingNumber().longValue());
        Assert.assertEquals(9856852235L, msisdnTracker.getNewCallingNumber().longValue());
        transactionManager.commit(status);
    }

    @Test
    public void testCreateNewChildRecordDifferentMsisdnViaXml() throws Exception {
        SetupImportNewChild();

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9876543212L).get(0);
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();

        //deactivate child subscription due to death
        subscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscription.setEndDate(new DateTime().withDate(2016, 8, 1));
        subscriptionDataService.update(subscription);
        subscriptionService.purgeOldInvalidSubscriptions();
        transactionManager.commit(status);

        //import a new child record for the same mother with different msisdn

        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_Child_With_Same_Mother_Diff_Msisdn_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, "abcd");
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);

        MotechEvent event = new MotechEvent(Constants.RCH_CHILD_READ, params);
        try {
            rchWebServiceFacade.readChildResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscribers = subscriberService.getSubscriber(9856852235L);
        assertEquals(1, subscribers.size());
        transactionManager.commit(status);
    }
    }

