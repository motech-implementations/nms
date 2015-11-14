package org.motechproject.nms.imi.service.impl;

import org.apache.commons.collections.ListUtils;
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
import org.motechproject.nms.imi.exception.InvalidCsrException;
import org.motechproject.nms.imi.repository.CallDetailRecordDataService;
import org.motechproject.nms.imi.repository.CallSummaryRecordDataService;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.contract.CdrFileProcessedNotification;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.nms.kilkari.dto.CallDetailRecordDto;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.service.CsrService;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Implementation of the {@link CdrFileService} interface.
 */
@Service("cdrFileService")
public class CdrFileServiceImpl implements CdrFileService {

    private static final String CDR_FILE_NOTIFICATION_URL = "imi.cdr_file_notification_url";
    private static final String LOCAL_CDR_DIR = "imi.local_cdr_dir";
    private static final String CDR_CSR_RETENTION_DURATION = "imi.cdr_csr.retention.duration";
    private static final int MIN_CALL_DATA_RETENTION_DURATION_IN_DAYS = 5;
    private static final String CDR_CSR_CLEANUP_SUBJECT = "nms.imi.cdr_csr.cleanup";
    private static final String CDR_TABLE_NAME = "motech_data_services.nms_imi_cdrs";
    private static final String CSR_TABLE_NAME = "motech_data_services.nms_imi_csrs";
    private static final String KK_CSR_TABLE_NAME = "motech_data_services.nms_kk_summary_records";
    private static final String PROCESS_SUMMARY_RECORD_SUBJECT = "nms.imi.kk.process_summary_record";
    private static final String END_OF_CDR_PROCESSING_SUBJECT = "nms.imi.kk.end_of_cdr_processing";
    private static final String CSR_PARAM_KEY = "csr";
    private static final String CDR_PHASE_2 = "nms.imi.kk.cdr_phase_2";
    private static final String CDR_PHASE_3 = "nms.imi.kk.cdr_phase_3";
    private static final String CDR_PHASE_4 = "nms.imi.kk.cdr_phase_4";
    private static final String CDR_PHASE_5 = "nms.imi.kk.cdr_phase_5";
    private static final String OBD_FILE_PARAM_KEY = "obdFile";
    private static final String CSR_FILE_PARAM_KEY = "csrFile";
    private static final String CSR_CHECKSUM_PARAM_KEY = "csrChecksum";
    private static final String CSR_COUNT_PARAM_KEY = "csrCount";
    private static final String CDR_FILE_PARAM_KEY = "cdrFile";
    private static final String CDR_CHECKSUM_PARAM_KEY = "cdrChecksum";
    private static final String CDR_COUNT_PARAM_KEY = "cdrCount";
    private static final String SORTED_SUFFIX = ".sorted";
    private static final String LOG_TEMPLATE = "Found %d records in table %s";
    private static final String CDR_DETAIL_FILE = "CDR Detail File";
    private static final String LINE_NUMBER_FMT = "Line %d: %s";
    private static final String UNABLE_TO_READ_FMT = "Unable to read %s: %s";
    private static final String UNABLE_TO_READ_HEADER_FMT = "Unable to read  header %s: %s";
    private static final int CDR_PROGRESS_REPORT_CHUNK = 1000;


    private static final Logger LOGGER = LoggerFactory.getLogger(CdrFileServiceImpl.class);
    public static final String INVALID_CDR_HEADER_P4 = "The CDR header should be valid in  Phase 4, this is an internal MOTECH error and must be investigated - ";
    public static final String INVALID_CDR_P4 = "The CDR should be readable & valid in Phase 4, this is an internal MOTECH error and must be investigated - ";
    public static final String INVALID_CSR_HEADER_P5 = "The CSR header should be valid in  Phase 5, this is an internal MOTECH error and must be investigated - ";
    public static final String INVALID_CSR_P5 = "The CSR should be readable & valid in Phase 5, this is an internal MOTECH error and must be investigated - ";
    public static final String INVALID_CDR_HEADER_P2 = "The CDR header should be valid in  Phase 2, this is an internal MOTECH error and must be investigated - ";
    public static final String INVALID_CDR_P2 = "The CDR should be readable & valid in Phase 2, this is an internal MOTECH error and must be investigated - ";
    public static final String INVALID_CSR_HEADER_P2 = "The CSR header should be valid in  Phase 2, this is an internal MOTECH error and must be investigated - ";
    public static final String INVALID_CSR_P2 = "The CSR should be readable & valid in Phase 2, this is an internal MOTECH error and must be investigated - ";
    public static final String COPY_ERROR = "Copy Error";

