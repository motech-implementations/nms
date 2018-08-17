package org.motechproject.nms.rch.handler;

import org.joda.time.LocalDate;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.rch.exception.RchImportConfigurationException;
import org.motechproject.nms.rch.service.RchWsImportService;
import org.motechproject.nms.rch.service.impl.RchWsImportServiceImpl;
import org.motechproject.nms.rch.utils.Constants;
import org.motechproject.scheduler.contract.CronSchedulableJob;
import org.motechproject.server.config.SettingsFacade;
import org.quartz.CronExpression;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Component responsible for scheduling and handling the RCH import job.
 */
@Component
public class RchImportJobHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RchWsImportServiceImpl.class);
    private static final String NO_CRON_CONFIGURED = "No cron expression configured for RCH data read, no import will be performed";

    @Autowired
    @Qualifier("rchSettings")
    private SettingsFacade settingsFacade;

    @Autowired
    private MotechSchedulerService motechSchedulerService;

    @Autowired
    private RchWsImportService rchWsImportService;

    @PostConstruct
    public void initImportJob() {
        String cronExpression = settingsFacade.getProperty(Constants.RCH_SYNC_CRON);
        if (StringUtils.isBlank(cronExpression)) {
            LOGGER.warn("No cron expression configured for RCH data import, no import will be performed");
            return;
        }

        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new RchImportConfigurationException("Cron expression from setting is invalid: " + cronExpression);
        }

        LOGGER.info("Created RCH Import Event");
        CronSchedulableJob rchImportJob = new CronSchedulableJob(new MotechEvent(Constants.RCH_IMPORT_EVENT), cronExpression);
        motechSchedulerService.safeScheduleJob(rchImportJob);

        initMotherReadJob();
        initChildReadJob();
        initAshaReadJob();
        initLocationReadJob();
    }

    public void initMotherReadJob() {
        String cronExpression = settingsFacade.getProperty(Constants.RCH_MOTHER_READ_CRON);
        if (StringUtils.isBlank(cronExpression)) {
            LOGGER.warn(NO_CRON_CONFIGURED);
            return;
        }

        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new RchImportConfigurationException("Cron expression for mother read is invalid: " + cronExpression);
        }

        LOGGER.info("Created RCH Mother Read Event");
        CronSchedulableJob rchMotherRead = new CronSchedulableJob(new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT), cronExpression);
        motechSchedulerService.safeScheduleJob(rchMotherRead);
    }

    public void initLocationReadJob() {
        String cronExpression = settingsFacade.getProperty(Constants.RCH_LOCATION_READ_CRON);
        if (StringUtils.isBlank(cronExpression)) {
            LOGGER.warn(NO_CRON_CONFIGURED);
            return;
        }

        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new RchImportConfigurationException("Cron expression for district read is invalid: " + cronExpression);
        }

        LOGGER.info("Created RCH location Read Event");
        CronSchedulableJob rchDistrictRead = new CronSchedulableJob(new MotechEvent(Constants.RCH_DISTRICT_READ_SUBJECT), cronExpression);
        motechSchedulerService.safeScheduleJob(rchDistrictRead);
    }

    public void initChildReadJob() {
        String cronExpression = settingsFacade.getProperty(Constants.RCH_CHILD_READ_CRON);
        if (StringUtils.isBlank(cronExpression)) {
            LOGGER.warn(NO_CRON_CONFIGURED);
            return;
        }

        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new RchImportConfigurationException("Cron expression for child read is invalid: " + cronExpression);
        }

        LOGGER.info("Created RCH Child Read Event");
        CronSchedulableJob rchMotherRead = new CronSchedulableJob(new MotechEvent(Constants.RCH_CHILD_READ_SUBJECT), cronExpression);
        motechSchedulerService.safeScheduleJob(rchMotherRead);
    }

    public void initAshaReadJob() {
        String cronExpression = settingsFacade.getProperty(Constants.RCH_ASHA_READ_CRON);
        if (StringUtils.isBlank(cronExpression)) {
            LOGGER.warn(NO_CRON_CONFIGURED);
            return;
        }

        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new RchImportConfigurationException("Cron expression for asha read is invalid: " + cronExpression);
        }

        LOGGER.info("Created RCH Asha Read Event");
        CronSchedulableJob rchMotherRead = new CronSchedulableJob(new MotechEvent(Constants.RCH_ASHA_READ_SUBJECT), cronExpression);
        motechSchedulerService.safeScheduleJob(rchMotherRead);
    }

    @MotechListener(subjects = Constants.RCH_IMPORT_EVENT)
    @Transactional
    public void handleImportEvent(MotechEvent event) {
        LOGGER.info("Starting import from RCH");

        List<Long> stateIds = getStateIds();
        URL endpoint = getEndpointUrl();
        int daysToPull = getDaysToPull();
        LocalDate referenceDate = DateUtil.today().minusDays(daysToPull);

        if (stateIds.isEmpty()) {
            LOGGER.warn("No states configured for import, not doing anything");
        } else {
            rchWsImportService.importFromRch(stateIds, referenceDate, endpoint);
        }
    }

    private int getDaysToPull() {
        int daysToPull;
        String daysToPullValue = settingsFacade.getProperty(Constants.DAYS_TO_PULL);
        try {
            daysToPull = Integer.parseInt(daysToPullValue);
        } catch (NumberFormatException e) {
            throw new RchImportConfigurationException("Malformed days to pull configured: " + daysToPullValue, e);
        }

        // Valid date range to get data is 1-7 days
        if (daysToPull > 7 || daysToPull < 1) {
            throw new RchImportConfigurationException("Malformed days to pull configured: " + daysToPull);
        }

        return daysToPull;
    }

    private List<Long> getStateIds() {
        String locationProp = settingsFacade.getProperty(Constants.RCH_LOCATIONS);
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
        String endpoint = settingsFacade.getProperty(Constants.RCH_ENDPOINT);
        try {
            return StringUtils.isBlank(endpoint) ? null : new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new RchImportConfigurationException("Malformed endpoint configured: " + endpoint, e);
        }
    }

}
