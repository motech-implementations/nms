package org.motechproject.nms.imi.service.impl;


import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.imi.domain.FileAuditRecord;
import org.motechproject.nms.imi.domain.FileProcessedStatus;
import org.motechproject.nms.imi.domain.FileType;
import org.motechproject.nms.imi.exception.ExecException;
import org.motechproject.nms.imi.exception.InternalException;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.TargetFileService;
import org.motechproject.nms.imi.service.contract.TargetFileNotification;
import org.motechproject.nms.imi.web.contract.FileProcessedStatusRequest;
import org.motechproject.nms.kilkari.domain.*;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.service.CallRetryService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.props.domain.RequestId;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.scheduler.contract.RepeatingSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service("targetFileService")
public class TargetFileServiceImpl implements TargetFileService {
    private static final String LOCAL_OBD_DIR = "imi.local_obd_dir";
    private static final String LOCAL_OBD_DIR_WHATSAPP = "imi.local_obd_dir_whatsapp";
    private static final String TARGET_FILE_TIME = "imi.target_file_time";
    private static final String TARGET_FILE_TIME_WHATSAPP = "imi.target_file_time_whatsapp";
    private static final String MAX_QUERY_BLOCK = "imi.max_query_block";
    private static final String TARGET_FILE_SEC_INTERVAL = "imi.target_file_sec_interval";
    private static final String TARGET_FILE_NOTIFICATION_URL = "imi.target_file_notification_url";
    private static final String TARGET_FILE_CALL_FLOW_URL = "imi.target_file_call_flow_url";
    private static final String TARGET_FILE_CALL_FLOW_URL_WHATSAPP = "imi.target_file_call_flow_url_wp";
    private static final String CONTENT_FILE_NAME_WHATSAPP_WELCOME_MESSAGE = "imi.content_file_name_whatsapp_welcome_message";
    private static final String NORMAL_PRIORITY = "0";
    private static final String HIGH_PRIORITY = "1";
    private static final String IMI_FRESH_CHECK_DND = "imi.fresh_check_dnd";
    private static final String IMI_FRESH_NO_CHECK_DND = "imi.fresh_no_check_dnd";
    private static final String IMI_RETRY_CHECK_DND = "imi.retry_check_dnd";
    private static final String IMI_RETRY_NO_CHECK_DND = "imi.retry_no_check_dnd";

    private static final String IMI_FRESH_CHECK_DND_JH = "imi.fresh_check_dnd_jh";
    private static final String IMI_FRESH_NO_CHECK_DND_JH = "imi.fresh_no_check_dnd_jh";
    private static final String IMI_RETRY_CHECK_DND_JH = "imi.retry_check_dnd_jh";
    private static final String IMI_RETRY_NO_CHECK_DND_JH = "imi.retry_no_check_dnd_jh";
    private static final String generateJhFile = "imi.obd_bifurcate";
    private static final String Jh = "JH";
    private static final String non_Jh = "NON-JH";

    private static final int PROGRESS_INTERVAL = 10000;

    private static final String GENERATE_TARGET_FILE_EVENT = "nms.obd.generate_target_file";

    private static final String GENERATE_TARGET_FILE_EVENT_WHATSAPP = "nms.obd.generate_target_file_WP";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private static final String DEFAULT_CIRCLE = "99";  // https://applab.atlassian.net/browse/NIP-64
    public static final String WROTE = "Wrote {}";

    private SettingsFacade settingsFacade;
    private MotechSchedulerService schedulerService;
    private AlertService alertService;
    private SubscriptionService subscriptionService;
    private CallRetryService callRetryService;
    private FileAuditRecordDataService fileAuditRecordDataService;
    private LanguageDataService languageDataService;
    private SubscriptionDataService subscriptionDataService;
    private static String freshCheckDND;
    private static String freshNoCheckDND;
    private static String retryCheckDND;
    private static String retryNoCheckDND;

    private static String freshCheckDNDJh;
    private static String freshNoCheckDNDJh;
    private static String retryCheckDNDJh;
    private static String retryNoCheckDNDJh;

    private static final Logger LOGGER = LoggerFactory.getLogger(TargetFileServiceImpl.class);


    /**
     * Use the MOTECH scheduler to setup a repeating job
     * The job will start today at the time stored in imi.target_file_time in imi.properties
     * It will repeat every imi.target_file_sec_interval seconds (default value is a day)
     */
    private void scheduleTargetFileGeneration() {
        //Calculate today's fire time
        DateTimeFormatter fmt = DateTimeFormat.forPattern("H:m");
        String timeProp = settingsFacade.getProperty(TARGET_FILE_TIME);
        DateTime time = fmt.parseDateTime(timeProp);
        DateTime today = DateTime.now()                     // This means today's date...
                .withHourOfDay(time.getHourOfDay())         // ...at the hour...
                .withMinuteOfHour(time.getMinuteOfHour())   // ...and minute specified in imi.properties
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        //Second interval between events
        String intervalProp = settingsFacade.getProperty(TARGET_FILE_SEC_INTERVAL);
        Integer secInterval = Integer.parseInt(intervalProp);

        if (secInterval < 1) {
            LOGGER.warn("{} is set to less than 1 second, no repeating schedule will be set to automatically generate " +
                    "target files!", TARGET_FILE_SEC_INTERVAL);
            return;
        }

        LOGGER.debug(String.format("The %s message will be sent every %ss starting %s", GENERATE_TARGET_FILE_EVENT,
                secInterval.toString(), today.toString()));

        //Schedule repeating job
        MotechEvent event = new MotechEvent(GENERATE_TARGET_FILE_EVENT);
        RepeatingSchedulableJob job = new RepeatingSchedulableJob(event,          //MOTECH event
                null,           //repeatCount, null means infinity
                secInterval,    //repeatIntervalInSeconds
                today.toDate(), //startTime
                null,           //endTime, null means no end time
                true);          //ignorePastFiresAtStart

        schedulerService.safeScheduleRepeatingJob(job);
    }

