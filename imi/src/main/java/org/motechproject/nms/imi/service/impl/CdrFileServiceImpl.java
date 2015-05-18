package org.motechproject.nms.imi.service.impl;

import org.apache.commons.codec.binary.Hex;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.nms.imi.domain.FileAuditRecord;
import org.motechproject.nms.imi.domain.FileType;
import org.motechproject.nms.imi.exception.InvalidCdrFileException;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.CsrValidatorService;
import org.motechproject.nms.imi.service.contract.ParseResults;
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
import java.util.List;
import java.util.Map;


/**
 * Implementation of the {@link CdrFileService} interface.
 */
@Service("cdrFileService")
public class CdrFileServiceImpl implements CdrFileService {

    private static final String CDR_FILE_DIRECTORY = "imi.cdr_file_directory";
    private static final String PROCESS_SUMMARY_RECORD = "nms.imi.kk.process_summary_record";
    private static final String CSR_PARAM_KEY = "csr";

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
        fileAuditRecordDataService.create(new FileAuditRecord(FileType.CDR_DETAIL_FILE, file, error, null, null));
        throw new IllegalStateException(error);
    }


    private void reportAuditAndThrow(String file, List<String> errors) {
        for (String error : errors) {
            LOGGER.error(error);
            alertService.create(file, "CDR Detail File", error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            fileAuditRecordDataService.create(new FileAuditRecord(FileType.CDR_DETAIL_FILE, file, error, null, null));
        }
        throw new InvalidCdrFileException(errors);
    }


    // Aggregate all detail records for a requestId into a single summary record
    private void parseDetailRecord(CallDetailRecordDto cdr, Map<String, CallSummaryRecordDto> records) {
        if (records.containsKey(cdr.getRequestId().toString())) {
            CallSummaryRecordDto record = records.get(cdr.getRequestId().toString());

            // Increment the statusCode stats
            int statusCodeCount = 1;
            if (record.getStatusStats().containsKey(cdr.getStatusCode())) {
                statusCodeCount = record.getStatusStats().get(cdr.getStatusCode()) + 1;
            }
            record.getStatusStats().put(cdr.getStatusCode().getValue(), statusCodeCount);

            // Increment the message play duration
            if (cdr.getMsgPlayDuration() > record.getSecondsPlayed()) {
                record.setSecondsPlayed(cdr.getMsgPlayDuration());
            }

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


    // Create an in-memory set of summary records from all detail records
    private ParseResults parseDetailRecords(File file, FileInfo fileInfo) {
        String thisChecksum = "";
        Map<String, CallSummaryRecordDto> records = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int lineNumber = 1;

        try (
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader reader = new BufferedReader(isr)
            ) {


            MessageDigest md = MessageDigest.getInstance("MD5");
            @SuppressWarnings("PMD.UnusedLocalVariable")
            DigestInputStream dis = new DigestInputStream(fis, md);

            String line;
            while ((line = reader.readLine()) != null) {

                try {
                    CallDetailRecordDto cdr = CsvHelper.csvLineToCdr(line);

                    parseDetailRecord(cdr, records);

                } catch (IllegalArgumentException e) {
                    errors.add(String.format("Line %d: %s", lineNumber, e.getMessage()));
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

        return new ParseResults(records, errors);
    }


    @Override
    public ParseResults processDetailFile(FileInfo fileInfo) {
        File userDirectory = new File(System.getProperty("user.home"));
        File cdrDirectory = new File(userDirectory, settingsFacade.getProperty(CDR_FILE_DIRECTORY));
        File file = new File(cdrDirectory, fileInfo.getCdrFile());

        return parseDetailRecords(file, fileInfo);
    }


    private void sendProcessCdrEvent(CallSummaryRecordDto record) {
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, record);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD, eventParams);
        eventRelay.sendEventMessage(motechEvent);
    }


    @Override
    public void dispatchSummaryRecords(FileInfo fileInfo) {
        ParseResults parseResults = processDetailFile(fileInfo);

        //todo: implement max failure count threshold
        if (parseResults.getErrors().size() > 0) {
            reportAuditAndThrow(fileInfo.getCdrFile(), parseResults.getErrors());
        }

        if (csrValidatorService.validateCallSummaryRecords(parseResults)) {
            for (CallSummaryRecordDto record : parseResults.getRecords().values()) {
                sendProcessCdrEvent(record);
            }
        } else {
            reportAuditAndThrow(fileInfo.getCdrFile(), parseResults.getErrors());
        }
    }
}
