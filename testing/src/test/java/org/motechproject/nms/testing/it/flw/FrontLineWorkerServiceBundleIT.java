package org.motechproject.nms.testing.it.flw;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.motechproject.event.MotechEvent;
import org.motechproject.mds.ex.JdoListenerInvocationException;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.repository.ServiceUsageDataService;
import org.motechproject.nms.flw.repository.WhitelistEntryDataService;
import org.motechproject.nms.flw.repository.WhitelistStateDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.service.ServiceUsageService;
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
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.util.List;

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
    private FrontLineWorkerDataService frontLineWorkerDataService;

    @Inject
    private FrontLineWorkerService frontLineWorkerService;

    @Inject
    private ServiceUsageDataService serviceUsageDataService;

    @Inject
    private ServiceUsageService serviceUsageService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    private LanguageDataService languageDataService;

    @Inject
    private LanguageLocationDataService languageLocationDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private CircleDataService circleDataService;

    @Inject
    private WhitelistEntryDataService whitelistEntryDataService;

    @Inject
    private WhitelistStateDataService whitelistStateDataService;

    @Inject
    private TestingService testingService;

    private void setupData() {
        testingService.clearDatabase();

        for (FrontLineWorker flw: frontLineWorkerDataService.retrieveAll()) {
            flw.setStatus(FrontLineWorkerStatus.INVALID);
            flw.setInvalidationDate(new DateTime().withDate(2011, 8, 1));

            frontLineWorkerDataService.update(flw);
        }

        serviceUsageDataService.deleteAll();
        frontLineWorkerDataService.deleteAll();
        languageLocationDataService.deleteAll();
        languageDataService.deleteAll();
        districtDataService.deleteAll();
        whitelistStateDataService.deleteAll();
        whitelistEntryDataService.deleteAll();
        stateDataService.deleteAll();
        circleDataService.deleteAll();
    }

    private void createLanguageLocationData() {
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
    }

    @Test
    public void testFrontLineWorkerServicePresent() throws Exception {
        assertNotNull(frontLineWorkerService);
    }

    @Test
    public void testPurgeOldInvalidFrontLineWorkers() {
        setupData();

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
        setupData();
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

    @Test
    public void testFrontLineWorkerUpdate() {
        setupData();
        createLanguageLocationData();

        District district = districtDataService.findByName("District 1");
        LanguageLocation languageLocation = languageLocationDataService.findByCode("50");

        FrontLineWorker flw = new FrontLineWorker("Test Worker", 2111111111L);
        frontLineWorkerService.add(flw);
        flw = frontLineWorkerService.getByContactNumber(2111111111L);

        assertEquals(FrontLineWorkerStatus.ANONYMOUS, flw.getStatus());

        flw.setDistrict(district);
        flw.setName("Frank Huster");
        flw.setLanguageLocation(languageLocation);

        frontLineWorkerService.update(flw);
        flw = frontLineWorkerService.getByContactNumber(2111111111L);
        assertEquals(FrontLineWorkerStatus.ACTIVE, flw.getStatus());

        flw.setStatus(FrontLineWorkerStatus.INVALID);
        frontLineWorkerService.update(flw);
        flw = frontLineWorkerService.getByContactNumber(2111111111L);
        assertEquals(FrontLineWorkerStatus.INVALID, flw.getStatus());
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testDeleteNonInvalidFrontLineWorker() {
        setupData();

        FrontLineWorker flw = new FrontLineWorker("Test Worker", 2111111111L);
        frontLineWorkerService.add(flw);
        flw = frontLineWorkerService.getByContactNumber(2111111111L);

        assertEquals(FrontLineWorkerStatus.ANONYMOUS, flw.getStatus());

        exception.expect(JdoListenerInvocationException.class);
        frontLineWorkerService.delete(flw);
    }

    @Test
    public void testDeleteRecentInvalidFrontLineWorker() {
        setupData();

        FrontLineWorker flw = new FrontLineWorker("Test Worker", 2111111111L);
        frontLineWorkerService.add(flw);

        flw = frontLineWorkerService.getByContactNumber(2111111111L);
        flw.setStatus(FrontLineWorkerStatus.INVALID);
        frontLineWorkerService.update(flw);

        flw = frontLineWorkerService.getByContactNumber(2111111111L);
        assertEquals(FrontLineWorkerStatus.INVALID, flw.getStatus());

        exception.expect(JdoListenerInvocationException.class);
        frontLineWorkerService.delete(flw);
    }

    @Test
    public void testDeleteOldInvalidFrontLineWorker() {
        setupData();

        FrontLineWorker flw = new FrontLineWorker("Test Worker", 2111111111L);
        frontLineWorkerService.add(flw);

        flw = frontLineWorkerService.getByContactNumber(2111111111L);
        flw.setStatus(FrontLineWorkerStatus.INVALID);
        flw.setInvalidationDate(new DateTime().withDate(2011, 8, 1));
        frontLineWorkerService.update(flw);

        flw = frontLineWorkerService.getByContactNumber(2111111111L);
        assertEquals(FrontLineWorkerStatus.INVALID, flw.getStatus());

        frontLineWorkerService.delete(flw);
    }
}