    private void scheduleTargetFileGenerationWhatsApp() {
        //Calculate today's fire time
        LOGGER.debug("test - scheduleTargetFileGenerationWA ");
        DateTimeFormatter fmt = DateTimeFormat.forPattern("H:m");
        String timeProp = settingsFacade.getProperty(TARGET_FILE_TIME_WHATSAPP);
        DateTime time = fmt.parseDateTime(timeProp);
        DateTime today = DateTime.now()                     // This means today's date...
                .withHourOfDay(time.getHourOfDay())         // ...at the hour...
                .withMinuteOfHour(time.getMinuteOfHour())   // ...and minute specified in imi.properties
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        //Second interval between events
        String intervalProp = settingsFacade.getProperty(TARGET_FILE_SEC_INTERVAL);
        Integer secInterval = Integer.parseInt(intervalProp);

        if (secInterval < 1) {
            LOGGER.warn("{} is set to less than 1 second, no repeating schedule will be set to automatically generate " +
                    "target files!", TARGET_FILE_SEC_INTERVAL);
            return;
        }

        LOGGER.debug(String.format("The %s message will be sent every %ss starting %s", GENERATE_TARGET_FILE_EVENT_WHATSAPP,
                secInterval.toString(), today.toString()));

        //Schedule repeating job
        LOGGER.debug("test - Schedule repeating job ");
        MotechEvent event = new MotechEvent(GENERATE_TARGET_FILE_EVENT_WHATSAPP);
        RepeatingSchedulableJob job = new RepeatingSchedulableJob(event,          //MOTECH event
                null,           //repeatCount, null means infinity
                secInterval,    //repeatIntervalInSeconds
                today.toDate(), //startTime
                null,           //endTime, null means no end time
                true);          //ignorePastFiresAtStart

        schedulerService.safeScheduleRepeatingJob(job);
    }

    @Autowired
    public TargetFileServiceImpl(@Qualifier("imiSettings") SettingsFacade settingsFacade,
                                 MotechSchedulerService schedulerService, AlertService alertService,
                                 SubscriptionService subscriptionService,
                                 CallRetryService callRetryService,
                                 FileAuditRecordDataService fileAuditRecordDataService) {
        this.schedulerService = schedulerService;
        this.settingsFacade = settingsFacade;
        this.alertService = alertService;
        this.subscriptionService = subscriptionService;
        this.callRetryService = callRetryService;
        this.fileAuditRecordDataService = fileAuditRecordDataService;

        scheduleTargetFileGeneration();

        freshCheckDND = settingsFacade.getProperty(IMI_FRESH_CHECK_DND);
        freshNoCheckDND = settingsFacade.getProperty(IMI_FRESH_NO_CHECK_DND);
        retryCheckDND = settingsFacade.getProperty(IMI_RETRY_CHECK_DND);
        retryNoCheckDND = settingsFacade.getProperty(IMI_RETRY_NO_CHECK_DND);

        freshCheckDNDJh = settingsFacade.getProperty(IMI_FRESH_CHECK_DND_JH);
        freshNoCheckDNDJh = settingsFacade.getProperty(IMI_FRESH_NO_CHECK_DND_JH);
        retryCheckDNDJh = settingsFacade.getProperty(IMI_RETRY_CHECK_DND_JH);
        retryNoCheckDNDJh = settingsFacade.getProperty(IMI_RETRY_NO_CHECK_DND_JH);
    }

    @Autowired
    public void TargetFileServiceImplWhatsApp(@Qualifier("imiSettings") SettingsFacade settingsFacade,
                                              MotechSchedulerService schedulerService, AlertService alertService,
                                              SubscriptionService subscriptionService,
                                              CallRetryService callRetryService,
                                              FileAuditRecordDataService fileAuditRecordDataService) {
        this.schedulerService = schedulerService;
        this.settingsFacade = settingsFacade;
        this.alertService = alertService;
        this.subscriptionService = subscriptionService;
        this.callRetryService = callRetryService;
        this.fileAuditRecordDataService = fileAuditRecordDataService;

        LOGGER.debug("test - TargetFileServiceImplWA ");

        scheduleTargetFileGenerationWhatsApp();

        freshCheckDND = settingsFacade.getProperty(IMI_FRESH_CHECK_DND);
        freshNoCheckDND = settingsFacade.getProperty(IMI_FRESH_NO_CHECK_DND);
        retryCheckDND = settingsFacade.getProperty(IMI_RETRY_CHECK_DND);
        retryNoCheckDND = settingsFacade.getProperty(IMI_RETRY_NO_CHECK_DND);

        freshCheckDNDJh = settingsFacade.getProperty(IMI_FRESH_CHECK_DND_JH);
        freshNoCheckDNDJh = settingsFacade.getProperty(IMI_FRESH_NO_CHECK_DND_JH);
        retryCheckDNDJh = settingsFacade.getProperty(IMI_RETRY_CHECK_DND_JH);
        retryNoCheckDNDJh = settingsFacade.getProperty(IMI_RETRY_NO_CHECK_DND_JH);
    }
    public String serviceIdFromOrigin(boolean freshCall, SubscriptionOrigin origin) {

        if (origin == SubscriptionOrigin.MCTS_IMPORT || origin == SubscriptionOrigin.RCH_IMPORT) {
            return freshCall ? freshCheckDND : retryCheckDND;
        }

        if (origin == SubscriptionOrigin.IVR) {
            return freshCall ? freshNoCheckDND : retryNoCheckDND;
        }

        throw new IllegalStateException("Unexpected SubscriptionOrigin value");
    }

