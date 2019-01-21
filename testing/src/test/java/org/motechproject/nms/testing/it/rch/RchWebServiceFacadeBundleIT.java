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
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.rch.service.RchWebServiceFacade;
import org.motechproject.nms.rch.service.RchWsImportService;
import org.motechproject.nms.rch.utils.Constants;
import org.motechproject.nms.region.domain.*;
import org.motechproject.nms.region.repository.*;
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
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
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
    private VillageDataService villageDataService;

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


    @Inject
    private TalukaDataService talukaDataService;

    @Inject
    private HealthFacilityDataService healthFacilityDataService;

    @Inject
    private HealthSubFacilityDataService healthSubFacilityDataService;

    @Inject
    private HealthBlockDataService healthBlockDataService;

    @Before
    public void setUp() throws ServletException, NamespaceException {
        testingService.clearDatabase();
        State state = stateDataService.create(new State("My State", 21L));

        District district = new District();
        district.setCode(4L);
        district.setState(state);
        district.setName("District_Name 4");
        district.setRegionalName("Regional Name 4");
        districtDataService.create(district);


        Taluka taluka = new Taluka();
        taluka.setRegionalName("Taluka Regional");
        taluka.setName("Taluka_Name 1");
        taluka.setCode("0046");
        taluka.setIdentity(55);

        taluka.setDistrict(district);
        district.setTalukas(new ArrayList<>(singletonList(taluka)));
        talukaDataService.create(taluka);

        taluka = talukaDataService.retrieveAll().get(0);
        HealthBlock healthBlock = new HealthBlock();
        healthBlock.setCode(113L);
        healthBlock.setName("HealthBlock_Name 1");
        healthBlock.setRegionalName("HB1");
        healthBlock.setHq("An HQ");
        healthBlock.setStateIdOID(stateDataService.retrieveAll().get(0).getId());
        healthBlock.setTalukaIdOID(taluka.getId());

        healthBlock.setDistrict(district);
        healthBlockDataService.create(healthBlock);

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
        facility.setDistrictIdOID(districtDataService.retrieveAll().get(0).getId());
        facility.setStateIdOID(stateDataService.retrieveAll().get(0).getId());
        facility.setTalukaIdOID(talukaDataService.retrieveAll().get(0).getId());
        healthBlock.setHealthFacilities(new ArrayList<>(singletonList(facility)));
        facility.setHealthBlock(healthBlock);
        healthFacilityDataService.create(facility);

        HealthSubFacility subcentre = new HealthSubFacility();
        subcentre.setName("SubCentre_Name 1");
        subcentre.setRegionalName("Regional sub name");
        subcentre.setCode(333L);
        subcentre.setTalukaIdOID(talukaDataService.retrieveAll().get(0).getId());
        subcentre.setStateIdOID(stateDataService.retrieveAll().get(0).getId());
        subcentre.setDistrictIdOID(districtDataService.retrieveAll().get(0).getId());
        subcentre.setHealthFacility(facility);
        facility.setHealthSubFacilities(new ArrayList<>(singletonList(subcentre)));

        healthSubFacilityDataService.create(subcentre);

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



    /*To check village record creating through Rch xml import*/

    @Test
    public void testVillageRCHImport() throws IOException {
        String response = RchImportTestHelper.getVillageLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_Village_import.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("villageendpoint", 200, response);
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

        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_VILLAGE_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readVillageResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<Village> villages = villageDataService.retrieveAll();
        assertEquals(1, villages.size());
    }

    /*To check District record creating through Rch xml import*/

    @Test
    public void testDistrictRCHImport() throws IOException {
        String response = RchImportTestHelper.getDistrictLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_District_import.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("districtendpoint", 200, response);
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
        // MotechEvent event = new MotechEvent("foobar", params);

        MotechEvent event1 = new MotechEvent(Constants.RCH_DISTRICT_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readDistrictResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<District> districts = districtDataService.retrieveAll();
        assertEquals(2, districts.size());
    }

    /*To check taluka record creating through Rch xml import*/

    @Test
    public void testTalukaRCHImport() throws IOException {
        String response = RchImportTestHelper.getTalukaLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_Taluka_import.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("talukaendpoint", 200, response);
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

        // MotechEvent event = new MotechEvent("foobar", params);

        MotechEvent event1 = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_TALUKA_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readTalukaResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<Taluka> talukas = talukaDataService.retrieveAll();
        assertEquals(2, talukas.size());
    }

    /*To check HealthBlock record creating through Rch xml import*/

    @Test
    public void testHealthBlockRCHImport() throws IOException {
        String response = RchImportTestHelper.getHealthBlockLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_HealthBlock_import.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("healthblockendpoint", 200, response);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(Constants.RCH_HEALTHBLOCK_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readHealthBlockResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<HealthBlock> healthBlocks = healthBlockDataService.retrieveAll();
        assertEquals(3, healthBlocks.size());
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
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_StateID_21_Child_Update_Response.xml";
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
        List<Subscription> subscriptions = subscriptionDataService.retrieveAll();
        Assert.assertEquals(1, subscriptions.size());
        Assert.assertEquals(SubscriptionPackType.CHILD, subscriptions.get(0).getSubscriptionPack().getType());

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

        MotechEvent event = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        List<Subscriber> subscriber = subscriberService.getSubscriber(9856852145L);
        assertEquals(1, subscriber.size());
        Set<Subscription> subscriptions = subscriber.get(0).getActiveAndPendingSubscriptions();
        assertEquals(1, subscriptions.size());

        List<Subscription> subscriptions1 = subscriptionDataService.retrieveAll();
        Assert.assertEquals(SubscriptionStatus.ACTIVE, subscriptions1.get(0).getStatus());
        transactionManager.commit(status);

        //Import child xml file with same msisdn as mother

        SetupImportNewChild();
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9856852145L);
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
        Subscriber subscriber = subscriberService.getSubscriber(9856852145L).get(0);
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
        subscriber = subscriberService.getSubscriber(9856852145L).get(0);
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
        Subscriber subscriber = subscriberService.getSubscriber(9856852145L).get(0);
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
        List<Subscriber> subscribersByMsisdn = subscriberService.getSubscriber(9856852145L);
        assertEquals(1, subscribersByMsisdn.size());
        List<ChildImportRejection> childImportRejections = childRejectionDataService.retrieveAll();
        Assert.assertEquals(1, childImportRejections.size());
        Assert.assertEquals("9856852145", childImportRejections.get(0).getMobileNo());
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
        Assert.assertEquals("9856852145", childImportRejections.get(0).getMobileNo());
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
        Subscriber subscriber = subscriberService.getSubscriber(9856852145L).get(0);
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
        Subscriber subscriber = subscriberService.getSubscriber(9856852145L).get(0);
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
        Assert.assertEquals("9856852145", childImportRejections.get(0).getMobileNo());
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
        SetupImportNewChild();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9856852145L).get(0);
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
        List<Subscriber> subscribersByMsisdn = subscriberService.getSubscriber(9856852145L);
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
        SetupImportNewChild();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9856852145L).get(0);
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
        Assert.assertEquals("9856852145", childImportRejections.get(0).getMobileNo());
        Assert.assertEquals(RejectionReasons.UPDATED_RECORD_ALREADY_EXISTS.toString(), childImportRejections.get(0).getRejectionReason());
        Assert.assertEquals("245893460722", childImportRejections.get(0).getRegistrationNo());
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
        SetupImportNewChild();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9856852145L).get(0);
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

        subscriber = subscriberService.getSubscriber(9856852145L).get(0);
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

        MotechEvent event = new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readMotherResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9856852145L).get(0);
        assertNotNull(subscriber);
        Set<Subscription> subscriptions = subscriber.getActiveAndPendingSubscriptions();
        Assert.assertEquals(1, subscriptions.size());
        transactionManager.commit(status);

        // import child with same MSISDN and above MotherID --> child should be updated and mother be deactivated

        SetupImportNewChild();
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscriber = subscriberService.getSubscriber(9856852145L).get(0);
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
        List<Subscriber> subscribers = subscriberService.getSubscriber(9856852145L);
        Assert.assertEquals(1, subscribers.size());

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
        transactionManager.commit(status);

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        subscribers = subscriberService.getSubscriber(9856852145L);
        Assert.assertEquals(0, subscribers.size());
        subscribers = subscriberService.getSubscriber(9856852235L);
        Assert.assertEquals(1, subscribers.size());

        List<SubscriberMsisdnTracker> msisdnTrackers = subscriberMsisdnTrackerDataService.retrieveAll();
        Assert.assertEquals(1, msisdnTrackers.size());
        SubscriberMsisdnTracker msisdnTracker = msisdnTrackers.get(0);
        Assert.assertEquals(9856852145L, msisdnTracker.getOldCallingNumber().longValue());
        Assert.assertEquals(9856852235L, msisdnTracker.getNewCallingNumber().longValue());
        transactionManager.commit(status);
    }

    @Test
    public void testCreateNewChildRecordDifferentMsisdnViaXml() throws Exception {
        SetupImportNewChild();

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Subscriber subscriber = subscriberService.getSubscriber(9856852145L).get(0);
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

    /*To check HealthFacility record creating through Rch xml import*/

    @Test
    public void testHealthFacilityRCHImport() throws IOException {
        String response = RchImportTestHelper.getHealthFacilityLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_HealthFacility_import.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("healthfacilityendpoint", 200, response);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(Constants.RCH_HEALTHFACILITY_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readHealthFacilityResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<HealthFacility> healthFacilities = healthFacilityDataService.retrieveAll();
        assertEquals(2, healthFacilities.size());


    }

    /*To check HealthSubFacility record creating through Rch xml import*/

    @Test
    public void testHealthSubFacilityRCHImport() throws IOException {
        String response = RchImportTestHelper.getHealthSubFacilityLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_HealthSubFacility_import.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("healthsubfacilityendpoint", 200, response);
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

        // MotechEvent event = new MotechEvent("foobar", params);

        MotechEvent event1 = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_HEALTHSUBFACILITY_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readHealthSubFacilityResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<HealthSubFacility> healthSubFacilities = healthSubFacilityDataService.retrieveAll();
        assertEquals(2, healthSubFacilities.size());


    }

    /* Import duplicate District Id/Code within the state */

    @Test
    public void testDistrictNameupdation() throws IOException {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        String response1 = RchImportTestHelper.getDistrictLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation1 = filepath.getAbsolutePath();
        String fileName1 = "RCH_District_import.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("districtendpoint", 200, response1);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(Constants.RCH_DISTRICT_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readDistrictResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List <District> districts1 = districtDataService.retrieveAll();
        assertEquals("District_test1", districts1.get(1).getName());
        transactionManager.commit(status);


        // Updating district name via another XML

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        String response = RchImportTestHelper.getDistrictLocationdataResponse();
        File filepath1 = new File("src/test/resources/rch");
        String remoteLocation = filepath1.getAbsolutePath();
        String fileName = "RCH_District_name_update.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("districtendpoint", 200, response);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event = new MotechEvent(Constants.RCH_DISTRICT_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readDistrictResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List <District> districts= districtDataService.retrieveAll();
        assertEquals("District_update1", districts.get(1).getName());
        transactionManager.commit(status);
    }

    /*To verify Taluka_Name updated successfully for an existing Taluka_ID*/

    @Test
    public void testTalukanameupdate() throws IOException {
        String response = RchImportTestHelper.getTalukaLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_Taluka_import.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("talukaendpoint", 200, response);
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
        rchWsImportService.importTalukaFromRch(a, yesterday, endpoint);
        MotechEvent event = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_TALUKA_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readTalukaResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<Taluka> talukas = talukaDataService.retrieveAll();
        assertEquals("Taluka_test1", talukas.get(1).getName());

        // Updating taluka name via another XML

        String response1 = RchImportTestHelper.getTalukaLocationdataResponse();
        File filepath1 = new File("src/test/resources/rch");
        String remoteLocation1 = filepath1.getAbsolutePath();
        String fileName1 = "RCH_Taluka_name_update.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("talukaendpoint", 200, response1);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_TALUKA_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readTalukaResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List<Taluka> talukas1 = talukaDataService.retrieveAll();
        assertEquals("Taluka_update1", talukas1.get(1).getName());

    }

    /*To verify HealthBlock_Name updated successfully for an existing HealthBlock_ID*/

    @Test
    public void testHealthBlocknameupdate() throws IOException {
        String response = RchImportTestHelper.getHealthBlockLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_HealthBlock_import.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("rchendpoint", 200, response);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event = new MotechEvent(Constants.RCH_HEALTHBLOCK_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readHealthBlockResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<HealthBlock> healthBlocks = healthBlockDataService.retrieveAll();
        assertEquals("HB_test1", healthBlocks.get(1).getName());

        // Updating HealthBlock name via another XML

        String response1 = RchImportTestHelper.getHealthBlockLocationdataResponse();
        File filepath1 = new File("src/test/resources/rch");
        String remoteLocation1 = filepath1.getAbsolutePath();
        String fileName1 = "RCH_HealthBlock_name_update.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("rchendpoint", 200, response1);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(Constants.RCH_HEALTHBLOCK_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readHealthBlockResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List<HealthBlock> healthBlocks1 = healthBlockDataService.retrieveAll();
        assertEquals("HB_update1", healthBlocks1.get(1).getName());

    }

    /* Import location data with duplicate Health Facility  ID */

    @Test
    public void testHealthFacilityWithDuplicateId() throws IOException {
        String response = RchImportTestHelper.getHealthFacilityLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_HealthFacility_import.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("healthfacilityendpoint", 200, response);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event = new MotechEvent(Constants.RCH_HEALTHFACILITY_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readHealthFacilityResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<HealthFacility> healthFacilities = healthFacilityDataService.retrieveAll();
        assertEquals("HF_Test1", healthFacilities.get(1).getName());

        // Duplicate Health Facility id via another XML

        String response1 = RchImportTestHelper.getHealthFacilityLocationdataResponse();
        File filepath1 = new File("src/test/resources/rch");
        String remoteLocation1 = filepath1.getAbsolutePath();
        String fileName1 = "RCH_HealthFacility_name_update.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("healthfacilityendpoint", 200, response1);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(Constants.RCH_HEALTHFACILITY_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readHealthFacilityResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List<HealthFacility> healthFacilities1 = healthFacilityDataService.retrieveAll();
        assertEquals("Test_update1", healthFacilities1.get(1).getName());
    }

    /*Import Location data with duplicate HealthSubFacility Id with in same state*/

    @Test
    public void testHealthSubFacilityWithDupId() throws IOException {
        String response = RchImportTestHelper.getHealthSubFacilityLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_HealthSubFacility_import.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("healthsubfacilityendpoint", 200, response);
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

        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_HEALTHSUBFACILITY_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readHealthSubFacilityResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<HealthSubFacility> healthSubFacilities = healthSubFacilityDataService.retrieveAll();
        assertEquals("HSF_test1", healthSubFacilities.get(1).getName());

        // Update healthSubFacility name through another XML

        String response1 = RchImportTestHelper.getHealthSubFacilityLocationdataResponse();
        File filepath1 = new File("src/test/resources/rch");
        String remoteLocation1 = filepath1.getAbsolutePath();
        String fileName1 = "RCH_HealthSubFacility_name_update.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("healthsubfacilityendpoint", 200, response1);
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

        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_HEALTHSUBFACILITY_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readHealthSubFacilityResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List<HealthSubFacility> healthSubFacilities1 = healthSubFacilityDataService.retrieveAll();
        assertEquals("HSF_update1", healthSubFacilities1.get(1).getName());
    }



    /*To  verify Districts with same codes(D101,D101,D101) existed in different states(S1,S2,S3) */

    @Test
    public void testSameDistrictcodeindiffstates() throws IOException {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        String response1 = RchImportTestHelper.getDistrictLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation1 = filepath.getAbsolutePath();
        String fileName1 = "RCH_District_same_state_21.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("districtendpoint", 200, response1);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(Constants.RCH_DISTRICT_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readDistrictResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List <District> districts1 = districtDataService.retrieveAll();
        assertEquals(2, districts1.size());
        transactionManager.commit(status);


        // Same district codes in different states

        State state1 = stateDataService.create(new State("Other State", 22L));
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        String response = RchImportTestHelper.getDistrictLocationdataResponse();
        File filepath1 = new File("src/test/resources/rch");
        String remoteLocation = filepath1.getAbsolutePath();
        String fileName = "RCH_District_same_state_22.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("districtendpoint", 200, response);
        URL endpoint = new URL(url);
        LocalDate lastDateToCheck = DateUtil.today().minusDays(1);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 22L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        params.put(Constants.REMOTE_LOCATION, remoteLocation);
        params.put(Constants.FILE_NAME, fileName);
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event = new MotechEvent(Constants.RCH_DISTRICT_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readDistrictResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List <District> districts= districtDataService.retrieveAll();
        assertEquals(3, districts.size());
        transactionManager.commit(status);
    }

    /*To verify District update received with existing District_Name but with change in District_ID */

    @Test
    public void testDupDistrictnameinSameState() throws IOException {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        String response1 = RchImportTestHelper.getDistrictLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation1 = filepath.getAbsolutePath();
        String fileName1 = "RCH_District_import.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("districtendpoint", 200, response1);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(Constants.RCH_DISTRICT_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readDistrictResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List <District> districts1 = districtDataService.retrieveAll();
        assertEquals(2, districts1.size());
        transactionManager.commit(status);


        // Duplicate district names in same state

        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        String response = RchImportTestHelper.getDistrictLocationdataResponse();
        File filepath1 = new File("src/test/resources/rch");
        String remoteLocation = filepath1.getAbsolutePath();
        String fileName = "RCH_dup_district_in_same_state.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("districtendpoint", 200, response);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event = new MotechEvent(Constants.RCH_DISTRICT_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readDistrictResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List <District> districts= districtDataService.retrieveAll();
        assertEquals(3, districts.size());
        transactionManager.commit(status);
    }

    /* To verify Village_Name updated successfully for an existing Village_ID*/

    @Test
    public void testVillageNameupdate() throws IOException {
        String response = RchImportTestHelper.getVillageLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_Village_import.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("villageendpoint", 200, response);
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

        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_VILLAGE_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readVillageResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<Village> villages = villageDataService.retrieveAll();
        assertEquals("Village_test1", villages.get(0).getName());

        // Updating village name via another XML

        String response1 = RchImportTestHelper.getVillageLocationdataResponse();
        File filepath1 = new File("src/test/resources/rch");
        String remoteLocation1 = filepath1.getAbsolutePath();
        String fileName1 = "RCH_Village_name_update.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("villageendpoint", 200, response1);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_VILLAGE_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readVillageResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List<Village> villages1 = villageDataService.retrieveAll();
        assertEquals("Village_update1", villages1.get(0).getName());


    }

    /*Verify import XML file without District_ID(Null) and with District_Name */

    @Test
    public void testDistrictwithoutID() throws IOException {
        String response = RchImportTestHelper.getDistrictLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_District_without_id.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("districtendpoint", 200, response);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_DISTRICT_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readDistrictResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<District> districts = districtDataService.retrieveAll();
        assertEquals(1, districts.size());

    }


    /*Verify import XML file with Invalid  District_ID and with District_Name */

    @Test
    public void testDistrictwithInvalidID() throws IOException {
        String response = RchImportTestHelper.getDistrictLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_invalid_district_id.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("districtendpoint", 200, response);
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
        MotechEvent event1 = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_DISTRICT_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readDistrictResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<District> districts = districtDataService.retrieveAll();
        assertEquals(1, districts.size());
    }

    /*Import location data with duplicate Taluka Id in different state*/

    @Test
    public void testDuplicateTalukaidInDiffState() throws IOException {
        String response = RchImportTestHelper.getTalukaLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_Taluka_import.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("talukaendpoint", 200, response);
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
//         MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_TALUKA_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readTalukaResponseFromFile(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<Taluka> talukas = talukaDataService.retrieveAll();
        assertEquals(2, talukas.size());

        // Importing Duplicate Taluka_id in different state via XML

        State state1 = stateDataService.create(new State("Other State", 22L));

        District district1 = new District();
        district1.setCode(5L);
        district1.setState(state1);
        district1.setName("District_New");
        district1.setRegionalName("Regional Name 5");
        districtDataService.create(district1);

        String response1 = RchImportTestHelper.getTalukaLocationdataResponse();
        File filepath1 = new File("src/test/resources/rch");
        String remoteLocation1 = filepath1.getAbsolutePath();
        String fileName1 = "RCH_dup_taluka_id.xml";
        SimpleHttpServer simpleServer1 = SimpleHttpServer.getInstance();
        String url1 = simpleServer1.start("talukaendpoint", 200, response1);
        URL endpoint1 = new URL(url1);
        LocalDate lastDateToCheck1 = DateUtil.today().minusDays(1);
        LocalDate yesterday1 = DateUtil.today().minusDays(1);
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(rchWsImportService.getClass().getClassLoader());
        Map<String, Object> params1 = new HashMap<>();
        params1.put(Constants.START_DATE_PARAM, lastDateToCheck1);
        params1.put(Constants.END_DATE_PARAM, yesterday1);
        params1.put(Constants.STATE_ID_PARAM, 22L);
        params1.put(Constants.ENDPOINT_PARAM, endpoint1);
        params1.put(Constants.REMOTE_LOCATION, remoteLocation1);
        params1.put(Constants.FILE_NAME, fileName1);
//        MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(Constants.RCH_TALUKA_READ_SUBJECT, params1);
        try {
            rchWebServiceFacade.readTalukaResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl1);
        List<Taluka> talukas1 = talukaDataService.retrieveAll();
        assertEquals(3, talukas1.size());
    }




    /*Verify import taluka XML file with Taluka_ID and Taluka_Name without District_ID  */

    @Test
    public void testTalukaWithoutDistrictID() throws IOException {
        String response = RchImportTestHelper.getTalukaLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_without_districtid_taluka_import.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("talukaendpoint", 200, response);
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
        MotechEvent event1 = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_TALUKA_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readTalukaResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<Taluka> talukas = talukaDataService.retrieveAll();
        assertEquals(1, talukas.size());


    }

    /* Verify import taluka XML file with invalid taluka ID/code*/

    @Test
    public void testTalukaWithInvalidID() throws IOException {
        String response = RchImportTestHelper.getTalukaLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_invalid_taluka_id.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("talukaendpoint", 200, response);
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
        rchWsImportService.importTalukaFromRch(a, yesterday, endpoint);
        MotechEvent event1 = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_TALUKA_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readTalukaResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<Taluka> talukas = talukaDataService.retrieveAll();
        assertEquals(2, talukas.size());


    }

    /* Verify import taluka XML file with invalid District ID/code */

    @Test
    public void testTalukaWithInvalidDistrictID() throws IOException {
        String response = RchImportTestHelper.getTalukaLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_invalid_districtid_taluka_import.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("talukaendpoint", 200, response);
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
        rchWsImportService.importTalukaFromRch(a, yesterday, endpoint);
        MotechEvent event1 = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_TALUKA_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readTalukaResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List talukas = talukaDataService.retrieveAll();
        assertEquals(1, talukas.size());
    }



    /*Verify import XML file without HealthBlock_Id and with HealthBlock_name,District_ID, Taluka_ID */

    @Test
    public void testHealthBlockWithoutId() throws IOException {
        String response = RchImportTestHelper.getHealthBlockLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_without_HealthBlock_id.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("healthblockendpoint", 200, response);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(Constants.RCH_HEALTHBLOCK_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readHealthBlockResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<HealthBlock> healthBlocks = healthBlockDataService.retrieveAll();
        assertEquals(1, healthBlocks.size());
    }




    /*Verify import  HealthFacility XML file without ID and with HealthFacility_Name,HealthBlock_ID, District_ID, Taluka_ID */

    @Test
    public void testHealthFacilityWithoutId() throws IOException {
        String response = RchImportTestHelper.getHealthFacilityLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_without_HealthFacility_id.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("healthfacilityendpoint", 200, response);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(Constants.RCH_HEALTHFACILITY_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readHealthFacilityResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<HealthFacility> healthFacilities = healthFacilityDataService.retrieveAll();
        assertEquals(1, healthFacilities.size());
    }



    /*Verify import HealthSubFacility XML file without id and with HealthSubFacility_Name,
    HealthFacility_ID,District_ID,Taluka_ID */

    @Test
    public void testHealthSubFacilityWithoutId() throws IOException {
        String response = RchImportTestHelper.getHealthSubFacilityLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_without_HealthSubFacility_id.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("healthsubfacilityendpoint", 200, response);
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
        // MotechEvent event = new MotechEvent("foobar", params);
        MotechEvent event1 = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_HEALTHSUBFACILITY_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readHealthSubFacilityResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<HealthSubFacility> healthSubFacilities = healthSubFacilityDataService.retrieveAll();
        assertEquals(1, healthSubFacilities.size());


    }



    /*Verify import Village Xml file without village_id  and with name,district_id,taluka_id*/

    @Test
    public void testVillageWithoutId() throws IOException {
        String response = RchImportTestHelper.getVillageLocationdataResponse();
        File filepath = new File("src/test/resources/rch");
        String remoteLocation = filepath.getAbsolutePath();
        String fileName = "RCH_without_Village_id.xml";
        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("villageendpoint", 200, response);
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
        MotechEvent event1 = new MotechEvent(org.motechproject.nms.rch.utils.Constants.RCH_VILLAGE_READ_SUBJECT, params);
        try {
            rchWebServiceFacade.readVillageResponseFromFile(event1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.currentThread().setContextClassLoader(cl);
        List<Village> villages = villageDataService.retrieveAll();
        assertEquals(0, villages.size());


    }
}


