package org.motechproject.nms.imi.service.impl;

import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTime;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.nms.imi.domain.AuditRecord;
import org.motechproject.nms.imi.domain.CallDetailRecord;
import org.motechproject.nms.imi.domain.FileType;
import org.motechproject.nms.imi.repository.AuditDataService;
import org.motechproject.nms.imi.repository.CallDetailRecordDataService;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.imi.service.RequestId;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.service.SubscriptionService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * Implementation of the {@link CdrFileService} interface.
 */
@Service("cdrFileService")
public class CdrFileServiceImpl implements CdrFileService {

    private static final String CDR_FILE_DIRECTORY = "imi.cdr_file_directory";
    private static final String RESCHEDULE_CALL = "nms.imi.reschedule_call";
    private static final String DEACTIVATE_SUBSCRIPTION = "nms.imi.deactivate_subscription";
    private static final String COMPLETE_SUBSCRIPTION = "nms.imi.complete_subscription";

    private static final Logger LOGGER = LoggerFactory.getLogger(CdrFileServiceImpl.class);
    public static final String CDR_SUMMARY = "cdrSummary";

    private SettingsFacade settingsFacade;
    private EventRelay eventRelay;
    private AuditDataService auditDataService;
    private AlertService alertService;
    private CallDetailRecordDataService cdrDataService;
    private SubscriptionService subscriptionService;



    @Autowired
    public CdrFileServiceImpl(@Qualifier("imiSettings") SettingsFacade settingsFacade, EventRelay eventRelay,
                              AuditDataService auditDataService, AlertService alertService,
                              CallDetailRecordDataService cdrDataService,
                              SubscriptionService subscriptionService) {
        this.settingsFacade = settingsFacade;
        this.eventRelay = eventRelay;
        this.auditDataService = auditDataService;
        this.alertService = alertService;
        this.cdrDataService = cdrDataService;
        this.subscriptionService = subscriptionService;
    }


    private List<CallDetailRecord> readCdrs(String cdrFileLocation, String summaryFileName, String checksum) {
        File userDirectory = new File(System.getProperty("user.home"));
        File cdrDirectory = new File(userDirectory, cdrFileLocation);
        File cdrSummary = new File(cdrDirectory, summaryFileName);

        if (cdrSummary.exists() && !cdrSummary.isDirectory()) {
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

                lines.add(CallDetailRecord.fromLine(line));
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            String error = String.format("Unable to read cdrSummary file %s: %s", cdrSummary, e.getMessage());
            LOGGER.error(error);
            alertService.create(cdrSummary.toString(), CDR_SUMMARY, error, AlertType.CRITICAL, AlertStatus.NEW,
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
            alertService.create(cdrSummary.toString(), CDR_SUMMARY, error, AlertType.CRITICAL, AlertStatus.NEW,
                    0, null);
            //todo: what do I want to do with the identifier field here?
            auditDataService.create(new AuditRecord(null, FileType.CDR_FILE, cdrSummary.toString(), error, null,
                    null));
            throw new IllegalStateException(error);
        }

        if (!thisChecksum.equals(checksum)) {
            String error = String.format("Checksums don't match %s - %s", checksum, thisChecksum);
            LOGGER.error(error);
            alertService.create(cdrSummary.toString(), CDR_SUMMARY, error, AlertType.CRITICAL, AlertStatus.NEW,
                    0, null);
            //todo: what do I want to do with the identifier field here?
            auditDataService.create(new AuditRecord(null, FileType.CDR_FILE, cdrSummary.toString(), error, null,
                    null));
            throw new IllegalStateException(error);
        }

        LOGGER.debug("Successfully read {} cdrSummary lines", lines.size());

        return lines;
    }


    private void sendMotechEvent(String event, CallDetailRecord cdr) {
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("CDR", cdr);
        MotechEvent motechEvent = new MotechEvent(event, eventParams);
        eventRelay.sendEventMessage(motechEvent);
    }


    @Override
    public void processCdrFile(CdrFileNotificationRequest request) {
        final String cdrFileLocation = settingsFacade.getProperty(CDR_FILE_DIRECTORY);
        LOGGER.debug("Processing {} located in {}", request, cdrFileLocation);

        //todo: audit this request

        //read the summary file and keep it in memory - so we can easily refer to it when we process the
        //cdrDetail file
        List<CallDetailRecord> cdrs = readCdrs(cdrFileLocation, request.getCdrSummary().getCdrFile(),
                request.getCdrSummary().getChecksum());

        if (cdrs.size() != request.getCdrSummary().getRecordsCount()) {
            String error = String.format("Record counts don't match, expected %d but read %d",
                    request.getCdrSummary().getRecordsCount(), cdrs.size());
            LOGGER.error(error);
            alertService.create(request.getCdrSummary().getCdrFile(), CDR_SUMMARY, error, AlertType.CRITICAL,
                    AlertStatus.NEW, 0, null);
            auditDataService.create(new AuditRecord(null, FileType.CDR_FILE, request.getCdrSummary().getCdrFile(),
                    error, null, null));
            throw new IllegalStateException(error);
        }

        //todo: handle invalid data and continue processing the valid data
        //for now, and hopefully for, like, ever, only process the summary file
        DateTime tomorrow = DateTime.now().plusDays(1);
        for (int lineNumber = 1; lineNumber <= cdrs.size(); lineNumber++) {
            CallDetailRecord cdr = cdrs.get(lineNumber - 1);
            cdrDataService.create(cdr);
            if (cdr.getFinalStatus() == CallStatus.SUCCESS) {
                RequestId requestId = RequestId.fromString(cdr.getRequestId());
                Subscription subscription = subscriptionService.getSubscription(requestId.getSubscriptionId());
                // We're checking if we just successfully sent the last message for a subscription.
                // Since we're potentially processing today's CDRs for next time let's see if the subscription
                // would end tomorrow. If it does, then we can mark this subscription as completed.
                if (subscription.hasCompleted(tomorrow)) {
                    sendMotechEvent(COMPLETE_SUBSCRIPTION, cdr);
                }
                //todo: check if the subscription finished and if so, deactivate it
            } else if (cdr.getFinalStatus() == CallStatus.FAILED) {
                sendMotechEvent(RESCHEDULE_CALL, cdr);
            } else if (cdr.getFinalStatus() == CallStatus.REJECTED) {
                sendMotechEvent(DEACTIVATE_SUBSCRIPTION, cdr);
            }
        }

        //todo: create recordCount, think about checksum
        String fileIdentifier = UUID.randomUUID().toString();
        auditDataService.create(new AuditRecord(fileIdentifier, FileType.CDR_FILE, request.getFileName(), null,
                null, "Success"));

    }
}
