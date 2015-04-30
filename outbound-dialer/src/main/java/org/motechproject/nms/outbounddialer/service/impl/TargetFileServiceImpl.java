package org.motechproject.nms.outbounddialer.service.impl;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.nms.outbounddialer.domain.FileProcessedStatus;
import org.motechproject.nms.outbounddialer.repository.Beneficiary;
import org.motechproject.nms.outbounddialer.repository.BeneficiaryDataService;
import org.motechproject.nms.outbounddialer.repository.BeneficiaryStatus;
import org.motechproject.nms.outbounddialer.repository.DayOfTheWeek;
import org.motechproject.nms.outbounddialer.service.TargetFileService;
import org.motechproject.nms.outbounddialer.web.contract.FileProcessedStatusRequest;
import org.motechproject.scheduler.contract.RepeatingSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Service("targetFileService")
public class TargetFileServiceImpl implements TargetFileService {
    private static final String TARGET_FILE_TIME = "outbound-dialer.target_file_time";
    private static final String MAX_BENEFICIARY_BLOCK = "outbound-dialer.max_beneficiary_block";
    private static final String TARGET_FILE_MS_INTERVAL = "outbound-dialer.target_file_ms_interval";
    private static final String TARGET_FILE_LOCATION = "outbound-dialer.target_file_location";
    private static final String GENERATE_TARGET_FILE_EVENT = "nms.obd.generate_target_file";

    private SettingsFacade settingsFacade;
    private MotechSchedulerService schedulerService;
    private AlertService alertService;
    private BeneficiaryDataService beneficiaryDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(TargetFileServiceImpl.class);


    private void scheduleTargetFileGeneration() {
        //Calculate today's fire time
        DateTimeFormatter fmt = DateTimeFormat.forPattern("H:m");
        String timeProp = settingsFacade.getProperty(TARGET_FILE_TIME);
        DateTime time = fmt.parseDateTime(timeProp);
        DateTime today = DateTime.now()
                .withHourOfDay(time.getHourOfDay())
                .withMinuteOfHour(time.getMinuteOfHour())
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        //Millisecond interval between events
        String intervalProp = settingsFacade.getProperty(TARGET_FILE_MS_INTERVAL);
        Long msInterval = Long.parseLong(intervalProp);

        LOGGER.debug(String.format("The %s message will be sent every %sms starting %s",
                GENERATE_TARGET_FILE_EVENT, msInterval.toString(), today.toString()));

        //Schedule repeating job
        MotechEvent event = new MotechEvent(GENERATE_TARGET_FILE_EVENT);
        RepeatingSchedulableJob job = new RepeatingSchedulableJob(
                event,          //MOTECH event
                today.toDate(), //startTime
                null,           //endTime, null means no end time
                null,           //repeatCount, null means infinity
                msInterval,     //repeatIntervalInMilliseconds
                true);          //ignorePastFiresAtStart
        schedulerService.safeScheduleRepeatingJob(job);
    }


    @Autowired
    public TargetFileServiceImpl(@Qualifier("outboundDialerSettings") SettingsFacade settingsFacade,
                                     MotechSchedulerService schedulerService, AlertService alertService,
                                 BeneficiaryDataService beneficiaryDataService) {
        this.schedulerService = schedulerService;
        this.settingsFacade = settingsFacade;
        this.alertService = alertService;
        this.beneficiaryDataService = beneficiaryDataService;

        scheduleTargetFileGeneration();
    }


    public String targetFileName() {
        DateTime today = DateTime.now();
        return String.format("OBD_%04d%02d%02d%02d%02d%02d.csv",
                today.getYear(),
                today.getMonthOfYear(),
                today.getDayOfMonth(),
                today.getHourOfDay(),
                today.getMinuteOfHour(),
                today.getSecondOfMinute());
    }


    public void generateTargetFile() {
        final String targetFileLocation = settingsFacade.getProperty(TARGET_FILE_LOCATION);
        final String targetFileName = targetFileName();
        final String targetFilePath = String.format("%s%s%s",
                targetFileLocation,
                targetFileLocation.endsWith("/") ? "" : "/",
                targetFileName);
        LOGGER.debug("Generating target file {}", targetFilePath);

        //figure out which day to work with
        final DayOfTheWeek today = DayOfTheWeek.today();

        long numBeneficiaries = 0;
        try (PrintWriter writer = new PrintWriter(targetFilePath, "UTF-8")) {
            int page = 1;
            int numBlockBeneficiaries = 0;
            int maxBeneficiaryBlock = Integer.parseInt(settingsFacade.getProperty(MAX_BENEFICIARY_BLOCK));
            do {
                List<Beneficiary> beneficiaries = beneficiaryDataService.findByNextCallDay(today,
                        BeneficiaryStatus.Active, new QueryParams(page, maxBeneficiaryBlock));
                numBlockBeneficiaries = beneficiaries.size();

                for (Beneficiary beneficiary : beneficiaries) {
                    writer.print(beneficiaryDataService.getDetachedField(beneficiary, "id"));
                    writer.print(",");
                    writer.print(beneficiary.getMsisdn());
                    writer.print(",");
                    writer.println(beneficiary.getLanguageLocationCode());
                    //todo...
                }

                page++;
                numBeneficiaries += numBlockBeneficiaries;

            } while (numBlockBeneficiaries > 0);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            String error = String.format("Unable to create targetFile %s: %s", targetFilePath, e.getMessage());
            LOGGER.error(error);
            alertService.create(targetFilePath, "targetFileName", error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            return;
        }

        LOGGER.info(String.format("Successfully created %s with %s beneficiar%s", targetFilePath, numBeneficiaries,
                numBeneficiaries == 1 ? "y" : "ies"));

        //todo...
        //notify the IVR system the file is ready

    }


    @MotechListener(subjects = { GENERATE_TARGET_FILE_EVENT })
    public void generateTargetFile(MotechEvent event) {
        LOGGER.debug(event.toString());
        generateTargetFile();
    }


    @Override
    public void handleFileProcessedStatusNotification(FileProcessedStatusRequest request) {
        if (request.getFileProcessedStatus() == FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY) {
            LOGGER.info(request.toString());
            //We're happy.
            //todo:...
        } else {
            LOGGER.error(request.toString());
            alertService.create(
                    request.getFileName(),
                    "targetFileName",
                    "Target File Processing Error",
                    AlertType.CRITICAL,
                    AlertStatus.NEW,
                    0,
                    null);
        }
    }
}
