package org.motechproject.nms.mcts.service.impl;


import org.apache.commons.lang.StringUtils;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.mcts.service.MctsImportService;
import org.motechproject.nms.mcts.soap.MctsServiceLocator;
import org.motechproject.nms.mcts.utils.Constants;
import org.motechproject.scheduler.contract.CronSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 *
 */
@Service("mctsImportService")
public class MctsImportServiceImpl implements MctsImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsImportServiceImpl.class);

    @Autowired
    private FrontLineWorkerDataService frontLineWorkerDataService;

    @Autowired
    private MctsChildDataService mctsChildDataService;

    @Autowired
    private MctsMotherDataService mctsMotherDataService;

    @Autowired
    private MotechSchedulerService motechSchedulerService;

    @Autowired
    @Qualifier("mctsSettings")
    private SettingsFacade settingsFacade;

    @Autowired
    @Qualifier("mctsServiceLocator")
    private MctsServiceLocator mctsServiceLocator;

    @PostConstruct
    public void init() {
        String cronExpression = settingsFacade.getProperty(Constants.MCTS_SYNC_START_TIME);
        if (StringUtils.isBlank(cronExpression) || !CronExpression.isValidExpression(cronExpression)) {
            LOGGER.warn("Cron expression from setting is invalid. Mcts import job will be scheduled with default Cron Expression ({})",
                    Constants.DEFAULT_MCTS_IMPORT_CRON_EXPRESSION);
            cronExpression = Constants.DEFAULT_MCTS_IMPORT_CRON_EXPRESSION;
        }

        CronSchedulableJob mctsImportJob = new CronSchedulableJob(new MotechEvent(Constants.MCTS_IMPORT_EVENT), cronExpression);
        motechSchedulerService.safeScheduleJob(mctsImportJob);
    }

    /**
     *
     * @param event
     */
    @Override
    @MotechListener(subjects = Constants.MCTS_IMPORT_EVENT)
    public void handleImportEvent(MotechEvent event) {

    }
}