    private SettingsFacade settingsFacade;
    private EventRelay eventRelay;
    private FileAuditRecordDataService fileAuditRecordDataService;
    private AlertService alertService;
    private CallDetailRecordDataService callDetailRecordDataService;
    private CallSummaryRecordDataService callSummaryRecordDataService;
    private CsrService csrService;


    @Autowired
    public CdrFileServiceImpl(@Qualifier("imiSettings") SettingsFacade settingsFacade, EventRelay eventRelay,
                              FileAuditRecordDataService fileAuditRecordDataService, AlertService alertService,
                              CallDetailRecordDataService callDetailRecordDataService,
                              CallSummaryRecordDataService callSummaryRecordDataService, CsrService csrService) {
        this.settingsFacade = settingsFacade;
        this.eventRelay = eventRelay;
        this.fileAuditRecordDataService = fileAuditRecordDataService;
        this.alertService = alertService;
        this.callDetailRecordDataService = callDetailRecordDataService;
        this.callSummaryRecordDataService = callSummaryRecordDataService;
        this.csrService = csrService;
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
                            CdrHelper.validateCsv(line);
                        } else {
                            CsrHelper.validateCsv(line);
                        }

                    } catch (IllegalArgumentException e) {
                        errors.add(String.format(LINE_NUMBER_FMT, lineNumber, e.getMessage()));
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
     * Send aggregated detail records for processing as CallSummaryRecordDto in MOTECH events
     * Additionally stores a copy of the provided CDR in the CallDetailRecord table, for reporting
     *
     * @param file      file to process
     * @return          a list of errors (failure) or an empty list (success)
     */
    @Override //NO CHECKSTYLE Cyclomatic Complexity
    public void sendAggregatedRecords(File file) { //NOPMD NcssMethodCount
        int lineNumber = 1;
        String fileName = file.getName();
        int aggregated = 0;

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
                String error = INVALID_CDR_HEADER_P4 + String.format(UNABLE_TO_READ_HEADER_FMT, fileName, e.getMessage());
                LOGGER.error(error);
                alertService.create(fileName, "Invalid CDR Header in Phase 4", error, AlertType.CRITICAL,
                        AlertStatus.NEW, 0, null);
                return;
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
                            aggregated++;
                            if (aggregated % CDR_PROGRESS_REPORT_CHUNK == 0) {
                                LOGGER.debug("CDRs, aggregated & sent {}", timer.frequency(aggregated));
                            }
                            csr = null; //todo: does that help the GC?
                        }

                        currentRequestId = cdr.getRequestId().toString();

                        // Start aggregating a new CSR
                        csr = new CallSummaryRecordDto();
                    }

                    aggregateDetailRecord(cdr, csr);

                    // Save a copy of the CDR into CallDetailRecord for reporting
                    //todo upsert!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    //todo upsert!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    //todo upsert!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    callDetailRecordDataService.create(CdrHelper.csvLineToCdr(line));

                } catch (IllegalArgumentException | InvalidCsrException e) {
                    //errors here should have been reported in Phase 2, let's just ignore them
                    //todo remove following line to not over confuse ops
                    LOGGER.debug("Ignoring CDR error line {}", lineNumber);
                }
                if (lineNumber % CDR_PROGRESS_REPORT_CHUNK == 0) {
                    LOGGER.debug("CDRs - saved {}", timer.frequency(lineNumber));
                }
                lineNumber++;
            }

            sendProcessSummaryRecordEvent(csr);

            LOGGER.info("Detail Records - aggregated & sent {}", timer.frequency(aggregated));
            LOGGER.info("Detail Records - saved {}", timer.frequency(lineNumber));

        } catch (IOException e) {
            String error = INVALID_CDR_P4 + String.format(UNABLE_TO_READ_FMT, fileName, e.getMessage());
            LOGGER.error(error);
            alertService.create(fileName, "Invalid CDR in Phase 4", error, AlertType.CRITICAL, AlertStatus.NEW, 0,
                    null);
        }
    }


    /**
     * Send summary records for processing as CallSummaryRecordDto in MOTECH events
     * Additionally stores a copy of the provided CSR in the CallSummaryRecord table, for reporting
     *
     * @param file      file to process
     * @return          a list of errors (failure) or an empty list (success)
     */
    @Override //NO CHECKSTYLE Cyclomatic Complexity
    public void sendSummaryRecords(File file) {
        int lineNumber = 1;
        String fileName = file.getName();

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr)) {

            String line;
            try {
                line = reader.readLine();
                CsrHelper.validateHeader(line);
            } catch (IllegalArgumentException e) {
                String error = INVALID_CSR_HEADER_P5 + String.format(UNABLE_TO_READ_HEADER_FMT,
                        fileName, e.getMessage());
                LOGGER.error(error);
                alertService.create(fileName, "Invalid CSR Header in Phase 5", error, AlertType.CRITICAL,
                        AlertStatus.NEW, 0, null);
                return;
            }

            Timer timer = new Timer("record", "records");
            while ((line = reader.readLine()) != null) {
                try {

                    CallSummaryRecord csr = CsrHelper.csvLineToCsr(line);
                    //todo upsert!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    //todo upsert!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    //todo upsert!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    callSummaryRecordDataService.create(csr);

                    // We only want to send summary records which contain information not in detail records
                    if (StatusCode.summaryOnlyFailure(csr.getStatusCode())) {
                        CallSummaryRecordDto dto = csr.toDto();
                        // Mark the CSR as FAILED (even thought it might have been REJECTED) so it's always retried
                        dto.setFinalStatus(FinalCallStatus.FAILED);
                        sendProcessSummaryRecordEvent(dto);
                    }

                } catch (IllegalArgumentException | InvalidCsrException e) {
                    // All errors here should have been reported in Phase 2, let's just ignore them
                    //todo remove following line to not over confuse ops
                    LOGGER.debug("Ignoring CSR error line {}", lineNumber);
                }

                if (lineNumber % CDR_PROGRESS_REPORT_CHUNK == 0) {
                    LOGGER.debug("CSRs - saved {}", timer.frequency(lineNumber));
                }

                lineNumber++;

            }

            LOGGER.info("CSRs - sent {}", timer.frequency(lineNumber));

        } catch (IOException e) {
            String error = INVALID_CSR_P5 + String.format(UNABLE_TO_READ_FMT, fileName, e.getMessage());
            LOGGER.error(error);
            alertService.create(fileName, "Invalid CSR in Phase 5", error, AlertType.CRITICAL, AlertStatus.NEW, 0,
                    null);
        }
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


    private void sendPhaseEvent(String subject, CdrFileNotificationRequest request) {
        MotechEvent motechEvent = new MotechEvent(subject, paramsFromRequest(request));
        eventRelay.sendEventMessage(motechEvent);
    }


    private File localCdrDir() {
        return new File(settingsFacade.getProperty(LOCAL_CDR_DIR));
    }


    // Phase 1: verify the file exists, the csv is valid and its record count and checksum match the provided info
    //          while collecting a list of errors on the go.
    //          Does not proceed to phase 2 if any error occurred and returns an error
    @Override
    public void cdrProcessingPhase1(CdrFileNotificationRequest request) {

        LOGGER.info("CDR Processing - Phase 1 - Start");

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
        LOGGER.info("Phase 1 - Sending Phase 2 event");
        sendPhaseEvent(CDR_PHASE_2, request);

        LOGGER.info("Phase 1 - Success");
    }


    private void copyFileIfNeeded(File file) throws ExecException {
        if (file.exists()) {
            LOGGER.debug("{} already exists on this MOTECH node, no need to copy it from IMI.", file.getName());
            return;
        }
        LOGGER.debug("{} doesn't exists on this MOTECH node, copying it from IMI...", file.getName());
        ScpHelper scpHelper = new ScpHelper(settingsFacade);
        scpHelper.scpCdrFromRemote(file.getName());
    }



    private List<String> verifyDetailFile(CdrFileNotificationRequest request) {
        List<String> errors = new ArrayList<>();
        File file = new File(localCdrDir(), request.getCdrDetail().getCdrFile());
        String fileName = file.getName();
        int lineNumber = 1;

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr)) {

            String line;

            try {
                line = reader.readLine();
                CdrHelper.validateHeader(line);
            } catch (IllegalArgumentException e) {
                String error = INVALID_CDR_HEADER_P2 + String.format(UNABLE_TO_READ_HEADER_FMT, fileName, e.getMessage());
                errors.add(error);
                LOGGER.error(error);
                alertService.create(fileName, "Invalid CDR Header in Phase 2", error, AlertType.CRITICAL,
                        AlertStatus.NEW, 0, null);
                return errors;
            }

            while ((line = reader.readLine()) != null) {
                try {

                    CdrHelper.csvLineToCdrDto(line);

                } catch (InvalidCsrException e) {
                    LOGGER.debug(String.format("%s(%d): %s\n\n%s\n", fileName, lineNumber, e.getMessage(), line));
                    errors.add(String.format(LINE_NUMBER_FMT, lineNumber, e.getMessage()));
                }
                lineNumber++;
            }

        } catch (IOException e) {
            String error = INVALID_CDR_P2 + String.format(UNABLE_TO_READ_FMT, fileName, e.getMessage());
            errors.add(error);
            LOGGER.error(error);
            alertService.create(fileName, "Invalid CDR in Phase 2", error, AlertType.CRITICAL, AlertStatus.NEW, 0,
                    null);
            return errors;
        }

        return errors;

    }


    private List<String> verifySummaryFile(CdrFileNotificationRequest request) {
        List<String> errors = new ArrayList<>();
        File file = new File(localCdrDir(), request.getCdrSummary().getCdrFile());
        String fileName = file.getName();
        int lineNumber = 1;

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr)) {

            String line;

            try {
                line = reader.readLine();
                CsrHelper.validateHeader(line);
            } catch (IllegalArgumentException e) {
                String error = INVALID_CSR_HEADER_P2 + String.format(UNABLE_TO_READ_HEADER_FMT,
                        fileName, e.getMessage());
                errors.add(error);
                LOGGER.error(error);
                alertService.create(fileName, "Invalid CSR Header in Phase 2", error, AlertType.CRITICAL,
                        AlertStatus.NEW, 0, null);
                return errors;
            }

            while ((line = reader.readLine()) != null) {
                try {

                    CsrHelper.csvLineToCsr(line);

                } catch (InvalidCsrException e) {
                    LOGGER.debug(String.format("%s(%d): %s\n\n%s\n", fileName, lineNumber, e.getMessage(), line));
                    errors.add(String.format(LINE_NUMBER_FMT, lineNumber, e.getMessage()));
                }
                lineNumber++;
            }

        } catch (IOException e) {
            String error = INVALID_CSR_P2 + String.format(UNABLE_TO_READ_FMT, fileName, e.getMessage());
            errors.add(error);
            LOGGER.error(error);
            alertService.create(fileName, "Invalid File in Phase 2", error, AlertType.CRITICAL, AlertStatus.NEW, 0,
                    null);
            return errors;
        }

        return errors;
    }


    // Phase 2: Verify files a little mode deeply than t=in phase 1 (ie: check csv field values are valid)
    //          Send list on invalid CDR/CSR rows in CdrFileProcessedNotification HTTP POST back to IMI and dispatches
    //          messages for Phase 3 & 4 processing to any node.
    @Override
    @MotechListener(subjects = { CDR_PHASE_2 })
    public List<String> cdrProcessPhase2(MotechEvent event) { //NOPMD NcssMethodCount

        LOGGER.info("Phase 2 - Start");

        CdrFileNotificationRequest request = requestFromParams(event.getParameters());

        LOGGER.debug("Phase 2 - cdrFileNotificationRequest: {}", request);

        //
        // Detail File
        //
        LOGGER.info("Phase 2 - copy detail File");
        File cdrFile = new File(localCdrDir(), request.getCdrDetail().getCdrFile());
        try {
            copyFileIfNeeded(cdrFile);
        } catch (ExecException e) {
            String error = String.format("Error copying CDR file %s: %s", cdrFile.getName(), e.getMessage());
            LOGGER.error(error);
            alertService.create(cdrFile.getName(), COPY_ERROR, error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            return Arrays.asList(error);
        }
        LOGGER.info("Phase 2 - verify detail File");
        List<String> detailErrors = verifyDetailFile(request);

        //
        // Summary File
        //
        LOGGER.info("Phase 2 - copy detail File");
        File csrFile = new File(localCdrDir(), request.getCdrSummary().getCdrFile());
        try {
            copyFileIfNeeded(csrFile);
        } catch (ExecException e) {
            String error = String.format("Error copying CSR file %s: %s", csrFile.getName(), e.getMessage());
            LOGGER.error(error);
            alertService.create(csrFile.getName(), COPY_ERROR, error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            // This is a monster error, let's not even bother talking about the potential errors in detailErrors
            return Arrays.asList(error);
        }
        LOGGER.info("Phase 2 - verifySummaryFile");
        List<String> summaryErrors = verifySummaryFile(request);


        List<String> allErrors = ListUtils.union(detailErrors, summaryErrors);
        FileProcessedStatus status = FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY;
        String failure = null;
        if (allErrors.size() > 0) {

            LOGGER.debug("Phase 2 - Error");

            //todo: what's the maximum number of errors I rcan report back in a POST?
            status = FileProcessedStatus.FILE_ERROR_IN_FILE_FORMAT;
            failure = StringUtils.join(allErrors, ",");

            for (String error : detailErrors) {
                LOGGER.error(error);
                alertService.create(request.getCdrDetail().getCdrFile(), "Invalid CDR", error, AlertType.MEDIUM,
                        AlertStatus.NEW, 0, null);
            }

            for (String error : summaryErrors) {
                LOGGER.error(error);
                alertService.create(request.getCdrSummary().getCdrFile(), "Invalid CSR", error, AlertType.MEDIUM,
                        AlertStatus.NEW, 0, null);
            }

            if (detailErrors.size() > 0) {
                fileAuditRecordDataService.create(new FileAuditRecord(FileType.CDR_DETAIL_FILE,
                        request.getCdrDetail().getCdrFile(), false,
                        String.format("%d invalid CDR rows, see tomcat log", detailErrors.size()), null, null));
            }

            if (summaryErrors.size() > 0) {
                fileAuditRecordDataService.create(new FileAuditRecord(FileType.CDR_SUMMARY_FILE,
                        request.getCdrSummary().getCdrFile(), false,
                        String.format("%d invalid CSR rows, see tomcat log", detailErrors.size()), null, null));
            }
        }

        LOGGER.info("Phase 2 - sendNotificationRequest");
        sendNotificationRequest(new CdrFileProcessedNotification(status.getValue(), request.getFileName(), failure));


        //
        // Distribute Phase 3 & 4 & 5
        //
        LOGGER.info("Phase 2 - Sending Phase 3 event");
        sendPhaseEvent(CDR_PHASE_3, request);
        LOGGER.info("Phase 2 - Sending Phase 4 event");
        sendPhaseEvent(CDR_PHASE_4, request);
        LOGGER.info("Phase 2 - Sending Phase 5 event");
        sendPhaseEvent(CDR_PHASE_5, request);

        LOGGER.info("Phase 2 - End");

        return allErrors;
    }


    // Phase 3:  Deletes old IMI CSR & IMI CDR & KK CSR
    @MotechListener(subjects = { CDR_PHASE_3 })
    public void cdrProcessPhase3(MotechEvent event) {

        Timer timer = new Timer();
        LOGGER.info("Phase 3 - Start");
        LOGGER.info("Phase 3 - cleanOldCallRecords");
        cleanOldCallRecords();
        LOGGER.info("Phase 3 - End {}", timer.time());

    }


    // Phase 4:  Aggregates & sends aggregated CDR rows into CSR for processing (by kilkari module) on any node
    @MotechListener(subjects = { CDR_PHASE_4 })
    public void cdrProcessPhase4(MotechEvent event) {

        Timer timer = new Timer();
        LOGGER.info("Phase 4 - Start");

        CdrFileNotificationRequest request = requestFromParams(event.getParameters());

        // Copy detail file, if needed
        LOGGER.info("Phase 4 - copying CDR");
        File cdrFile = new File(localCdrDir(), request.getCdrDetail().getCdrFile());
        String cdrFileName = cdrFile.getName();
        try {
            copyFileIfNeeded(cdrFile);
        } catch (ExecException e) {
            String error = String.format("Error copying CDR file %s: %s", cdrFileName, e.getMessage());
            LOGGER.error(error);
            alertService.create(cdrFileName, COPY_ERROR, error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            return;
        }

        // Sort the file (on requestId & attemptNo) into a new file with the same name plus the ".sorted" suffix
        LOGGER.info("Phase 4 - sorting CDR");
        SortHelper sortHelper = new SortHelper(settingsFacade);
        try {
            sortHelper.sort(cdrFileName);
        } catch (ExecException e) {
            String error = String.format("Error sorting CDR file %s: %s", cdrFileName, e.getMessage());
            LOGGER.error(error);
            alertService.create(cdrFileName, "Sort Error", error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            return;
        }

        // We want to be dealing with the sorted file from now onwards
        cdrFile = new File(localCdrDir(), request.getCdrDetail().getCdrFile() + SORTED_SUFFIX);

        LOGGER.info("Phase 4 - sendAggregatedRecords");
        sendAggregatedRecords(cdrFile);

        LOGGER.info("Phase 4 - End {}", timer.time());

    }


    // Phase 5: Sends CSR rows from IMI (only ones not covered by CDR rows) for processing on any node
    @MotechListener(subjects = { CDR_PHASE_5 })
    public void cdrProcessPhase5(MotechEvent event) {

        Timer timer = new Timer();
        LOGGER.info("Phase 5 - Start");

        CdrFileNotificationRequest request = requestFromParams(event.getParameters());

        File csrFile = new File(localCdrDir(), request.getCdrSummary().getCdrFile());
        String csrFileName = csrFile.getName();

        // Copy summary file, if needed
        LOGGER.info("Phase 5 - copying CSR");
        try {
            copyFileIfNeeded(csrFile);
        } catch (ExecException e) {
            String error = String.format("Error copying CDR file %s: %s", csrFileName, e.getMessage());
            LOGGER.error(error);
            alertService.create(csrFileName, COPY_ERROR, error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            return;
        }

        LOGGER.info("Phase 5 - sendSummaryRecords");
        sendSummaryRecords(csrFile);

        LOGGER.info("Phase 5 - End {}", timer.time());
    }


    @Override
    @MotechListener(subjects = { CDR_CSR_CLEANUP_SUBJECT })
    public void cleanOldCallRecords() {
        LOGGER.info("cleanOldCallRecords() called");
        int cdrDuration = MIN_CALL_DATA_RETENTION_DURATION_IN_DAYS;

        try {
            int durationFromConfig = Integer.parseInt(settingsFacade.getProperty(CDR_CSR_RETENTION_DURATION));
            if (durationFromConfig < cdrDuration) {
                LOGGER.debug(String.format("Discarding retention property from config since it is less than MIN: %d",
                        cdrDuration));
            } else {
                cdrDuration = durationFromConfig;
                LOGGER.debug(String.format("Using retention property from config: %d", cdrDuration));
            }
        } catch (NumberFormatException ne) {
            LOGGER.debug(String.format("Unable to get property from config: %s", CDR_CSR_RETENTION_DURATION));
        }

        LOGGER.debug(String.format(LOG_TEMPLATE, callDetailRecordDataService.count(), CDR_TABLE_NAME));
        LOGGER.debug(String.format(LOG_TEMPLATE, callSummaryRecordDataService.count(), CSR_TABLE_NAME));

        deleteRecords(cdrDuration, CDR_TABLE_NAME);
        deleteRecords(cdrDuration, CSR_TABLE_NAME);
        deleteRecords(cdrDuration, KK_CSR_TABLE_NAME);

        LOGGER.debug(String.format(LOG_TEMPLATE, callDetailRecordDataService.count(), CDR_TABLE_NAME));
        LOGGER.debug(String.format(LOG_TEMPLATE, callSummaryRecordDataService.count(), CSR_TABLE_NAME));

        csrService.deleteOldCallSummaryRecords(cdrDuration);
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
