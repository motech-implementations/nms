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
import org.motechproject.nms.imi.exception.ExecException;
import org.motechproject.nms.imi.exception.InternalException;
import org.motechproject.nms.imi.exception.InvalidCdrFileException;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.CsrValidatorService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
    private static final String SORTED_SUFFIX = ".sorted";

    private static final Logger LOGGER = LoggerFactory.getLogger(CdrFileServiceImpl.class);
    public static final String CDR_DETAIL_FILE = "CDR Detail File";

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
        alertService.create(file, CDR_DETAIL_FILE, error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
        fileAuditRecordDataService.create(new FileAuditRecord(FileType.CDR_DETAIL_FILE, file, false, error, null,
                null));
        throw new IllegalStateException(error);
    }


    private void reportAuditAndThrow(String file, List<String> errors) {
        for (String error : errors) {
            LOGGER.error(error);
            alertService.create(file, CDR_DETAIL_FILE, error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            fileAuditRecordDataService.create(new FileAuditRecord(FileType.CDR_DETAIL_FILE, file, false, error,
                    null, null));
        }
        throw new InvalidCdrFileException(errors);
    }


    private void reportAuditAndThrowInternalError(String file, String error) {
        LOGGER.error(error);
        alertService.create(file, CDR_DETAIL_FILE, error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
        fileAuditRecordDataService.create(new FileAuditRecord(FileType.CDR_DETAIL_FILE, file, false, error,
                null, null));
        throw new InternalException(error);
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
            alertService.create(file, CDR_DETAIL_FILE, error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            fileAuditRecordDataService.create(new FileAuditRecord(FileType.CDR_DETAIL_FILE, file, false, error,
                    null, null));
        }
        sendNotificationRequest(new CdrFileProcessedNotification(
                FileProcessedStatus.FILE_ERROR_IN_FILE_FORMAT.getValue(),
                file,
                StringUtils.join(errors, ",")
        ));
    }


    // Aggregate detail records for a requestId into a single summary record
    private void aggregateDetailRecord(CallDetailRecordDto cdr, CallSummaryRecordDto csr) {
        if (csr.getRequestId() == null) {
            csr.setRequestId(cdr.getRequestId());
            csr.setMsisdn(cdr.getMsisdn());
            csr.setContentFileName(cdr.getContentFile());
            csr.setWeekId(cdr.getWeekId());
            csr.setLanguageLocationCode(cdr.getLanguageLocationId());
            csr.setCircle(cdr.getCircleId());
            csr.setFinalStatus(FinalCallStatus.fromStatusCode(cdr.getStatusCode()));
            Map<Integer, Integer> statusStats = new HashMap<>();
            statusStats.put(cdr.getStatusCode().getValue(), 1);
            csr.setStatusStats(statusStats);
            csr.setSecondsPlayed(cdr.getMsgPlayDuration());
            csr.setCallAttempts(1);
        } else {
            // Increment the statusCode stats
            int statusCodeCount = 1;
            if (csr.getStatusStats().containsKey(cdr.getStatusCode().getValue())) {
                statusCodeCount = csr.getStatusStats().get(cdr.getStatusCode().getValue()) + 1;
            }
            csr.getStatusStats().put(cdr.getStatusCode().getValue(), statusCodeCount);

            // Increment the message play duration
            if (cdr.getMsgPlayDuration() > csr.getSecondsPlayed()) {
                csr.setSecondsPlayed(cdr.getMsgPlayDuration());
            }

            //todo: maybe verify the attemptCount provided by IMI and see if there are holes?

            /*
               Update the final status

               Because we don't know that the detail csrs are ordered, we could be receiving a failed csr
               after a success csr. We don't want to override a success csr.
             */
            if (csr.getFinalStatus() != FinalCallStatus.SUCCESS) {
                csr.setFinalStatus(FinalCallStatus.fromStatusCode(cdr.getStatusCode()));
            }

            csr.setCallAttempts(csr.getCallAttempts() + 1);
        }
    }


    private int getMaxErrorCount() {
        try {
            return Integer.parseInt(settingsFacade.getProperty(MAX_CDR_ERROR_COUNT));
        } catch (NumberFormatException e) {
            return MAX_CDR_ERROR_COUNT_DEFAULT;
        }
    }


    private void sendProcessSummaryRecordEvent(CallSummaryRecordDto record) {
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, record);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        eventRelay.sendEventMessage(motechEvent);
    }


    /**
     *
     * @param file          the actual file to process
     * @param fileInfo      file information provided about the file (ie: expected checksum & recordCount)
     * @param action        PASS1: verify checksum, verify record count, verify CSV valid
     *                      PASS2: PASS1 + verify individual records point to existing entities, check sort order
     *                      PASS3: PASS1 + aggregate same call CDRS into CSR and send for distributed processing
     * @return
     */
    @Override // NO CHECKSTYLE Cyclomatic Complexity
    @SuppressWarnings("PMD.NcssMethodCount")
    public List<String> iterateDetailFile(File file, FileInfo fileInfo, Action action) {
        int maxErrorCount = getMaxErrorCount();
        int errorCount = 0;
        List<String> errors = new ArrayList<>();
        Set<String> requestIds = new HashSet<>();
        int lineNumber = 1;
        String thisChecksum = "";
        // Might end with .sorted (PASS2, PASS3) or not (PASS1)
        String fileName = file.getName();

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr)) {

            MessageDigest md = null;
            @SuppressWarnings("PMD.UnusedLocalVariable")
            DigestInputStream dis = null;
            if (action == Action.PASS1) {
                md = MessageDigest.getInstance("MD5");
                dis = new DigestInputStream(fis, md);
            }

            String line;
            String currentRequestId = "";
            CallSummaryRecordDto csr = null;
            while ((line = reader.readLine()) != null) {
                try {

                    // Parse the CSV line into a CDR
                    // This will trow IllegalArgumentException if the CSV is malformed
                    CallDetailRecordDto cdr = CsvHelper.csvLineToCdr(line);

                    if (currentRequestId.equals(cdr.getRequestId().toString())) {
                        // Aggregate CDRs for the same call into a single CSR
                        if (action == Action.PASS3) {
                            aggregateDetailRecord(cdr, csr);
                        }
                    } else {
                        // Send last CSR, if any, for processing
                        if (action == Action.PASS3 && csr != null) {
                            sendProcessSummaryRecordEvent(csr);
                            csr = null; //todo: does that help the GC?
                        }

                        currentRequestId = cdr.getRequestId().toString();

                        // Check for sort order, we don't care about the ascending or descending thing, what's
                        // important is that all the CDRs for one call are grouped.
                        if (action != Action.PASS1) {
                            if (requestIds.contains(currentRequestId)) {
                                // An identical requestId was found, that means this file isn't sorted
                                errors.add(String.format("%s is not sorted properly!", fileName));
                                reportAuditAndThrow(fileName, errors);
                            }
                            requestIds.add(currentRequestId);
                        }

                        // Start aggregating a new CSR
                        csr = new CallSummaryRecordDto();
                        aggregateDetailRecord(cdr, csr);
                        csrValidatorService.validateSummaryRecord(csr);
                    }
                } catch (IllegalArgumentException e) {
                    errors.add(String.format("Line %d: %s", lineNumber, e.getMessage()));
                    errorCount++;
                }
                if (errorCount >= maxErrorCount) {
                    errors.add(String.format("The maximum number of allowed errors of %d has been reached, " +
                            "ending file verification.", maxErrorCount));
                    reportAuditAndThrow(fileName, errors);
                }
                lineNumber++;
            }

            if (action == Action.PASS1) {
                thisChecksum = new String(Hex.encodeHex(md.digest()));
            }

        } catch (IOException e) {
            String error = String.format("Unable to read %s: %s", fileName, e.getMessage());
            reportAuditAndThrowInternalError(file.getName(), error);
        } catch (NoSuchAlgorithmException e) {
            String error = String.format("Unable to compute checksum: %s", e.getMessage());
            reportAuditAndThrowInternalError(file.getName(), error);
        }

        if (action == Action.PASS1 && !thisChecksum.equals(fileInfo.getChecksum())) {
            String error = String.format("Checksum mismatch, provided checksum: %s, calculated checksum: %s",
                    fileInfo.getChecksum(), thisChecksum);
            reportAuditAndThrow(fileName, error);
        }

        if (lineNumber - 1 != fileInfo.getRecordsCount()) {
            String error = String.format("Record count mismatch, provided count: %d, actual count: %d",
                    fileInfo.getRecordsCount(), lineNumber - 1);
            reportAuditAndThrow(fileName, error);
        }

        return errors;
    }


    private void sendProcessDetailFileEvent(FileInfo fileInfo) {
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(FILE_INFO_PARAM_KEY, fileInfo);
        MotechEvent motechEvent = new MotechEvent(PROCESS_DETAIL_FILE_SUBJECT, eventParams);
        eventRelay.sendEventMessage(motechEvent);
    }


    private File localCdrDir() {
        return new File(settingsFacade.getProperty(LOCAL_CDR_DIR));
    }


    // Phase 1: verify the file exists, the csv is valid and its record count and checksum match the provided info
    //          while collecting a list of errors on the go.
    //          Does not proceed to phase 2 if any error occurred and returns an error
    @Override
    public List<String> verifyDetailFileChecksumAndCount(FileInfo fileInfo) {
        File file = new File(localCdrDir(), fileInfo.getCdrFile());

        List<String> errors = iterateDetailFile(file, fileInfo, Action.PASS1);
        if (errors.size() > 0) {
            // Ends up in a HttpStatus.BAD_REQUEST response to IMI which should generate an IMI-OPS alert
            reportAuditAndThrow(fileInfo.getCdrFile(), errors);
        }

        // Send a MOTECH event to continue to phase 2 (without timing out the POST from IMI)
        sendProcessDetailFileEvent(fileInfo);

        return errors;
    }


    // Phase 2: Aggregate detail records into summary records
    //          Validate all CallSummaryRecord fields exist in the database
    //          Stop processing if errors occurred
    @Override
    @MotechListener(subjects = { PROCESS_DETAIL_FILE_SUBJECT })
    public List<String> processDetailFile(MotechEvent event) {
        FileInfo fileInfo = (FileInfo) event.getParameters().get(FILE_INFO_PARAM_KEY);
        File file = new File(localCdrDir(), fileInfo.getCdrFile());
        String fileName = file.getName();

        // Copy the file from the IMI network share (imi.remote_cdr_dir) into local cdr dir (imi.local_cdr_dir)
        // Another MOTECH node than the one which picked up the HTTP cdrFileNotification might be picking up this
        // event, so make sure to copy the file locally, if it's not there already.

        if (file.exists()) {
            LOGGER.debug("{} already exists on this MOTECH node, no need to copy it from IMI.", file.getName());
        } else {
            LOGGER.debug("{} doesn't exists on this MOTECH node, copying it from IMI...", file.getName());
            ScpHelper scpHelper = new ScpHelper(settingsFacade);
            try {
                scpHelper.scpCdrFromRemote(fileName);
            } catch (ExecException e) {
                String error = String.format("Error copying CDR file %s: %s", fileName, e.getMessage());
                LOGGER.error(error);
                fileAuditRecordDataService.create(new FileAuditRecord(
                        FileType.CDR_DETAIL_FILE,
                        fileName,
                        false,
                        error,
                        null,
                        null
                ));
                //todo: send alert
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }

        // Sort the file (on requestId & attemptNo) into a new file with the same name plus the ".sorted" suffix
        SortHelper sortHelper = new SortHelper(settingsFacade);
        try {
            sortHelper.sort(fileName);
        } catch (ExecException e) {
            String error = String.format("Error sorting CDR file %s: %s", fileName, e.getMessage());
            LOGGER.error(error);
            fileAuditRecordDataService.create(new FileAuditRecord(
                    FileType.CDR_DETAIL_FILE,
                    fileName,
                    false,
                    error,
                    null,
                    null
            ));
            //todo: send alert
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        // We want to be dealing with the sorted file from now onwards
        file = new File(localCdrDir(), fileInfo.getCdrFile() + SORTED_SUFFIX);

        // NOTE: once sorted the checksums won't match anymore.

        // Second verification pass (more in detail) verify all entities & sort order
        List<String> errors = iterateDetailFile(file, fileInfo, Action.PASS2);
        if (errors.size() > 0) {
            reportAuditAndPost(fileInfo.getCdrFile(), errors);
            return errors;
        }


        // We processed as much as we can, let IMI know before distributing the work to phase 3
        sendNotificationRequest(new CdrFileProcessedNotification(
                FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY.getValue(),
                fileInfo.getCdrFile(),
                null
        ));


        // Phase 3: distribute the processing of individual CSRs to all nodes.
        //          each node may generate an individual error that NMS-OPS will have to respond to.
        errors = iterateDetailFile(file, fileInfo, Action.PASS3);
        if (errors.size() > 0) {
            reportAuditAndPost(fileInfo.getCdrFile(), errors);
        }

        return errors;
    }
}
