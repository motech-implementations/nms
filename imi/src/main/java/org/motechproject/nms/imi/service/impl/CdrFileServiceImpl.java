package org.motechproject.nms.imi.service.impl;

import org.apache.commons.codec.binary.Hex;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.nms.imi.domain.FileAuditRecord;
import org.motechproject.nms.imi.domain.FileType;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.contract.CdrParseResult;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.kilkari.domain.CallDetailRecord;
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
    private static final String PROCESS_CDR = "nms.imi.kk.process_cdr";

    private static final Logger LOGGER = LoggerFactory.getLogger(CdrFileServiceImpl.class);
    public static final String CDR_SUMMARY = "cdrSummary";

    private SettingsFacade settingsFacade;
    private EventRelay eventRelay;
    private FileAuditRecordDataService fileAuditRecordDataService;
    private AlertService alertService;



    @Autowired
    public CdrFileServiceImpl(@Qualifier("imiSettings") SettingsFacade settingsFacade, EventRelay eventRelay,
                              FileAuditRecordDataService fileAuditRecordDataService, AlertService alertService) {
        this.settingsFacade = settingsFacade;
        this.eventRelay = eventRelay;
        this.fileAuditRecordDataService = fileAuditRecordDataService;
        this.alertService = alertService;
    }


    private void reportErrorAndThrow(String file, String error) {
        LOGGER.error(error);
        alertService.create(file, CDR_SUMMARY, error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
        fileAuditRecordDataService.create(new FileAuditRecord(FileType.CDR_SUMMARY_FILE, file, error, null, null));
        throw new IllegalStateException(error);
    }



    private CdrParseResult parseSummaryCdrs(String dir, String fileName, String checksum) {
        List<CallDetailRecord> cdrs = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        File userDirectory = new File(System.getProperty("user.home"));
        File cdrDirectory = new File(userDirectory, dir);
        File cdrSummary = new File(cdrDirectory, fileName);

        if (cdrSummary.exists() && !cdrSummary.isDirectory()) {
            LOGGER.debug("Found CDR summary file. Starting to read...");
        }

        MessageDigest md;
        String thisChecksum = "";
        try (FileInputStream fis = new FileInputStream(cdrSummary);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader reader = new BufferedReader(isr)) {
            md = MessageDigest.getInstance("MD5");

            @SuppressWarnings("PMD.UnusedLocalVariable")
            DigestInputStream dis = new DigestInputStream(fis, md);

            String line;
            while ((line = reader.readLine()) != null) {

                cdrs.add(CallDetailRecord.fromCsvLine(line));
            }
            thisChecksum = new String(Hex.encodeHex(md.digest()));
        } catch (NoSuchAlgorithmException | IOException e) {
            String error = String.format("Unable to read cdrSummary file %s: %s", cdrSummary, e.getMessage());
            reportErrorAndThrow(cdrSummary.getName(), error);
        }

        if (!thisChecksum.equals(checksum)) {
            String error = String.format("Checksum mismatch, provided checksum: %s, calculated checksum: %s",
                    checksum, thisChecksum);
            reportErrorAndThrow(cdrSummary.getName(), error);
        }

        fileAuditRecordDataService.create(new FileAuditRecord(FileType.CDR_SUMMARY_FILE, fileName, "SUCCESS", cdrs.size(),
                thisChecksum));

        return new CdrParseResult(cdrs, errors);
    }


    @Override
    public CdrParseResult processCdrFile(CdrFileNotificationRequest request) {
        final String cdrFileLocation = settingsFacade.getProperty(CDR_FILE_DIRECTORY);

        CdrParseResult result = parseSummaryCdrs(cdrFileLocation, request.getCdrSummary().getCdrFile(),
                request.getCdrSummary().getChecksum());

        if (result.getCdrs().size() != request.getCdrSummary().getRecordsCount()) {
            String error = String.format("Record counts don't match, expected %d but read %d",
                    request.getCdrSummary().getRecordsCount(), result.getCdrs().size());
            reportErrorAndThrow(request.getCdrSummary().getCdrFile(), error);
        }


        //todo: see if we need to get the cdr detail file and aggregate the stats ourselves


        for (CallDetailRecord cdr : result.getCdrs()) {
            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put("CDR", cdr);
            MotechEvent motechEvent = new MotechEvent(PROCESS_CDR, eventParams);
            eventRelay.sendEventMessage(motechEvent);
        }

        // This return value is really only used by ITs
        return result;
    }
}
