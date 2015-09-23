package org.motechproject.nms.testing.it.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.nms.imi.domain.CallSummaryRecord;
import org.motechproject.nms.imi.domain.FileAuditRecord;
import org.motechproject.nms.imi.domain.FileType;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.imi.web.contract.FileInfo;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.dto.CallDetailRecordDto;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.CallDisconnectReason;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.RequestId;
import org.motechproject.nms.props.domain.StatusCode;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.LanguageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

public class CdrHelper {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private static final String OBD_FILENAME_FORMAT = "OBD_NMS_%s.csv";
    private static final Pattern OBD_TIMESTAMP_PATTERN = Pattern.compile("OBD_NMS_([0-9]*).csv");

    private static final Logger LOGGER = LoggerFactory.getLogger(CdrHelper.class);

    public static final String LOCAL_CDR_DIR_PROP = "imi.local_cdr_dir";
    public static final String REMOTE_CDR_DIR_PROP = "imi.remote_cdr_dir";

    private static final String OBD_FILE_PARAM_KEY = "obdFile";
    private static final String CSR_FILE_PARAM_KEY = "csrFile";
    private static final String CSR_CHECKSUM_PARAM_KEY = "csrChecksum";
    private static final String CSR_COUNT_PARAM_KEY = "csrCount";
    private static final String CDR_FILE_PARAM_KEY = "cdrFile";
    private static final String CDR_CHECKSUM_PARAM_KEY = "cdrChecksum";
    private static final String CDR_COUNT_PARAM_KEY = "cdrCount";

    private final String TEST_OBD_TIMESTAMP;
    private final String TEST_OBD_FILENAME;
    private final String TEST_CDR_DETAIL_FILENAME;
    private final String TEST_CDR_SUMMARY_FILENAME;
    private static final int CHILD_PACK_WEEKS = 48;
    private static final int NORMAL_PRIORITY = 0;

    private SettingsService settingsService;
    private FileAuditRecordDataService fileAuditRecordDataService;
    private SubscriptionHelper sh;
    private RegionHelper rh;

    private List<CallDetailRecordDto> cdrs = new ArrayList<>();

    private List<CallSummaryRecord> csrs = new ArrayList<>();


    public CdrHelper(
            SettingsService settingsService,
            SubscriptionService subscriptionService,
            SubscriberDataService subscriberDataService,
            SubscriptionPackDataService subscriptionPackDataService,
            LanguageDataService languageDataService,
            LanguageService languageService,
            CircleDataService circleDataService,
            StateDataService stateDataService,
            DistrictDataService districtDataService,
            FileAuditRecordDataService fileAuditRecordDataService,
            DistrictService districtService,
            String obdFileName
    ) throws IOException {

        sh = new SubscriptionHelper(subscriptionService, subscriberDataService, subscriptionPackDataService,
                languageDataService, languageService, circleDataService, stateDataService, districtDataService,
                districtService);

        rh = new RegionHelper(languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, districtService);

        this.settingsService = settingsService;
        this.fileAuditRecordDataService = fileAuditRecordDataService;

        TEST_OBD_FILENAME = obdFileName;
        Matcher m = OBD_TIMESTAMP_PATTERN.matcher(obdFileName);
        assertTrue(m.find());
        TEST_OBD_TIMESTAMP = m.group(1);
        TEST_CDR_DETAIL_FILENAME = String.format("cdrDetail_%s", TEST_OBD_FILENAME);
        TEST_CDR_SUMMARY_FILENAME = String.format("cdrSummary_%s", TEST_OBD_FILENAME);
    }


