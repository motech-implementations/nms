package org.motechproject.nms.outbounddialer.service.impl;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.outbounddialer.service.OutboundDialerService;
import org.motechproject.scheduler.contract.RepeatingSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link OutboundDialerService} interface.
 */
@Service("outboundDialerService")
public class OutboundDialerServiceImpl implements OutboundDialerService {

    private static final String GENERATE_TARGET_FILE_TIME = "outbound-dialer.generate_target_file_time";
    private static final String GENERATE_TARGET_FILE_MS_INTERVAL =
            "outbound-dialer.generate_target_file_ms_interval";
    private static final String GENERATE_TARGET_FILE_EVENT = "nms.obd.generate_target_file";

    private SettingsFacade settingsFacade;
    private MotechSchedulerService schedulerService;
    private static final Logger LOGGER = LoggerFactory.getLogger(OutboundDialerServiceImpl.class);



    private void scheduleTargetFileGeneration() {
        //Calculate today's fire time
        DateTimeFormatter fmt = DateTimeFormat.forPattern("H:m");
        String timeProp = settingsFacade.getProperty(GENERATE_TARGET_FILE_TIME);
        DateTime time = fmt.parseDateTime(timeProp);
        DateTime today = DateTime.now()
                .withHourOfDay(time.getHourOfDay())
                .withMinuteOfHour(time.getMinuteOfHour())
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        //Millisecond interval between events
        String intervalProp = settingsFacade.getProperty(GENERATE_TARGET_FILE_MS_INTERVAL);
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
    public OutboundDialerServiceImpl(@Qualifier("outboundDialerSettings") SettingsFacade settingsFacade,
                                     MotechSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
        this.settingsFacade = settingsFacade;

        scheduleTargetFileGeneration();
    }

    @MotechListener(subjects = { GENERATE_TARGET_FILE_EVENT })
    public void handleFileChanged(MotechEvent event) {
        LOGGER.debug(event.toString());
    }

    @Override
    public void handleNewCdrFile() {
        //TODO: download the files from the specified locations and validate their checksums

        //TODO: post a message to begin processing the files

    }

    @Override
    public void handleFileProcessedStatusNotification() {

    }
}
