package org.motechproject.nms.testing.it.mcts;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.event.MotechEvent;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.exception.FlwExistingRecordException;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.mcts.domain.MctsImportAudit;
import org.motechproject.nms.mcts.domain.MctsImportFailRecord;
import org.motechproject.nms.mcts.domain.MctsUserType;
import org.motechproject.nms.mcts.repository.MctsImportAuditDataService;
import org.motechproject.nms.mcts.repository.MctsImportFailRecordDataService;
import org.motechproject.nms.mcts.service.MctsWsImportService;
import org.motechproject.nms.mcts.utils.Constants;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthFacilityType;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.it.mcts.util.MockWsHttpServlet;
import org.motechproject.nms.testing.it.mcts.util.MockWsHttpServletForASHAValidation;
import org.motechproject.nms.testing.it.mcts.util.MockWsHttpServletForFail;
import org.motechproject.nms.testing.it.mcts.util.MockWsHttpServletForNoUpdateDate;
import org.motechproject.nms.testing.it.mcts.util.MockWsHttpServletForOneUpdateDate;
import org.motechproject.nms.testing.it.mcts.util.MockWsHttpServletRemoteException;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.utils.TestContext;
import org.motechproject.testing.utils.TimeFaker;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class MctsImportBundleIT extends BasePaxIT {

    @Inject
    private MctsWsImportService mctsWsImportService;

    @Inject
    private HttpService httpService;

    @Inject
    private MctsMotherDataService mctsMotherDataService;

    @Inject
    private MctsChildDataService mctsChildDataService;

    @Inject
    private MctsImportAuditDataService mctsImportAuditDataService;

    @Inject
    private MctsImportFailRecordDataService mctsImportFailRecordDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;

    @Inject
    private FrontLineWorkerDataService flwDataService;

    @Inject
    private TestingService testingService;

    @Inject
    private SettingsService settingsService;

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

        healthBlock.setTaluka(taluka);
        taluka.setHealthBlocks(new ArrayList<>(singletonList(healthBlock)));

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


        httpService.registerServlet("/mctsWs", new MockWsHttpServlet(), null, null);
        httpService.registerServlet("/mctsWsFailedStructure", new MockWsHttpServletForFail(), null, null);
        httpService.registerServlet("/mctsWsASHAValidation", new MockWsHttpServletForASHAValidation(), null, null);
        httpService.registerServlet("/mctsWsRemoteException", new MockWsHttpServletRemoteException(), null, null);
        httpService.registerServlet("/mctsWsNoUpdateDate", new MockWsHttpServletForNoUpdateDate(), null, null);
        httpService.registerServlet("/mctsWsOneUpdateDate", new MockWsHttpServletForOneUpdateDate(), null, null);

    }

    @After
    public void tearDown() {
        testingService.clearDatabase();
        httpService.unregister("/mctsWs");
        httpService.unregister("/mctsWsFailedStructure");
        httpService.unregister("/mctsWsASHAValidation");
        httpService.unregister("/mctsWsRemoteException");
        httpService.unregister("/mctsWsNoUpdateDate");
        httpService.unregister("/mctsWsOneUpdateDate");
    }

    @Test
    public void shouldUpdateFailedTableWhenImportFailsDueToFailedStructure() throws MalformedURLException {
        URL endpoint = new URL(String.format("http://localhost:%d/mctsWsFailedStructure", TestContext.getJettyPort()));
        LocalDate lastDateToCheck = DateUtil.today().minusDays(7);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        List<Long> stateIds = singletonList(21L);

        // this CL workaround is for an issue with PAX IT logging messing things up
        // shouldn't affect production
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mctsWsImportService.getClass().getClassLoader());

        // setup motech event
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        MotechEvent event = new MotechEvent("foobar", params);
        mctsWsImportService.importMothersData(event);
        mctsWsImportService.importChildrenData(event);
        mctsWsImportService.importAnmAshaData(event);
        Thread.currentThread().setContextClassLoader(cl);

//        Since the structure is wrong in the xmls, the import should not take place and the data should be updated in nms_mcts_failure table
        List<MctsImportFailRecord> mctsImportFailRecords = mctsImportFailRecordDataService.retrieveAll();
        assertEquals(3, mctsImportFailRecords.size());

    }

    @Test
    public void shouldUpdateFailedTableWhenImportFailsDueRemoteException() throws MalformedURLException {
        URL endpoint = new URL(String.format("http://localhost:%d/mctsWsRemoteException", TestContext.getJettyPort()));
        LocalDate lastDateToCheck = DateUtil.today().minusDays(7);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        List<Long> stateIds = singletonList(21L);

        // this CL workaround is for an issue with PAX IT logging messing things up
        // shouldn't affect production
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mctsWsImportService.getClass().getClassLoader());

        // setup motech event
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        MotechEvent event = new MotechEvent("foobar", params);
        mctsWsImportService.importMothersData(event);
        mctsWsImportService.importChildrenData(event);
        mctsWsImportService.importAnmAshaData(event);
        Thread.currentThread().setContextClassLoader(cl);

