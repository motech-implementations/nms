package org.motechproject.nms.imi.service.impl;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.nms.imi.domain.FileAuditRecord;
import org.motechproject.nms.imi.domain.FileProcessedStatus;
import org.motechproject.nms.imi.domain.FileType;
import org.motechproject.nms.imi.exception.ExecException;
import org.motechproject.nms.imi.exception.InternalException;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.TargetFileService;
import org.motechproject.nms.imi.service.contract.TargetFileNotification;
import org.motechproject.nms.imi.web.contract.FileProcessedStatusRequest;
import org.motechproject.nms.kilkari.domain.CallRetry;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.props.domain.RequestId;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.scheduler.contract.RepeatingSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

@Service("targetFileService")
public class TargetFileServiceImpl implements TargetFileService {
    private static final String LOCAL_OBD_DIR = "imi.local_obd_dir";
    private static final String TARGET_FILE_TIME = "imi.target_file_time";
    private static final String MAX_QUERY_BLOCK = "imi.max_query_block";
    private static final String TARGET_FILE_SEC_INTERVAL = "imi.target_file_sec_interval";
    private static final String TARGET_FILE_NOTIFICATION_URL = "imi.target_file_notification_url";
    private static final String TARGET_FILE_CALL_FLOW_URL = "imi.target_file_call_flow_url";
    private static final String NORMAL_PRIORITY = "0";
    private static final String IMI_FRESH_CHECK_DND = "imi.fresh_check_dnd";
    private static final String IMI_FRESH_NO_CHECK_DND = "imi.fresh_no_check_dnd";
    private static final String IMI_RETRY_CHECK_DND = "imi.retry_check_dnd";
    private static final String IMI_RETRY_NO_CHECK_DND = "imi.retry_no_check_dnd";

    private static final String GENERATE_TARGET_FILE_EVENT = "nms.obd.generate_target_file";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private static final String DEFAULT_CIRCLE = "99";  // https://applab.atlassian.net/browse/NIP-64

    private SettingsFacade settingsFacade;
    private MotechSchedulerService schedulerService;
    private AlertService alertService;
    private SubscriptionService subscriptionService;
    private SubscriberDataService subscriberDataService;
    private CallRetryDataService callRetryDataService;
    private FileAuditRecordDataService fileAuditRecordDataService;

    private static String freshCheckDND;
    private static String freshNoCheckDND;
    private static String retryCheckDND;
    private static String retryNoCheckDND;

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

        LOGGER.debug(String.format("The %s message will be sent every %ss starting %s",
                GENERATE_TARGET_FILE_EVENT, secInterval.toString(), today.toString()));

        //Schedule repeating job
        MotechEvent event = new MotechEvent(GENERATE_TARGET_FILE_EVENT);
        RepeatingSchedulableJob job = new RepeatingSchedulableJob(
                event,          //MOTECH event
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
                                 CallRetryDataService callRetryDataService,
                                 SubscriberDataService subscriberDataService,
                                 FileAuditRecordDataService fileAuditRecordDataService) {
        this.schedulerService = schedulerService;
        this.settingsFacade = settingsFacade;
        this.alertService = alertService;
        this.subscriptionService = subscriptionService;
        this.callRetryDataService = callRetryDataService;
        this.subscriberDataService = subscriberDataService;
        this.fileAuditRecordDataService = fileAuditRecordDataService;

        scheduleTargetFileGeneration();

        freshCheckDND = settingsFacade.getProperty(IMI_FRESH_CHECK_DND);
        freshNoCheckDND = settingsFacade.getProperty(IMI_FRESH_NO_CHECK_DND);
        retryCheckDND = settingsFacade.getProperty(IMI_RETRY_CHECK_DND);
        retryNoCheckDND = settingsFacade.getProperty(IMI_RETRY_NO_CHECK_DND);
    }


