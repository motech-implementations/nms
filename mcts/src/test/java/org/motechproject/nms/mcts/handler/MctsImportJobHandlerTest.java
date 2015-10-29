package org.motechproject.nms.mcts.handler;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.event.MotechEvent;
import org.motechproject.nms.mcts.exception.MctsImportConfigurationException;
import org.motechproject.nms.mcts.service.MctsWsImportService;
import org.motechproject.nms.mcts.utils.Constants;
import org.motechproject.scheduler.contract.CronSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.testing.utils.TimeFaker;

import java.net.URL;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MctsImportJobHandlerTest {

    @InjectMocks
    private MctsImportJobHandler mctsImportJobHandler = new MctsImportJobHandler();

    @Mock
    private SettingsFacade settingsFacade;

    @Mock
    private MotechSchedulerService schedulerService;

    @Mock
    private MctsWsImportService mctsWsImportService;

    @Test
    public void shouldScheduleCronJobOnInit() {
        when(settingsFacade.getProperty(Constants.MCTS_SYNC_CRON)).thenReturn("0 0 16 * * ? *");

        mctsImportJobHandler.initImportJob();

        ArgumentCaptor<CronSchedulableJob> captor = ArgumentCaptor.forClass(CronSchedulableJob.class);
        verify(schedulerService).safeScheduleJob(captor.capture());
        assertEquals("0 0 16 * * ? *", captor.getValue().getCronExpression());
        assertNull(captor.getValue().getEndTime());
        assertNull(captor.getValue().getStartTime());
        assertEquals(Constants.MCTS_IMPORT_EVENT, captor.getValue().getMotechEvent().getSubject());
    }

    public void shouldNotScheduleJobWhenNoCronInSettings() {
        when(settingsFacade.getProperty(Constants.MCTS_SYNC_CRON)).thenReturn("");

        mctsImportJobHandler.initImportJob();

        verifyZeroInteractions(schedulerService);
    }

    @Test(expected = MctsImportConfigurationException.class)
    public void shouldThrowExceptionOnInvalidCron() {
        when(settingsFacade.getProperty(Constants.MCTS_SYNC_CRON)).thenReturn("whatever");
        try {
            mctsImportJobHandler.initImportJob();
        } finally {
            verifyZeroInteractions(schedulerService);
        }
    }

    @Test
    public void shouldStartImport() {
        final LocalDate today = DateUtil.today();
        final LocalDate yesterday = today.minusDays(1);
        try {
            TimeFaker.fakeToday(today);

            when(settingsFacade.getProperty(Constants.MCTS_LOCATIONS)).thenReturn("4,15,51,2");
            when(settingsFacade.getProperty(Constants.MCTS_ENDPOINT)).thenReturn("http://localhost:9090/test.svc");

            mctsImportJobHandler.handleImportEvent(new MotechEvent());

            ArgumentCaptor<URL> urlCaptor = ArgumentCaptor.forClass(URL.class);
            verify(mctsWsImportService).importFromMcts(eq(asList(4L, 15L, 51L, 2L)), eq(yesterday), urlCaptor.capture());
            assertEquals("http://localhost:9090/test.svc", urlCaptor.getValue().toString());
        } finally {
            TimeFaker.stopFakingTime();
        }
    }
}