    public CdrHelper(
            SettingsService settingsService,
            SubscriptionService subscriptionService,
            SubscriberDataService subscriberDataService,
            SubscriptionPackDataService subscriptionPackDataService,
            LanguageDataService languageDataService,
            LanguageService languageService,
            CircleDataService circleDataService,
            StateDataService stateDataService,
            DistrictDataService districtDataService,
            FileAuditRecordDataService fileAuditRecordDataService,
            DistrictService districtService
    ) throws IOException {
        this(settingsService, subscriptionService, subscriberDataService, subscriptionPackDataService,
                languageDataService, languageService, circleDataService, stateDataService, districtDataService,
                fileAuditRecordDataService, districtService,
                String.format(OBD_FILENAME_FORMAT, DateTime.now().toString(TIME_FORMATTER)));
    }


    public List<CallDetailRecordDto> getCdrs() {
        return cdrs;
    }


    private CallDetailRecordDto makeCdrDto(Subscription sub) {
        CallDetailRecordDto cdr = new CallDetailRecordDto();
        cdr.setRequestId(new RequestId(sub.getSubscriptionId(), timestamp()));
        cdr.setMsisdn(sub.getSubscriber().getCallingNumber());
        cdr.setCallAnswerTime(DateTime.now().minusHours(5));
        cdr.setMsgPlayDuration(110 + (int) (Math.random() * 20));
        cdr.setLanguageLocationId(rh.hindiLanguage().getCode());
        cdr.setCircleId(rh.delhiCircle().getName());
        cdr.setOperatorId("xx");
        return cdr;
    }


    private CallSummaryRecord makeCsr(Subscription sub) {
        CallSummaryRecord csr = new CallSummaryRecord();
        csr.setRequestId(new RequestId(sub.getSubscriptionId(), timestamp()).toString());
        csr.setMsisdn(sub.getSubscriber().getCallingNumber());
        csr.setLanguageLocationCode(rh.hindiLanguage().getCode());
        csr.setCircle(rh.delhiCircle().getName());
        return csr;
    }


    private static final StatusCode[] failureReasons = {
            StatusCode.OBD_FAILED_BUSY,
            StatusCode.OBD_FAILED_NOANSWER,
            StatusCode.OBD_FAILED_NOATTEMPT,
            StatusCode.OBD_FAILED_OTHERS,
            StatusCode.OBD_FAILED_SWITCHEDOFF
    };


    private StatusCode randomFailureStatusCode() {
        return failureReasons[(int) (Math.random() * failureReasons.length)];
    }


    public void makeSingleCallCdrs(int numFailure, boolean eventuallySuccessful) {
        if (cdrs == null) { cdrs = new ArrayList<>(); }

        Subscription sub = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(30));
        for (int i = 0; i < numFailure; i++) {
            CallDetailRecordDto cdr = makeCdrDto(sub);
            cdr.setStatusCode(randomFailureStatusCode());
            cdr.setContentFile(sh.childPack().getMessages().get(5).getMessageFileName());
            cdr.setCallDisconnectReason(CallDisconnectReason.NORMAL_DROP);
            cdr.setWeekId("w5_1");
            cdrs.add(cdr);
        }

