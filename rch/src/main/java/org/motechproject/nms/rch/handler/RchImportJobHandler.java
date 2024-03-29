package org.motechproject.nms.rch.handler;

import org.joda.time.LocalDate;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.rch.domain.RchUserType;
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

        initSyncJob(Constants.RCH_SYNC_MOTHER_CRON, Constants.RCH_MOTHER_IMPORT_SUBJECT_CRON);
        initSyncJob(Constants.RCH_SYNC_CHILD_CRON, Constants.RCH_CHILD_IMPORT_SUBJECT_CRON);
        initSyncJob(Constants.RCH_SYNC_TALUKA_CRON, Constants.RCH_TALUKA_IMPORT_SUBJECT_CRON);
        initSyncJob(Constants.RCH_SYNC_DISTRICT_CRON, Constants.RCH_DISTRICT_IMPORT_SUBJECT_CRON);
        initSyncJob(Constants.RCH_SYNC_VILLAGE_CRON, Constants.RCH_VILLAGE_IMPORT_SUBJECT_CRON);
        initSyncJob(Constants.RCH_SYNC_HEALTHBLOCK_CRON, Constants.RCH_HEALTHBLOCK_IMPORT_SUBJECT_CRON);
        initSyncJob(Constants.RCH_SYNC_HEALTHFACILITY_CRON, Constants.RCH_HEALTHFACILITY_IMPORT_SUBJECT_CRON);
        initSyncJob(Constants.RCH_SYNC_HEALTHSUBFACILITY_CRON , Constants.RCH_HEALTHSUBFACILITY_IMPORT_SUBJECT_CRON);
        initSyncJob(Constants.RCH_SYNC_TALUKA_HEALTHBLOCK_CRON , Constants.RCH_TALUKA_HEALTHBLOCK_IMPORT_SUBJECT_CRON);
        initSyncJob(Constants.RCH_SYNC_VILLAGE_HEALTHFACILITYCRON, Constants.RCH_VILLAGEHEALTHSUBFACILITY_IMPORT_SUBJECT_CRON);
        initSyncJob(Constants.RCH_SYNC_ASHA_CRON, Constants.RCH_ASHA_IMPORT_SUBJECT_CRON);


        initMotherReadJob();
        initChildReadJob();
        initAshaReadJob();
        initLocationReadJob();
    }

    public void initSyncJob(String cronExpConst , String syncEventName){
        String cronExpression = settingsFacade.getProperty(cronExpConst);
        if (StringUtils.isBlank(cronExpression)) {
            LOGGER.warn("No cron expression configured for RCH data import, no import will be performed");
            return;
        }

        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new RchImportConfigurationException("Cron expression from setting is invalid: " + cronExpression);
        }

        LOGGER.info("Created RCH Import Event");
        CronSchedulableJob rchImportJob = new CronSchedulableJob(new MotechEvent(syncEventName), cronExpression);
        motechSchedulerService.safeScheduleJob(rchImportJob);
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
            throw new RchImportConfigurationException("Cron expression for location read is invalid: " + cronExpression);
        }

        LOGGER.info("Created RCH location Read Event");
        CronSchedulableJob rchLocationRead = new CronSchedulableJob(new MotechEvent(Constants.RCH_LOCATION_READ_SUBJECT), cronExpression);
        motechSchedulerService.safeScheduleJob(rchLocationRead);
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
        CronSchedulableJob rchChildRead = new CronSchedulableJob(new MotechEvent(Constants.RCH_CHILD_READ_SUBJECT), cronExpression);
        motechSchedulerService.safeScheduleJob(rchChildRead);
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
        CronSchedulableJob rchAshaRead = new CronSchedulableJob(new MotechEvent(Constants.RCH_ASHA_READ_SUBJECT), cronExpression);
        motechSchedulerService.safeScheduleJob(rchAshaRead);
    }

