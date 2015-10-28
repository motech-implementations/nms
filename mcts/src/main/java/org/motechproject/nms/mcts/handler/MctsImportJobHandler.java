package org.motechproject.nms.mcts.handler;

import org.apache.commons.lang.StringUtils;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.mcts.exception.MctsImportConfigurationException;
import org.motechproject.nms.mcts.service.MctsWsImportService;
import org.motechproject.nms.mcts.service.impl.MctsWsImportServiceImpl;
import org.motechproject.nms.mcts.utils.Constants;
import org.motechproject.scheduler.contract.CronSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Component responsible for scheduling and handling the MCTS import job.
 */
@Component
public class MctsImportJobHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsWsImportServiceImpl.class);

    @Autowired
    @Qualifier("mctsSettings")
    private SettingsFacade settingsFacade;

    @Autowired
    private MotechSchedulerService motechSchedulerService;

    @Autowired
    private MctsWsImportService mctsWsImportService;

    @PostConstruct
    public void initImportJob() {
        String cronExpression = settingsFacade.getProperty(Constants.MCTS_SYNC_START_TIME);
        if (StringUtils.isBlank(cronExpression) || !CronExpression.isValidExpression(cronExpression)) {
            LOGGER.error("Cron expression from setting is invalid");
            throw new MctsImportConfigurationException("Cron expression from setting is invalid");
        }

        CronSchedulableJob mctsImportJob = new CronSchedulableJob(new MotechEvent(Constants.MCTS_IMPORT_EVENT), cronExpression);
        motechSchedulerService.safeScheduleJob(mctsImportJob);
    }

    @MotechListener(subjects = Constants.MCTS_IMPORT_EVENT)
    public void handleImportEvent(MotechEvent event) {
        LOGGER.info("Starting import from MCTS");
        mctsWsImportService.importFromMcts();
    }
}
