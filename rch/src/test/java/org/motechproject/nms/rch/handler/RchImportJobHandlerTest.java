package org.motechproject.nms.rch.handler;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.event.MotechEvent;
import org.motechproject.nms.rch.exception.RchImportConfigurationException;
import org.motechproject.nms.rch.service.RchWsImportService;
import org.motechproject.nms.rch.utils.Constants;
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
public class RchImportJobHandlerTest {

    @InjectMocks
    private RchImportJobHandler rchImportJobHandler = new RchImportJobHandler();

    @Mock
    private SettingsFacade settingsFacade;

    @Mock
    private MotechSchedulerService schedulerService;

    @Mock
    private RchWsImportService rchWsImportService;

    @Test
    public void shouldScheduleCronJobOnInit() {
        when(settingsFacade.getProperty(Constants.DAYS_TO_PULL)).thenReturn("1");
        when(settingsFacade.getProperty(Constants.RCH_SYNC_CRON)).thenReturn("0 0 16 * * ? *");

        rchImportJobHandler.initImportJob();

        ArgumentCaptor<CronSchedulableJob> captor = ArgumentCaptor.forClass(CronSchedulableJob.class);
        verify(schedulerService).safeScheduleJob(captor.capture());
        assertEquals("0 0 16 * * ? *", captor.getValue().getCronExpression());
        assertNull(captor.getValue().getEndTime());
        assertNull(captor.getValue().getStartTime());
        assertEquals(Constants.RCH_IMPORT_EVENT, captor.getValue().getMotechEvent().getSubject());
    }

    public void shouldNotScheduleJobWhenNoCronInSettings() {
        when(settingsFacade.getProperty(Constants.DAYS_TO_PULL)).thenReturn("1");
        when(settingsFacade.getProperty(Constants.RCH_SYNC_CRON)).thenReturn("");

        rchImportJobHandler.initImportJob();

        verifyZeroInteractions(schedulerService);
    }

    @Test(expected = RchImportConfigurationException.class)
    public void shouldThrowExceptionOnInvalidCron() {
        when(settingsFacade.getProperty(Constants.DAYS_TO_PULL)).thenReturn("1");
        when(settingsFacade.getProperty(Constants.RCH_SYNC_CRON)).thenReturn("whatever");
        try {
            rchImportJobHandler.initImportJob();
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

            when(settingsFacade.getProperty(Constants.DAYS_TO_PULL)).thenReturn("1");
            when(settingsFacade.getProperty(Constants.RCH_LOCATIONS)).thenReturn("4,15,51,2");
            when(settingsFacade.getProperty(Constants.RCH_ENDPOINT)).thenReturn("http://localhost:9090/test.svc");

            rchImportJobHandler.handleImportEvent(new MotechEvent());

            ArgumentCaptor<URL> urlCaptor = ArgumentCaptor.forClass(URL.class);
            verify(rchWsImportService).importFromRch(eq(asList(4L, 15L, 51L, 2L)), eq(yesterday), urlCaptor.capture());
            assertEquals("http://localhost:9090/test.svc", urlCaptor.getValue().toString());
        } finally {
            TimeFaker.stopFakingTime();
        }
    }
}
