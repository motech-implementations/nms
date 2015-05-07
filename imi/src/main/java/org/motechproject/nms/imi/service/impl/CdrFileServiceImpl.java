package org.motechproject.nms.imi.service.impl;

import org.apache.commons.codec.binary.Hex;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.nms.imi.domain.AuditRecord;
import org.motechproject.nms.imi.domain.CallDetailRecord;
import org.motechproject.nms.imi.domain.FileType;
import org.motechproject.nms.imi.repository.AuditDataService;
import org.motechproject.nms.imi.repository.CallDetailRecordDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.ReschedulerService;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.props.domain.CallStatus;
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
import java.util.List;
import java.util.UUID;


/**
 * Implementation of the {@link CdrFileService} interface.
 */
@Service("cdrFileService")
public class CdrFileServiceImpl implements CdrFileService {

    private static final String CDR_FILE_DIRECTORY = "imi.cdr_file_directory";

    private static final Logger LOGGER = LoggerFactory.getLogger(CdrFileServiceImpl.class);

    private SettingsFacade settingsFacade;
    private AuditDataService auditDataService;
    private AlertService alertService;
    private CallDetailRecordDataService cdrDataService;
    private ReschedulerService reschedulerService;



    @Autowired
    public CdrFileServiceImpl(@Qualifier("imiSettings") SettingsFacade settingsFacade,
                              AuditDataService auditDataService, AlertService alertService,
                              CallDetailRecordDataService cdrDataService, ReschedulerService reschedulerService) {
        this.settingsFacade = settingsFacade;
        this.auditDataService = auditDataService;
        this.alertService = alertService;
        this.cdrDataService = cdrDataService;
        this.reschedulerService = reschedulerService;
    }


    private List<CallDetailRecord> readCdrs(String cdrFileLocation, String summaryFileName, String checksum) {
        File userDirectory = new File(System.getProperty("user.home"));
        File cdrDirectory = new File(userDirectory, cdrFileLocation);
        File cdrSummary = new File(cdrDirectory, summaryFileName);

        if(cdrSummary.exists() && !cdrSummary.isDirectory()) {
            LOGGER.debug("Found CDR summary file. Starting to read...");
        }

        List<CallDetailRecord> lines = new ArrayList<>();
        MessageDigest md;
        try (FileInputStream fis = new FileInputStream(cdrSummary);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader reader = new BufferedReader(isr)) {
            md = MessageDigest.getInstance("MD5");

            @SuppressWarnings("PMD.UnusedLocalVariable")
            DigestInputStream dis = new DigestInputStream(fis, md);

            String line;
            while ((line = reader.readLine()) != null) {
                // TODO: debugging. take this out.
                LOGGER.debug("Read line: " + line);

                lines.add(CallDetailRecord.fromLine(line));
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            String error = String.format("Unable to read cdrSummary file %s: %s", cdrSummary, e.getMessage());
            LOGGER.error(error);
            alertService.create(cdrSummary.toString(), "cdrSummary", error, AlertType.CRITICAL, AlertStatus.NEW,
                    0, null);

            //todo: what do I want to do with the identifier field here?
            auditDataService.create(new AuditRecord(null, FileType.CDR_FILE, cdrSummary.toString(), error, null,
                    null));
            throw new IllegalStateException(error);
        }

        String thisChecksum = new String(Hex.encodeHex(md.digest()));

        if (!thisChecksum.equals(checksum)) {
            String error = String.format("Checksums don't match %s - %s", checksum, thisChecksum);
            LOGGER.error(error);
            alertService.create(cdrSummary.toString(), "cdrSummary", error, AlertType.CRITICAL, AlertStatus.NEW,
                    0, null);
            //todo: what do I want to do with the identifier field here?
            auditDataService.create(new AuditRecord(null, FileType.CDR_FILE, cdrSummary.toString(), error, null,
                    null));
            throw new IllegalStateException(error);
        }

        if (!thisChecksum.equals(checksum)) {
            String error = String.format("Checksums don't match %s - %s", checksum, thisChecksum);
            LOGGER.error(error);
            alertService.create(cdrSummary.toString(), "cdrSummary", error, AlertType.CRITICAL, AlertStatus.NEW,
                    0, null);
            //todo: what do I want to do with the identifier field here?
            auditDataService.create(new AuditRecord(null, FileType.CDR_FILE, cdrSummary.toString(), error, null,
                    null));
            throw new IllegalStateException(error);
        }

        LOGGER.info("Successfully read {} cdrSummary lines", lines.size());

        return lines;
    }


    @Override
    public void processCdrFile(CdrFileNotificationRequest request) {
        final String cdrFileLocation = settingsFacade.getProperty(CDR_FILE_DIRECTORY);
        LOGGER.debug("Processing {} located in {}", request, cdrFileLocation);

        //todo: audit this request

        //read the summary file and keep it in memory - so we can easily refer to it when we process the cdrDetail file
        List<CallDetailRecord> cdrs = readCdrs(cdrFileLocation, request.getCdrSummary().getCdrFile(),
                request.getCdrSummary().getChecksum());

        //for now, and hopefully for, like, ever, only process the summary file
        for (int lineNumber = 1; lineNumber < cdrs.size(); lineNumber++) {
            CallDetailRecord cdr = cdrs.get(lineNumber - 1);
            cdrDataService.create(cdr);
            if (cdr.getFinalStatus() != CallStatus.SUCCESS) {
                //Sending a MOTECH message distributes the work to all nodes
                LOGGER.debug("*****BEFORE MESSAGE*****");
                reschedulerService.sendRescheduleMessage(cdr);
                LOGGER.debug("*****AFTER MESSAGE******");
            }
        }

        //todo: add recordCount, think about checksum
        String fileIdentifier = UUID.randomUUID().toString();
        auditDataService.create(new AuditRecord(fileIdentifier, FileType.CDR_FILE, request.getFileName(), null,
                null, "Success"));

    }
}
