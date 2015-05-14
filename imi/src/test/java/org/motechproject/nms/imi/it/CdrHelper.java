package org.motechproject.nms.imi.it;

import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.nms.imi.service.SettingsService;
import org.motechproject.nms.kilkari.domain.CallDetailRecord;
import org.motechproject.nms.kilkari.domain.StatusCode;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.CallStatus;
import org.motechproject.nms.props.domain.RequestId;
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

    private List<CallDetailRecord> cdrs;
    private List<CallDetailRecord> retryCdrs = new ArrayList<>();
    private List<String> completedSubscriptionIds = new ArrayList<>();


    public CdrHelper(SettingsService settingsService, SubscriptionService subscriptionService,
                     SubscriberDataService subscriberDataService, LanguageDataService languageDataService,
                     LanguageLocationDataService languageLocationDataService,
                     CircleDataService circleDataService, StateDataService stateDataService,
                     DistrictDataService districtDataService) {

        this.settingsService = settingsService;
        this.subscriptionService = subscriptionService;
        this.subscriberDataService = subscriberDataService;
        this.languageDataService = languageDataService;
        this.languageLocationDataService = languageLocationDataService;
        this.circleDataService = circleDataService;
        this.stateDataService = stateDataService;
        this.districtDataService = districtDataService;

        String date = DateTime.now().toString(TIME_FORMATTER);
        TEST_OBD_FILENAME = String.format("OBD_%s.csv", date);
        TEST_CDR_DETAIL_FILENAME = String.format("cdrDetail_%s", TEST_OBD_FILENAME);
        TEST_CDR_SUMMARY_FILENAME = String.format("cdrSummary_%s", TEST_OBD_FILENAME);
    }


    public void setCrds(List<CallDetailRecord> cdrs) {
        this.cdrs = cdrs;
    }


    public boolean shouldRetryCdr(CallDetailRecord cdr) {
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

    private LanguageLocation makeLanguageLocation() {
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

    private Circle makeCircle() {
        Circle circle = circleDataService.findByName("XX");
        if (circle != null) {
            return circle;
        }

        return circleDataService.create(new Circle("XX"));
    }

    private State makeState() {
        State state = stateDataService.findByCode(1l);
        if (state != null) {
            return state;
        }

        state = new State();
        state.setName("State 1");
        state.setCode(1L);

        return stateDataService.create(state);
    }

    private District makeDistrict() {
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

    private Long makeNumber() {
        return (long) (Math.random() * 9000000000L) + 1000000000L;
    }


    private Subscription makeSubscription(SubscriptionOrigin origin, DateTime startDate) {
        subscriptionService.createSubscriptionPacks();
        Subscriber subscriber = subscriberDataService.create(new Subscriber(
                makeNumber(),
                makeLanguageLocation(),
                makeCircle()
        ));
        SubscriptionPack subscriptionPack = subscriptionService.getSubscriptionPack("childPack");
        Subscription subscription = new Subscription(subscriber, subscriptionPack, origin);
        subscription.setStartDate(startDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription = subscriptionService.create(subscription);
        LOGGER.debug("Created subscription {}", subscription.toString());
        return subscription;
    }


    public List<CallDetailRecord> makeCdrs() {
        //todo: look into that property - does it need to go to kilkari?
        String imiServiceId = settingsService.getSettingsFacade().getProperty("imi.target_file_imi_service_id");
        List<CallDetailRecord> cdrs = new ArrayList<>();

        Subscription subscription = makeSubscription(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));
        CallDetailRecord cdr = new CallDetailRecord(
                new RequestId(subscription.getSubscriptionId(), "somefile.csv").toString(),
                imiServiceId,
                subscription.getSubscriber().getCallingNumber(),
                null,
                0,
                null,
                subscription.getSubscriptionPack().getMessages().get(2).getMessageFileName(),
                subscription.getSubscriptionPack().getMessages().get(2).getWeekId(),
                makeLanguageLocation().getCode(),
                makeCircle().getName(),
                CallStatus.SUCCESS,
                StatusCode.OBD_SUCCESS_CALL_CONNECTED.getValue(),
                1);
        cdrs.add(cdr);

        return cdrs;
    }


    public String obdFileName() {
        return TEST_OBD_FILENAME;
    }


    public String cdrSummaryFileName() {
        return TEST_CDR_SUMMARY_FILENAME;
    }


    public String cdrDetailFileName() {
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


    public void makeCdrSummaryFile() throws IOException {
        File dstFile = new File(makeCdrDirectory(), cdrSummaryFileName());
        LOGGER.debug("Creating summary file {}...", dstFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(dstFile));
        String s;
        for(CallDetailRecord cdr : cdrs) {
            writer.write(cdr.toCsvLine());
            writer.write("\n");
        }

        writer.close();
    }


    public void makeCdrDetailFile() throws IOException {
        File dstFile = new File(makeCdrDirectory(), cdrDetailFileName());
        LOGGER.debug("Creating detail file {}...", dstFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(dstFile));

        //todo:...

        writer.close();
    }


    private String getFileChecksum(File file) throws IOException, NoSuchAlgorithmException {
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader reader = new BufferedReader(isr);
        MessageDigest md = MessageDigest.getInstance("MD5");
        DigestInputStream dis = new DigestInputStream(fis, md);

        String line;
        while ((line = reader.readLine()) != null) { }

        return new String(Hex.encodeHex(md.digest()));
    }


    public String summaryFileChecksum() throws IOException, NoSuchAlgorithmException {
        return getFileChecksum(new File(cdrDirectory(), cdrSummaryFileName()));
    }


    public String detailFileChecksum() throws IOException, NoSuchAlgorithmException {
        return getFileChecksum(new File(cdrDirectory(), cdrDetailFileName()));
    }
}