    public String serviceIdFromOrigin(boolean freshCall, SubscriptionOrigin origin) {

        if (origin == SubscriptionOrigin.MCTS_IMPORT) {
            return freshCall ? freshCheckDND : retryCheckDND;
        }

        if (origin == SubscriptionOrigin.IVR) {
            return freshCall ? freshNoCheckDND : retryNoCheckDND;
        }

        throw new IllegalStateException("Unexpected SubscriptionOrigin value");
    }


    private String targetFileName(String timestamp) {
        return String.format("OBD_NMS_%s.csv", timestamp);
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


    private int generateFreshCalls(DateTime timestamp, int maxQueryBlock, String callFlowUrl,
                                   OutputStreamWriter writer) throws IOException {

        DayOfTheWeek dow = DayOfTheWeek.fromDateTime(timestamp);
        int recordCount = 0;
        int page = 1;
        int numBlockRecord;
        do {
            List<Subscription> subscriptions = subscriptionService.findActiveSubscriptionsForDay(dow, page,
                    maxQueryBlock);
            numBlockRecord = subscriptions.size();
            int messageWriteCount = 0;
            for (Subscription subscription : subscriptions) {


                Subscriber subscriber = subscription.getSubscriber();

                RequestId requestId = new RequestId(subscription.getSubscriptionId(),
                        TIME_FORMATTER.print(timestamp));

                try {
                    SubscriptionPackMessage msg = subscription.nextScheduledMessage(timestamp);

                    //todo: don't understand why subscriber.getLanguage() doesn't work here...
                    // it's not working because of https://applab.atlassian.net/browse/MOTECH-1678
                    Language language = (Language) subscriberDataService.getDetachedField(subscriber, "language");
                    Circle circle = (Circle) subscriberDataService.getDetachedField(subscriber, "circle");

                    writeSubscriptionRow(
                            requestId.toString(),
                            serviceIdFromOrigin(true, subscription.getOrigin()),
                            subscriber.getCallingNumber().toString(),
                            NORMAL_PRIORITY, //todo: how do we choose a priority?
                            callFlowUrl,
                            msg.getMessageFileName(),
                            msg.getWeekId(),
                            // we are happy with empty language and circle since they are optional
                            language == null ? "" : language.getCode(),
                            circle == null ? "" : circle.getName(),
                            subscription.getOrigin().getCode(),
                            writer);
                    messageWriteCount += 1;

                } catch (IllegalStateException se) {
                    LOGGER.error(se.toString());
                }
            }

            page++;
            recordCount += messageWriteCount;

        } while (numBlockRecord > 0);

        return recordCount;
    }

    private int generateRetryCalls(DateTime timestamp, int maxQueryBlock, String callFlowUrl,
                                   OutputStreamWriter writer) throws IOException {

        DayOfTheWeek dow = DayOfTheWeek.fromDateTime(timestamp);
        int recordCount = 0;
        int page = 1;
        int numBlockRecord;
        do {
            List<CallRetry> callRetries = callRetryDataService.findByDayOfTheWeek(dow,
                    new QueryParams(page, maxQueryBlock));
            numBlockRecord = callRetries.size();

            for (CallRetry callRetry : callRetries) {
                RequestId requestId = new RequestId(callRetry.getSubscriptionId(),
                        TIME_FORMATTER.print(timestamp));

                writeSubscriptionRow(
                        requestId.toString(),
                        serviceIdFromOrigin(false, callRetry.getSubscriptionOrigin()),
                        callRetry.getMsisdn().toString(),
                        NORMAL_PRIORITY, //todo: look into priorities...
                        callFlowUrl,
                        callRetry.getContentFileName(),
                        callRetry.getWeekId(),
                        callRetry.getLanguageLocationCode(),
                        callRetry.getCircle(),
                        callRetry.getSubscriptionOrigin().getCode(),
                        writer);
            }

            page++;
            recordCount += numBlockRecord;

        } while (numBlockRecord > 0);

        return recordCount;
    }


    private File localObdDir() {
        return new File(settingsFacade.getProperty(LOCAL_OBD_DIR));
    }


    /*
    /**
     * 4.4.1 Target File Format
     */
    public TargetFileNotification generateTargetFile() {
        DateTime today = DateTime.now();
        String targetFileName = targetFileName(TIME_FORMATTER.print(today));
        File localTargetDir = localObdDir();
        int recordCount = 0;
        String checksum;

        File targetFile = new File(localTargetDir, targetFileName);
        try (FileOutputStream fos = new FileOutputStream(targetFile);
            OutputStreamWriter writer = new OutputStreamWriter(fos)) {

            int maxQueryBlock = Integer.parseInt(settingsFacade.getProperty(MAX_QUERY_BLOCK));
            String callFlowUrl = settingsFacade.getProperty(TARGET_FILE_CALL_FLOW_URL);
            if (callFlowUrl == null) {
                //it's ok to have an empty call flow url - the spec says the default call flow will be used
                //whatever that is...
                callFlowUrl = "";
            }

            activatePendingSubscriptions(today, maxQueryBlock);

            //Header
            writeHeader(writer);

            //Fresh calls
            recordCount = generateFreshCalls(today, maxQueryBlock, callFlowUrl, writer);

            //Retry calls
            recordCount += generateRetryCalls(today, maxQueryBlock, callFlowUrl, writer);

            LOGGER.debug("Created targetFile with {} record{}", recordCount, recordCount == 1 ? "" : "s");

            writer.close();
            fos.close();

            checksum = ChecksumHelper.checksum(targetFile);

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            alert(targetFile.toString(), "targetFile", e.getMessage());
            fileAuditRecordDataService.create(new FileAuditRecord(FileType.TARGET_FILE, targetFile.getName(),
                    false, e.getMessage(), null, null));
            return null;
        }

        TargetFileNotification tfn = new TargetFileNotification(targetFileName, checksum, recordCount);
        LOGGER.debug("TargetFileNotification = {}", tfn.toString());

        //audit the success
        fileAuditRecordDataService.create(new FileAuditRecord(FileType.TARGET_FILE, tfn.getFileName(), true,
                null, tfn.getRecordsCount(), tfn.getChecksum()));

        return tfn;
    }

    private void activatePendingSubscriptions(DateTime today, int maxQueryBlock) {
        int page = 1;
        while (true) {
            List<Subscription> subscriptions = subscriptionService
                    .findPendingSubscriptionsFromDate(today, page, maxQueryBlock);
            if (CollectionUtils.isNotEmpty(subscriptions)) {
                for (Subscription subscription : subscriptions) {
                    subscriptionService.activateSubscription(subscription);
                }
                page++;
            } else {
                break;
            }
        }
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
            httpPost.setEntity(new StringEntity(requestJson));
        } catch (IOException e) {
            throw new InternalException(String.format("Unable to create targetFile notification request: %s",
                    e.getMessage()), e);
        }

        sender.sendNotificationRequest(httpPost, HttpStatus.SC_ACCEPTED, tfn.getFileName(), "targetFile Notification Request");
    }


    @MotechListener(subjects = { GENERATE_TARGET_FILE_EVENT })
    public void generateTargetFile(MotechEvent event) {
        LOGGER.debug(event.toString());

        TargetFileNotification tfn = generateTargetFile();

        if (tfn != null) {
            // Copy the OBD file from the local imi.local_obd_dir to the remote imi.local_obd_dir network share
            ScpHelper scpHelper = new ScpHelper(settingsFacade);
            try {
                scpHelper.scpObdToRemote(tfn.getFileName());
            } catch (ExecException e) {
                String error = String.format("Error copying CDR file %s: %s", tfn.getFileName(), e.getMessage());
                LOGGER.error(error);
                fileAuditRecordDataService.create(new FileAuditRecord(
                        FileType.TARGET_FILE,
                        tfn.getFileName(),
                        false,
                        error,
                        null,
                        null
                ));
                //todo: send alert
                return;
            }

            //notify the IVR system the file is ready
            sendNotificationRequest(tfn);
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
            alert(request.getFileName(), "targetFileName", "Target File Processing Error");
        }
    }
}
