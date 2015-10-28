package org.motechproject.nms.mcts.importer;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.event.MotechEvent;
import org.motechproject.nms.flw.service.FrontLineWorkerImportService;
import org.motechproject.nms.flw.utils.FlwConstants;
import org.motechproject.nms.mcts.contract.AnmAshaDataSet;
import org.motechproject.nms.mcts.contract.AnmAshaRecord;
import org.motechproject.nms.mcts.contract.ChildrenDataSet;
import org.motechproject.nms.mcts.contract.MothersDataSet;
import org.motechproject.nms.mcts.service.MctsWebServiceFacade;
import org.motechproject.nms.mcts.utils.Constants;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.scheduler.contract.CronSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.testing.utils.TimeFaker;

import java.net.URL;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MctsWsImporterTest {

    @InjectMocks
    private MctsWsImporter mctsWsImporter = new MctsWsImporter();

    @Mock
    private SettingsFacade settingsFacade;

    @Mock
    private MotechSchedulerService schedulerService;

    @Mock
    private StateDataService stateDataService;

    @Mock
    private MctsWebServiceFacade mctsWebServiceFacade;

    @Mock
    private FrontLineWorkerImportService flwImportService;

    @Mock
    private State state1;

    @Mock
    private State state15;

    private AnmAshaDataSet flwDs1;
    private AnmAshaDataSet flwDs2;

    private final LocalDate today = DateUtil.today();
    private final LocalDate yesterday = today.minusDays(1);

    @Before
    public void setUp() {
        TimeFaker.fakeToday(today);
    }

    @After
    public void tearDown() {
        TimeFaker.stopFakingTime();
    }

    @Test
    public void shouldScheduleCronJobOnInit() {
        when(settingsFacade.getProperty(Constants.MCTS_SYNC_START_TIME)).thenReturn("0 0 16 * * ? *");

        mctsWsImporter.initImportJob();

        ArgumentCaptor<CronSchedulableJob> captor = ArgumentCaptor.forClass(CronSchedulableJob.class);
        verify(schedulerService).safeScheduleJob(captor.capture());
        assertEquals("0 0 16 * * ? *", captor.getValue().getCronExpression());
        assertNull(captor.getValue().getEndTime());
        assertNull(captor.getValue().getStartTime());
        assertEquals(Constants.MCTS_IMPORT_EVENT, captor.getValue().getMotechEvent().getSubject());
    }

    @Test
    public void shouldImportData() throws InvalidLocationException {
        prepStates();
        prepFlwData();
        prepEmtyImport(); // TODO: remove after child and mother import is added
        when(settingsFacade.getProperty(Constants.MCTS_ENDPOINT)).thenReturn("http://localhost/web-service");
        when(settingsFacade.getProperty(Constants.MCTS_LOCATIONS)).thenReturn("1,15");
        when(stateDataService.findByCode(1L)).thenReturn(state1);
        when(stateDataService.findByCode(15L)).thenReturn(state15);
        when(mctsWebServiceFacade.getAnmAshaData(eq(yesterday), eq(yesterday), any(URL.class), eq(1L)))
                .thenReturn(flwDs1);
        when(mctsWebServiceFacade.getAnmAshaData(eq(yesterday), eq(yesterday), any(URL.class), eq(15L)))
                .thenReturn(flwDs2);

        mctsWsImporter.handleImportEvent(new MotechEvent());

        ArgumentCaptor<Map> flwMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(flwImportService, times(2)).importFrontLineWorker(flwMapCaptor.capture(), eq(state1));
        verifyFlw(flwMapCaptor.getAllValues().get(0), 0L);
        verifyFlw(flwMapCaptor.getAllValues().get(1), 1L);

        flwMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(flwImportService, times(2)).importFrontLineWorker(flwMapCaptor.capture(), eq(state15));
        verifyFlw(flwMapCaptor.getAllValues().get(0), 2L);
        verifyFlw(flwMapCaptor.getAllValues().get(1), 3L);
    }

    private void prepFlwData() {
        AnmAshaRecord flw1 = createFlw(0);
        AnmAshaRecord flw2 = createFlw(1);
        AnmAshaRecord flw3 = createFlw(2);
        AnmAshaRecord flw4 = createFlw(3);

        flwDs1 = new AnmAshaDataSet();
        flwDs1.setRecords(asList(flw1, flw2));

        flwDs2 = new AnmAshaDataSet();
        flwDs2.setRecords(asList(flw3, flw4));
    }

    private void prepStates() {
        when(state1.getCode()).thenReturn(1L);
        when(state15.getCode()).thenReturn(15L);
    }

    private AnmAshaRecord createFlw(long id) {
        AnmAshaRecord record = new AnmAshaRecord();

        record.setId(id);
        record.setContactNo("48" + id);
        record.setName("AnmAsha " + id);
        record.setTalukaId("Taluka " + id);
        record.setHealthBlockId(id + 1);
        record.setPhcId(id + 2);
        record.setSubCentreId(id + 3);
        record.setVillageId(id + 4);
        record.setType("Type " + id);

        return record;
    }

    private void verifyFlw(Map<String, Object> flwpMap, Long id) {
        assertEquals(id.toString(), flwpMap.get(FlwConstants.ID));
        assertEquals(Long.valueOf("48" + id), flwpMap.get(FlwConstants.CONTACT_NO));
        assertEquals("AnmAsha " + id, flwpMap.get(FlwConstants.NAME));
        assertEquals("Taluka " + id, flwpMap.get(FlwConstants.TALUKA));
        assertEquals(id + 1, flwpMap.get(FlwConstants.HEALTH_BLOCK));
        assertEquals(id + 2, flwpMap.get(FlwConstants.PHC));
        assertEquals(id + 3, flwpMap.get(FlwConstants.SUBCENTRE));
        assertEquals(id + 4, flwpMap.get(FlwConstants.CENSUS_VILLAGE));
        assertEquals("Type " + id, flwpMap.get(FlwConstants.TYPE));
    }

    private void prepEmtyImport() {
        when(mctsWebServiceFacade.getMothersData(any(LocalDate.class), any(LocalDate.class), any(URL.class),
                any(Long.class))).thenReturn(new MothersDataSet());
        when(mctsWebServiceFacade.getChildrenData(any(LocalDate.class), any(LocalDate.class), any(URL.class),
                any(Long.class))).thenReturn(new ChildrenDataSet());
    }
}
