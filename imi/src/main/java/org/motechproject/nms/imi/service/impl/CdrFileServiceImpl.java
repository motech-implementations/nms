package org.motechproject.nms.imi.service.impl;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.imi.domain.FileAuditRecord;
import org.motechproject.nms.imi.domain.FileProcessedStatus;
import org.motechproject.nms.imi.domain.FileType;
import org.motechproject.nms.imi.exception.InvalidCdrFileException;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.CsrValidatorService;
import org.motechproject.nms.imi.service.contract.VerifyResults;
import org.motechproject.nms.imi.service.contract.CdrFileProcessedNotification;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.nms.kilkari.dto.CallDetailRecordDto;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Implementation of the {@link CdrFileService} interface.
 */
@Service("cdrFileService")
public class CdrFileServiceImpl implements CdrFileService {

    private static final String CDR_FILE_NOTIFICATION_URL = "imi.cdr_file_notification_url";
    private static final String LOCAL_CDR_DIR = "imi.local_cdr_dir";
    private static final String MAX_CDR_ERROR_COUNT = "imi.max_cdr_error_count";
    private static final int MAX_CDR_ERROR_COUNT_DEFAULT = 100;
    private static final String PROCESS_SUMMARY_RECORD_SUBJECT = "nms.imi.kk.process_summary_record";
    private static final String CSR_PARAM_KEY = "csr";
    private static final String PROCESS_DETAIL_FILE_SUBJECT = "nms.imi.kk.process_detail_file";
    private static final String FILE_INFO_PARAM_KEY = "fileInfo";
    private static final String MAX_NOTIFICATION_RETRY_COUNT = "imi.notification_retry_count";
    private static final int MAX_NOTIFICATION_RETRY_COUNT_DEFAULT = 3;

    private static final Logger LOGGER = LoggerFactory.getLogger(CdrFileServiceImpl.class);

    private SettingsFacade settingsFacade;
    private EventRelay eventRelay;
    private FileAuditRecordDataService fileAuditRecordDataService;
    private AlertService alertService;
    private CsrValidatorService csrValidatorService;


    @Autowired
    public CdrFileServiceImpl(@Qualifier("imiSettings") SettingsFacade settingsFacade, EventRelay eventRelay,
                              FileAuditRecordDataService fileAuditRecordDataService, AlertService alertService,
                              CsrValidatorService csrValidatorService) {
        this.settingsFacade = settingsFacade;
        this.eventRelay = eventRelay;
        this.fileAuditRecordDataService = fileAuditRecordDataService;
        this.alertService = alertService;
        this.csrValidatorService = csrValidatorService;
    }


