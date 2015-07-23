package org.motechproject.nms.mobileacademy.service.impl;

import org.joda.time.DateTime;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.imi.service.SmsNotificationService;
import org.motechproject.nms.mobileacademy.domain.CompletionRecord;
import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.service.CourseNotificationService;
import org.motechproject.scheduler.contract.RepeatingSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * This handles all the integration pieces between MA and sms module to trigger and handle notifications
 * for course completion
 */
@Service("courseNotificationService")
public class CourseNotificationServiceImpl implements CourseNotificationService {

    private static final String COURSE_COMPLETED_SUBJECT = "nms.ma.course.completed";
    private static final String SMS_STATUS_SUBJECT = "nms.ma.sms.deliveryStatus";
    private static final String SMS_RETRY_COUNT = "sms.retry.count";
    private static final String DELIVERY_IMPOSSIBLE = "DeliveryImpossible";
    private static final String RETRY_FLAG = "retry.flag";
    private static final String CALLING_NUMBER = "callingNumber";
    private static final String DELIVERY_STATUS = "deliveryStatus";
    private static final String ADDRESS = "address";
    private static final Logger LOGGER = LoggerFactory.getLogger(CourseNotificationServiceImpl.class);

    /**
     * Data service with course completion info
     */
    private CompletionRecordDataService completionRecordDataService;

    /**
     * SMS bridge used to talk to IMI
     */
    private SmsNotificationService smsNotificationService;

    /**
     * Used to retrieve course data
     */
    private SettingsFacade settingsFacade;

    /**
     * scheduler for future sms retries
     */
    private MotechSchedulerService schedulerService;

    @Autowired
    public CourseNotificationServiceImpl(CompletionRecordDataService completionRecordDataService,
                                         SmsNotificationService smsNotificationService,
                                         @Qualifier("maSettings") SettingsFacade settingsFacade,
                                         MotechSchedulerService schedulerService) {

        this.completionRecordDataService = completionRecordDataService;
        this.smsNotificationService = smsNotificationService;
        this.settingsFacade = settingsFacade;
        this.schedulerService = schedulerService;
    }

    @MotechListener(subjects = { COURSE_COMPLETED_SUBJECT })
    public void sendSmsNotification(MotechEvent event) {

        LOGGER.debug("Handling course completion notification event");
        Long callingNumber = (Long) event.getParameters().get(CALLING_NUMBER);
        CompletionRecord cr = completionRecordDataService.findRecordByCallingNumber(callingNumber);

        if (cr == null) {
            // this should never be possible since the event dispatcher upstream adds the record
            LOGGER.error("No completion record found for callingNumber: " + callingNumber);
            return;
        }

        if (event.getParameters().containsKey(RETRY_FLAG)) {
            LOGGER.debug("Handling retry for SMS notification");
            cr.setNotificationRetryCount(cr.getNotificationRetryCount() + 1);
        }

        cr.setSentNotification(smsNotificationService.sendSms(callingNumber));
        completionRecordDataService.update(cr);
    }

    @MotechListener(subjects = { SMS_STATUS_SUBJECT })
    public void updateSmsStatus(MotechEvent event) {

        LOGGER.debug("Handling update sms delivery status event");
        String callingNumber = (String) event.getParameters().get(ADDRESS);
        int startIndex = callingNumber.indexOf(':') + 2;
        callingNumber = callingNumber.substring(startIndex);
        CompletionRecord cr = completionRecordDataService.findRecordByCallingNumber(
                Long.parseLong(callingNumber));

        if (cr == null) {
            // this should never be possible since the event dispatcher upstream adds the record
            LOGGER.error("No completion record found for callingNumber: " + callingNumber);
            return;
        }

        // read properties
        String deliveryStatus = (String) event.getParameters().get(DELIVERY_STATUS);
        DateTime currentTime = DateTime.now();
        DateTime nextRetryTime = cr.getModificationDate().plusDays(1);

        // update completion record and status
        cr.setLastDeliveryStatus(deliveryStatus);
        completionRecordDataService.update(cr);

        // handle sms failures and retry
        if (DELIVERY_IMPOSSIBLE.equals(deliveryStatus) &&
                cr.getNotificationRetryCount() < Integer.parseInt(settingsFacade.getProperty(SMS_RETRY_COUNT))) {

            MotechEvent retryEvent = new MotechEvent(COURSE_COMPLETED_SUBJECT);
            retryEvent.getParameters().put(CALLING_NUMBER, Long.parseLong(callingNumber));
            retryEvent.getParameters().put(RETRY_FLAG, true);
            if (nextRetryTime.isBefore(currentTime)) {
                // retry right away
                sendSmsNotification(retryEvent);
            } else {
                RepeatingSchedulableJob job = new RepeatingSchedulableJob(
                        retryEvent,     // MOTECH event
                        1,              // repeatCount, null means infinity
                        1,              // repeatIntervalInSeconds
                        nextRetryTime.toDate(), //startTime
                        null,           // endTime, null means no end time
                        true);          // ignorePastFiresAtStart

                schedulerService.safeScheduleRepeatingJob(job);
            }
        }
    }
}
