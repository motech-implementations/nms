package org.motechproject.nms.mobileacademy.service.impl;

import org.joda.time.DateTime;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mtraining.service.ActivityService;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.imi.service.SmsNotificationService;
import org.motechproject.nms.mobileacademy.domain.CourseCompletionRecord;
import org.motechproject.nms.mobileacademy.repository.CourseCompletionRecordDataService;
import org.motechproject.nms.mobileacademy.service.CourseNotificationService;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.scheduler.contract.RepeatingSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private static final String SMS_CONTENT = "smsContent";
    private static final String DELIVERY_STATUS = "deliveryStatus";
    private static final String ADDRESS = "address";
    private static final String SMS_CONTENT_PREFIX = "sms.content.";
    private static final String SMS_DEFAULT_LANGUAGE_PROPERTY = "default";
    private static final Logger LOGGER = LoggerFactory.getLogger(CourseNotificationServiceImpl.class);

    /**
     * Data service with course completion info
     */

    /**
     * SMS bridge used to talk to IMI
     */
    private SmsNotificationService smsNotificationService;

    /**
     * Used to retrieve course data
     */
    private SettingsFacade settingsFacade;

    /**
     * Used to get flw information
     */
    private FrontLineWorkerService frontLineWorkerService;

    private CourseCompletionRecordDataService courseCompletionRecordDataService;

    /**
     * Used to get detached field
     */
    private DistrictDataService districtDataService;

    /**
     * scheduler for future sms retries
     */
    private MotechSchedulerService schedulerService;

    /**
     * Used to pull completion activity for flw
     */
    private ActivityService activityService;

    /**
     * Used for raising alerts
     */
    private AlertService alertService;

    @Autowired
    public CourseNotificationServiceImpl(SmsNotificationService smsNotificationService,
                                         @Qualifier("maSettings") SettingsFacade settingsFacade,
                                         ActivityService activityService,
                                         MotechSchedulerService schedulerService,
                                         CourseCompletionRecordDataService courseCompletionRecordDataService,
                                         AlertService alertService,
                                         FrontLineWorkerService frontLineWorkerService,
                                         DistrictDataService districtDataService) {

        this.smsNotificationService = smsNotificationService;
        this.settingsFacade = settingsFacade;
        this.schedulerService = schedulerService;
        this.alertService = alertService;
        this.activityService = activityService;
        this.frontLineWorkerService = frontLineWorkerService;
        this.districtDataService = districtDataService;
        this.courseCompletionRecordDataService = courseCompletionRecordDataService;
    }

    @MotechListener(subjects = { COURSE_COMPLETED_SUBJECT })
    @Transactional
    public void sendSmsNotification(MotechEvent event) {

        try {
            LOGGER.debug("Handling course completion notification event");
            Long callingNumber = (Long) event.getParameters().get(CALLING_NUMBER);

            List<CourseCompletionRecord> ccrs = courseCompletionRecordDataService.findByCallingNumber(callingNumber);
            if (ccrs == null || ccrs.isEmpty()) {
                // this should never be possible since the event dispatcher upstream adds the record
                LOGGER.error("No completion record found for callingNumber: " + callingNumber);
                return;
            }

            CourseCompletionRecord ccr = ccrs.get(ccrs.size()-1);

            if (event.getParameters().containsKey(RETRY_FLAG)) {
                LOGGER.debug("Handling retry for SMS notification");
                ccr.setNotificationRetryCount(ccr.getNotificationRetryCount() + 1);
            }

            String smsContent = buildSmsContent(callingNumber, ccr);
            ccr.setSentNotification(smsNotificationService.sendSms(callingNumber, smsContent));
            courseCompletionRecordDataService.update(ccr);
        } catch (IllegalStateException se) {
            LOGGER.error("Unable to send sms notification. Stack: " + se.toString());
            alertService.create("SMS Content", "MA SMS", se.getMessage(), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
        }

    }

    @MotechListener(subjects = { SMS_STATUS_SUBJECT })
    @Transactional
    public void updateSmsStatus(MotechEvent event) {

        LOGGER.debug("Handling update sms delivery status event");
        String callingNumber = (String) event.getParameters().get(ADDRESS);
        int startIndex = callingNumber.indexOf(':') + 2;
        callingNumber = callingNumber.substring(startIndex);
        List<CourseCompletionRecord> ccrs = courseCompletionRecordDataService.findByCallingNumber(Long.parseLong(callingNumber));

        if (ccrs == null || ccrs.isEmpty()) {
            // this should never be possible since the event dispatcher upstream adds the record
            LOGGER.error("No completion record found for callingNumber: " + callingNumber);
            return;
        }
        CourseCompletionRecord ccr = ccrs.get(ccrs.size()-1);

        // read properties
        String deliveryStatus = (String) event.getParameters().get(DELIVERY_STATUS);
        DateTime currentTime = DateTime.now();
        DateTime nextRetryTime = ccr.getModificationDate().plusDays(1);

        // update completion record and status
        ccr.setLastDeliveryStatus(deliveryStatus);
        courseCompletionRecordDataService.update(ccr);

        // handle sms failures and retry
        if (DELIVERY_IMPOSSIBLE.equals(deliveryStatus) &&
                ccr.getNotificationRetryCount() < Integer.parseInt(settingsFacade.getProperty(SMS_RETRY_COUNT))) {

            try {
                Long msisdn = Long.parseLong(callingNumber);
                String smsContent = buildSmsContent(msisdn, ccr);
                MotechEvent retryEvent = new MotechEvent(COURSE_COMPLETED_SUBJECT);
                retryEvent.getParameters().put(CALLING_NUMBER, msisdn);
                retryEvent.getParameters().put(SMS_CONTENT, smsContent);
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
            } catch (IllegalStateException se) {
                LOGGER.error("Unable to send sms notification. Stack: " + se.toString());
                alertService.create("SMS Content", "MA SMS", "Error generating SMS content", AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            }
        }
    }

    /**
     * Helper to generate the completion sms content for an flw
     * @param callingNumber calling number of the flw
     * @return localized sms content based on flw preferences or national default otherwise
     */
    private String buildSmsContent(Long callingNumber, CourseCompletionRecord ccr) {

        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(callingNumber);
        String locationCode = "XX"; // unknown location id
        String smsLanguageProperty = null;

        if (flw == null) {
            throw new IllegalStateException("Unable to find flw for calling number: " + callingNumber);
        }

        // Build location code
        if (flw.getState() != null && flw.getDistrict() != null) {
            locationCode = flw.getState().getCode().toString() + flw.getDistrict().getCode();
        }

        // set sms content language
        if (flw.getLanguage() != null) {
            // get language from flw, if exists
            smsLanguageProperty = flw.getLanguage().getCode();
        } else {
            District flwDistrict = flw.getDistrict();
            if (flwDistrict != null) {
                // get language from flw location (district), if exists
                Language flwLanguage = (Language) districtDataService.getDetachedField(flwDistrict, "language");
                if (flwLanguage != null) {
                    smsLanguageProperty = flwLanguage.getCode();
                }
            }
        }
        if (smsLanguageProperty == null || smsLanguageProperty.isEmpty()) {
            LOGGER.debug("No language code found in FLW. Reverting to national default");
            smsLanguageProperty = SMS_DEFAULT_LANGUAGE_PROPERTY;
        }

        // fetch sms content
        String smsContent = settingsFacade.getProperty(SMS_CONTENT_PREFIX + smsLanguageProperty);
        if (smsContent == null) {
            throw new IllegalStateException("Unable to get sms content for flw language: " +
                    SMS_CONTENT_PREFIX + smsLanguageProperty);
        }

        int attempts = activityService.getCompletedActivityForUser(callingNumber.toString()).size();
        String smsReferenceNumber = locationCode + callingNumber + attempts;
        ccr.setSmsReferenceNumber(smsReferenceNumber);
        courseCompletionRecordDataService.update(ccr);
        return smsContent + smsReferenceNumber;
    }
}
