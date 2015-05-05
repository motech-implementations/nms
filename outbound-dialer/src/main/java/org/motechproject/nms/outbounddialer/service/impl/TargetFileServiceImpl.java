package org.motechproject.nms.outbounddialer.service.impl;


import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.region.language.domain.Language;
import org.motechproject.nms.outbounddialer.domain.AuditRecord;
import org.motechproject.nms.outbounddialer.domain.CallRetry;
import org.motechproject.nms.outbounddialer.domain.DayOfTheWeek;
import org.motechproject.nms.outbounddialer.domain.FileProcessedStatus;
import org.motechproject.nms.outbounddialer.domain.FileType;
import org.motechproject.nms.outbounddialer.repository.CallRetryDataService;
import org.motechproject.nms.outbounddialer.repository.FileAuditDataService;
import org.motechproject.nms.outbounddialer.service.TargetFileNotification;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service("targetFileService")
public class TargetFileServiceImpl implements TargetFileService {
    private static final String TARGET_FILE_TIME = "outbound-dialer.target_file_time";
    private static final String MAX_QUERY_BLOCK = "outbound-dialer.max_query_block";
    private static final String TARGET_FILE_MS_INTERVAL = "outbound-dialer.target_file_ms_interval";
    private static final String TARGET_FILE_DIRECTORY = "outbound-dialer.target_file_directory";
    private static final String TARGET_FILE_NOTIFICATION_URL = "outbound-dialer.target_file_notification_url";

    private static final String GENERATE_TARGET_FILE_EVENT = "nms.obd.generate_target_file";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    private SettingsFacade settingsFacade;
    private MotechSchedulerService schedulerService;
    private AlertService alertService;
    private SubscriptionDataService subscriptionDataService;
    private SubscriberDataService subscriberDataService;
    private CallRetryDataService callRetryDataService;
    private FileAuditDataService fileAuditDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(TargetFileServiceImpl.class);


    /**
     * Use the MOTECH scheduler to setup a repeating job
     * The job will start today at the time stored in outbound-dialer.target_file_time in outbound-dialer.properties
     * It will repeat every outbound-dialer.target_file_ms_interval milliseconds (default value is a day)
     */
    private void scheduleTargetFileGeneration() {
        //Calculate today's fire time
        DateTimeFormatter fmt = DateTimeFormat.forPattern("H:m");
        String timeProp = settingsFacade.getProperty(TARGET_FILE_TIME);
        DateTime time = fmt.parseDateTime(timeProp);
        DateTime today = DateTime.now()                     // This means today's date...
                .withHourOfDay(time.getHourOfDay())         // ...at the hour...
                .withMinuteOfHour(time.getMinuteOfHour())   // ...and minute specified in outbound-dialer.properties
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
                                 SubscriptionDataService subscriptionDataService,
                                 CallRetryDataService callRetryDataService,
                                 SubscriberDataService subscriberDataService,
                                 FileAuditDataService fileAuditDataService) {
        this.schedulerService = schedulerService;
        this.settingsFacade = settingsFacade;
        this.alertService = alertService;
        this.subscriptionDataService = subscriptionDataService;
        this.callRetryDataService = callRetryDataService;
        this.subscriberDataService = subscriberDataService;
        this.fileAuditDataService = fileAuditDataService;

        scheduleTargetFileGeneration();
    }


    private String targetFileName() {
        return String.format("OBD_%s.csv", TIME_FORMATTER.print(DateTime.now()));
    }


    private void insertTargetFileAuditRecord(TargetFileNotification tfn, String status) {
        fileAuditDataService.create(new AuditRecord(FileType.TARGET_FILE, tfn.getFileName(), tfn.getRecordCount(),
                tfn.getChecksum(), status));
    }


    //todo: verify we can do that - if the shared directory is an FTP share this might not work
    private File createTargetFileDirectory() {
        File userHome = new File(System.getProperty("user.home"));
        File targetFileDirectory = new File(userHome, settingsFacade.getProperty(TARGET_FILE_DIRECTORY));

        if (targetFileDirectory.exists()) {
            LOGGER.info("targetFile directory exists: {}", targetFileDirectory);
        } else {
            LOGGER.info("creating targetFile directory: {}", targetFileDirectory);
            if (!targetFileDirectory.mkdirs()) {
                String error = String.format("Unable to create targetFileDirectory %s: mkdirs() failed",
                        targetFileDirectory);
                LOGGER.error(error);
                alertService.create(targetFileDirectory.toString(), "targetFileDirectory", "mkdirs() failed",
                        AlertType.CRITICAL, AlertStatus.NEW, 0, null);
                insertTargetFileAuditRecord(new TargetFileNotification(), error);
                throw new IllegalStateException();
            }
        }
        return targetFileDirectory;
    }


