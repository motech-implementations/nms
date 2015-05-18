package org.motechproject.nms.imi.it;

import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.nms.imi.domain.FileAuditRecord;
import org.motechproject.nms.imi.domain.FileType;
import org.motechproject.nms.imi.repository.FileAuditRecordDataService;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.dto.CallDetailRecordDto;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.CallDisconnectReason;
import org.motechproject.nms.props.domain.RequestId;
import org.motechproject.nms.props.domain.StatusCode;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class CdrHelper {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private static final Logger LOGGER = LoggerFactory.getLogger(CdrHelper.class);

    private static final int CHILD_PACK_WEEKS = 48;
    private final String TEST_OBD_TIMESTAMP;
    private final String TEST_OBD_FILENAME;
    private final String TEST_CDR_DETAIL_FILENAME;
    private final String TEST_CDR_SUMMARY_FILENAME;

    private SettingsService settingsService;
    private SubscriptionService subscriptionService;
    private SubscriberDataService subscriberDataService;
    private LanguageDataService languageDataService;
    private LanguageLocationDataService languageLocationDataService;
    private CircleDataService circleDataService;
    private StateDataService stateDataService;
    private DistrictDataService districtDataService;
    private FileAuditRecordDataService fileAuditRecordDataService;

    private List<CallDetailRecordDto> cdrs;
    private List<CallDetailRecordDto> retryCdrs = new ArrayList<>();
    private List<String> completedSubscriptionIds = new ArrayList<>();


    public CdrHelper(SettingsService settingsService, SubscriptionService subscriptionService,
                     SubscriberDataService subscriberDataService, LanguageDataService languageDataService,
                     LanguageLocationDataService languageLocationDataService,
                     CircleDataService circleDataService, StateDataService stateDataService,
                     DistrictDataService districtDataService,
                     FileAuditRecordDataService fileAuditRecordDataService) {

        this.settingsService = settingsService;
        this.subscriptionService = subscriptionService;
        this.subscriberDataService = subscriberDataService;
        this.languageDataService = languageDataService;
        this.languageLocationDataService = languageLocationDataService;
        this.circleDataService = circleDataService;
        this.stateDataService = stateDataService;
        this.districtDataService = districtDataService;
        this.fileAuditRecordDataService = fileAuditRecordDataService;

        TEST_OBD_TIMESTAMP = DateTime.now().toString(TIME_FORMATTER);
        TEST_OBD_FILENAME = String.format("OBD_%s.csv", TEST_OBD_TIMESTAMP);
        TEST_CDR_DETAIL_FILENAME = String.format("cdrDetail_%s", TEST_OBD_FILENAME);
        TEST_CDR_SUMMARY_FILENAME = String.format("cdrSummary_%s", TEST_OBD_FILENAME);
    }


    public void setCrds(List<CallDetailRecordDto> cdrs) {
        this.cdrs = cdrs;
    }


    public List<CallDetailRecordDto> getCrds() {
        return cdrs;
    }


    public boolean shouldRetryCdr(CallDetailRecordDto cdr) {
        return retryCdrs.contains(cdr);
    }


    public boolean isSubscriptionCompleted(String subscriptionId) {
        return completedSubscriptionIds.contains(subscriptionId);
    }


    private Language makeLanguage() {
        Language language = languageDataService.findByName("Hindi");
        if (language != null) {
            return language;
        }
        return languageDataService.create(new Language("Hindi"));
    }

    public LanguageLocation makeLanguageLocation() {
        LanguageLocation languageLocation = languageLocationDataService.findByCode("99");
        if (languageLocation != null) {
            return languageLocation;
        }

        Language language = makeLanguage();
        Circle circle = makeCircle();

        languageLocation = new LanguageLocation("99", circle, language, false);
        languageLocation.getDistrictSet().add(makeDistrict());
        return languageLocationDataService.create(languageLocation);
    }

    public SubscriptionPack getChildPack() {
        subscriptionService.createSubscriptionPacks();
        return subscriptionService.getSubscriptionPack("childPack");
    }

    public Circle makeCircle() {
        Circle circle = circleDataService.findByName("XX");
        if (circle != null) {
            return circle;
        }

        return circleDataService.create(new Circle("XX"));
    }

    public State makeState() {
        State state = stateDataService.findByCode(1l);
        if (state != null) {
            return state;
        }

        state = new State();
        state.setName("State 1");
        state.setCode(1L);

        return stateDataService.create(state);
    }

    public District makeDistrict() {
        District district = districtDataService.findById(1L);
        if (district != null) {
            return district;
        }

        district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);
        district.setState(makeState());

        return districtDataService.create(district);
    }

    public Long makeNumber() {
        return (long) (Math.random() * 9000000000L) + 1000000000L;
    }


    public Subscription makeSubscription(SubscriptionOrigin origin, DateTime startDate) {
        subscriptionService.createSubscriptionPacks();
        Subscriber subscriber = subscriberDataService.create(new Subscriber(
                makeNumber(),
                makeLanguageLocation(),
                makeCircle()
        ));
        Subscription subscription = new Subscription(subscriber, getChildPack(), origin);
        subscription.setStartDate(startDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription = subscriptionService.create(subscription);
        LOGGER.debug("Created subscription {}", subscription.toString());
        return subscription;
    }


    private CallDetailRecordDto makeCdr(Subscription sub) {
        CallDetailRecordDto cdr = new CallDetailRecordDto();
        cdr.setRequestId(new RequestId(sub.getSubscriptionId(), timestamp()));
        cdr.setMsisdn(sub.getSubscriber().getCallingNumber());
        cdr.setCallAnswerTime(DateTime.now().minusHours(5));
        cdr.setMsgPlayDuration(110 + (int) (Math.random() * 20));
        cdr.setLanguageLocationId(makeLanguageLocation().getCode());
        cdr.setCircleId(makeCircle().getName());
        cdr.setOperatorId("xx");
        return cdr;
    }


    public void makeCdrs(int numSuccess, int numFailed, int numComplete, int numIvr) {
        cdrs = new ArrayList<>();

        for (int i=0 ; i<numSuccess ; i++) {
            Subscription sub = makeSubscription(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(30));
            CallDetailRecordDto cdr = makeCdr(sub);
            cdr.setStatusCode(StatusCode.OBD_SUCCESS_CALL_CONNECTED);
            cdr.setContentFile(getChildPack().getMessages().get(5).getMessageFileName());
            cdr.setCallDisconnectReason(CallDisconnectReason.NORMAL_DROP);
            cdr.setWeekId("w5_1");
            cdrs.add(cdr);
        }

        for (int i=0 ; i<numFailed ; i++) {
            Subscription sub = makeSubscription(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(30));
            CallDetailRecordDto cdr = makeCdr(sub);
            cdr.setStatusCode(StatusCode.OBD_SUCCESS_CALL_CONNECTED);
            cdr.setContentFile(getChildPack().getMessages().get(5).getMessageFileName());
            cdr.setCallDisconnectReason(CallDisconnectReason.NORMAL_DROP);
            cdr.setWeekId("w5_1");
            cdrs.add(cdr);
        }

        for (int i=0 ; i<numComplete ; i++) {
            int days = CHILD_PACK_WEEKS * 7;
            Subscription sub = makeSubscription(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(days));
            CallDetailRecordDto cdr = makeCdr(sub);
            cdr.setStatusCode(StatusCode.OBD_SUCCESS_CALL_CONNECTED);
            cdr.setContentFile(getChildPack().getMessages().get(CHILD_PACK_WEEKS-1).getMessageFileName());
            cdr.setCallDisconnectReason(CallDisconnectReason.NORMAL_DROP);
            cdr.setWeekId(String.format("w%d_1", CHILD_PACK_WEEKS));
            cdrs.add(cdr);
        }

        for (int i=0 ; i<numIvr ; i++) {
            Subscription sub = makeSubscription(SubscriptionOrigin.IVR, DateTime.now().minusDays(30));
            CallDetailRecordDto cdr = makeCdr(sub);
            cdr.setStatusCode(StatusCode.OBD_SUCCESS_CALL_CONNECTED);
            cdr.setContentFile(getChildPack().getMessages().get(5).getMessageFileName());
            cdr.setCallDisconnectReason(CallDisconnectReason.NORMAL_DROP);
            cdr.setWeekId("w5_1");
            cdrs.add(cdr);
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


    public File cdrDirectory() {
        File userDir = new File(System.getProperty("user.home"));
        String cdrDirProp = settingsService.getSettingsFacade().getProperty("imi.cdr_file_directory");
        return new File(userDir, cdrDirProp);
    }


    public File makeCdrDirectory() throws IOException {
        File dstDirectory = cdrDirectory();
        if (dstDirectory.mkdirs()) {
            LOGGER.debug("Created required directories for {}", dstDirectory);
        } else {
            LOGGER.debug("Required directories all exist for {}", dstDirectory);
        }
        return dstDirectory;
    }


    public int cdrCount() {
        return cdrs.size();
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


    public void makeCsr() throws IOException {
        File dstFile = new File(makeCdrDirectory(), csr());
        LOGGER.debug("Creating summary file {}...", dstFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(dstFile));

        //todo:...

        writer.close();
    }


    public void createCdrFileAuditRecord(boolean valid, boolean success) throws IOException, NoSuchAlgorithmException {
        fileAuditRecordDataService.create(new FileAuditRecord(
                FileType.CDR_DETAIL_FILE,
                valid ? cdr() : "xxx",
                success ? "OK" : "ERROR",
                cdrCount(),
                csrChecksum()
        ));
    }


    public void createObdFileAuditRecord(boolean valid, boolean success) throws IOException, NoSuchAlgorithmException {
        fileAuditRecordDataService.create(new FileAuditRecord(
                FileType.TARGET_FILE,
                valid ? obd() : "xxx",
                success ? "OK" : "ERROR",
                123, //todo
                "foobar" //todo
        ));
    }


    public void makeCdr() throws IOException {
        File dstFile = new File(makeCdrDirectory(), cdr());
        LOGGER.debug("Creating detail file {}...", dstFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(dstFile));

        for(CallDetailRecordDto cdr : cdrs) {
            writer.write(csvLineFromCdr(cdr));
            writer.write("\n");
        }

        writer.close();
    }


    private String checksum(File file) throws IOException, NoSuchAlgorithmException {
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader reader = new BufferedReader(isr);
        MessageDigest md = MessageDigest.getInstance("MD5");
        DigestInputStream dis = new DigestInputStream(fis, md);

        String line;
        while ((line = reader.readLine()) != null) { }

        return new String(Hex.encodeHex(md.digest()));
    }


    public String csrChecksum() throws IOException, NoSuchAlgorithmException {
        return checksum(new File(cdrDirectory(), csr()));
    }


    public String cdrChecksum() throws IOException, NoSuchAlgorithmException {
        return checksum(new File(cdrDirectory(), cdr()));
    }
}