    private void reportAuditAndThrow(String file, String error) {
        LOGGER.error(error);
        alertService.create(file, "CDR Detail File", error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
        fileAuditRecordDataService.create(new FileAuditRecord(FileType.CDR_DETAIL_FILE, file, false, error, null,
                null));
        throw new IllegalStateException(error);
    }


    private void reportAuditAndThrow(String file, List<String> errors) {
        for (String error : errors) {
            LOGGER.error(error);
            alertService.create(file, "CDR Detail File", error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            fileAuditRecordDataService.create(new FileAuditRecord(FileType.CDR_DETAIL_FILE, file, false, error,
                    null, null));
        }
        throw new InvalidCdrFileException(errors);
    }


    private void sendNotificationRequest(CdrFileProcessedNotification cfpn) {
        String notificationUrl = settingsFacade.getProperty(CDR_FILE_NOTIFICATION_URL);
        LOGGER.debug("Sending {} to {}", cfpn, notificationUrl);

        int maxRetryCount;
        try {
            maxRetryCount = Integer.parseInt(settingsFacade.getProperty(MAX_NOTIFICATION_RETRY_COUNT));
        } catch (NumberFormatException e) {
            maxRetryCount = MAX_NOTIFICATION_RETRY_COUNT_DEFAULT;
        }
        int count = 0;

        String error = "";

        while (count < maxRetryCount) {
            try {
                HttpClient httpClient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(notificationUrl);
                ObjectMapper mapper = new ObjectMapper();
                String requestJson = mapper.writeValueAsString(cfpn);
                httpPost.setHeader("Content-type", "application/json");
                httpPost.setEntity(new StringEntity(requestJson));
                HttpResponse response = httpClient.execute(httpPost);
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == HttpStatus.SC_OK) {
                    return;
                } else {
                    error = String.format("Expecting HTTP 200 response from %s but received HTTP %d : %s ",
                            notificationUrl, responseCode, EntityUtils.toString(response.getEntity()));
                    LOGGER.warn(error);
                    alertService.create(cfpn.getFileName(), "cdrFile notification request", error,
                            AlertType.MEDIUM, AlertStatus.NEW, 0, null);
                }
            } catch (IOException e) {
                error = String.format("Unable to send cdrFile notification request: %s", e.getMessage());
                LOGGER.warn(error);
                alertService.create(cfpn.getFileName(), "cdrFile notification request", error,
                        AlertType.MEDIUM, AlertStatus.NEW, 0, null);
            }
            count++;
        }

        // Retry count exceeded, consider this a critical error
        LOGGER.error(error);
        alertService.create(cfpn.getFileName(), "cdrFile notification request", error,
                AlertType.CRITICAL, AlertStatus.NEW, 0, null);

    }


    private void reportAuditAndPost(String file, List<String> errors) {
        for (String error : errors) {
            LOGGER.error(error);
            alertService.create(file, "CDR Detail File", error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            fileAuditRecordDataService.create(new FileAuditRecord(FileType.CDR_DETAIL_FILE, file, false, error,
                    null, null));
        }
        sendNotificationRequest(new CdrFileProcessedNotification(
                FileProcessedStatus.FILE_ERROR_IN_FILE_FORMAT.getValue(),
                file,
                StringUtils.join(errors, ",")
        ));
    }


    // Aggregate detail records for a requestId into single summary records using a (potentially huge) map
    private void aggregateDetailRecord(CallDetailRecordDto cdr, Map<String, CallSummaryRecordDto> records) {
        if (records.containsKey(cdr.getRequestId().toString())) {
            CallSummaryRecordDto record = records.get(cdr.getRequestId().toString());

            // Increment the statusCode stats
            int statusCodeCount = 1;
            if (record.getStatusStats().containsKey(cdr.getStatusCode().getValue())) {
                statusCodeCount = record.getStatusStats().get(cdr.getStatusCode().getValue()) + 1;
            }
            record.getStatusStats().put(cdr.getStatusCode().getValue(), statusCodeCount);

            // Increment the message play duration
            if (cdr.getMsgPlayDuration() > record.getSecondsPlayed()) {
                record.setSecondsPlayed(cdr.getMsgPlayDuration());
            }

            //todo: maybe verify the attemptCount provided by IMI and see if there are holes?

            /*
               Update the final status

               Because we don't know that the detail records are ordered, we could be receiving a failed record
               after a success record. We don't want to override a success record.
             */
            if (record.getFinalStatus() != FinalCallStatus.SUCCESS) {
                record.setFinalStatus(FinalCallStatus.fromStatusCode(cdr.getStatusCode()));
            }

            record.setCallAttempts(record.getCallAttempts() + 1);

        } else {
            CallSummaryRecordDto record = new CallSummaryRecordDto();
            record.setRequestId(cdr.getRequestId());
            record.setMsisdn(cdr.getMsisdn());
            record.setContentFileName(cdr.getContentFile());
            record.setWeekId(cdr.getWeekId());
            record.setLanguageLocationCode(cdr.getLanguageLocationId());
            record.setCircle(cdr.getCircleId());
            record.setFinalStatus(FinalCallStatus.fromStatusCode(cdr.getStatusCode()));
            Map<Integer, Integer> statusStats = new HashMap<>();
            statusStats.put(cdr.getStatusCode().getValue(), 1);
            record.setStatusStats(statusStats);
            record.setSecondsPlayed(cdr.getMsgPlayDuration());
            record.setCallAttempts(1);
            records.put(cdr.getRequestId().toString(), record);
        }
    }


    private int getMaxErrorCount() {
        try {
            return Integer.parseInt(settingsFacade.getProperty(MAX_CDR_ERROR_COUNT));
        } catch (NumberFormatException e) {
            return MAX_CDR_ERROR_COUNT_DEFAULT;
        }
    }


    @Override
    public VerifyResults aggregateDetailFile(File file, FileInfo fileInfo, boolean verifyOnly) {
        int maxErrorCount = getMaxErrorCount();
        int errorCount = 0;
        String thisChecksum = "";
        Map<String, CallSummaryRecordDto> records = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int lineNumber = 1;

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr)) {


            MessageDigest md = MessageDigest.getInstance("MD5");
            @SuppressWarnings("PMD.UnusedLocalVariable")
            DigestInputStream dis = new DigestInputStream(fis, md);

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    CallDetailRecordDto cdr = CsvHelper.csvLineToCdr(line);

                    if (!verifyOnly) {
                        aggregateDetailRecord(cdr, records);
                    }

                } catch (IllegalArgumentException e) {
                    errors.add(String.format("Line %d: %s", lineNumber, e.getMessage()));
                    errorCount++;
                }
                if (errorCount >= maxErrorCount) {
                    errors.add(String.format("The maximum number of allowed errors of %d has been reached, " +
                            "discarding all remaining errors.", maxErrorCount));
                    reportAuditAndThrow(file.getName(), errors);
                }
                lineNumber++;
            }