    public String serviceIdFromOriginJh(boolean freshCall, SubscriptionOrigin origin) {

        if (origin == SubscriptionOrigin.MCTS_IMPORT || origin == SubscriptionOrigin.RCH_IMPORT) {
            return freshCall ? freshCheckDNDJh : retryCheckDNDJh;
        }

        if (origin == SubscriptionOrigin.IVR) {
            return freshCall ? freshNoCheckDNDJh : retryNoCheckDNDJh;
        }

        throw new IllegalStateException("Unexpected SubscriptionOrigin value");
    }


    private String targetFileName(String timestamp) {
        return String.format("OBD_NMS_%s.csv", timestamp);
    }

    private String wpTargetFileName(String timestamp) {
        return String.format("WP_OBD_NMS_%s.csv", timestamp);
    }
    // Helper method that makes the code a bit cleaner
    private void alert(String id, String name, String description) {
        alertService.create(id, name, description, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
    }


    private void writeHeader(OutputStreamWriter writer) throws IOException {
        /*
         * #1 RequestId
         *
         */
        writer.write("RequestId");
        writer.write(",");

        /*
         * #2 ServiceId
         *
         */
        writer.write("ServiceId");
        writer.write(",");

        /*
         * #3 Msisdn
         *
         */
        writer.write("Msisdn");
        writer.write(",");

        /*
         * #4 Cli
         *
         */
        writer.write("Cli");
        writer.write(",");

        /*
         * #5 Priority
         *
         */
        writer.write("Priority");
        writer.write(",");

        /*
         * #6 CallFlowURL
         *
         */
        writer.write("CallFlowURL");
        writer.write(",");

        /*
         * #7 ContentFileName
         *
         */
        writer.write("ContentFileName");
        writer.write(",");

        /*
         * #8 WeekId
         *
         */
        writer.write("WeekId");
        writer.write(",");

        /*
         * #9 LanguageLocationCode
         *
         */
        writer.write("LanguageLocationCode");
        writer.write(",");

        /*
         * #10 Circle
         *
         */
        writer.write("Circle");
        writer.write(",");

        /*
         * #11 subscription mode
         *
         */
        writer.write("SubscriptionOrigin");

        writer.write("\n");
    }


    private void writeSubscriptionRow(String requestId, String serviceId, // NO CHECKSTYLE More than 7 parameters
                                      String msisdn, String priority,  String callFlowUrl, String contentFileName,
                                      String weekId, String languageLocationCode, String circle,
                                      String subscriptionOrigin, OutputStreamWriter writer) throws IOException {
        /*
         * #1 RequestId
         *
         * A unique Request id for each obd record
         */
        writer.write(requestId);
        writer.write(",");

        /*
         * #2 ServiceId
         *
         * Unique Id provided by IMImobile for a particular service
         */
        writer.write(serviceId);
        writer.write(",");

        /*
         * #3 Msisdn
         *
         * 10 digit number to be dialed out
         */
        writer.write(msisdn);
        writer.write(",");

        /*
         * #4 Cli
         *
         * 10 Digit number to be displayed as CLI for the call. If left blank, the default CLI of the service
         * shall be picked up.
         */
        writer.write(""); // No idea why/what that field is: let's write nothing
        writer.write(",");

        /*
         * #5 Priority
         *
         * Specifies the priority with which the call is to be made. By default value is 0.
         * Possible Values: 0-Default, 1-Medium Priority, 2-High Priority
         */
        writer.write(priority); //todo: priorities, what do we want to do?
        writer.write(",");

        /*
         * #6 CallFlowURL
         *
         * The URL of the VXML flow. If unspecified, default VXML URL specified for the service shall be picked up
         */
        writer.write(callFlowUrl);
        writer.write(",");

        /*
         * #7 ContentFileName
         *
         * Content file to be played
         */
        writer.write(contentFileName);
        writer.write(",");

        /*
         * #8 WeekId
         *
         * Week id of the messaged delivered in OBD
         */
        writer.write(weekId);
        writer.write(",");

        /*
         * #9 LanguageLocationCode
         *
         * To identify the language
         */
        writer.write(languageLocationCode);
        writer.write(",");

        /*
         * #10 Circle
         *
         * Circle of the beneficiary.
         */
        if (circle != null && !circle.isEmpty()) {
            writer.write(circle);
        } else {
            writer.write(DEFAULT_CIRCLE);
        }
        writer.write(",");

        /*
         * #11 subscription mode
         *
         * I for IVR origin, M for MCTS origin
         */
        writer.write(subscriptionOrigin);

        writer.write("\n");
    }

    private void writeHeaderWP(OutputStreamWriter writer) throws IOException{
        /*
         * #1 RequestId - external_id
         *
         */
        writer.write("external_id");
        writer.write(",");

        /*
         * #2 MobileNumber
         *
         */
        writer.write("urn");
        writer.write(",");

        /*
         * #3 Week_id
         *
         */
        writer.write("week_id");
        writer.write(",");

        /*
         * #4 ContentFileName
         *
         */
        writer.write("content_file_name");
        writer.write(",");

        /*
         * #5 Preferred_language - ISO Code:
         *
         */
        writer.write("preferred_language");
        writer.write(",");

        /*
         * #6 State_Code
         *
         */
        writer.write("state_code");

        writer.write("\n");
    }

    private void writeSubscriptionRowWP(String requestId, String urn , String weekId,String contentFileName,
                                        String preferredLanguage , String stateCode , OutputStreamWriter writer) throws IOException {
        LOGGER.debug("test writeSubscriptionRowWP : ");
        LOGGER.debug("test - requestId " + requestId + " urn " + urn + " weekId " + weekId + " contentFileName " + contentFileName);
        LOGGER.debug(" test - preferredLanguage " + preferredLanguage + " stateCode " + stateCode );

        /*
         * #1 RequestId
         *
         * A unique Request id for each obd record
         */
        writer.write(requestId);
        writer.write(",");

        /*
         * #2 urn - mobile number
         *
         *10 digit number to be dialed out
         */
        writer.write(urn);
        writer.write(",");

        /*
         * #3 weekId
         *
         * Week id of the messaged delivered in OBD
         */
        writer.write(weekId);
        writer.write(",");

        /*
         * #4 contentFileName
         *
         * Content file to be played
         */
        writer.write(contentFileName);
        writer.write(",");

        /*
         * #5 preferredLanguage
         *
         */
        writer.write(preferredLanguage);
        writer.write(",");

        /*
         * #6 stateCode
         *
         */
        writer.write(stateCode);

        writer.write("\n");
    }
    private HashMap<String, Integer> generateFreshCalls(DateTime timestamp, int maxQueryBlock, String callFlowUrl,
                                                           HashMap<String, OutputStreamWriter> wr, List<String> subscriptionIdsJh, boolean split) throws IOException {

        LOGGER.info("generateFreshCallsObd({})", timestamp);

        int skippedrecords = 0;

        DayOfTheWeek dow = DayOfTheWeek.fromDateTime(timestamp);
        HashMap<String, Integer> recordsMap = new HashMap<>();
        int recordsWritten = 0;
        int recordsWrittenJh = 0;
        Long offset = 0L;

        do {
            List<Subscription> subscriptions = subscriptionService.findActiveSubscriptionsForDay(dow, offset, maxQueryBlock);
            LOGGER.info("Subs_block_size"  + subscriptions.size());
            if (subscriptions.size() == 0) {
                break;
            }

            for (Subscription subscription : subscriptions) {
                LOGGER.debug("Handling Subscription " + subscription.getId());
                offset = subscription.getId();

                Subscriber subscriber = subscription.getSubscriber();
                RequestId requestId = new RequestId(subscription.getSubscriptionId(), TIME_FORMATTER.print(timestamp));

                try {
                    SubscriptionPack pack = subscription.getSubscriptionPack();
                    int daysIntoPack = Days.daysBetween(subscription.getStartDate(), timestamp).getDays();
                    if (daysIntoPack == pack.getWeeks() * 7) {
                        //
                        // Do not add subscriptions on their last day to the fresh call list since we
                        // will try to fetch message for current +1 week, which wouldn't exist
                        // See https://applab.atlassian.net/browse/NMS-301
                        //
                        LOGGER.debug("Ignoring last day for subscription {} from fresh calls.",
                                subscription.getSubscriptionId());
                        skippedrecords++;
                        continue;
                    }

                    SubscriptionPackMessage msg = subscription.nextScheduledMessage(timestamp);
                    if(split && subscriptionIdsJh.contains(subscription.getSubscriptionId())) {
                            writeSubscriptionRow(
                                    requestId.toString(),
                                    serviceIdFromOriginJh(true, subscription.getOrigin()),
                                    subscriber.getCallingNumber().toString(),
                                    HIGH_PRIORITY, //todo: how do we choose a priority?
                                    callFlowUrl,
                                    msg.getMessageFileName(),
                                    msg.getWeekId(),
                                    // we are happy with empty language and circle since they are optional
                                    subscriber.getLanguage() == null ? "" : subscriber.getLanguage().getCode(),
                                    subscriber.getCircle() == null ? "" : subscriber.getCircle().getName(),
                                    subscription.getOrigin().getCode(),
                                    wr.get(Jh));
                            recordsWrittenJh++;
                        }
                    else {
                            writeSubscriptionRow(
                                    requestId.toString(),
                                    serviceIdFromOrigin(true, subscription.getOrigin()),
                                    subscriber.getCallingNumber().toString(),
                                    NORMAL_PRIORITY, //todo: how do we choose a priority?
                                    callFlowUrl,
                                    msg.getMessageFileName(),
                                    msg.getWeekId(),
                                    // we are happy with empty language and circle since they are optional
                                    subscriber.getLanguage() == null ? "" : subscriber.getLanguage().getCode(),
                                    subscriber.getCircle() == null ? "" : subscriber.getCircle().getName(),
                                    subscription.getOrigin().getCode(),
                                    wr.get(non_Jh));
                            recordsWritten++;

                    }
                } catch (IllegalStateException se) {
                    String message = se.toString();
                    alertService.create(subscription.getSubscriptionId(), "IllegalStateException", message,
                            AlertType.HIGH, AlertStatus.NEW, 0, null);
                    LOGGER.error(message,se);
                }
            }

        } while (true);

        LOGGER.info(WROTE+non_Jh, recordsWritten);
        recordsMap.put(non_Jh, recordsWritten);
        if(split){
            LOGGER.info(WROTE+Jh, recordsWrittenJh);
            recordsMap.put(Jh, recordsWrittenJh);
        }
        return recordsMap;
    }

    private Integer generateFreshCallsWP(DateTime timestamp, int maxQueryBlock,
                                                           OutputStreamWriter wr) throws IOException{
        LOGGER.info("generateFreshCallsWPObd({})", timestamp);

        int skippedrecords = 0;

        DayOfTheWeek dow = DayOfTheWeek.fromDateTime(timestamp);
        int recordsWritten = 0;
        Long offset = 0L;
        do {
            LOGGER.debug("test - findActiveSubscriptionsForDayWP ");
            LOGGER.debug("offset is " + offset);
            List<Subscription> subscriptions = subscriptionService.findActiveSubscriptionsForDayWP(dow, offset, maxQueryBlock);
            LOGGER.info("Subs_block_size "  + subscriptions.size());
            if (subscriptions.size() == 0) {
                break;
            }
                for (Subscription subscription : subscriptions) {
                    LOGGER.debug("Handling Subscription " + subscription.getId());
                    offset = subscription.getId();

                    Subscriber subscriber = subscription.getSubscriber();
                    RequestId requestId = new RequestId(subscription.getSubscriptionId(), TIME_FORMATTER.print(timestamp));
                    LOGGER.debug("test - subscriber is " + subscriber.getId());
                    try {
                        SubscriptionPack pack = subscription.getSubscriptionPack();
                        int daysIntoPack = Days.daysBetween(subscription.getStartDate(), timestamp).getDays();
                        if (daysIntoPack == pack.getWeeks() * 7) {
                            //
                            // Do not add subscriptions on their last day to the fresh call list since we
                            // will try to fetch message for current +1 week, which wouldn't exist
                            // See https://applab.atlassian.net/browse/NMS-301
                            //
                            LOGGER.debug("Ignoring last day for subscription {} from fresh calls.",
                                    subscription.getSubscriptionId());
                            skippedrecords++;
                            continue;
                        }

                        SubscriptionPackMessage msg = subscription.nextScheduledMessage(timestamp);
                        String stateCode;
                        if(subscriber.getMother() == null){
                            stateCode = subscriber.getChild().getState().getCode().toString();
                        }
                        else{
                            stateCode = subscriber.getMother().getState().getCode().toString();
                        }

                        writeSubscriptionRowWP(
                                requestId.toString() ,
                                subscriber.getCallingNumber().toString(),
                                msg.getWeekId(),
                                msg.getMessageFileName(),
                                subscriber.getLanguage().getIsoCode(),
                                stateCode,
                                wr
                                );

                            recordsWritten++;

                    } catch (IllegalStateException se) {
                        String message = se.toString();
                        alertService.create(subscription.getSubscriptionId(), "IllegalStateException", message,
                                AlertType.HIGH, AlertStatus.NEW, 0, null);
                        LOGGER.error(message,se);
                    }
                }
            }
        while (true);
        LOGGER.debug("test - " + recordsWritten);
        return recordsWritten;
    }


    private int generateWelcomeMessageWP(DateTime timestamp , int maxQueryBlock , OutputStreamWriter writer) throws  IOException{
        LOGGER.info("test generateWelcomeMessageWP({})", timestamp);

        int skippedrecords = 0;

        DayOfTheWeek dow = DayOfTheWeek.fromDateTime(timestamp);
        int recordCountWelcome = 0;
        Long offset = 0L;

        do {

            LOGGER.debug("offset is " + offset);
            List<Subscription> subscriptions = subscriptionService.findWelcomeActiveSubscriptionsForDayWP(dow, offset, maxQueryBlock);
            LOGGER.info("Subs_block_size "  + subscriptions.size());
            if(subscriptions.size()==0){
                break;
            }
            List<Long> subscriptionIdList = new ArrayList<>();
            for(Subscription subscription : subscriptions){
                Subscriber subscriber = subscription.getSubscriber();
                RequestId requestId = new RequestId(subscription.getSubscriptionId(), TIME_FORMATTER.print(timestamp));
                LOGGER.debug("test - subscriber is " + subscriber.getId());
                offset = subscription.getId();
                try {
                    SubscriptionPack pack = subscription.getSubscriptionPack();
                    int daysIntoPack = Days.daysBetween(subscription.getStartDate(), timestamp).getDays();
                    if (daysIntoPack == pack.getWeeks() * 7) {
                        //
                        // Do not add subscriptions on their last day to the fresh call list since we
                        // will try to fetch message for current +1 week, which wouldn't exist
                        // See https://applab.atlassian.net/browse/NMS-301
                        //
                        LOGGER.debug("Ignoring last day for subscription {} from fresh calls.",
                                subscription.getSubscriptionId());
                        skippedrecords++;
                        continue;
                    }

//                    SubscriptionPackMessage msg = subscription.nextScheduledMessage(timestamp);
                    String stateCode;
                    if(subscriber.getMother() == null){
                        stateCode = subscriber.getChild().getState().getCode().toString();
                    }
                    else{
                        stateCode = subscriber.getMother().getState().getCode().toString();
                    }
                    LOGGER.debug("test - calling number " + subscriber.getCallingNumber().toString());
                    LOGGER.debug("test - stateCode " + stateCode );
                    LOGGER.debug("test - contentFileNameWhatsappWelcomeMessage " + contentFileNameWhatsappWelcomeMessage());
                    writeSubscriptionRowWP(
                            requestId.toString() ,
                            subscriber.getCallingNumber().toString(),
                            "w1_1",
                            contentFileNameWhatsappWelcomeMessage(),
                            subscriber.getLanguage().getIsoCode(),
                            stateCode,
                            writer
                    );

//                    subscription.setNeedsWelcomeMessageViaObd(false);
                    subscriptionIdList.add(subscription.getId());

                    recordCountWelcome++;


                } catch (IllegalStateException se) {
                    String message = se.toString();
                    alertService.create(subscription.getSubscriptionId(), "IllegalStateException", message,
                            AlertType.HIGH, AlertStatus.NEW, 0, null);
                    LOGGER.error(message,se);
                }
            }
            LOGGER.debug("Updating 'optInWelcome' to false of subscription list with size " + subscriptionIdList.size());
            subscriptionService.UpdateWelcomeMessageOptIn(subscriptionIdList);

        }while (true);
        LOGGER.debug("test - recordCountWelcome " + recordCountWelcome );
        return recordCountWelcome;
    }
    private HashMap<String, Integer> generateRetryCalls(DateTime timestamp, int maxQueryBlock, String callFlowUrl,
                                   HashMap<String, OutputStreamWriter> wr, List<String> subscriptionIdsJh, boolean split) throws IOException {

        LOGGER.info("generateRetryCallsObd({})", timestamp);
        int count = 0;
        int countJh = 0;
        HashMap<String, Integer> retryCount = new HashMap<>();
        Long offset = 0L;
        do {
            // All calls are rescheduled for the next day which means that we should query for all CallRetry records
            List<CallRetry> callRetries = callRetryService.retrieveAll(offset, maxQueryBlock);
            LOGGER.info("Call_Retries"+callRetries.size());

            if (callRetries.size() == 0) {
                break;
            }

            for (CallRetry callRetry : callRetries) {
                    offset = callRetry.getId();
                    RequestId requestId = new RequestId(callRetry.getSubscriptionId(), TIME_FORMATTER.print(timestamp));
                    if(split && subscriptionIdsJh.contains(callRetry.getSubscriptionId())) {
                        writeSubscriptionRow(
                                requestId.toString(),
                                serviceIdFromOriginJh(false, callRetry.getSubscriptionOrigin()),
                                callRetry.getMsisdn().toString(),
                                HIGH_PRIORITY,
                                callFlowUrl,
                                callRetry.getContentFileName(),
                                callRetry.getWeekId(),
                                callRetry.getLanguageLocationCode(),
                                callRetry.getCircle(),
                                callRetry.getSubscriptionOrigin().getCode(),
                                wr.get(Jh));
                        countJh++;
                    }
                    else {
                        writeSubscriptionRow(
                                requestId.toString(),
                                serviceIdFromOrigin(false, callRetry.getSubscriptionOrigin()),
                                callRetry.getMsisdn().toString(),
                                NORMAL_PRIORITY,
                                callFlowUrl,
                                callRetry.getContentFileName(),
                                callRetry.getWeekId(),
                                callRetry.getLanguageLocationCode(),
                                callRetry.getCircle(),
                                callRetry.getSubscriptionOrigin().getCode(),
                                wr.get(non_Jh));
                        count++;
                    }
                }

        } while (true);

        LOGGER.info(WROTE+non_Jh+"Retry", count);
        retryCount.put(non_Jh, count);
        if(split) {
            LOGGER.info(WROTE+Jh+"Retry", countJh);
            retryCount.put(Jh, countJh);
        }

        return retryCount;
    }

    private File localObdDir() {
        return new File(settingsFacade.getProperty(LOCAL_OBD_DIR));
    }

    private File wpLocalObdDir() {
        return new File(settingsFacade.getProperty(LOCAL_OBD_DIR_WHATSAPP));
    }

    private String contentFileNameWhatsappWelcomeMessage(){ return new String(settingsFacade.getProperty(CONTENT_FILE_NAME_WHATSAPP_WELCOME_MESSAGE)); }
    @Transactional
    public HashMap<String, TargetFileNotification> generateTargetFile(boolean split) {
        LOGGER.info("generateTargetFile()"+split);
        DateTime today = DateTime.now();
        String targetFileName = targetFileName(TIME_FORMATTER.print(today));
        File localTargetDir = localObdDir();
        String checksum;
        File targetFile = new File(localTargetDir, targetFileName);

        HashMap<String, Integer> recordCount;
        HashMap<String, Integer> recordCountRetry;

        int maxQueryBlock = Integer.parseInt(settingsFacade.getProperty(MAX_QUERY_BLOCK));
        String callFlowUrl = settingsFacade.getProperty(TARGET_FILE_CALL_FLOW_URL);

        if (callFlowUrl == null) {
            //it's ok to have an empty call flow url - the spec says the default call flow will be used
            //whatever that is...
            callFlowUrl = "";
        }
        HashMap<String, OutputStreamWriter> wr = new HashMap<>();
        HashMap<String, TargetFileNotification> tfn = new HashMap<>();
        if(split){
            String targetFileNameJh = targetFileName(TIME_FORMATTER.print(today)+"JH");
            File localTargetDirJh = localObdDir();
            String checksumJh;
            File targetFileJh = new File(localTargetDirJh, targetFileNameJh);

            try {
                FileOutputStream fos = new FileOutputStream(targetFile);
                OutputStreamWriter writer = new OutputStreamWriter(fos);

                FileOutputStream fosJh = new FileOutputStream(targetFileJh);
                OutputStreamWriter writerJh = new OutputStreamWriter(fosJh);

                //Header
                writeHeader(writer);
                writeHeader(writerJh);

                List<String> subscriptionIdsJh = subscriptionService.findJhSubscriptionIds();
                LOGGER.info("JH-Subscriptions-Number"+subscriptionIdsJh.size());


                wr.put(Jh, writerJh);
                wr.put(non_Jh, writer);

                //Fresh calls
                recordCount = generateFreshCalls(today, maxQueryBlock, callFlowUrl, wr, subscriptionIdsJh, true);

                //Retry calls
                recordCountRetry = generateRetryCalls(today, maxQueryBlock, callFlowUrl, wr, subscriptionIdsJh, true);

                writer.close();
                fos.close();

                writerJh.close();
                fosJh.close();

                checksum = ChecksumHelper.checksum(targetFile);
                checksumJh = ChecksumHelper.checksum(targetFileJh);
                tfn.put(Jh, new TargetFileNotification(targetFileNameJh, checksumJh, recordCount.get(Jh)+recordCountRetry.get(Jh)));

            } catch (IOException e) {
                LOGGER.error(e.getMessage(),e);
                alert(targetFile.toString(), "targetFile", e.getMessage());
                fileAuditRecordDataService.create(new FileAuditRecord(FileType.TARGET_FILE, targetFile.getName(),
                        false, e.getMessage(), null, null));
                return null;
            }
        }
        else{
            try {
                FileOutputStream fos = new FileOutputStream(targetFile);
                OutputStreamWriter writer = new OutputStreamWriter(fos);

                //Header
                writeHeader(writer);
                wr.put(non_Jh, writer);

                //Fresh calls
                recordCount = generateFreshCalls(today, maxQueryBlock, callFlowUrl, wr, Collections.emptyList(), false);

                //Retry calls
                recordCountRetry = generateRetryCalls(today, maxQueryBlock, callFlowUrl, wr, Collections.emptyList(), false);

                writer.close();
                fos.close();

                checksum = ChecksumHelper.checksum(targetFile);

            } catch (IOException e) {
                LOGGER.error(e.getMessage(),e);
                alert(targetFile.toString(), "targetFile", e.getMessage());
                fileAuditRecordDataService.create(new FileAuditRecord(FileType.TARGET_FILE, targetFile.getName(),
                        false, e.getMessage(), null, null));
                return null;
            }
        }

        tfn.put(non_Jh, new TargetFileNotification(targetFileName, checksum, recordCount.get(non_Jh)+recordCountRetry.get(non_Jh)));
        LOGGER.debug("TargetFileNotification = {}", tfn.toString());

        //audit the success
        for(TargetFileNotification t: tfn.values()) {
            fileAuditRecordDataService.create(new FileAuditRecord(FileType.TARGET_FILE, t.getFileName(), true,
                    null, t.getRecordsCount(), t.getChecksum()));
        }
        return tfn;
    }

    private void sendNotificationRequest(TargetFileNotification tfn) {
        String notificationUrl = settingsFacade.getProperty(TARGET_FILE_NOTIFICATION_URL);
        LOGGER.debug("Sending {} to {}", tfn, notificationUrl);


        ExponentialRetrySender sender = new ExponentialRetrySender(settingsFacade, alertService);

        HttpPost httpPost = new HttpPost(notificationUrl);
        ObjectMapper mapper = new ObjectMapper();

        try {
            String requestJson = mapper.writeValueAsString(tfn);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setEntity(new StringEntity(requestJson));
        } catch (IOException e) {
            throw new InternalException(String.format("Unable to create targetFile notification request: %s",
                    e.getMessage()), e);
        }

        sender.sendNotificationRequest(httpPost, HttpStatus.SC_ACCEPTED, tfn.getFileName(), "targetFile Notification Request");
    }

    private void sendNotificationRequestWhatsApp(TargetFileNotification tfn) {
        String notificationUrl = settingsFacade.getProperty(TARGET_FILE_NOTIFICATION_URL);
        LOGGER.debug("Sending {} to {}", tfn, notificationUrl);

        ExponentialRetrySender sender = new ExponentialRetrySender(settingsFacade, alertService);

        HttpPost httpPost = new HttpPost(notificationUrl);
        ObjectMapper mapper = new ObjectMapper();

        try {
            String requestJson = mapper.writeValueAsString(tfn);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setEntity(new StringEntity(requestJson));
        } catch (IOException e) {
            throw new InternalException(String.format("Unable to create targetFile notification request: %s",
                    e.getMessage()), e);
        }
        LOGGER.debug("test - httpPost object created");
        sender.sendNotificationRequestWhatsApp(httpPost, HttpStatus.SC_ACCEPTED, tfn.getFileName(), "targetFile Notification Request");
    }

    @MotechListener(subjects = { GENERATE_TARGET_FILE_EVENT })
    @Transactional
    public void generateTargetFile(MotechEvent event) {
        LOGGER.debug(event.toString());
            HashMap<String, TargetFileNotification> tfn = generateTargetFile(Boolean.valueOf(settingsFacade.getProperty(generateJhFile)));
            if (tfn != null) {
                // Copy the OBD file from the local imi.local_obd_dir to the remote imi.local_obd_dir network share
                ScpHelper scpHelper = new ScpHelper(settingsFacade);
                for(TargetFileNotification t: tfn.values()) {
                    try {
                        scpHelper.scpObdToRemote(t.getFileName());
                    } catch (ExecException e) {
                        String error = String.format("Error copying target file %s: %s", t.getFileName(),
                                e.getMessage());
                        LOGGER.error(error,e);
                        fileAuditRecordDataService.create(new FileAuditRecord(
                                FileType.TARGET_FILE,
                                t.getFileName(),
                                false,
                                error,
                                null,
                                null
                        ));
                        alert(t.getFileName(), "targetFileName", error);
                        return;
                    }

                    //notify the IVR system the file is ready
                    sendNotificationRequest(t);
                }

            }
    }


    /**
     * Log & audit the fact that IMI processed the OBD file (successfully or not)
     *
     * @param request file name & status
     */
    @Override
    public void handleFileProcessedStatusNotification(FileProcessedStatusRequest request) {
        fileAuditRecordDataService.create(new FileAuditRecord(
                FileType.TARGET_FILE,
                request.getFileName(),
                request.getFileProcessedStatus() == FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY,
                request.getFileProcessedStatus().getName(),
                null,
                null
        ));
        if (request.getFileProcessedStatus() != FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY) {
            LOGGER.error(request.toString());
            //todo: IT check if alert was created
            alert(request.getFileName(), "targetFileName",
                    String.format("Target File Processing Error: %s", request.getFailureReason()));
        }
    }


    /**
     * Log & audit the fact that IMI processed the OBD whatsapp file (successfully or not)
     *
     * @param request file name & status
     */
    @Override
    public void handleFileProcessedStatusNotificationWhatsApp(FileProcessedStatusRequest request) {
        fileAuditRecordDataService.create(new FileAuditRecord(
                FileType.WHATSAPP_TARGET_FILE,
                request.getFileName(),
                request.getFileProcessedStatus() == FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY,
                request.getFileProcessedStatus().getName(),
                null,
                null
        ));
        if (request.getFileProcessedStatus() != FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY) {
            LOGGER.error(request.toString());
            //todo: IT check if alert was created
            alert(request.getFileName(), "targetFileName",
                    String.format("Target File Processing Error: %s", request.getFailureReason()));
        }
    }


    @MotechListener(subjects = {GENERATE_TARGET_FILE_EVENT_WHATSAPP})
    @Transactional
    public void generateTargetFileWhatsApp(MotechEvent event) {
        LOGGER.debug(event.toString());
        LOGGER.debug("Scheduler is called for Whatsapp ");
        TargetFileNotification tfn = generateTargetFileWhatsApp();
        LOGGER.debug("test - target file generated ");
        if (tfn != null) {
            // Copy the OBD file from the local imi.local_obd_dir to the remote imi.local_obd_dir network share
            ScpHelper scpHelper = new ScpHelper(settingsFacade);
                try {
                    scpHelper.scpWhatsAppObdToRemote(tfn.getFileName());
                } catch (ExecException e) {
                    String error = String.format("Error copying target file %s: %s", tfn.getFileName(),
                            e.getMessage());
                    LOGGER.error(error,e);
                    fileAuditRecordDataService.create(new FileAuditRecord(
                            FileType.WHATSAPP_TARGET_FILE,
                            tfn.getFileName(),
                            false,
                            error,
                            null,
                            null
                    ));
                    alert(tfn.getFileName(), "targetFileName", error);
                    return;
                }
                //notify the IVR system the file is ready
            LOGGER.debug("notifying IVR system file is ready");
                sendNotificationRequestWhatsApp(tfn);
        }
    }


    @Transactional
    public TargetFileNotification generateTargetFileWhatsApp() {
        LOGGER.info("generateTargetFileWP()");
        DateTime today = DateTime.now();
        String targetFileName = wpTargetFileName(TIME_FORMATTER.print(today));
        File localTargetDir = wpLocalObdDir();
        String checksum;
        File targetFile = new File(localTargetDir, targetFileName);

        LOGGER.debug("test - targetFileName");

        Integer recordCount = 0;
        Integer recordCountWelcome = 0;

        int maxQueryBlock = Integer.parseInt(settingsFacade.getProperty(MAX_QUERY_BLOCK));

        try {
            FileOutputStream fos = new FileOutputStream(targetFile);
            OutputStreamWriter writer = new OutputStreamWriter(fos);

            //Header
            writeHeaderWP(writer);

            LOGGER.debug("test - generateFreshCallsWP ");
            //Fresh calls
            recordCount = generateFreshCallsWP(today, maxQueryBlock, writer);

            LOGGER.debug("test - generateWelcomeMessageWP");
            //welcome message
            recordCountWelcome = generateWelcomeMessageWP(today , maxQueryBlock , writer);

            writer.close();
            fos.close();

            checksum = ChecksumHelper.checksum(targetFile);

        } catch (IOException e) {
            LOGGER.error(e.getMessage(),e);
            alert(targetFile.toString(), "targetFile", e.getMessage());
            fileAuditRecordDataService.create(new FileAuditRecord(FileType.WHATSAPP_TARGET_FILE, targetFile.getName(),
                    false, e.getMessage(), null, null));
            return null;
        }

        TargetFileNotification tfn = new TargetFileNotification(targetFileName, checksum, recordCount+recordCountWelcome  ) ;
        LOGGER.debug("TargetFileNotification = {}", tfn.toString());

        //audit the success

            fileAuditRecordDataService.create(new FileAuditRecord(FileType.WHATSAPP_TARGET_FILE, tfn.getFileName(), true,
                    null, tfn.getRecordsCount(), tfn.getChecksum()));

        return tfn;
    }
}
