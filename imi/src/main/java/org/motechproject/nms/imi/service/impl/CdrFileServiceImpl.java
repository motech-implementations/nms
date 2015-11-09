package org.motechproject.nms.imi.service.impl;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.imi.domain.CallSummaryRecord;
import org.motechproject.nms.imi.domain.FileAuditRecord;
import org.motechproject.nms.imi.domain.FileProcessedStatus;
import org.motechproject.nms.imi.domain.FileType;
import org.motechproject.nms.imi.exception.ExecException;
import org.motechproject.nms.imi.exception.InternalException;
import org.motechproject.nms.imi.exception.InvalidCdrFileException;
import org.motechproject.nms.imi.repository.CallDetailRecordDataService;
import org.motechproject.nms.imi.repository.CallSummaryRecordDataService;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.contract.CdrFileProcessedNotification;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.nms.kilkari.dto.CallDetailRecordDto;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.StatusCode;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private static final String CDR_CSR_RETENTION_DURATION = "imi.cdr_csr.retention.duration";
    private static final int MIN_CALL_DATA_RETENTION_DURATION_IN_DAYS = 5;
    private static final String CDR_CSR_CLEANUP_SUBJECT = "nms.imi.cdr_csr.cleanup";
    private static final String CDR_TABLE_NAME = "motech_data_services.nms_imi_cdrs";
    private static final String CSR_TABLE_NAME = "motech_data_services.nms_imi_csrs";
    private static final int MAX_CDR_ERROR_COUNT_DEFAULT = 100;
    private static final String PROCESS_SUMMARY_RECORD_SUBJECT = "nms.imi.kk.process_summary_record";
    private static final String END_OF_CDR_PROCESSING_SUBJECT = "nms.imi.kk.end_of_cdr_processing";
    private static final String CSR_PARAM_KEY = "csr";
    private static final String PROCESS_FILES_SUBJECT = "nms.imi.kk.process_files";
    private static final String OBD_FILE_PARAM_KEY = "obdFile";
    private static final String CSR_FILE_PARAM_KEY = "csrFile";
    private static final String CSR_CHECKSUM_PARAM_KEY = "csrChecksum";
    private static final String CSR_COUNT_PARAM_KEY = "csrCount";
    private static final String CDR_FILE_PARAM_KEY = "cdrFile";
    private static final String CDR_CHECKSUM_PARAM_KEY = "cdrChecksum";
    private static final String CDR_COUNT_PARAM_KEY = "cdrCount";
    private static final String SORTED_SUFFIX = ".sorted";
    public static final String CDR_PROCESS_PHASE_2_ERROR = "Phase 2 - Error";
    private static String logTemplate = "Found %d records in table %s";
    private static final String CDR_DETAIL_FILE = "CDR Detail File";
    private static final String LINE_NUMBER_FMT = "Line %d: %s";
    private static final String MAX_ERROR_REACHED_FMT = "The maximum number of allowed errors of %d has been " +
            "reached, ending file verification.";
    private static final String UNABLE_TO_READ_FMT = "Unable to read %s: %s";
    private static final String UNABLE_TO_READ_HEADER_FMT = "Unable to read  header %s: %s";
    private static final int CDR_PROGRESS_REPORT_CHUNK = 1000;


    private static final Logger LOGGER = LoggerFactory.getLogger(CdrFileServiceImpl.class);

    private SettingsFacade settingsFacade;
    private EventRelay eventRelay;
    private FileAuditRecordDataService fileAuditRecordDataService;
    private AlertService alertService;
    private CallDetailRecordDataService callDetailRecordDataService;
    private CallSummaryRecordDataService callSummaryRecordDataService;


    @Autowired
    public CdrFileServiceImpl(@Qualifier("imiSettings") SettingsFacade settingsFacade, EventRelay eventRelay,
                              FileAuditRecordDataService fileAuditRecordDataService, AlertService alertService,
                              CallDetailRecordDataService callDetailRecordDataService,
                              CallSummaryRecordDataService callSummaryRecordDataService) {
        this.settingsFacade = settingsFacade;
        this.eventRelay = eventRelay;
        this.fileAuditRecordDataService = fileAuditRecordDataService;
        this.alertService = alertService;
        this.callDetailRecordDataService = callDetailRecordDataService;
        this.callSummaryRecordDataService = callSummaryRecordDataService;
    }

    private void alertAuditAndThrow(String file, List<String> errors) {
        alertAndAudit(file, errors);

        throw new InvalidCdrFileException(errors);
    }


    private void alertAndAudit(String file, List<String> errors) {
        for (String error : errors) {
            alertAndAudit(file, error);
        }
    }


    private void alertAndAudit(String file, String error) {
        LOGGER.error(error);
        alertService.create(file, CDR_DETAIL_FILE, error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
        fileAuditRecordDataService.create(new FileAuditRecord(FileType.CDR_DETAIL_FILE, file, false, error,
                null, null));
    }


    private void sendNotificationRequest(CdrFileProcessedNotification cfpn) {
        String notificationUrl = settingsFacade.getProperty(CDR_FILE_NOTIFICATION_URL);
        LOGGER.debug("Sending {} to {}", cfpn, notificationUrl);

        ExponentialRetrySender sender = new ExponentialRetrySender(settingsFacade, alertService);

        HttpPost httpPost = new HttpPost(notificationUrl);
        ObjectMapper mapper = new ObjectMapper();

        try {
            String requestJson = mapper.writeValueAsString(cfpn);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setEntity(new StringEntity(requestJson));
        } catch (IOException e) {
            throw new InternalException(String.format("Unable to create cdrFile notification request: %s",
                    e.getMessage()), e);
        }

        sender.sendNotificationRequest(httpPost, HttpStatus.SC_OK, cfpn.getFileName(), "cdrFile Notification Request");
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

            /** Increment the message play duration
             *  The thinking here is that we're trying to capture the longest amount of message that was played
             *  for a call.
             */
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
     * Verifies the checksum & record count provided in fileInfo match the checksum & record count of file
     * also verifies all csv rows are valid.
     *
     * @param fileInfo  file information provided about the file (ie: expected checksum & recordCount)
     *
     * @return          a list of errors (failure) or an empty list (success)
     */
    @Override
    public List<String> verifyChecksumAndCountAndCsv(FileInfo fileInfo, Boolean isDetailFile) {
        File file = new File(localCdrDir(), fileInfo.getCdrFile());
        int maxErrorCount = getMaxErrorCount();
        int errorCount = 0;
        List<String> errors = new ArrayList<>();
        int lineNumber = 1;
        String thisChecksum = "";
        String fileName = file.getName();

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr)) {

            String line;
            try {

                line = reader.readLine();
                if (isDetailFile) {
                    CdrHelper.validateHeader(line);
                } else {
                    CsrHelper.validateHeader(line);
                }

                while ((line = reader.readLine()) != null) {

                    try {
                        // Parse the CSV line into a CDR or CSR (which we actually discard in this phase)
                        // This will trow IllegalArgumentException if the CSV is malformed
                        if (isDetailFile) {
                            CdrHelper.csvLineToCdrDto(line);
                        } else {
                            CsrHelper.csvLineToCsr(line);
                        }

                    } catch (IllegalArgumentException e) {
                        errors.add(String.format(LINE_NUMBER_FMT, lineNumber, e.getMessage()));
                        errorCount++;
                    }
                    if (errorCount >= maxErrorCount) {
                        errors.add(String.format(MAX_ERROR_REACHED_FMT, maxErrorCount));
                        return errors;
                    }

                    lineNumber++;


                }
                reader.close();
                isr.close();
                fis.close();

                thisChecksum = ChecksumHelper.checksum(file);

            } catch (IllegalArgumentException e) {
                String error = String.format("Unable to read header %s: %s", fileName, e.getMessage());
                errors.add(error);
            }

            if (!thisChecksum.equalsIgnoreCase(fileInfo.getChecksum())) {
                String error = String.format("Checksum mismatch, provided checksum: %s, calculated checksum: %s",
                        fileInfo.getChecksum(), thisChecksum);
                errors.add(error);
            }

            if (lineNumber - 1 != fileInfo.getRecordsCount()) {
                String error = String.format("Record count mismatch, provided count: %d, actual count: %d",
                        fileInfo.getRecordsCount(), lineNumber - 1);
                errors.add(error);
            }

        } catch (IOException e) {
                String error = String.format(UNABLE_TO_READ_FMT, fileName, e.getMessage());
                errors.add(error);
            }
        return errors;
    }


    /**
     * Verifies the file is sorted
     *
     * @param file          file to process

     * @return              a list of errors (failure) or an empty list (success)
     */
    private List<String> verifySortOrder(File file) {
        int maxErrorCount = getMaxErrorCount();
        int errorCount = 0;
        List<String> errors = new ArrayList<>();
        Set<String> requestIds = new HashSet<>();
        int lineNumber = 1;
        String fileName = file.getName();

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr)) {

            String line;
            String currentRequestId = "";

            try {

                line = reader.readLine();
                CdrHelper.validateHeader(line);

            } catch (IllegalArgumentException e) {
                String error = String.format(UNABLE_TO_READ_HEADER_FMT, fileName, e.getMessage());
                errors.add(error);
                return errors;
            }

            while ((line = reader.readLine()) != null) {
                try {

                    CallDetailRecordDto cdr = CdrHelper.csvLineToCdrDto(line);

                    if (!currentRequestId.equals(cdr.getRequestId().toString())) {
                        currentRequestId = cdr.getRequestId().toString();

                        // Check for sort order, in reality we don't care about the ascending or descending
                        // thing, what's important is that all the CDRs for one call are grouped.
                        if (requestIds.contains(currentRequestId)) {
                            // An identical requestId was found, that means this file isn't sorted
                            errors.add(String.format("%s is not sorted properly!", fileName));
                            alertAuditAndThrow(fileName, errors);
                        }
                        requestIds.add(currentRequestId);

                        // Start aggregating a new CSR
                    }
                    CallSummaryRecordDto csrDto = new CallSummaryRecordDto();
                    aggregateDetailRecord(cdr, csrDto);


                } catch (IllegalArgumentException e) {
                    errors.add(String.format(LINE_NUMBER_FMT, lineNumber, e.getMessage()));
                    errorCount++;
                }
                if (errorCount >= maxErrorCount) {
                    errors.add(String.format(MAX_ERROR_REACHED_FMT, maxErrorCount));
                    return errors;
                }
                lineNumber++;
            }

        } catch (IOException e) {
            String error = String.format(UNABLE_TO_READ_FMT, fileName, e.getMessage());
            errors.add(error);
        }

        return errors;
    }


    /**
     * Send aggregated detail records for processing as CallSummaryRecordDto in MOTECH events
     * Additionally stores a copy of the provided CDR in the CallDetailRecord table, for reporting
     *
     * @param file      file to process
     * @return          a list of errors (failure) or an empty list (success)
     */
    @Override //NO CHECKSTYLE Cyclomatic Complexity
    public List<String> sendAggregatedRecords(File file) { //NOPMD NcssMethodCount
        int maxErrorCount = getMaxErrorCount();
        int errorCount = 0;
        List<String> errors = new ArrayList<>();
        int lineNumber = 1;
        String fileName = file.getName();
        int record = 0;

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr)) {

            String line;
            String currentRequestId = "";
            CallSummaryRecordDto csr = null;

            try {
                line = reader.readLine();
                CdrHelper.validateHeader(line);
            } catch (IllegalArgumentException e) {
                String error = String.format(UNABLE_TO_READ_HEADER_FMT, fileName, e.getMessage());
                errors.add(error);
                return errors;
            }

            Timer timer = new Timer("record", "records");
            while ((line = reader.readLine()) != null) {
                try {
                    // Parse the CSV line into a CDR
                    // This will trow IllegalArgumentException if the CSV is malformed
                    CallDetailRecordDto cdr = CdrHelper.csvLineToCdrDto(line);

                    if (!currentRequestId.equals(cdr.getRequestId().toString())) {
                        // Send last CSR, if any, for processing
                        if (csr != null) {
                            sendProcessSummaryRecordEvent(csr);
                            record++;
                            if (record % CDR_PROGRESS_REPORT_CHUNK == 0) {
                                LOGGER.debug("Detail Records - aggregated & sent {}", timer.frequency(record));
                            }
                            csr = null; //todo: does that help the GC?
                        }

                        currentRequestId = cdr.getRequestId().toString();

                        // Start aggregating a new CSR
                        csr = new CallSummaryRecordDto();
                    }

                    aggregateDetailRecord(cdr, csr);

                    // Save a copy of the CDR into CallDetailRecord for reporting
                    callDetailRecordDataService.create(CdrHelper.csvLineToCdr(line));

                } catch (IllegalArgumentException e) {
                    String error = String.format(LINE_NUMBER_FMT, lineNumber, e.getMessage());
                    if (LOGGER.isDebugEnabled()) {
                        error += "\n***\n" + line + "\n***";
                    }
                    errors.add(error);
                    errorCount++;
                }
                if (errorCount >= maxErrorCount) {
                    errors.add(String.format(MAX_ERROR_REACHED_FMT, maxErrorCount));
                    return errors;
                }
                if (lineNumber % CDR_PROGRESS_REPORT_CHUNK == 0) {
                    LOGGER.debug("Detail Records - saved {}", timer.frequency(lineNumber));
                }
                lineNumber++;
            }

            sendProcessSummaryRecordEvent(csr);

            LOGGER.info("Detail Records - aggregated & sent {}", timer.frequency(record));
            LOGGER.info("Detail Records - saved {}", timer.frequency(lineNumber));

        } catch (IOException e) {
            String error = String.format(UNABLE_TO_READ_FMT, fileName, e.getMessage());
            LOGGER.warn(error);
            errors.add(error);
        }

        // return errors from processing
        return errors;
    }


    /**
     * Send summary records for processing as CallSummaryRecordDto in MOTECH events
     * Additionally stores a copy of the provided CSR in the CallSummaryRecord table, for reporting
     *
     * @param file      file to process
     * @return          a list of errors (failure) or an empty list (success)
     */
    @Override //NO CHECKSTYLE Cyclomatic Complexity
    public List<String> sendSummaryRecords(File file) {
        int maxErrorCount = getMaxErrorCount();
        int errorCount = 0;
        List<String> errors = new ArrayList<>();
        int lineNumber = 1;
        int record = 0;
        String fileName = file.getName();

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr)) {

            try {
                String line = reader.readLine();
                CsrHelper.validateHeader(line);

                Timer timer = new Timer("record", "records");
                while ((line = reader.readLine()) != null) {
                    try {

                        CallSummaryRecord csr = CsrHelper.csvLineToCsr(line);
                        callSummaryRecordDataService.create(csr);

                        // We only want to send summary records which contain information not in detail records
                        if (StatusCode.summaryOnlyFailure(csr.getStatusCode())) {
                            CallSummaryRecordDto dto = csr.toDto();
                            // Mark the CSR as FAILED (even thought it might have been REJECTED) so it's always retried
                            dto.setFinalStatus(FinalCallStatus.FAILED);
                            sendProcessSummaryRecordEvent(dto);
                            record++;
                            if (record % CDR_PROGRESS_REPORT_CHUNK == 0) {
                                LOGGER.debug("Summary Records - sent {}", timer.frequency(record));
                            }
                        }

                    } catch (IllegalArgumentException e) {
                        errors.add(String.format(LINE_NUMBER_FMT, lineNumber, e.getMessage()));
                        errorCount++;
                    }
                    if (errorCount >= maxErrorCount) {
                        errors.add(String.format(MAX_ERROR_REACHED_FMT, maxErrorCount));
                        return errors;
                    }

                    if (lineNumber % CDR_PROGRESS_REPORT_CHUNK == 0) {
                        LOGGER.debug("Summary Records - saved {}", timer.frequency(lineNumber));
                    }

                    lineNumber++;

                }

                LOGGER.info("Summary Records - sent {}", timer.frequency(record));
                LOGGER.info("Summary Records - saved {}", timer.frequency(lineNumber));


            } catch (IllegalArgumentException e) {
                String error = String.format(UNABLE_TO_READ_HEADER_FMT, fileName, e.getMessage());
                errors.add(error);
            }

        } catch (IOException e) {
            String error = String.format(UNABLE_TO_READ_FMT, fileName, e.getMessage());
            errors.add(error);
        }

        return errors;
    }


    private CdrFileNotificationRequest requestFromParams(Map<String, Object> params) {
        return new CdrFileNotificationRequest(
            (String) params.get(OBD_FILE_PARAM_KEY),
            new FileInfo(
                    (String) params.get(CSR_FILE_PARAM_KEY),
                    (String) params.get(CSR_CHECKSUM_PARAM_KEY),
                    (int) params.get(CSR_COUNT_PARAM_KEY)
            ),
        new FileInfo(
                (String) params.get(CDR_FILE_PARAM_KEY),
                (String) params.get(CDR_CHECKSUM_PARAM_KEY),
                (int) params.get(CDR_COUNT_PARAM_KEY)
            )
        );
    }


    /**
     * Send a simple END_OF_CDR_PROCESSING_SUBJECT message to aid in debugging & diagnosing - no functional impact
     *
     */
    private void sendEndCdrProcessingEvent(CdrFileNotificationRequest request) {
        MotechEvent motechEvent = new MotechEvent(END_OF_CDR_PROCESSING_SUBJECT, paramsFromRequest(request));
        eventRelay.sendEventMessage(motechEvent);
    }


    private  Map<String, Object> paramsFromRequest(CdrFileNotificationRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put(OBD_FILE_PARAM_KEY, request.getFileName());
        params.put(CSR_FILE_PARAM_KEY, request.getCdrSummary().getCdrFile());
        params.put(CSR_CHECKSUM_PARAM_KEY, request.getCdrSummary().getChecksum());
        params.put(CSR_COUNT_PARAM_KEY, request.getCdrSummary().getRecordsCount());
        params.put(CDR_FILE_PARAM_KEY, request.getCdrDetail().getCdrFile());
        params.put(CDR_CHECKSUM_PARAM_KEY, request.getCdrDetail().getChecksum());
        params.put(CDR_COUNT_PARAM_KEY, request.getCdrDetail().getRecordsCount());
        return params;
    }


    private void sendProcessFilesEvent(CdrFileNotificationRequest request) {
        MotechEvent motechEvent = new MotechEvent(PROCESS_FILES_SUBJECT, paramsFromRequest(request));
        eventRelay.sendEventMessage(motechEvent);
    }


    private File localCdrDir() {
        return new File(settingsFacade.getProperty(LOCAL_CDR_DIR));
    }


    // Phase 1: verify the file exists, the csv is valid and its record count and checksum match the provided info
    //          while collecting a list of errors on the go.
    //          Does not proceed to phase 2 if any error occurred and returns an error
    @Override
    public void verifyDetailFileChecksumAndCount(CdrFileNotificationRequest request) {

        LOGGER.info("Phase 1 - Start");

        List<String> cdrErrors = verifyChecksumAndCountAndCsv(request.getCdrDetail(), true);
        alertAndAudit(request.getCdrDetail().getCdrFile(), cdrErrors);


        List<String> csrErrors = verifyChecksumAndCountAndCsv(request.getCdrSummary(), false);
        alertAndAudit(request.getCdrSummary().getCdrFile(), csrErrors);

        List<String> errors = new ArrayList<>(cdrErrors);
        errors.addAll(csrErrors);
        if (errors.size() > 0) {

            throw new InvalidCdrFileException(errors);
        }

        // Send a MOTECH event to continue to phase 2 (without timing out the POST from IMI)
        sendProcessFilesEvent(request);

        LOGGER.info("Phase 1 - Success");
    }


    private List<String> processDetailFile(CdrFileNotificationRequest request) {

        File cdrFile = new File(localCdrDir(), request.getCdrDetail().getCdrFile());
        String cdrFileName = cdrFile.getName();

        // Copy detail file from IMI network share (imi.remote_cdr_dir) into local cdr dir (imi.local_cdr_dir)
        // Another MOTECH node than the one which picked up the HTTP cdrFileNotification might be picking up this
        // event, so make sure to copy the file locally, if it's not there already.

        if (cdrFile.exists()) {
            LOGGER.debug("{} already exists on this MOTECH node, no need to copy it from IMI.", cdrFile.getName());
        } else {
            LOGGER.debug("{} doesn't exists on this MOTECH node, copying it from IMI...", cdrFile.getName());
            ScpHelper scpHelper = new ScpHelper(settingsFacade);
            try {
                scpHelper.scpCdrFromRemote(cdrFileName);
            } catch (ExecException e) {
                List<String> errors = new ArrayList<>();
                errors.add(String.format("Error copying CDR file %s: %s", cdrFileName, e.getMessage()));
                return errors;
            }
        }

        // Sort the file (on requestId & attemptNo) into a new file with the same name plus the ".sorted" suffix
        SortHelper sortHelper = new SortHelper(settingsFacade);
        try {
            sortHelper.sort(cdrFileName);
        } catch (ExecException e) {
            List<String> errors = new ArrayList<>();
            errors.add(String.format("Error sorting CDR file %s: %s", cdrFileName, e.getMessage()));
            return errors;
        }

        // We want to be dealing with the sorted file from now onwards
        cdrFile = new File(localCdrDir(), request.getCdrDetail().getCdrFile() + SORTED_SUFFIX);

        // NOTE: once sorted the checksums won't match anymore.

        // Second verification pass (more in detail) verify all entities & sort order
        return verifySortOrder(cdrFile);
    }


    private List<String> processSummaryFile(CdrFileNotificationRequest request) {

        List<String> errors = new ArrayList<>();
        File csrFile = new File(localCdrDir(), request.getCdrSummary().getCdrFile());
        String csrFileName = csrFile.getName();

        // Copy summary file from IMI network share (imi.remote_cdr_dir) into local cdr dir (imi.local_cdr_dir)

        if (csrFile.exists()) {
            LOGGER.debug("{} already exists on this MOTECH node, no need to copy it from IMI.", csrFile.getName());
        } else {
            LOGGER.debug("{} doesn't exists on this MOTECH node, copying it from IMI...", csrFile.getName());
            ScpHelper scpHelper = new ScpHelper(settingsFacade);
            try {
                scpHelper.scpCdrFromRemote(csrFileName);
            } catch (ExecException e) {
                errors.add(String.format("Error copying CSR file %s: %s", csrFileName, e.getMessage()));
                return errors;
            }
        }

        return errors;
    }


    // Phase 2: Aggregate detail records into summary records
    //          Validate all CallSummaryRecord fields exist in the database
    //          Validate CallSummaryFile
    //          Stop processing if errors occurred
    @Override
    @MotechListener(subjects = { PROCESS_FILES_SUBJECT })
    public List<String> processDetailFile(MotechEvent event) {
        LOGGER.info("Phase 2 - Start");

        CdrFileNotificationRequest request = requestFromParams(event.getParameters());

        LOGGER.debug("Phase 2 - cdrFileNotificationRequest: {}", request);

        //
        // Detail File
        //
        LOGGER.info("Phase 2 - processDetailFile");
        List<String> errors = processDetailFile(request);
        if (errors.size() > 0) {
            reportAuditAndPost(request.getFileName(), errors);
            LOGGER.debug(CDR_PROCESS_PHASE_2_ERROR);
            return errors;
        }

        //
        // Summary File
        //
        LOGGER.info("Phase 2 - processSummaryFile");
        errors = processSummaryFile(request);
        if (errors.size() > 0) {
            reportAuditAndPost(request.getFileName(), errors);
            LOGGER.debug(CDR_PROCESS_PHASE_2_ERROR);
            return errors;
        }

        //
        // We processed as much as we can, let IMI know before distributing the work to phase 3
        //
        LOGGER.info("Phase 2 - sendNotificationRequest");
        sendNotificationRequest(new CdrFileProcessedNotification(
                FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY.getValue(),
                request.getFileName(),
                null
        ));


        //
        // Clean up old CDRs/CSRs before distributing work out
        //
        LOGGER.info("Phase 2 - cleanOldCallRecords");
        cleanOldCallRecords();


        LOGGER.info("Phase 2 - End");


        LOGGER.info("Phase 3 - Start");

        // Phase 3: distribute the processing of aggregated CDRs to all nodes.
        //          copy CDRs from IMI to the database
        //          each node may generate an individual error that NMS-OPS will have to respond to.
        File cdrFile = new File(localCdrDir(), request.getCdrDetail().getCdrFile() + SORTED_SUFFIX);
        LOGGER.info("Phase 3 - sendAggregatedRecords - distributing aggregate CDRs");
        errors = sendAggregatedRecords(cdrFile);
        if (errors.size() > 0) {
            reportAuditAndPost(request.getFileName() + SORTED_SUFFIX, errors);
            LOGGER.debug(CDR_PROCESS_PHASE_2_ERROR);
            return errors;
        }

        LOGGER.info("Phase 3 - done distributing aggregate CDRs");



        LOGGER.info("Phase 4 - Start");

        // Phase 4: distribute the processing of CSRs to all nodes, make backup copy of all received CSR
        File csrFile = new File(localCdrDir(), request.getCdrSummary().getCdrFile());
        LOGGER.info("Phase 4 - sendSummaryRecords - distributing CSRs");
        errors = sendSummaryRecords(csrFile);
        if (errors.size() > 0) {
            reportAuditAndPost(request.getFileName(), errors);
            LOGGER.debug(CDR_PROCESS_PHASE_2_ERROR);
            return errors;
        }

        LOGGER.info("Phase 4 - done distributing CSRs");


        sendEndCdrProcessingEvent(request);

        return errors;
    }

    @Override
    @MotechListener(subjects = { CDR_CSR_CLEANUP_SUBJECT })
    public void cleanOldCallRecords() {
        LOGGER.info("cleanOldCallRecords() called");
        int cdrDuration = MIN_CALL_DATA_RETENTION_DURATION_IN_DAYS;

        try {
            int durationFromConfig = Integer.parseInt(settingsFacade.getProperty(CDR_CSR_RETENTION_DURATION));
            if (durationFromConfig < cdrDuration) {
                LOGGER.debug(String.format("Discarding retention property from config since it is less than MIN: %d", cdrDuration));
            } else {
                cdrDuration = durationFromConfig;
                LOGGER.debug(String.format("Using retention property from config: %d", cdrDuration));
            }
        } catch (NumberFormatException ne) {
            LOGGER.debug(String.format("Unable to get property from config: %s", CDR_CSR_RETENTION_DURATION));
        }

        LOGGER.debug(String.format(logTemplate, callDetailRecordDataService.count(), CDR_TABLE_NAME));
        LOGGER.debug(String.format(logTemplate, callSummaryRecordDataService.count(), CSR_TABLE_NAME));

        deleteRecords(cdrDuration, CDR_TABLE_NAME);
        deleteRecords(cdrDuration, CSR_TABLE_NAME);

        LOGGER.debug(String.format(logTemplate, callDetailRecordDataService.count(), CDR_TABLE_NAME));
        LOGGER.debug(String.format(logTemplate, callSummaryRecordDataService.count(), CSR_TABLE_NAME));
    }

    /**
     * Helper to clean out the CDR table with the given retention policy
     * @param retentionInDays min days to keep CDR for
     * @param tableName name of the cdr or csr table
     */
    private void deleteRecords(final int retentionInDays, final String tableName) {
        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = String.format(
                        "DELETE FROM %s where creationDate < now() - INTERVAL %d DAY", tableName, retentionInDays);
                LOGGER.debug("SQL QUERY: {}", query);
                return query;
            }

            @Override
            public Long execute(Query query) {

                return (Long) query.execute();
            }
        };

        // FYI: doesn't matter what data service we use since it is just used as a vehicle to execute the custom query
        Long rowCount = callDetailRecordDataService.executeSQLQuery(queryExecution);
        LOGGER.debug(String.format("Table %s cleaned up and deleted %d rows", tableName, rowCount));

        // evict caches for the changes to be read again
        if (tableName.equalsIgnoreCase(CDR_TABLE_NAME)) {
            callDetailRecordDataService.evictEntityCache(false);
        }
        if (tableName.equalsIgnoreCase(CSR_TABLE_NAME)) {
            callSummaryRecordDataService.evictEntityCache(false);
        }
    }


    @MotechListener(subjects = { END_OF_CDR_PROCESSING_SUBJECT })
    public void processEndOfCdrProcessing(MotechEvent event) {
        LOGGER.info("End of CDR processing");

        CdrFileNotificationRequest request = requestFromParams(event.getParameters());

        LOGGER.debug("cdrFileNotificationRequest: {}", request);
    }
}