//    public void initReadJobs(RchUserType type, String cronExpression) {
//        switch (type) {
//            case MOTHER:
//                LOGGER.info("Created RCH Mother Read Event");
//                CronSchedulableJob rchMotherRead = new CronSchedulableJob(new MotechEvent(Constants.RCH_MOTHER_READ_SUBJECT), cronExpression);
//                motechSchedulerService.safeScheduleJob(rchMotherRead);
//                break;
//            case CHILD:
//                LOGGER.info("Created RCH Child Read Event");
//                CronSchedulableJob rchChildRead = new CronSchedulableJob(new MotechEvent(Constants.RCH_CHILD_READ_SUBJECT), cronExpression);
//                motechSchedulerService.safeScheduleJob(rchChildRead);
//                break;
//            case ASHA:
//                LOGGER.info("Created RCH Asha Read Event");
//                CronSchedulableJob rchAshaRead = new CronSchedulableJob(new MotechEvent(Constants.RCH_ASHA_READ_SUBJECT), cronExpression);
//                motechSchedulerService.safeScheduleJob(rchAshaRead);
//                break;
//            default:
//                LOGGER.info("Created RCH location Read Event");
//                CronSchedulableJob rchLocationRead = new CronSchedulableJob(new MotechEvent(Constants.RCH_LOCATION_READ_SUBJECT), cronExpression);
//                motechSchedulerService.safeScheduleJob(rchLocationRead);
//                break;
//        }
//    }



    @MotechListener(subjects = Constants.RCH_CHILD_IMPORT_SUBJECT_CRON)
    @Transactional
    public void handleChildImportEvent(MotechEvent event) {
        LOGGER.info("Starting import from RCH");

        List<Long> stateIds = getStateIds();
        URL endpoint = getEndpointUrl();
        int daysToPull = getDaysToPull();
        LocalDate referenceDate = DateUtil.today().minusDays(daysToPull);

        if (stateIds.isEmpty()) {
            LOGGER.warn("No states configured for import, not doing anything");
        } else {
            rchWsImportService.importChildFromRch(stateIds, referenceDate, endpoint);
        }
    }

    @MotechListener(subjects = Constants.RCH_MOTHER_IMPORT_SUBJECT_CRON)
    @Transactional
    public void handleMotherImportEvent(MotechEvent event) {
        LOGGER.info("Starting import from RCH");

        List<Long> stateIds = getStateIds();
        URL endpoint = getEndpointUrl();
        int daysToPull = getDaysToPull();
        LocalDate referenceDate = DateUtil.today().minusDays(daysToPull);

        if (stateIds.isEmpty()) {
            LOGGER.warn("No states configured for import, not doing anything");
        } else {
            rchWsImportService.importMothersFromRch(stateIds, referenceDate, endpoint);
        }
    }
    @MotechListener(subjects = Constants.RCH_ASHA_IMPORT_SUBJECT_CRON)
    @Transactional
    public void handleAshaImportEvent(MotechEvent event) {
        LOGGER.info("Starting import from RCH");

        List<Long> stateIds = getStateIds();
        URL endpoint = getEndpointUrl();
        int daysToPull = getDaysToPull();
        LocalDate referenceDate = DateUtil.today().minusDays(daysToPull);

        if (stateIds.isEmpty()) {
            LOGGER.warn("No states configured for import, not doing anything");
        } else {
            rchWsImportService.importAshaFromRch(stateIds, referenceDate, endpoint);
        }
    }
    @MotechListener(subjects = Constants.RCH_DISTRICT_IMPORT_SUBJECT_CRON)
    @Transactional
    public void handleDistrictImportEvent(MotechEvent event) {
        LOGGER.info("Starting import from RCH");

        List<Long> stateIds = getStateIds();
        URL endpoint = getEndpointUrl();
        int daysToPull = getDaysToPull();
        LocalDate referenceDate = DateUtil.today().minusDays(daysToPull);

        if (stateIds.isEmpty()) {
            LOGGER.warn("No states configured for import, not doing anything");
        } else {
            rchWsImportService.importDistrictFromRch(stateIds, referenceDate, endpoint);
        }
    }
    @MotechListener(subjects = Constants.RCH_TALUKA_IMPORT_SUBJECT_CRON)
    @Transactional
    public void handleTalukaImportEvent(MotechEvent event) {
        LOGGER.info("Starting import from RCH");

        List<Long> stateIds = getStateIds();
        URL endpoint = getEndpointUrl();
        int daysToPull = getDaysToPull();
        LocalDate referenceDate = DateUtil.today().minusDays(daysToPull);

        if (stateIds.isEmpty()) {
            LOGGER.warn("No states configured for import, not doing anything");
        } else {
            rchWsImportService.importTalukaFromRch(stateIds, referenceDate, endpoint);
        }
    }@MotechListener(subjects = Constants.RCH_VILLAGE_IMPORT_SUBJECT_CRON)
    @Transactional
    public void handleVillageImportEvent(MotechEvent event) {
        LOGGER.info("Starting import from RCH");

        List<Long> stateIds = getStateIds();
        URL endpoint = getEndpointUrl();
        int daysToPull = getDaysToPull();
        LocalDate referenceDate = DateUtil.today().minusDays(daysToPull);

        if (stateIds.isEmpty()) {
            LOGGER.warn("No states configured for import, not doing anything");
        } else {
            rchWsImportService.importVillageFromRch(stateIds, referenceDate, endpoint);
        }
    }@MotechListener(subjects = Constants.RCH_HEALTHBLOCK_IMPORT_SUBJECT_CRON)
    @Transactional
    public void handleHealthBlockImportEvent(MotechEvent event) {
        LOGGER.info("Starting import from RCH");

        List<Long> stateIds = getStateIds();
        URL endpoint = getEndpointUrl();
        int daysToPull = getDaysToPull();
        LocalDate referenceDate = DateUtil.today().minusDays(daysToPull);

        if (stateIds.isEmpty()) {
            LOGGER.warn("No states configured for import, not doing anything");
        } else {
            rchWsImportService.importHealthBlockFromRch(stateIds, referenceDate, endpoint);
        }
    }@MotechListener(subjects = Constants.RCH_HEALTHFACILITY_IMPORT_SUBJECT_CRON)
    @Transactional
    public void handleHealthFacilityImportEvent(MotechEvent event) {
        LOGGER.info("Starting import from RCH");

        List<Long> stateIds = getStateIds();
        URL endpoint = getEndpointUrl();
        int daysToPull = getDaysToPull();
        LocalDate referenceDate = DateUtil.today().minusDays(daysToPull);

        if (stateIds.isEmpty()) {
            LOGGER.warn("No states configured for import, not doing anything");
        } else {
            rchWsImportService.importHealthFacilityFromRch(stateIds, referenceDate, endpoint);
        }
    }@MotechListener(subjects = Constants.RCH_HEALTHSUBFACILITY_IMPORT_SUBJECT_CRON)
    @Transactional
    public void handleHealthSubFacilityImportEvent(MotechEvent event) {
        LOGGER.info("Starting import from RCH");

        List<Long> stateIds = getStateIds();
        URL endpoint = getEndpointUrl();
        int daysToPull = getDaysToPull();
        LocalDate referenceDate = DateUtil.today().minusDays(daysToPull);

        if (stateIds.isEmpty()) {
            LOGGER.warn("No states configured for import, not doing anything");
        } else {
            rchWsImportService.importHealthSubFacilityFromRch(stateIds, referenceDate, endpoint);
        }
    }@MotechListener(subjects = Constants.RCH_TALUKA_HEALTHBLOCK_IMPORT_SUBJECT_CRON)
    @Transactional
    public void handleTalukaHealthBlockImportEvent(MotechEvent event) {
        LOGGER.info("Starting import from RCH");

        List<Long> stateIds = getStateIds();
        URL endpoint = getEndpointUrl();
        int daysToPull = getDaysToPull();
        LocalDate referenceDate = DateUtil.today().minusDays(daysToPull);

        if (stateIds.isEmpty()) {
            LOGGER.warn("No states configured for import, not doing anything");
        } else {
            rchWsImportService.importTalukaHealthBlockFromRch(stateIds, referenceDate, endpoint);
        }
    }@MotechListener(subjects = Constants.RCH_VILLAGEHEALTHSUBFACILITY_IMPORT_SUBJECT_CRON)
    @Transactional
    public void handleVillageHealthSubFacilityImportEvent(MotechEvent event) {
        LOGGER.info("Starting import from RCH");

        List<Long> stateIds = getStateIds();
        URL endpoint = getEndpointUrl();
        int daysToPull = getDaysToPull();
        LocalDate referenceDate = DateUtil.today().minusDays(daysToPull);

        if (stateIds.isEmpty()) {
            LOGGER.warn("No states configured for import, not doing anything");
        } else {
            rchWsImportService.importVillageHealthSubFacilityFromRch(stateIds, referenceDate, endpoint);
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