    private void writeSubscriptionRow(Subscription subscription, OutputStreamWriter writer) throws IOException {
        //subscription id
        writer.write(subscription.getSubscriptionId());
        writer.write(",");

        //phone number
        writer.write(subscription.getSubscriber().getCallingNumber().toString());
        writer.write(",");

        //language
        Subscriber subscriber = subscription.getSubscriber();
        //todo: don't understand why subscriber.getLanguage() doesn't work here...
        Language language = (Language) subscriberDataService.getDetachedField(subscriber, "language");
        writer.write(language.getCode());

        //todo...

        writer.write("\n");
    }


    public TargetFileNotification generateTargetFile() {
        String targetFileName = targetFileName();
        File targetFileDirectory;
        MessageDigest md;
        int recordCount = 0;

        try {
            targetFileDirectory = createTargetFileDirectory();
        } catch (IllegalStateException e) {
            return null;
        }

        File targetFile = new File(targetFileDirectory, targetFileName);
        try (FileOutputStream fos = new FileOutputStream(targetFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {

            md = MessageDigest.getInstance("MD5");
            @SuppressWarnings("PMD.UnusedLocalVariable")
            DigestOutputStream dos = new DigestOutputStream(fos, md);

            //figure out which day to work with
            final DayOfTheWeek today = DayOfTheWeek.today();


            int maxQueryBlock = Integer.parseInt(settingsFacade.getProperty(MAX_QUERY_BLOCK));

            //FRESH calls
            int page = 1;
            int numBlockRecord;
            do {
                List<Subscription> subscriptions = subscriptionDataService.findByStatus(SubscriptionStatus.ACTIVE,
                    new QueryParams(page, maxQueryBlock));
                numBlockRecord = subscriptions.size();

                for (Subscription subscription : subscriptions) {
                    writeSubscriptionRow(subscription, writer);
                }

                page++;
                recordCount += numBlockRecord;

            } while (numBlockRecord > 0);

            //Retry calls
            page = 1;
            do {
                List<CallRetry> callRetries = callRetryDataService.findByDayOfTheWeek(today,
                        new QueryParams(page, maxQueryBlock));
                numBlockRecord = callRetries.size();

                for (CallRetry callRetry : callRetries) {
                    writer.write(callRetry.getSubscriptionId());
                    writer.write(",");
                    writer.write(callRetry.getMsisdn().toString());
                    writer.write(",");
                    writer.write(callRetry.getLanguageLocationCode());
                    //todo...
                    writer.write("\n");
                }

                page++;
                recordCount += numBlockRecord;

            } while (numBlockRecord > 0);

            LOGGER.info("Created targetFile with {} record{}", recordCount, recordCount == 1 ? "" : "s");

        } catch (NoSuchAlgorithmException | IOException e) {
            LOGGER.error(e.getMessage());
            alertService.create(targetFile.toString(), "targetFile", e.getMessage(), AlertType.CRITICAL,
                    AlertStatus.NEW, 0, null);
            insertTargetFileAuditRecord(new TargetFileNotification(targetFile.toString(), null, null), e.getMessage());
            return null;
        }

        String md5Checksum = new String(Hex.encodeHex(md.digest()));
        TargetFileNotification tfn = new TargetFileNotification(targetFileName, md5Checksum, recordCount);
        LOGGER.info("TargetFileNotification = {}", tfn.toString());

        //audit the success
        insertTargetFileAuditRecord(tfn, "Success");

        return tfn;
    }


    private void sendNotificationRequest(TargetFileNotification tfn) {
        String notificationUrl = settingsFacade.getProperty(TARGET_FILE_NOTIFICATION_URL);
        LOGGER.info("Sending {} to {}", tfn, notificationUrl);

        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(notificationUrl);
            ObjectMapper mapper = new ObjectMapper();
            String requestJson = mapper.writeValueAsString(tfn);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(new StringEntity(requestJson));
            HttpResponse response = httpClient.execute(httpPost);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != HttpStatus.SC_OK) {
                String error = String.format("Expecting HTTP 200 response from %s but received HTTP %d : %s ",
                        notificationUrl, responseCode, EntityUtils.toString(response.getEntity()));
                LOGGER.error(error);
                alertService.create("targetFile notification request", "targetFile", error, AlertType.CRITICAL,
                        AlertStatus.NEW, 0, null);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to send targetFile notification request: {}", e.getMessage());
            alertService.create("targetFile notification request", "targetFile", e.getMessage(), AlertType.CRITICAL,
                    AlertStatus.NEW, 0, null);
        }
    }


    @MotechListener(subjects = { GENERATE_TARGET_FILE_EVENT })
    public void generateTargetFile(MotechEvent event) {
        LOGGER.info(event.toString());

        TargetFileNotification tfn = generateTargetFile();

        if (tfn != null) {
            //notify the IVR system the file is ready
            sendNotificationRequest(tfn);
        }
    }


    @Override
    public void handleFileProcessedStatusNotification(FileProcessedStatusRequest request) {
        if (request.getFileProcessedStatus() == FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY) {
            LOGGER.info(request.toString());
            //We're happy.
            //todo: audit that?
        } else {
            LOGGER.error(request.toString());
            alertService.create(request.getFileName(), "targetFileName", "Target File Processing Error",
                    AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            //todo: audit that?
        }
    }
}
