package org.motechproject.nms.testing.it.flw;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.mds.ex.JdoListenerInvocationException;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.WhitelistEntryDataService;
import org.motechproject.nms.flw.repository.WhitelistStateDataService;
import org.motechproject.nms.flw.service.FlwSettingsService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.service.ServiceUsageService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Verify that FrontLineWorkerService present, functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class FrontLineWorkerServiceBundleIT extends BasePaxIT {

    @Inject
    FrontLineWorkerDataService frontLineWorkerDataService;
    @Inject
    FrontLineWorkerService frontLineWorkerService;
    @Inject
    ServiceUsageService serviceUsageService;
    @Inject
    DistrictDataService districtDataService;
    @Inject
    DistrictService districtService;
    @Inject
    LanguageDataService languageDataService;
    @Inject
    StateDataService stateDataService;
    @Inject
    CircleDataService circleDataService;
    @Inject
    WhitelistEntryDataService whitelistEntryDataService;
    @Inject
    WhitelistStateDataService whitelistStateDataService;
    @Inject
    TestingService testingService;


    private State sampleState;

    private static final String WEEKS_TO_KEEP_INVALID_FLWS = "flw.weeks_to_keep_invalid_flws";

    private static final String FLW_PURGE_EVENT_SUBJECT = "nms.flw.purge_invalid_flw";

    @Inject
    private FlwSettingsService flwSettingsService;

    private SettingsFacade settingsFacade;

    @Inject
    private EventRelay eventRelay;

    String oldWeeksToKeepInvalidFLWs;

    @Before
    public void doTheNeedful() {
        testingService.clearDatabase();

        settingsFacade = flwSettingsService.getSettingsFacade();
        oldWeeksToKeepInvalidFLWs = settingsFacade
                .getProperty(WEEKS_TO_KEEP_INVALID_FLWS);
    }

    @After
    public void restore() {
        settingsFacade.setProperty(WEEKS_TO_KEEP_INVALID_FLWS,
                oldWeeksToKeepInvalidFLWs);
    }

    private void createLanguageLocationData() {
        Language ta = languageDataService.create(new Language("50", "tamil"));

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setLanguage(ta);
        district.setCode(1L);

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);
        state.getDistricts().add(district);
        sampleState = stateDataService.create(state);

        Circle circle = new Circle("AA");
        circle.setDefaultLanguage(ta);
        circleDataService.create(circle);
    }

    @Test
    public void testFrontLineWorkerServicePresent() throws Exception {
        assertNotNull(frontLineWorkerService);
    }

    @Test
    public void testPurgeOldInvalidFrontLineWorkers() {
        // FLW1 & 2 Should be purged, the others should remain

        FrontLineWorker flw1 = new FrontLineWorker("Test Worker", 1111111110L);
        frontLineWorkerService.add(flw1);
        flw1.setStatus(FrontLineWorkerStatus.INVALID);
        flw1.setInvalidationDate(new DateTime().withDate(2011, 8, 1));
        frontLineWorkerService.update(flw1);

        FrontLineWorker flw2 = new FrontLineWorker("Test Worker", 1111111111L);
        frontLineWorkerService.add(flw2);
        flw2.setStatus(FrontLineWorkerStatus.INVALID);
        flw2.setInvalidationDate(new DateTime().now().minusWeeks(6).minusDays(1));
        frontLineWorkerService.update(flw2);

        FrontLineWorker flw3 = new FrontLineWorker("Test Worker", 1111111112L);
        frontLineWorkerService.add(flw3);
        flw3.setStatus(FrontLineWorkerStatus.INVALID);
        flw3.setInvalidationDate(new DateTime().now().minusWeeks(6));
        frontLineWorkerService.update(flw3);

        FrontLineWorker flw4 = new FrontLineWorker("Test Worker", 2111111111L);
        frontLineWorkerService.add(flw4);
        flw4.setInvalidationDate(new DateTime().withDate(2011, 8, 1));
        frontLineWorkerService.update(flw4);

        FrontLineWorker flw5 = new FrontLineWorker("Test Worker", 2111111112L);
        frontLineWorkerService.add(flw5);
        flw5.setStatus(FrontLineWorkerStatus.INVALID);
        frontLineWorkerService.update(flw5);

        FrontLineWorker flw6 = new FrontLineWorker("Test Worker", 2111111113L);
        frontLineWorkerService.add(flw6);

        List<FrontLineWorker> records = frontLineWorkerService.getRecords();
        assertEquals(6, records.size());
        assertTrue(records.contains(flw1));
        assertTrue(records.contains(flw2));
        assertTrue(records.contains(flw3));
        assertTrue(records.contains(flw4));
        assertTrue(records.contains(flw5));
        assertTrue(records.contains(flw6));

        frontLineWorkerService.purgeOldInvalidFLWs(new MotechEvent());
        records = frontLineWorkerService.getRecords();
        assertEquals(4, records.size());
        assertFalse(records.contains(flw1));
        assertFalse(records.contains(flw2));
        assertTrue(records.contains(flw3));
        assertTrue(records.contains(flw4));
        assertTrue(records.contains(flw5));
        assertTrue(records.contains(flw6));
    }

    @Test
    public void testFrontLineWorkerService() throws Exception {
        FrontLineWorker flw = new FrontLineWorker("Test Worker", 1111111111L);
        frontLineWorkerService.add(flw);

        FrontLineWorker otherFlw = frontLineWorkerDataService.findByContactNumber(1111111111L);
        assertNotNull(otherFlw);

        FrontLineWorker record = frontLineWorkerService.getByContactNumber(flw.getContactNumber());
        assertEquals(flw, record);

        List<FrontLineWorker> records = frontLineWorkerService.getRecords();
        assertTrue(records.contains(flw));

        flw.setStatus(FrontLineWorkerStatus.INVALID);
        flw.setInvalidationDate(new DateTime().withDate(2011, 8, 1));
        frontLineWorkerService.update(flw);
        frontLineWorkerService.delete(flw);
        record = frontLineWorkerService.getByContactNumber(flw.getContactNumber());
        assertNull(record);
    }

    /**
     * NMS_FT_515 : To verify that status of Active flw is set to "Invalid" successfully
     */
    @Test
    public void testFrontLineWorkerUpdate() {
        createLanguageLocationData();

        District district = districtService.findByStateAndCode(sampleState, 1L);
        Language language = languageDataService.findByCode("50");

        FrontLineWorker flw = new FrontLineWorker("Test Worker", 2111111111L);
        frontLineWorkerService.add(flw);
        flw = frontLineWorkerService.getByContactNumber(2111111111L);

        assertEquals(FrontLineWorkerStatus.ANONYMOUS, flw.getStatus());

        flw.setState(sampleState);
        flw.setDistrict(district);
        flw.setName("Frank Huster");
        flw.setLanguage(language);

        frontLineWorkerService.update(flw);
        flw = frontLineWorkerService.getByContactNumber(2111111111L);
        assertEquals(FrontLineWorkerStatus.ACTIVE, flw.getStatus());

        flw.setStatus(FrontLineWorkerStatus.INVALID);
        frontLineWorkerService.update(flw);
        flw = frontLineWorkerService.getById(flw.getId());
        assertEquals(FrontLineWorkerStatus.INVALID, flw.getStatus());
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testDeleteNonInvalidFrontLineWorker() {
        FrontLineWorker flw = new FrontLineWorker("Test Worker", 2111111111L);
        frontLineWorkerService.add(flw);
        flw = frontLineWorkerService.getByContactNumber(2111111111L);

        assertEquals(FrontLineWorkerStatus.ANONYMOUS, flw.getStatus());

        exception.expect(JdoListenerInvocationException.class);
        frontLineWorkerService.delete(flw);
    }

    @Test
    public void testDeleteRecentInvalidFrontLineWorker() {
        FrontLineWorker flw = new FrontLineWorker("Test Worker", 2111111111L);
        frontLineWorkerService.add(flw);

        flw = frontLineWorkerService.getByContactNumber(2111111111L);
        flw.setStatus(FrontLineWorkerStatus.INVALID);
        frontLineWorkerService.update(flw);

        flw = frontLineWorkerService.getById(flw.getId());
        assertEquals(FrontLineWorkerStatus.INVALID, flw.getStatus());

        exception.expect(JdoListenerInvocationException.class);
        frontLineWorkerService.delete(flw);
    }

    @Test
    public void testDeleteOldInvalidFrontLineWorker() {
        FrontLineWorker flw = new FrontLineWorker("Test Worker", 2111111111L);
        frontLineWorkerService.add(flw);

        flw = frontLineWorkerService.getByContactNumber(2111111111L);
        flw.setStatus(FrontLineWorkerStatus.INVALID);
        flw.setInvalidationDate(new DateTime().withDate(2011, 8, 1));
        frontLineWorkerService.update(flw);

        flw = frontLineWorkerService.getById(flw.getId());
        assertEquals(FrontLineWorkerStatus.INVALID, flw.getStatus());

        frontLineWorkerService.delete(flw);
    }

    /**
     * To disable automatic deletion of all records of beneficiary which were
     * marked invalid 6 weeks ago.
     * 
     * @throws InterruptedException
     */
    // TODO https://applab.atlassian.net/browse/NMS-257
    @Test
    public void verifyFT548() throws InterruptedException {
        Map<String, Object> eventParams = new HashMap<>();
        MotechEvent motechEvent = new MotechEvent(FLW_PURGE_EVENT_SUBJECT,
                eventParams);

        FrontLineWorker flw = new FrontLineWorker("Test Worker", 2111111111L);
        frontLineWorkerService.add(flw);
        flw = frontLineWorkerService.getByContactNumber(2111111111L);
        flw.setStatus(FrontLineWorkerStatus.INVALID);
        flw.setInvalidationDate(DateTime.now().minusWeeks(7));
        frontLineWorkerService.update(flw);
        
        //call purge event
        frontLineWorkerService.purgeOldInvalidFLWs(motechEvent);

        // assert flW deleted
        flw = frontLineWorkerService.getByContactNumber(2111111111L);
        assertNull(flw);

        // change configuration to disable deletion by setting weeks to large value
        settingsFacade.setProperty(WEEKS_TO_KEEP_INVALID_FLWS, "1000");

        // add new invalidated flw
        flw = new FrontLineWorker("Test Worker", 2111111111L);
        flw.setFlwId("FlwId");
        frontLineWorkerService.add(flw);
        flw = frontLineWorkerService.getByContactNumber(2111111111L);
        flw.setStatus(FrontLineWorkerStatus.INVALID);
        // set invalid date to 2 years back
        flw.setInvalidationDate(DateTime.now().minusYears(2));
        frontLineWorkerService.update(flw);

        //call purge event
        frontLineWorkerService.purgeOldInvalidFLWs(motechEvent);

        // assert flW not deleted
        flw = frontLineWorkerService.getByFlwId("FlwId");
        assertNotNull(flw);
    }

    /**
     * To verify that status of Anonymous flw is set to "Invalid" successfully
     */
    @Test
    public void verifyFT514() {
        FrontLineWorker flw = new FrontLineWorker("Test Worker", 2111111111L);
        flw.setStatus(FrontLineWorkerStatus.ANONYMOUS);
        frontLineWorkerService.add(flw);

        flw = frontLineWorkerService.getByContactNumber(2111111111L);
        flw.setStatus(FrontLineWorkerStatus.INVALID);
        flw.setInvalidationDate(new DateTime().withDate(2011, 8, 1));
        frontLineWorkerService.update(flw);

        flw = frontLineWorkerService.getById(flw.getId());
        assertEquals(FrontLineWorkerStatus.INVALID, flw.getStatus());
        assertNull(flw.getContactNumber());
    }

    /**
     * To verify that status of Inactive flw is set to "Invalid" successfully
     */
    @Test
    public void verifyFT516() {
        createLanguageLocationData();

        District district = districtService.findByStateAndCode(sampleState, 1L);
        Language language = languageDataService.findByCode("50");
        FrontLineWorker flw = new FrontLineWorker("Test Worker", 2111111111L);
        flw.setState(sampleState);
        flw.setDistrict(district);
        flw.setLanguage(language);
        frontLineWorkerService.add(flw);

        flw = frontLineWorkerService.getByContactNumber(2111111111L);
        flw.setStatus(FrontLineWorkerStatus.INVALID);
        flw.setInvalidationDate(new DateTime().withDate(2011, 8, 1));
        frontLineWorkerService.update(flw);

        flw = frontLineWorkerService.getById(flw.getId());
        assertEquals(FrontLineWorkerStatus.INVALID, flw.getStatus());
        assertNull(flw.getContactNumber());
    }
}
