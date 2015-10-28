package org.motechproject.nms.mcts.importer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.motechproject.nms.mcts.utils.Constants;
import org.motechproject.scheduler.contract.CronSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
}
