package org.motechproject.nms.mobileacademy.service.impl;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.imi.service.SmsNotificationService;
import org.motechproject.nms.mobileacademy.domain.CompletionRecord;
import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.service.CourseNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This handles all the integration pieces between MA and sms module to trigger and handle notifications
 * for course completion
 */
@Service("courseNotificationService")
public class CourseNotificationServiceImpl implements CourseNotificationService {

    private static final String COURSE_COMPLETED_SUBJECT = "nms.ma.course.completed";

    private static final String SMS_STATUS_SUBJECT = "nms.ma.sms.deliveryStatus";

    private static final Logger LOGGER = LoggerFactory.getLogger(CourseNotificationServiceImpl.class);

    private CompletionRecordDataService completionRecordDataService;

    private SmsNotificationService smsNotificationService;

    @Autowired
    public CourseNotificationServiceImpl(CompletionRecordDataService completionRecordDataService,
                                         SmsNotificationService smsNotificationService) {

        this.completionRecordDataService = completionRecordDataService;
        this.smsNotificationService = smsNotificationService;
    }

    @MotechListener(subjects = { COURSE_COMPLETED_SUBJECT } )
    public void sendSmsNotification(MotechEvent event) {

        LOGGER.debug("Handling course completion notification event");
        Long callingNumber = (Long) event.getParameters().get("callingNumber");
        CompletionRecord cr = completionRecordDataService.findRecordByCallingNumber(callingNumber);

        if (cr == null) {
            // this should never be possible since the event dispatcher upstream adds the record
            LOGGER.error("No completion record found for callingNumber: " + callingNumber);
            return;
        }

        cr.setSentNotification(smsNotificationService.sendSms(callingNumber));
        completionRecordDataService.update(cr);
    }

    @MotechListener(subjects = { SMS_STATUS_SUBJECT })
    public void updateSmsStatus(MotechEvent event) {

        LOGGER.debug("Handling update sms delivery status event");
        String callingNumber = (String) event.getParameters().get("address");
        int startIndex = callingNumber.indexOf(':') + 2;
        callingNumber = callingNumber.substring(startIndex);
        CompletionRecord cr = completionRecordDataService.findRecordByCallingNumber(
                Long.parseLong(callingNumber));

        if (cr == null) {
            // this should never be possible since the event dispatcher upstream adds the record
            LOGGER.error("No completion record found for callingNumber: " + callingNumber);
            return;
        }

        cr.setLastDeliveryStatus((String) event.getParameters().get("deliveryStatus"));
        completionRecordDataService.update(cr);
    }




}
