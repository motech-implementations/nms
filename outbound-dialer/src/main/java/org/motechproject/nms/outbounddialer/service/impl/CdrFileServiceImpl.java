package org.motechproject.nms.outbounddialer.service.impl;

import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.nms.outbounddialer.domain.AuditRecord;
import org.motechproject.nms.outbounddialer.domain.FileType;
import org.motechproject.nms.outbounddialer.repository.FileAuditDataService;
import org.motechproject.nms.outbounddialer.service.CdrFileService;
import org.motechproject.nms.outbounddialer.web.contract.CdrFileNotificationRequest;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


/**
 * Implementation of the {@link CdrFileService} interface.
 */
@Service("cdrFileService")
public class CdrFileServiceImpl implements CdrFileService {

    private static final String CDR_FILE_DIRECTORY = "outbound-dialer.cdr_file_directory";

    private SettingsFacade settingsFacade;
    private FileAuditDataService fileAuditDataService;
    private AlertService alertService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CdrFileServiceImpl.class);


    static class SummaryLine {
        private String requestId;
        private String serviceId;
        private String msisdn;
        private String cli;
        private Integer priority;
        private String callFlowUrl;
        private String contentFileName;
        private String weekId;
        private String languageLocationCode;
        private String circle;
        private Integer finalStatus;
        private Integer statusCode;
        private Integer attempts;

        static final int NUMBER_OF_FIELDS = 13;

        public SummaryLine() { }

        public SummaryLine(String requestId, String serviceId, String msisdn, String cli, Integer priority, 
                           String callFlowUrl, String contentFileName, String weekId, String languageLocationCode, 
                           String circle, Integer finalStatus, Integer statusCode, Integer attempts) {
            this.requestId = requestId;
            this.serviceId = serviceId;
            this.msisdn = msisdn;
            this.cli = cli;
            this.priority = priority;
            this.callFlowUrl = callFlowUrl;
            this.contentFileName = contentFileName;
            this.weekId = weekId;
            this.languageLocationCode = languageLocationCode;
            this.circle = circle;
            this.finalStatus = finalStatus;
            this.statusCode = statusCode;
            this.attempts = attempts;
        }

        public static SummaryLine fromLine(String line) {
            String[] fields = line.split(",");
            if (fields.length != NUMBER_OF_FIELDS) {
                throw new IllegalStateException(String.format("Wrong number of fields, expecting %d but seeing %d",
                        NUMBER_OF_FIELDS,
                        fields.length));
            }
            return new SummaryLine(fields[0], fields[1], fields[2], fields[3], Integer.parseInt(fields[4]), fields[5],
                    fields[6], fields[7], fields[8], fields[9], Integer.parseInt(fields[10]),
                    Integer.parseInt(fields[11]), Integer.parseInt(fields[12]));
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getMsisdn() {
            return msisdn;
        }

        public void setMsisdn(String msisdn) {
            this.msisdn = msisdn;
        }

        public String getCli() {
            return cli;
        }

        public void setCli(String cli) {
            this.cli = cli;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }

        public String getCallFlowUrl() {
            return callFlowUrl;
        }

        public void setCallFlowUrl(String callFlowUrl) {
            this.callFlowUrl = callFlowUrl;
        }

        public String getContentFileName() {
            return contentFileName;
        }

        public void setContentFileName(String contentFileName) {
            this.contentFileName = contentFileName;
        }

        public String getWeekId() {
            return weekId;
        }

        public void setWeekId(String weekId) {
            this.weekId = weekId;
        }

        public String getLanguageLocationCode() {
            return languageLocationCode;
        }

        public void setLanguageLocationCode(String languageLocationCode) {
            this.languageLocationCode = languageLocationCode;
        }

        public String getCircle() {
            return circle;
        }

        public void setCircle(String circle) {
            this.circle = circle;
        }

        public Integer getFinalStatus() {
            return finalStatus;
        }

        public void setFinalStatus(Integer finalStatus) {
            this.finalStatus = finalStatus;
        }

        public Integer getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(Integer statusCode) {
            this.statusCode = statusCode;
        }

        public Integer getAttempts() {
            return attempts;
        }

        public void setAttempts(Integer attempts) {
            this.attempts = attempts;
        }

        public static int getNumberOfFields() {
            return NUMBER_OF_FIELDS;
        }

        @Override
        public String toString() {
            return "SummaryLine{" +
                    "requestId='" + requestId + '\'' +
                    ", serviceId='" + serviceId + '\'' +
                    ", msisdn='" + msisdn + '\'' +
                    ", cli='" + cli + '\'' +
                    ", priority=" + priority +
                    ", callFlowUrl='" + callFlowUrl + '\'' +
                    ", contentFileName='" + contentFileName + '\'' +
                    ", weekId='" + weekId + '\'' +
                    ", languageLocationCode='" + languageLocationCode + '\'' +
                    ", circle='" + circle + '\'' +
                    ", finalStatus=" + finalStatus +
                    ", statusCode=" + statusCode +
                    ", attempts=" + attempts +
                    '}';
        }
    }

    @Autowired
    public CdrFileServiceImpl(@Qualifier("outboundDialerSettings") SettingsFacade settingsFacade,
                              FileAuditDataService fileAuditDataService, AlertService alertService) {
        this.settingsFacade = settingsFacade;
        this.fileAuditDataService = fileAuditDataService;
        this.alertService = alertService;
    }


    @Override
    public void processCdrFile(CdrFileNotificationRequest request) {
        final String cdrFileLocation = settingsFacade.getProperty(CDR_FILE_DIRECTORY);
        LOGGER.debug("Processing {} located in {}", request, cdrFileLocation);

        //todo: audit this request

        // read the summary file and keep it in memory
        File userDirectory = new File(System.getProperty("user.home"));
        File cdrDirectory = new File(userDirectory, cdrFileLocation);
        File cdrSummary = new File(cdrDirectory, request.getCdrSummary().getCdrFile());
        Set<SummaryLine> summaryLines = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(cdrSummary));
            String line;
            while ((line = reader.readLine()) != null) {
                summaryLines.add(SummaryLine.fromLine(line));
            }
        } catch (IOException e) {
            String error = String.format("Unable to read cdrSummary file %s: %s", cdrSummary, e.getMessage());
            LOGGER.error(error);
            alertService.create(cdrSummary.toString(), "cdrSummary", error, AlertType.CRITICAL, AlertStatus.NEW, 0,
                    null);
            //todo: what do I want to do with the identifier field here?
            fileAuditDataService.create(new AuditRecord(null, FileType.CDR_FILE, cdrSummary.toString(), error, null,
                    null));
            throw new IllegalStateException(error);
        }

        LOGGER.info("Successfully read {} cdrSummary lines", summaryLines.size());


        
        //todo: add recordCount, think about checksum
        String fileIdentifier = UUID.randomUUID().toString();
        fileAuditDataService.create(new AuditRecord(fileIdentifier, FileType.CDR_FILE, request.getFileName(), null,
                null, "Success"));

    }
}