        if (eventuallySuccessful) {
            CallDetailRecordDto cdr = makeCdrDto(sub);
            cdr.setStatusCode(StatusCode.OBD_SUCCESS_CALL_CONNECTED);
            cdr.setContentFile(sh.childPack().getMessages().get(5).getMessageFileName());
            cdr.setCallDisconnectReason(CallDisconnectReason.NORMAL_DROP);
            cdr.setWeekId("w5_1");
            cdrs.add(cdr);
        }
    }


    public void makeCdrs(int numSuccess, int numFailed, int numComplete, int numIvr) {
        if (cdrs == null) { cdrs = new ArrayList<>(); }

        for (int i=0 ; i<numSuccess ; i++) {
            Subscription sub = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(30));
            CallDetailRecordDto cdr = makeCdrDto(sub);
            cdr.setStatusCode(StatusCode.OBD_SUCCESS_CALL_CONNECTED);
            cdr.setContentFile(sh.childPack().getMessages().get(4).getMessageFileName());
            cdr.setCallDisconnectReason(CallDisconnectReason.NORMAL_DROP);
            cdr.setWeekId("w4_1");
            cdrs.add(cdr);
        }

        for (int i=0 ; i<numFailed ; i++) {
            Subscription sub = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(30));
            CallDetailRecordDto cdr = makeCdrDto(sub);
            cdr.setStatusCode(StatusCode.OBD_FAILED_NOANSWER);
            cdr.setContentFile(sh.childPack().getMessages().get(5).getMessageFileName());
            cdr.setCallDisconnectReason(CallDisconnectReason.NORMAL_DROP);
            cdr.setWeekId("w5_1");
            cdrs.add(cdr);
        }

        for (int i=0 ; i<numComplete ; i++) {
            int days = CHILD_PACK_WEEKS * 7;
            Subscription sub = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(days));
            CallDetailRecordDto cdr = makeCdrDto(sub);
            cdr.setStatusCode(StatusCode.OBD_SUCCESS_CALL_CONNECTED);
            cdr.setContentFile(sh.childPack().getMessages().get(CHILD_PACK_WEEKS-1).getMessageFileName());
            cdr.setCallDisconnectReason(CallDisconnectReason.NORMAL_DROP);
            cdr.setWeekId(String.format("w%d_1", CHILD_PACK_WEEKS));
            cdrs.add(cdr);
        }

        for (int i=0 ; i<numIvr ; i++) {
            Subscription sub = sh.mksub(SubscriptionOrigin.IVR, DateTime.now().minusDays(30));
            CallDetailRecordDto cdr = makeCdrDto(sub);
            cdr.setStatusCode(StatusCode.OBD_SUCCESS_CALL_CONNECTED);
            cdr.setContentFile(sh.childPack().getMessages().get(6).getMessageFileName());
            cdr.setCallDisconnectReason(CallDisconnectReason.NORMAL_DROP);
            cdr.setWeekId("w6_1");
            cdrs.add(cdr);
        }
    }


    public void makeCsrs(int numFailed) {
        if (csrs == null) { csrs = new ArrayList<>(); }

        for (int i=0 ; i<numFailed ; i++) {
            Subscription sub = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(30));
            CallSummaryRecord csr = makeCsr(sub);
            csr.setStatusCode(StatusCode.OBD_FAILED_NOATTEMPT.getValue());
            csr.setContentFileName(sh.childPack().getMessages().get(7).getMessageFileName());
            csr.setWeekId("w7_1");
            csr.setPriority(NORMAL_PRIORITY);
            csr.setFinalStatus(FinalCallStatus.FAILED.getValue());
            csr.setAttempts(1);
            csr.setCallFlowUrl("url");
            csr.setCli("cli");
            csr.setServiceId("id");
            csrs.add(csr);
        }
    }


    public String timestamp() {
        return TEST_OBD_TIMESTAMP;
    }


    public String obd() {
        return TEST_OBD_FILENAME;
    }


    public String csr() {
        return TEST_CDR_SUMMARY_FILENAME;
    }


    public String cdr() {
        return TEST_CDR_DETAIL_FILENAME;
    }


    public int cdrCount() {
        return cdrs.size();
    }


    public int csrCount() {
        return csrs.size();
    }


    public static String csvLineFromCdr(CallDetailRecordDto cdr) {
        StringBuilder sb = new StringBuilder();

        //REQUEST_ID,
        sb.append(cdr.getRequestId().toString());
        sb.append(',');

        //MSISDN,
        sb.append(cdr.getMsisdn());
        sb.append(',');

        //CALL_ID,
        sb.append("xxx");
        sb.append(',');

        //ATTEMPT_NO,
        sb.append(1);
        sb.append(',');

        //CALL_START_TIME,
        sb.append(1);
        sb.append(',');

        //CALL_ANSWER_TIME,
        sb.append(cdr.getCallAnswerTime().getMillis() / 1000);
        sb.append(',');

        //CALL_END_TIME,
        sb.append(1);
        sb.append(',');

        //CALL_DURATION_IN_PULSE,
        sb.append(1);
        sb.append(',');

        //CALL_STATUS,
        sb.append(cdr.getStatusCode().getValue());
        sb.append(',');

        //LANGUAGE_LOCATION_ID,
        sb.append(cdr.getLanguageLocationId());
        sb.append(',');

        //CONTENT_FILE,
        sb.append(cdr.getContentFile());
        sb.append(',');

        //MSG_PLAY_START_TIME,
        sb.append(1);
        sb.append(',');

        //MSG_PLAY_END_TIME,
        sb.append(1 + cdr.getMsgPlayDuration());
        sb.append(',');

        //CIRCLE_ID,
        sb.append(cdr.getCircleId());
        sb.append(',');

        //OPERATOR_ID,
        sb.append(cdr.getOperatorId());
        sb.append(',');

        //PRIORITY,
        sb.append(0);
        sb.append(',');

        //CALL_DISCONNECT_REASON,
        sb.append(cdr.getCallDisconnectReason().getValue());
        sb.append(',');

        //WEEK_ID,
        sb.append(cdr.getWeekId());


        return sb.toString();
    }


    public static String csvLineFromCsr(CallSummaryRecord csr) {
        StringBuilder sb = new StringBuilder();

        sb.append(csr.getRequestId());
        sb.append(',');

        sb.append(csr.getServiceId());
        sb.append(',');

        sb.append(csr.getMsisdn());
        sb.append(',');

        sb.append(csr.getCli());
        sb.append(',');

        sb.append(csr.getPriority());
        sb.append(',');

        sb.append(csr.getCallFlowUrl());
        sb.append(',');

        sb.append(csr.getContentFileName());
        sb.append(',');

        sb.append(csr.getWeekId());
        sb.append(',');

        sb.append(csr.getLanguageLocationCode());
        sb.append(',');

        sb.append(csr.getCircle());
        sb.append(',');

        sb.append(csr.getFinalStatus());
        sb.append(',');

        sb.append(csr.getStatusCode());
        sb.append(',');

        sb.append(csr.getAttempts());

        return sb.toString();
    }


    public String remoteDir() {
        return settingsService.getSettingsFacade().getProperty(REMOTE_CDR_DIR_PROP);
    }


    public String localDir() {
        return settingsService.getSettingsFacade().getProperty(LOCAL_CDR_DIR_PROP);
    }


    private File doMakeCsrFile(String dir, int numInvalidLines) throws IOException {
        File file = new File(dir, csr());
        LOGGER.debug("Creating summary file {}...", file);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        int remainingInvalidLines = numInvalidLines;

        writer.write(org.motechproject.nms.imi.service.impl.CsrHelper.CSR_HEADER);
        writer.write("\n");
        for(CallSummaryRecord csr : csrs) {
            writer.write(csvLineFromCsr(csr));
            if (remainingInvalidLines > 0) {
                writer.write(",invalid_field");
                remainingInvalidLines--;
            }
            writer.write("\n");
        }

        writer.close();
        return file;
    }


    public void makeLocalCsrFile(int numInvalidLines) throws IOException {
        doMakeCsrFile(localDir(), numInvalidLines);
    }


    public void makeLocalCsrFile() throws IOException {
        doMakeCsrFile(localDir(), 0);
    }


    public void makeRemoteCsrFile(int numInvalidLines) throws IOException {
        doMakeCsrFile(remoteDir(), numInvalidLines);
    }

    public void makeRemoteCsrFile() throws IOException {
        doMakeCsrFile(remoteDir(), 0);
    }


    public void createObdFileAuditRecord(boolean valid, boolean success) throws IOException, NoSuchAlgorithmException {
        fileAuditRecordDataService.create(new FileAuditRecord(
                FileType.TARGET_FILE,
                valid ? obd() : "xxx",
                success,
                success ? null : "ERROR",
                123,
                "123abc"
        ));
    }


    private File doMakeCdrFile(String dir, int numInvalidLines) throws IOException {
        File file = new File(dir, cdr());
        LOGGER.debug("Creating detail file {}...", file);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        int remainingInvalidLines = numInvalidLines;

        writer.write(org.motechproject.nms.imi.service.impl.CdrHelper.CDR_HEADER);
        writer.write("\n");
        for(CallDetailRecordDto cdr : cdrs) {
            writer.write(csvLineFromCdr(cdr));
            if (remainingInvalidLines > 0) {
                writer.write(",invalid_field");
                remainingInvalidLines--;
            }
            writer.write("\n");
        }

        writer.close();
        return file;
    }


    public File makeLocalCdrFile() throws IOException {
        return doMakeCdrFile(localDir(), 0);
    }


    public File makeLocalCdrFile(int numInvalidLines) throws IOException {
        return doMakeCdrFile(localDir(), numInvalidLines);
    }


    public File makeRemoteCdrFile() throws IOException {
        return doMakeCdrFile(remoteDir(), 0);
    }


    public File makeRemoteCdrFile(String dir, int numInvalidLines) throws IOException {
        return doMakeCdrFile(remoteDir(), numInvalidLines);
    }


    public String csrLocalChecksum() throws IOException, NoSuchAlgorithmException {
        return ChecksumHelper.checksum(new File(localDir(), csr()));
    }


    private int recordCount(File file) throws FileNotFoundException, IOException {

        int recordCount = 0;
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader reader = new BufferedReader(isr);

        // skip header
        reader.readLine();

        while (reader.readLine() != null) {
            recordCount++;
        }
        reader.close();
        isr.close();
        fis.close();

        return recordCount;
    }


    public String cdrLocalChecksum() throws IOException, NoSuchAlgorithmException {
        return ChecksumHelper.checksum(new File(localDir(), cdr()));
    }


    public int cdrRemoteRecordCount() throws IOException, FileNotFoundException {
        return recordCount(new File(remoteDir(), cdr()));
    }


    public int csrRemoteRecordCount() throws IOException, FileNotFoundException {
        return recordCount(new File(remoteDir(), csr()));
    }


    public String csrRemoteChecksum() throws IOException, NoSuchAlgorithmException {
        return ChecksumHelper.checksum(new File(remoteDir(), csr()));
    }


    public String cdrRemoteChecksum() throws IOException, NoSuchAlgorithmException {
        return ChecksumHelper.checksum(new File(remoteDir(), cdr()));
    }


    public CdrFileNotificationRequest cdrFileNotificationRequest() throws IOException, NoSuchAlgorithmException {
        FileInfo cdrFileInfo = new FileInfo(cdr(), cdrLocalChecksum(), cdrCount());
        FileInfo csrFileInfo = new FileInfo(csr(), csrLocalChecksum(), csrCount());
        return new CdrFileNotificationRequest(obd(), csrFileInfo, cdrFileInfo);
    }


    public static CdrFileNotificationRequest requestFromParams(Map<String, Object> params) {
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


    public Map<String, Object> cdrFileNotificationParams() throws IOException, NoSuchAlgorithmException{
        Map<String, Object> params = new HashMap<>();
        params.put(OBD_FILE_PARAM_KEY, obd());
        params.put(CSR_FILE_PARAM_KEY, csr());
        params.put(CSR_CHECKSUM_PARAM_KEY, csrLocalChecksum());
        params.put(CSR_COUNT_PARAM_KEY, csrCount());
        params.put(CDR_FILE_PARAM_KEY, cdr());
        params.put(CDR_CHECKSUM_PARAM_KEY, cdrLocalChecksum());
        params.put(CDR_COUNT_PARAM_KEY, cdrCount());
        return params;
    }

}