//        Since the response while reading the xmls is a Remote server exception, the import should not take place and the data should be updated in nms_mcts_failure table
        List<MctsImportFailRecord> mctsImportFailRecords = mctsImportFailRecordDataService.retrieveAll();
        assertEquals(3, mctsImportFailRecords.size());


    }

    @Test
    public void shouldRejectNonASHAWorkers() throws MalformedURLException {
        URL endpoint = new URL(String.format("http://localhost:%d/mctsWsASHAValidation", TestContext.getJettyPort()));
        LocalDate lastDateToCheck = DateUtil.today().minusDays(7);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        List<Long> stateIds = singletonList(21L);

        // this CL workaround is for an issue with PAX IT logging messing things up
        // shouldn't affect production
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mctsWsImportService.getClass().getClassLoader());

        // setup motech event
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.START_DATE_PARAM, lastDateToCheck);
        params.put(Constants.END_DATE_PARAM, yesterday);
        params.put(Constants.STATE_ID_PARAM, 21L);
        params.put(Constants.ENDPOINT_PARAM, endpoint);
        MotechEvent event = new MotechEvent("foobar", params);
        mctsWsImportService.importAnmAshaData(event);
        Thread.currentThread().setContextClassLoader(cl);

//        Should reject non ASHA FLWs
        List<FrontLineWorker> flws = flwDataService.retrieveAll();
        assertEquals(1, flws.size());
        assertEquals("ASHA",flws.get(0).getDesignation());

    }


    @Test
    public void shouldPerformImportWithUpdatesAndDeleteInFailedTable() throws MalformedURLException {
        URL endpoint = new URL(String.format("http://localhost:%d/mctsWs", TestContext.getJettyPort()));

        LocalDate lastDateToCheck = DateUtil.today().minusDays(7);
        LocalDate failDate = DateUtil.today().minusDays(2);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        MctsImportFailRecord mctsImportFailRecord1 = new MctsImportFailRecord(failDate, MctsUserType.ASHA, 21L);

        MctsImportFailRecord mctsImportFailRecord2 = new MctsImportFailRecord(failDate, MctsUserType.MOTHER, 21L);

        MctsImportFailRecord mctsImportFailRecord3 = new MctsImportFailRecord(failDate, MctsUserType.CHILD, 21L);

        mctsImportFailRecordDataService.create(mctsImportFailRecord1);
        mctsImportFailRecordDataService.create(mctsImportFailRecord2);
        mctsImportFailRecordDataService.create(mctsImportFailRecord3);

        try {
            TimeFaker.fakeToday(DateUtil.newDate(2015, 7, 24));
            // this CL workaround is for an issue with PAX IT logging messing things up
            // shouldn't affect production
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(mctsWsImportService.getClass().getClassLoader());

            // setup motech event
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.START_DATE_PARAM, lastDateToCheck);
            params.put(Constants.END_DATE_PARAM, yesterday);
            params.put(Constants.STATE_ID_PARAM, 21L);
            params.put(Constants.ENDPOINT_PARAM, endpoint);
            MotechEvent event = new MotechEvent("foobar", params);

            /* Hard to test this since we do async loading now, using test hook. UT already tests message distribution */
            mctsWsImportService.importMothersData(event);
            mctsWsImportService.importChildrenData(event);
            mctsWsImportService.importAnmAshaData(event);
            Thread.currentThread().setContextClassLoader(cl);

            // we expect two of each - the second entry in each ds (4 total) has wrong location data and the first one is a duplicate of the fourth record with updated date. So the updated record should stay. The audit table should update with three errors created manually above. And after the import the three errors should clear from failure table.
            List<MctsImportAudit> mctsImportAudits = mctsImportAuditDataService.retrieveAll();
            assertEquals(3, mctsImportAudits.size());
            assertEquals(2, mctsImportAudits.get(0).getAccepted());
            assertEquals(2, mctsImportAudits.get(0).getRejected());
            assertEquals(2, mctsImportAudits.get(1).getAccepted());
            assertEquals(2, mctsImportAudits.get(1).getRejected());
            assertEquals(2, mctsImportAudits.get(2).getAccepted());
            assertEquals(2, mctsImportAudits.get(2).getRejected());
            assertEquals(lastDateToCheck, mctsImportAudits.get(0).getStartImportDate());
            assertEquals(yesterday, mctsImportAudits.get(0).getEndImportDate());

            List<FrontLineWorker> flws = flwDataService.retrieveAll();
            assertEquals(2, flws.size());

            List<MctsImportFailRecord> mctsImportFailRecords = mctsImportFailRecordDataService.retrieveAll();
            assertEquals(0, mctsImportFailRecords.size());
            assertEquals("Name a", flws.get(0).getName());

            List<MctsChild> children = mctsChildDataService.retrieveAll();
            assertEquals(2, children.size());
            assertEquals("Name y", children.get(0).getName());

            List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
            assertEquals(4, mothers.size());  // 2 records from mother import and 2 from child
            assertEquals("Name x", mothers.get(0).getName());
        } finally {
            TimeFaker.stopFakingTime();
        }


    }

    @Test
    public void shouldPerformImportWithUpdatesAndDeleteInFailedTableNoUpdateDate() throws MalformedURLException {
        URL endpoint = new URL(String.format("http://localhost:%d/mctsWsNoUpdateDate", TestContext.getJettyPort()));

        LocalDate lastDateToCheck = DateUtil.today().minusDays(7);
        LocalDate failDate = DateUtil.today().minusDays(2);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        MctsImportFailRecord mctsImportFailRecord1 = new MctsImportFailRecord(failDate, MctsUserType.ASHA, 21L);

        MctsImportFailRecord mctsImportFailRecord2 = new MctsImportFailRecord(failDate, MctsUserType.MOTHER, 21L);

        MctsImportFailRecord mctsImportFailRecord3 = new MctsImportFailRecord(failDate, MctsUserType.CHILD, 21L);

        mctsImportFailRecordDataService.create(mctsImportFailRecord1);
        mctsImportFailRecordDataService.create(mctsImportFailRecord2);
        mctsImportFailRecordDataService.create(mctsImportFailRecord3);

        try {
            TimeFaker.fakeToday(DateUtil.newDate(2015, 7, 24));
            // this CL workaround is for an issue with PAX IT logging messing things up
            // shouldn't affect production
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(mctsWsImportService.getClass().getClassLoader());

            // setup motech event
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.START_DATE_PARAM, lastDateToCheck);
            params.put(Constants.END_DATE_PARAM, yesterday);
            params.put(Constants.STATE_ID_PARAM, 21L);
            params.put(Constants.ENDPOINT_PARAM, endpoint);
            MotechEvent event = new MotechEvent("foobar", params);

            /* Hard to test this since we do async loading now, using test hook. UT already tests message distribution */
            mctsWsImportService.importMothersData(event);
            mctsWsImportService.importChildrenData(event);
            mctsWsImportService.importAnmAshaData(event);
            Thread.currentThread().setContextClassLoader(cl);


            // we expect two of each - the second entry in each ds (4 total) has wrong location data and the first one is a duplicate of the fourth record with no updated dates on any record. So only one of the duplicates should be in the database. And after the import the three errors should clear from failure table.
            List<FrontLineWorker> flws = flwDataService.retrieveAll();
            assertEquals(2, flws.size());

            List<MctsImportFailRecord> mctsImportFailRecords = mctsImportFailRecordDataService.retrieveAll();
            assertEquals(0, mctsImportFailRecords.size());
            assertEquals("Sample Name 1", flws.get(0).getName());

            List<MctsChild> children = mctsChildDataService.retrieveAll();
            assertEquals(2, children.size());
            assertEquals("Name 1", children.get(0).getName());

            List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
            assertEquals(4, mothers.size());  // 2 records from mother import and 2 from child
            assertEquals("Name 1", mothers.get(0).getName());

        } finally {
            TimeFaker.stopFakingTime();
        }
    }

    @Test
    public void shouldPerformImportWithUpdatesAndDeleteInFailedTableOneUpdateDate() throws MalformedURLException {
        URL endpoint = new URL(String.format("http://localhost:%d/mctsWsOneUpdateDate", TestContext.getJettyPort()));

        LocalDate lastDateToCheck = DateUtil.today().minusDays(7);
        LocalDate failDate = DateUtil.today().minusDays(2);
        LocalDate yesterday = DateUtil.today().minusDays(1);
        MctsImportFailRecord mctsImportFailRecord1 = new MctsImportFailRecord(failDate, MctsUserType.ASHA, 21L);

        MctsImportFailRecord mctsImportFailRecord2 = new MctsImportFailRecord(failDate, MctsUserType.MOTHER, 21L);

        MctsImportFailRecord mctsImportFailRecord3 = new MctsImportFailRecord(failDate, MctsUserType.CHILD, 21L);

        mctsImportFailRecordDataService.create(mctsImportFailRecord1);
        mctsImportFailRecordDataService.create(mctsImportFailRecord2);
        mctsImportFailRecordDataService.create(mctsImportFailRecord3);


        try {
            TimeFaker.fakeToday(DateUtil.newDate(2015, 7, 24));
            // this CL workaround is for an issue with PAX IT logging messing things up
            // shouldn't affect production
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(mctsWsImportService.getClass().getClassLoader());

            // setup motech event
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.START_DATE_PARAM, lastDateToCheck);
            params.put(Constants.END_DATE_PARAM, yesterday);
            params.put(Constants.STATE_ID_PARAM, 21L);
            params.put(Constants.ENDPOINT_PARAM, endpoint);
            MotechEvent event = new MotechEvent("foobar", params);

            /* Hard to test this since we do async loading now, using test hook. UT already tests message distribution */
            mctsWsImportService.importMothersData(event);
            mctsWsImportService.importChildrenData(event);
            mctsWsImportService.importAnmAshaData(event);
            Thread.currentThread().setContextClassLoader(cl);

            // we expect one of each - the first entry in each ds (2 total) has an updated dated unlike the previous data. So only the one with updated date should be in the database. And after the import the three errors should clear from failure table.
            List<FrontLineWorker> flws = flwDataService.retrieveAll();
            assertEquals(1, flws.size());

            List<MctsImportFailRecord> mctsImportFailRecords = mctsImportFailRecordDataService.retrieveAll();
            assertEquals(0, mctsImportFailRecords.size());
            assertEquals("Name a", flws.get(0).getName());

            List<MctsChild> children = mctsChildDataService.retrieveAll();
            assertEquals(1, children.size());
            assertEquals("Name y", children.get(0).getName());

            List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
            assertEquals(2, mothers.size());  // 1 record from mother import and 1 from child
            assertEquals("Name x", mothers.get(0).getName());

        } finally {
            TimeFaker.stopFakingTime();
        }

    }

    @Test
    @Ignore
    public void shouldFilterHpdImport() throws MalformedURLException {
        URL endpoint = new URL(String.format("http://localhost:%d/mctsWs", TestContext.getJettyPort()));
        LocalDate yesterday = DateUtil.today().minusDays(1);
        LocalDate lastDayToCheck = DateUtil.today().minusDays(7);
        List<Long> stateIds = singletonList(21L);

        // setup config
        String originalStateFilter = settingsService.getSettingsFacade().getProperty("mcts.hpd.states");
        settingsService.getSettingsFacade().setProperty("mcts.hpd.states", "21");
        settingsService.getSettingsFacade().setProperty("mcts.hpd.states21", "9");

        try {
            TimeFaker.fakeToday(DateUtil.newDate(2015, 7, 24));
            // this CL workaround is for an issue with PAX IT logging messing things up
            // shouldn't affect production
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(mctsWsImportService.getClass().getClassLoader());

            // setup motech event
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.START_DATE_PARAM, lastDayToCheck);
            params.put(Constants.END_DATE_PARAM, yesterday);
            params.put(Constants.STATE_ID_PARAM, 21L);
            params.put(Constants.ENDPOINT_PARAM, endpoint);
            MotechEvent event = new MotechEvent("foobar", params);

            /* Hard to test this since we do async loading now, using test hook. UT already tests message distribution */
            mctsWsImportService.importMothersData(event);
            mctsWsImportService.importChildrenData(event);
            mctsWsImportService.importAnmAshaData(event);
            Thread.currentThread().setContextClassLoader(cl);


            // we expect two of each - the second entry in each ds (3 total) has wrong location data
            List<MctsMother> mothers = mctsMotherDataService.retrieveAll();
            assertEquals(2, mothers.size());

            List<MctsChild> children = mctsChildDataService.retrieveAll();
            assertEquals(2, children.size());

            List<FrontLineWorker> flws = flwDataService.retrieveAll();
            assertEquals(2, flws.size());

        } finally {
            TimeFaker.stopFakingTime();
            // revert config
            settingsService.getSettingsFacade().setProperty("mcts.hpd.states", originalStateFilter);
        }
    }

}

