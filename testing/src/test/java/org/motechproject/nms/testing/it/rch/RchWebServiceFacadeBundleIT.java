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
import org.motechproject.nms.kilkari.repository.*;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.rch.utils.Constants;
import org.motechproject.nms.rch.service.RchWebServiceFacade;
import org.motechproject.nms.rch.service.RchWsImportService;
import org.motechproject.nms.region.domain.*;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;
import org.motechproject.nms.rejectionhandler.domain.FlwImportRejection;
import org.motechproject.nms.rejectionhandler.domain.MotherImportRejection;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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
    SubscriberService subscriberService;

    @Inject
    SubscriberDataService subscriberDataService;

    @Inject
    SubscriptionDataService subscriptionDataService;

    @Inject
    SubscriptionService subscriptionService;

    @Inject
    BlockedMsisdnRecordDataService blockedMsisdnRecordDataService;

    @Inject
    SubscriberMsisdnTrackerDataService subscriberMsisdnTrackerDataService;

    @Inject
    PlatformTransactionManager transactionManager;





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

    public void RchMotherActiveImport() throws IOException {
        String response = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "motherActiveImport.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);
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
        rchWsImportService.importMothersFromRch(a, yesterday, endpoint);
        MotechEvent event1 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<MotherImportRejection> motherImportRejectionList = motherRejectionDataService.retrieveAll();
        assertEquals(0, motherImportRejectionList.size());
        List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
        assertEquals(1, mothers.size());
        Subscriber subscriber = subscriberService.getSubscriber(9856852145L).get(0);
        List<Subscription> subscriptions = subscriptionDataService.retrieveAll();
        Assert.assertEquals(SubscriptionStatus.ACTIVE, subscriptions.get(0).getStatus());
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
    @Ignore
    public void testChildRCHImport() throws IOException {
        String response = RchImportTestHelper.getRchChildrenResponseData();
        String remoteLocation = "/home/beehyv/IdeaProjects/nsp/testing/src/test/resources/rch";
        String fileName = "RCH_StateID_21_Child_Response.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("childendpoint", 200, response);
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
        String response = RchImportTestHelper.getAnmAshaResponseData();
        String remoteLocation = "/home/beehyv/nms-nmsbugfix/testing/src/test/resources/rch";
        String fileName =  "rch-anm-asha-data.xml"; //done by vishnu
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("ashendpoint", 200, response);
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

    @Test
    public void testMotherRCHActiveImport() throws IOException {
        RchMotherActiveImport();

    }

    @Test
    public void testMotherRCHPendingActivationImport() throws IOException {
        String response = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "pendingActivation.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);
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
        rchWsImportService.importMothersFromRch(a, yesterday, endpoint);
        MotechEvent event1 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<MotherImportRejection> motherImportRejectionList = motherRejectionDataService.retrieveAll();
        assertEquals(0, motherImportRejectionList.size());
        List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
        assertEquals(1, mothers.size());
        Subscriber subscriber = subscriberService.getSubscriber(9856852145L).get(0);
        List<Subscription> subscriptions = subscriptionDataService.retrieveAll();
        Assert.assertEquals(SubscriptionStatus.PENDING_ACTIVATION, subscriptions.get(0).getStatus());
    }

    @Test
    public void testMotherRCHActivetoPendingImport() throws IOException {
        RchMotherActiveImport();

        //updating LMP by importing another xml file

        String response1 = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation1 = filepath.getAbsolutePath();
        String fileName1 = "pendingActivation.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("rchEndpoint", 200, response1);
        URL endpoint1 = new URL(url1);
        LocalDate lastDateToCheck1 = DateUtil.today().minusDays(1);
        LocalDate yesterday1 = DateUtil.today().minusDays(1);
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params1 = new HashMap<>();
        params1.put(Constants.START_DATE_PARAM, lastDateToCheck1);
        params1.put(Constants.END_DATE_PARAM, yesterday1);
        params1.put(Constants.STATE_ID_PARAM, 21L);
        params1.put(Constants.ENDPOINT_PARAM, endpoint1);
        params1.put(Constants.REMOTE_LOCATION, remoteLocation1);
        params1.put(Constants.FILE_NAME, fileName1);
        List<Long> a1 = new ArrayList<>();
        a1.add(21L);
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event11 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event11);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List<MotherImportRejection> motherImportRejectionList1 = motherRejectionDataService.retrieveAll();
        assertEquals(0, motherImportRejectionList1.size());
        List<MctsMother> mothers1 = mctsMotherDataService.retrieveAll();
        assertEquals(1, mothers1.size());
        //Subscriber subscriber1 = subscriberService.getSubscriber(9856852145L).get(0);
        List<Subscription> subscriptions1 = subscriptionDataService.retrieveAll();
        Assert.assertEquals(SubscriptionStatus.PENDING_ACTIVATION, subscriptions1.get(0).getStatus());

    }

    @Test
    public void testMotherRCHPendingtoActiveImport() throws IOException {
        RchMotherActiveImport();

        //updating LMP by importing another xml file

        String response = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "motherActiveImport.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);
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
        MotechEvent event1 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<MotherImportRejection> motherImportRejectionList = motherRejectionDataService.retrieveAll();
        assertEquals(0, motherImportRejectionList.size());
        List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
        assertEquals(1, mothers.size());
        //Subscriber subscriber = subscriberService.getSubscriber(9856852145L).get(0);
        List<Subscription> subscriptions = subscriptionDataService.retrieveAll();
        Assert.assertEquals(SubscriptionStatus.ACTIVE, subscriptions.get(0).getStatus());
    }

    @Test
    public void testMotherRCHInvalidLmpUpdate() throws IOException {
        RchMotherActiveImport();

        //updating LMP by importing another xml file

        String response1 = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation1 = filepath.getAbsolutePath();
        String fileName1 = "invalidLmp.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("rchEndpoint", 200, response1);
        URL endpoint1 = new URL(url1);
        LocalDate lastDateToCheck1 = DateUtil.today().minusDays(1);
        LocalDate yesterday1 = DateUtil.today().minusDays(1);
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params1 = new HashMap<>();
        params1.put(Constants.START_DATE_PARAM, lastDateToCheck1);
        params1.put(Constants.END_DATE_PARAM, yesterday1);
        params1.put(Constants.STATE_ID_PARAM, 21L);
        params1.put(Constants.ENDPOINT_PARAM, endpoint1);
        params1.put(Constants.REMOTE_LOCATION, remoteLocation1);
        params1.put(Constants.FILE_NAME, fileName1);
        List<Long> a1 = new ArrayList<>();
        a1.add(21L);
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event11 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event11);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List<MotherImportRejection> motherImportRejectionList1 = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejectionList1.size());
        Assert.assertEquals(RejectionReasons.INVALID_LMP_DATE.toString(), motherImportRejectionList1.get(0).getRejectionReason());
    }

    @Test
    public void testMotherRCHFutureLmpImport() throws IOException {
        String response = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "futureLmp.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);
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
        rchWsImportService.importMothersFromRch(a, yesterday, endpoint);
        MotechEvent event1 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<MotherImportRejection> motherImportRejectionList = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejectionList.size());
        Assert.assertEquals(RejectionReasons.INVALID_LMP_DATE.toString(), motherImportRejectionList.get(0).getRejectionReason());

    }

    @Test
    public void testMotherRCHMissingLmpImport() throws IOException {
        String response = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "missingLmp.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);
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
        rchWsImportService.importMothersFromRch(a, yesterday, endpoint);
        MotechEvent event1 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<MotherImportRejection> motherImportRejectionList = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejectionList.size());
        List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
        assertEquals(0, mothers.size());
        Assert.assertEquals(RejectionReasons.INVALID_LMP_DATE.toString(), motherImportRejectionList.get(0).getRejectionReason());
    }

    @Test
    public void testMotherAbortionImportReason1() throws IOException {
        String response = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "mtpLessAbortion.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);
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
        rchWsImportService.importMothersFromRch(a, yesterday, endpoint);
        MotechEvent event1 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<MotherImportRejection> motherImportRejectionList = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejectionList.size());
        List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
        assertEquals(0, mothers.size());
        Assert.assertEquals(RejectionReasons.ABORT_STILLBIRTH_DEATH.toString(), motherImportRejectionList.get(0).getRejectionReason());

    }

    @Test
    public void testMotherAbortionImportReason2() throws IOException {
        String response = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "mtpGreaterAbortion.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);
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
        rchWsImportService.importMothersFromRch(a, yesterday, endpoint);
        MotechEvent event1 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<MotherImportRejection> motherImportRejectionList = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejectionList.size());
        List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
        assertEquals(0, mothers.size());
        Assert.assertEquals(RejectionReasons.ABORT_STILLBIRTH_DEATH.toString(), motherImportRejectionList.get(0).getRejectionReason());

    }

    @Test
    public void testMotherAbortionImportReason3() throws IOException {
        String response = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "spontaneousAbortion.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);
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
        rchWsImportService.importMothersFromRch(a, yesterday, endpoint);
        MotechEvent event1 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<MotherImportRejection> motherImportRejectionList = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejectionList.size());
        List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
        assertEquals(0, mothers.size());
        Assert.assertEquals(RejectionReasons.ABORT_STILLBIRTH_DEATH.toString(), motherImportRejectionList.get(0).getRejectionReason());

    }

    @Test
    public void testMotherRCHAbortionUpdate() throws IOException {
        RchMotherActiveImport();

        String response1 = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation1 = filepath.getAbsolutePath();
        String fileName1 = "spontaneousAbortion.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("rchEndpoint", 200, response1);
        URL endpoint1 = new URL(url1);
        LocalDate lastDateToCheck1 = DateUtil.today().minusDays(1);
        LocalDate yesterday1 = DateUtil.today().minusDays(1);
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params1 = new HashMap<>();
        params1.put(Constants.START_DATE_PARAM, lastDateToCheck1);
        params1.put(Constants.END_DATE_PARAM, yesterday1);
        params1.put(Constants.STATE_ID_PARAM, 21L);
        params1.put(Constants.ENDPOINT_PARAM, endpoint1);
        params1.put(Constants.REMOTE_LOCATION, remoteLocation1);
        params1.put(Constants.FILE_NAME, fileName1);
        List<Long> a1 = new ArrayList<>();
        a1.add(21L);
        // MotechEvent event = new MotechEvent("foobar", params);
        rchWsImportService.importMothersFromRch(a1, yesterday1, endpoint1);
        MotechEvent event11 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event11);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List<Subscription> subscriptions = subscriptionDataService.retrieveAll();
        Assert.assertEquals(SubscriptionStatus.DEACTIVATED, subscriptions.get(0).getStatus());
        Assert.assertEquals(DeactivationReason.MISCARRIAGE_OR_ABORTION, subscriptions.get(0).getDeactivationReason());

    }

    @Test
    public void testMotherDeathImport() throws IOException {
        String response = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "motherDeath.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);
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
        rchWsImportService.importMothersFromRch(a, yesterday, endpoint);
        MotechEvent event1 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<MotherImportRejection> motherImportRejectionList = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejectionList.size());
        List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
        assertEquals(0, mothers.size());
        Assert.assertEquals(RejectionReasons.ABORT_STILLBIRTH_DEATH.toString(), motherImportRejectionList.get(0).getRejectionReason());

    }

    @Test
    public void testMotherRCHDeathUpdate() throws IOException {
        RchMotherActiveImport();

        String response1 = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation1 = filepath.getAbsolutePath();
        String fileName1 = "motherDeath.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("rchEndpoint", 200, response1);
        URL endpoint1 = new URL(url1);
        LocalDate lastDateToCheck1 = DateUtil.today().minusDays(1);
        LocalDate yesterday1 = DateUtil.today().minusDays(1);
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params1 = new HashMap<>();
        params1.put(Constants.START_DATE_PARAM, lastDateToCheck1);
        params1.put(Constants.END_DATE_PARAM, yesterday1);
        params1.put(Constants.STATE_ID_PARAM, 21L);
        params1.put(Constants.ENDPOINT_PARAM, endpoint1);
        params1.put(Constants.REMOTE_LOCATION, remoteLocation1);
        params1.put(Constants.FILE_NAME, fileName1);
        List<Long> a1 = new ArrayList<>();
        a1.add(21L);
        // MotechEvent event = new MotechEvent("foobar", params);
        rchWsImportService.importMothersFromRch(a1, yesterday1, endpoint1);
        MotechEvent event11 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event11);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List<Subscription> subscriptions1 = subscriptionDataService.retrieveAll();
        Assert.assertEquals(SubscriptionStatus.DEACTIVATED, subscriptions1.get(0).getStatus());
        Assert.assertEquals(DeactivationReason.MATERNAL_DEATH, subscriptions1.get(0).getDeactivationReason());

    }

    @Test
    public void testMotherImportStillbirth() throws IOException {
        String response = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "stillbirth.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);
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
        rchWsImportService.importMothersFromRch(a, yesterday, endpoint);
        MotechEvent event1 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<MotherImportRejection> motherImportRejectionList = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejectionList.size());
        List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
        assertEquals(0, mothers.size());
        Assert.assertEquals(RejectionReasons.ABORT_STILLBIRTH_DEATH.toString(), motherImportRejectionList.get(0).getRejectionReason());

    }

    @Test
    public void testMotherRCHStillbirthUpdate() throws IOException {
        RchMotherActiveImport();

        String response1 = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation1 = filepath.getAbsolutePath();
        String fileName1 = "stillbirth.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("rchEndpoint", 200, response1);
        URL endpoint1 = new URL(url1);
        LocalDate lastDateToCheck1 = DateUtil.today().minusDays(1);
        LocalDate yesterday1 = DateUtil.today().minusDays(1);
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params1 = new HashMap<>();
        params1.put(Constants.START_DATE_PARAM, lastDateToCheck1);
        params1.put(Constants.END_DATE_PARAM, yesterday1);
        params1.put(Constants.STATE_ID_PARAM, 21L);
        params1.put(Constants.ENDPOINT_PARAM, endpoint1);
        params1.put(Constants.REMOTE_LOCATION, remoteLocation1);
        params1.put(Constants.FILE_NAME, fileName1);
        List<Long> a1 = new ArrayList<>();
        a1.add(21L);
        // MotechEvent event = new MotechEvent("foobar", params);
        rchWsImportService.importMothersFromRch(a1, yesterday1, endpoint1);
        MotechEvent event11 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event11);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List<Subscription> subscriptions1 = subscriptionDataService.retrieveAll();
        Assert.assertEquals(SubscriptionStatus.DEACTIVATED, subscriptions1.get(0).getStatus());
        Assert.assertEquals(DeactivationReason.STILL_BIRTH, subscriptions1.get(0).getDeactivationReason());

    }

    @Test
    public void testMotherRCHValidCaseNo() throws IOException {
        RchMotherActiveImport();
    }

    //Null Pointer
    @Ignore
    @Test
    public void testMotherRCHMissingCaseNo() throws IOException {
        String response = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "missingCaseNo.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);
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
        rchWsImportService.importMothersFromRch(a, yesterday, endpoint);
        MotechEvent event1 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscribers = subscriberService.getSubscriber(9856852145L);
        assertTrue(subscribers.isEmpty());
        List<MotherImportRejection> motherImportRejections = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejections.size());
        Assert.assertEquals("9856852145", motherImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.INVALID_CASE_NO.toString(), motherImportRejections.get(0).getRejectionReason());
        assertEquals("121003648144", motherImportRejections.get(0).getRegistrationNo());
        transactionManager.commit(status);
    }

    @Test
    public void testMotherRCHMobileNoUpdate() throws IOException {
        RchMotherActiveImport();

        //updating Mobile No by importing another xml file

        String response1 = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation1 = filepath.getAbsolutePath();
        String fileName1 = "mobileNoUpdate.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("rchEndpoint", 200, response1);
        URL endpoint1 = new URL(url1);
        LocalDate lastDateToCheck1 = DateUtil.today().minusDays(1);
        LocalDate yesterday1 = DateUtil.today().minusDays(1);
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params1 = new HashMap<>();
        params1.put(Constants.START_DATE_PARAM, lastDateToCheck1);
        params1.put(Constants.END_DATE_PARAM, yesterday1);
        params1.put(Constants.STATE_ID_PARAM, 21L);
        params1.put(Constants.ENDPOINT_PARAM, endpoint1);
        params1.put(Constants.REMOTE_LOCATION, remoteLocation1);
        params1.put(Constants.FILE_NAME, fileName1);
        List<Long> a1 = new ArrayList<>();
        a1.add(21L);
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event11 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event11);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List<MotherImportRejection> motherImportRejectionList1 = motherRejectionDataService.retrieveAll();
        assertEquals(0, motherImportRejectionList1.size());
        List<MctsMother> mothers1 = mctsMotherDataService.retrieveAll();
        assertEquals(1, mothers1.size());
        Subscriber subscriber1 = subscriberService.getSubscriber(8977825553L).get(0);
        List<Subscription> subscriptions1 = subscriptionDataService.retrieveAll();
        Assert.assertEquals(SubscriptionStatus.ACTIVE, subscriptions1.get(0).getStatus());
        Assert.assertEquals("8977825553", subscriber1.getCallingNumber().toString());

    }

    @Test
    public void testMotherRCHMsisdnAlreadySubscribedSameState() throws IOException {
        RchMotherActiveImport();

        String response1 = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation1 = filepath.getAbsolutePath();
        String fileName1 = "sameMsisdnSameState.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("rchEndpoint", 200, response1);
        URL endpoint1 = new URL(url1);
        LocalDate lastDateToCheck1 = DateUtil.today().minusDays(1);
        LocalDate yesterday1 = DateUtil.today().minusDays(1);
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params1 = new HashMap<>();
        params1.put(Constants.START_DATE_PARAM, lastDateToCheck1);
        params1.put(Constants.END_DATE_PARAM, yesterday1);
        params1.put(Constants.STATE_ID_PARAM, 21L);
        params1.put(Constants.ENDPOINT_PARAM, endpoint1);
        params1.put(Constants.REMOTE_LOCATION, remoteLocation1);
        params1.put(Constants.FILE_NAME, fileName1);
        List<Long> a1 = new ArrayList<>();
        a1.add(21L);
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event11 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event11);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List<MotherImportRejection> motherImportRejectionList1 = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejectionList1.size());
        List<MctsMother> mothers1 = mctsMotherDataService.retrieveAll();
        assertEquals(1, mothers1.size());
        Assert.assertEquals(RejectionReasons.MOBILE_NUMBER_ALREADY_SUBSCRIBED.toString(), motherImportRejectionList1.get(0).getRejectionReason());
    }

    @Test
    public void testMotherRCHMsisdnAlreadySubscribedDiffState() throws IOException {
        RchMotherActiveImport();

        // attempt to create subscriber with same msisdn but different rch id.
        State state20 = createState(20L, "State 20");
        stateDataService.create(state20);
        District district = createDistrict(state20, 3L, "EXAMPLE DISTRICT");
        districtDataService.create(district);


        String response1 = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation1 = filepath.getAbsolutePath();
        String fileName1 = "sameMsisdnDifferentState.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("rchEndpoint", 200, response1);
        URL endpoint1 = new URL(url1);
        LocalDate lastDateToCheck1 = DateUtil.today().minusDays(1);
        LocalDate yesterday1 = DateUtil.today().minusDays(1);
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params1 = new HashMap<>();
        params1.put(Constants.START_DATE_PARAM, lastDateToCheck1);
        params1.put(Constants.END_DATE_PARAM, yesterday1);
        params1.put(Constants.STATE_ID_PARAM, 20L);
        params1.put(Constants.ENDPOINT_PARAM, endpoint1);
        params1.put(Constants.REMOTE_LOCATION, remoteLocation1);
        params1.put(Constants.FILE_NAME, fileName1);

        MotechEvent event11 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event11);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List<MotherImportRejection> motherImportRejectionList1 = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejectionList1.size());
        List<MctsMother> mothers1 = mctsMotherDataService.retrieveAll();
        assertEquals(1, mothers1.size());
        Assert.assertEquals(RejectionReasons.MOBILE_NUMBER_ALREADY_SUBSCRIBED.toString(), motherImportRejectionList1.get(0).getRejectionReason());


    }

    @Test
    public void testMotherRCHActiveToActiveUpdate() throws IOException {
        RchMotherActiveImport();
        //updating LMP by importing another xml file to 2018-07-04

        String response1 = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation1 = filepath.getAbsolutePath();
        String fileName1 = "ActiveLmpUpdate.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("rchEndpoint", 200, response1);
        URL endpoint1 = new URL(url1);
        LocalDate lastDateToCheck1 = DateUtil.today().minusDays(1);
        LocalDate yesterday1 = DateUtil.today().minusDays(1);
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params1 = new HashMap<>();
        params1.put(Constants.START_DATE_PARAM, lastDateToCheck1);
        params1.put(Constants.END_DATE_PARAM, yesterday1);
        params1.put(Constants.STATE_ID_PARAM, 21L);
        params1.put(Constants.ENDPOINT_PARAM, endpoint1);
        params1.put(Constants.REMOTE_LOCATION, remoteLocation1);
        params1.put(Constants.FILE_NAME, fileName1);
        List<Long> a1 = new ArrayList<>();
        a1.add(21L);
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event11 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event11);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);

        List<MotherImportRejection> motherImportRejectionList1 = motherRejectionDataService.retrieveAll();
        assertEquals(0, motherImportRejectionList1.size());
        List<MctsMother> mothers1 = mctsMotherDataService.retrieveAll();
        assertEquals(1, mothers1.size());
        List<Subscription> subscriptions1 = subscriptionDataService.retrieveAll();
        Assert.assertEquals(SubscriptionStatus.ACTIVE, subscriptions1.get(0).getStatus());
        List<Subscriber> Subscriber = subscriberService.getSubscriber(9856852145L);
        //assertEquals("2018-07-04T00:00:00.000+05:30", Subscriber.get(0).getLastMenstrualPeriod());
    }

    @Test
    public void testMotherRCHInValidCaseNo() throws IOException {
        RchMotherActiveImport();

        //Update Case No to 1

        String response1 = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation1 = filepath.getAbsolutePath();
        String fileName1 = "invalidCaseNo.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("rchEndpoint", 200, response1);
        URL endpoint1 = new URL(url1);
        LocalDate lastDateToCheck1 = DateUtil.today().minusDays(1);
        LocalDate yesterday1 = DateUtil.today().minusDays(1);
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params1 = new HashMap<>();
        params1.put(Constants.START_DATE_PARAM, lastDateToCheck1);
        params1.put(Constants.END_DATE_PARAM, yesterday1);
        params1.put(Constants.STATE_ID_PARAM, 21L);
        params1.put(Constants.ENDPOINT_PARAM, endpoint1);
        params1.put(Constants.REMOTE_LOCATION, remoteLocation1);
        params1.put(Constants.FILE_NAME, fileName1);
        List<Long> a1 = new ArrayList<>();
        a1.add(21L);
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event11 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event11);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);

        List<MotherImportRejection> motherImportRejectionList1 = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejectionList1.size());
        List<MctsMother> mothers1 = mctsMotherDataService.retrieveAll();
        assertEquals(1, mothers1.size());
        Assert.assertEquals(RejectionReasons.INVALID_CASE_NO.toString(), motherImportRejectionList1.get(0).getRejectionReason());


    }

    @Test
    public void testMotherRCHSubscriptionExistingDeactivatedStatus() throws IOException {
        String response = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "motherActiveImport.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchEndpoint", 200, response);
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
        rchWsImportService.importMothersFromRch(a, yesterday, endpoint);
        MotechEvent event1 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        //Mark subscription deactivate
        Subscriber subscriber = subscriberService.getSubscriber(9856852145L).get(0);
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.MISCARRIAGE_OR_ABORTION);
        transactionManager.commit(status);

        //Create New Subscription for the Deactivated Subscription

        String response1 = RchImportTestHelper.getRchMotherActiveResponseData();
        String remoteLocation1 = "/home/beehyv/IdeaProjects/nms/testing/src/test/resources/rch";
        String fileName1 = "ActiveLmpUpdate.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("rchEndpoint", 200, response1);
        URL endpoint1 = new URL(url1);
        LocalDate lastDateToCheck1 = DateUtil.today().minusDays(1);
        LocalDate yesterday1 = DateUtil.today().minusDays(1);
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params1 = new HashMap<>();
        params1.put(Constants.START_DATE_PARAM, lastDateToCheck1);
        params1.put(Constants.END_DATE_PARAM, yesterday1);
        params1.put(Constants.STATE_ID_PARAM, 21L);
        params1.put(Constants.ENDPOINT_PARAM, endpoint1);
        params1.put(Constants.REMOTE_LOCATION, remoteLocation1);
        params1.put(Constants.FILE_NAME, fileName1);
        List<Long> a1 = new ArrayList<>();
        a1.add(21L);
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event11 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event11);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);

        List<MotherImportRejection> motherImportRejectionList1 = motherRejectionDataService.retrieveAll();
        assertEquals(1, motherImportRejectionList1.size());
        List<MctsMother> mothers1 = mctsMotherDataService.retrieveAll();
        assertEquals(1, mothers1.size());
        Assert.assertEquals(RejectionReasons.UPDATED_RECORD_ALREADY_EXISTS.toString(), motherImportRejectionList1.get(0).getRejectionReason());

    }

    @Test
    public void testMotherRCHSubscriptionExistingCompletedStatus() throws IOException {
        DateTime lmp = DateTime.now();

        RchMotherActiveImport();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        //Mark Subscription as Completed
        Subscriber subscriber = subscriberService.getSubscriber(9856852145L).get(0);
        subscriber.setLastMenstrualPeriod(lmp.minusDays(35000));
        subscriberService.updateStartDate(subscriber);
        transactionManager.commit(status);

        //Create New Subscription for the Completed Subscription

        String response1 = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation1 = filepath.getAbsolutePath();
        String fileName1 = "ActiveLmpUpdate.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("rchEndpoint", 200, response1);
        URL endpoint1 = new URL(url1);
        LocalDate lastDateToCheck1 = DateUtil.today().minusDays(1);
        LocalDate yesterday1 = DateUtil.today().minusDays(1);
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params1 = new HashMap<>();
        params1.put(Constants.START_DATE_PARAM, lastDateToCheck1);
        params1.put(Constants.END_DATE_PARAM, yesterday1);
        params1.put(Constants.STATE_ID_PARAM, 21L);
        params1.put(Constants.ENDPOINT_PARAM, endpoint1);
        params1.put(Constants.REMOTE_LOCATION, remoteLocation1);
        params1.put(Constants.FILE_NAME, fileName1);
        List<Long> a1 = new ArrayList<>();
        a1.add(21L);
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event11 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event11);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        List<MotherImportRejection> motherImportRejectionList1 = motherRejectionDataService.retrieveAll();
        assertEquals(0, motherImportRejectionList1.size());
        List<MctsMother> mothers1 = mctsMotherDataService.retrieveAll();
        assertEquals(1, mothers1.size());
        List<Subscription> subscriptions1 = subscriptionDataService.retrieveAll();
        Assert.assertEquals(SubscriptionStatus.ACTIVE, subscriptions1.get(1).getStatus());
        transactionManager.commit(status);

    }

    @Test
    public void testMotherRCHSelfDeactivationStatus() throws IOException {
        RchMotherActiveImport();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());


        //Mark subscription deactivate
        Subscriber subscriber = subscriberService.getSubscriber(9856852145L).get(0);
        Subscription subscription = subscriber.getActiveAndPendingSubscriptions().iterator().next();
        subscriptionService.deactivateSubscription(subscription, DeactivationReason.DEACTIVATED_BY_USER);
        transactionManager.commit(status);

        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.DEACTIVATED_BY_USER, subscription.getDeactivationReason());

    }

    @Test
    public void testMotherRCHUpdateWithBlockedMsisdn() throws IOException {
        RchMotherActiveImport();

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        blockedMsisdnRecordDataService.create(new BlockedMsisdnRecord(8977825553L, DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED));
        transactionManager.commit(status);

        String response1 = RchImportTestHelper.getRchMotherActiveResponseData();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation1 = filepath.getAbsolutePath();
        String fileName1 = "blockedMsisdn.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("rchEndpoint", 200, response1);
        URL endpoint1 = new URL(url1);
        LocalDate lastDateToCheck1 = DateUtil.today().minusDays(1);
        LocalDate yesterday1 = DateUtil.today().minusDays(1);
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params1 = new HashMap<>();
        params1.put(Constants.START_DATE_PARAM, lastDateToCheck1);
        params1.put(Constants.END_DATE_PARAM, yesterday1);
        params1.put(Constants.STATE_ID_PARAM, 21L);
        params1.put(Constants.ENDPOINT_PARAM, endpoint1);
        params1.put(Constants.REMOTE_LOCATION, remoteLocation1);
        params1.put(Constants.FILE_NAME, fileName1);
        List<Long> a1 = new ArrayList<>();
        a1.add(21L);
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event11 = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event11);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<SubscriberMsisdnTracker> msisdnTrackers = subscriberMsisdnTrackerDataService.retrieveAll();
        assertEquals(1, msisdnTrackers.size());
        SubscriberMsisdnTracker msisdnTracker = msisdnTrackers.get(0);
        transactionManager.commit(status);
        assertEquals(9856852145L, msisdnTracker.getOldCallingNumber().longValue());
        assertEquals(8977825553L, msisdnTracker.getNewCallingNumber().longValue());

        assertNull(blockedMsisdnRecordDataService.findByNumber(8977825553L));



    }


}
