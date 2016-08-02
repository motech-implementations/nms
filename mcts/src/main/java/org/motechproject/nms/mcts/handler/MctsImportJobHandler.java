package org.motechproject.nms.mcts.handler;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.motechproject.commons.date.util.DateUtil;
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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        String cronExpression = settingsFacade.getProperty(Constants.MCTS_SYNC_CRON);
        if (StringUtils.isBlank(cronExpression)) {
            LOGGER.warn("No cron expression configured for MCTS data import, no import will be performed");
            return;
        }

        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new MctsImportConfigurationException("Cron expression from setting is invalid: " + cronExpression);
        }

        LOGGER.info("Created MCTS Import Event");
        CronSchedulableJob mctsImportJob = new CronSchedulableJob(new MotechEvent(Constants.MCTS_IMPORT_EVENT), cronExpression);
        motechSchedulerService.safeScheduleJob(mctsImportJob);
    }

    @MotechListener(subjects = Constants.MCTS_IMPORT_EVENT)
    @Transactional
    public void handleImportEvent(MotechEvent event) {
        LOGGER.info("Starting import from MCTS");

        List<Long> stateIds = getStateIds();
        URL endpoint = getEndpointUrl();
        int daysToPull = getDaysToPull();
        LocalDate referenceDate = DateUtil.today().minusDays(daysToPull);

        if (stateIds.isEmpty()) {
            LOGGER.warn("No states configured for import, not doing anything");
        } else {
            mctsWsImportService.importFromMcts(stateIds, referenceDate, endpoint);
        }
    }

    private int getDaysToPull() {
        int daysToPull;
        String daysToPullValue = settingsFacade.getProperty(Constants.DAYS_TO_PULL);
        try {
            daysToPull = Integer.parseInt(daysToPullValue);
        } catch (NumberFormatException e) {
            throw new MctsImportConfigurationException("Malformed days to pull configured: " + daysToPullValue, e);
        }

        // Valid date range to get data is 1-7 days
        if (daysToPull > 7 || daysToPull < 1) {
            throw new MctsImportConfigurationException("Malformed days to pull configured: " + daysToPull);
        }

        return daysToPull;
    }

    private List<Long> getStateIds() {
        String locationProp = settingsFacade.getProperty(Constants.MCTS_LOCATIONS);
        if (StringUtils.isBlank(locationProp)) {

            return Collections.emptyList();
        }

        String[] locationParts = StringUtils.split(locationProp, ',');

        List<Long> stateIds = new ArrayList<>();
        for (String locationPart : locationParts) {
            stateIds.add(Long.valueOf(locationPart));
        }

        return stateIds;
    }

    private URL getEndpointUrl() {
        String endpoint = settingsFacade.getProperty(Constants.MCTS_ENDPOINT);
        try {
            return StringUtils.isBlank(endpoint) ? null : new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new MctsImportConfigurationException("Malformed endpoint configured: " + endpoint, e);
        }
    }
}