            thisChecksum = new String(Hex.encodeHex(md.digest()));

        } catch (IOException e) {
            String error = String.format("Unable to read %s: %s", file, e.getMessage());
            reportAuditAndThrow(file.getName(), error);
        } catch (NoSuchAlgorithmException e) {
            String error = String.format("Unable to compute checksum: %s", e.getMessage());
            reportAuditAndThrow(file.getName(), error);
        }

        if (!thisChecksum.equals(fileInfo.getChecksum())) {
            String error = String.format("Checksum mismatch, provided checksum: %s, calculated checksum: %s",
                    fileInfo.getChecksum(), thisChecksum);
            reportAuditAndThrow(file.getName(), error);
        }

        if (lineNumber - 1 != fileInfo.getRecordsCount()) {
            String error = String.format("Record count mismatch, provided count: %d, actual count: %d",
                    fileInfo.getRecordsCount(), lineNumber - 1);
            reportAuditAndThrow(file.getName(), error);
        }

        return new VerifyResults(records, errors);
    }


    private void sendProcessDetailFileEvent(FileInfo fileInfo) {
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(FILE_INFO_PARAM_KEY, fileInfo);
        MotechEvent motechEvent = new MotechEvent(PROCESS_DETAIL_FILE_SUBJECT, eventParams);
        eventRelay.sendEventMessage(motechEvent);
    }


    private void sendProcessSummaryRecordEvent(CallSummaryRecordDto record) {
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, record);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        eventRelay.sendEventMessage(motechEvent);
    }


    private File localCdrDir() {
        return new File(settingsFacade.getProperty(LOCAL_CDR_DIR));
    }


    // Phase 1: verify the file exists, the csv is valid and its record count and checksum match the provided info
    //          while collecting a list of errors on the go.
    //          Does not proceed to phase 2 if any error occurred and returns an error
    @Override
    public VerifyResults verifyDetailFile(FileInfo fileInfo) {
        File file = new File(localCdrDir(), fileInfo.getCdrFile());

        VerifyResults results = aggregateDetailFile(file, fileInfo, true);
        if (results.getErrors().size() > 0) {
            // Ends up in a HttpStatus.BAD_REQUEST response to IMI which should generate an IMI-OPS alert
            reportAuditAndThrow(fileInfo.getCdrFile(), results.getErrors());
        }

        // Send a MOTECH event to continue to phase 2 (without timing out the POST from IMI)
        sendProcessDetailFileEvent(fileInfo);

        return results;
    }


    // Phase 2: Aggregate detail records into summary records
    //          Validate all CallSummaryRecord fields exist in the database
    //          Stop processing if errors occurred
    @Override
    @MotechListener(subjects = {PROCESS_DETAIL_FILE_SUBJECT})
    public List<String> processDetailFile(MotechEvent event) {
        FileInfo fileInfo = (FileInfo) event.getParameters().get(FILE_INFO_PARAM_KEY);
        File file = new File(localCdrDir(), fileInfo.getCdrFile());

        VerifyResults results = aggregateDetailFile(file, fileInfo, false);
        if (results.getErrors().size() > 0) {
            // Since this passed phase 1, there's no reason we should receive errors here, but just in case...
            reportAuditAndPost(fileInfo.getCdrFile(), results.getErrors());
            return results.getErrors();
        }

        // Do as much validation as we can (ie: verify subscription exists, ...) before distributing the CSR processing
        List<String> errors = csrValidatorService.validateSummaryRecords(results.getRecords());
        if (errors.size() > 0) {
            reportAuditAndPost(fileInfo.getCdrFile(), errors);
            return results.getErrors();
        }

        // We processed as much as we can, let IMI know before distributing the work to phase 3
        sendNotificationRequest(new CdrFileProcessedNotification(
                FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY.getValue(),
                fileInfo.getCdrFile(),
                null
        ));

        // Phase 3: distribute the processing of individual CSRs to all nodes.
        //          each node may generate an individual error that NMS-OPS will have to respond to.
        Iterator it = results.getRecords().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            sendProcessSummaryRecordEvent((CallSummaryRecordDto) entry.getValue());
        }

        return results.getErrors();
    }
}
