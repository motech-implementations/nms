package org.motechproject.nms.testing.it.kilkari;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.alerts.contract.AlertCriteria;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.Alert;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.nms.kilkari.domain.CallRetry;
import org.motechproject.nms.kilkari.domain.CallStage;
import org.motechproject.nms.kilkari.domain.CallSummaryRecord;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.CallSummaryRecordDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.CsrService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.StatusCode;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.LanguageService;
import org.motechproject.nms.testing.it.utils.CsrHelper;
import org.motechproject.nms.testing.it.utils.RegionHelper;
import org.motechproject.nms.testing.it.utils.SubscriptionHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class CsrServiceBundleIT extends BasePaxIT {

    private static final String PROCESS_SUMMARY_RECORD_SUBJECT = "nms.imi.kk.process_summary_record";
    private static final String CSR_PARAM_KEY = "csr";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    @Inject
    CsrService csrService;
    @Inject
    SubscriptionService subscriptionService;
    @Inject
    SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    SubscriptionDataService subscriptionDataService;
    @Inject
    SubscriberDataService subscriberDataService;
    @Inject
    LanguageDataService languageDataService;
    @Inject
    LanguageService languageService;
    @Inject
    CallRetryDataService callRetryDataService;
    @Inject
    CallSummaryRecordDataService csrDataService;
    @Inject
    CircleDataService circleDataService;
    @Inject
    StateDataService stateDataService;
    @Inject
    DistrictDataService districtDataService;
    @Inject
    DistrictService districtService;
    @Inject
    TestingService testingService;
    @Inject
    AlertService alertService;


    private RegionHelper rh;
    private SubscriptionHelper sh;


    @Before
    public void doTheNeedful() {

        testingService.clearDatabase();

        rh = new RegionHelper(languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, districtService);

        sh = new SubscriptionHelper(subscriptionService, subscriberDataService, subscriptionPackDataService,
                languageDataService, languageService, circleDataService, stateDataService, districtDataService,
                districtService);

        sh.childPack();
        sh.pregnancyPack();

    }


    @Test
    public void testServicePresent() {
        assertTrue(csrService != null);
    }


    private Map<Integer, Integer> makeStatsMap(StatusCode statusCode, int count) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(statusCode.getValue(), count);
        return map;
    }


    private void processCsr(CallSummaryRecordDto csr) {
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, CallSummaryRecordDto.toParams(csr));
        csrService.processCallSummaryRecord(motechEvent);
    }


    @Test
    public void verifyServiceFunctional() {
        Subscription subscription = sh.mksub(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));

        CallSummaryRecordDto csr = new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_NOANSWER,
                FinalCallStatus.FAILED,
                "w1_1.wav",
                "w1_1",
                rh.hindiLanguage(),
                rh.delhiCircle()
        );

        processCsr(csr);
    }


    @Test
    public void verifyInvalidWeekId() {
        Subscription subscription = sh.mksub(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));
        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_NOANSWER,
                FinalCallStatus.FAILED,
                "w1_1.wav",
                null,
                rh.hindiLanguage(),
                rh.delhiCircle()
        ));

        AlertCriteria criteria = new AlertCriteria().byExternalId(subscription.getSubscriptionId());
        List<Alert> alerts = alertService.search(criteria);
        assertEquals(1, alerts.size());
        assertEquals(AlertType.HIGH, alerts.get(0).getAlertType());
    }


    @Test
    public void verifyInvalidFilename() {
        Subscription subscription = sh.mksub(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));
        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_NOANSWER,
                FinalCallStatus.FAILED,
                null,
                "w1_1",
                rh.hindiLanguage(),
                rh.delhiCircle()
        ));

        AlertCriteria criteria = new AlertCriteria().byExternalId(subscription.getSubscriptionId());
        List<Alert> alerts = alertService.search(criteria);
        assertEquals(1, alerts.size());
        assertEquals(AlertType.HIGH, alerts.get(0).getAlertType());
    }


    @Test
    public void verifyInvalidCircle() {
        Subscription subscription = sh.mksub(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));
        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_NOANSWER,
                FinalCallStatus.FAILED,
                "w1_1.wav",
                "w1_1",
                rh.hindiLanguage(),
                null
        ));

        AlertCriteria criteria = new AlertCriteria().byExternalId(subscription.getSubscriptionId());
        List<Alert> alerts = alertService.search(criteria);
        assertEquals(1, alerts.size());
        assertEquals(AlertType.HIGH, alerts.get(0).getAlertType());
    }


    @Test
    public void verifyInvalidLanguage() {
        Subscription subscription = sh.mksub(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));
        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_NOANSWER,
                FinalCallStatus.FAILED,
                "w1_1.wav",
                "w1_1",
                null,
                rh.delhiCircle()
        ));

        AlertCriteria criteria = new AlertCriteria().byExternalId(subscription.getSubscriptionId());
        List<Alert> alerts = alertService.search(criteria);
        assertEquals(1, alerts.size());
        assertEquals(AlertType.HIGH, alerts.get(0).getAlertType());
    }


    // Deactivate if user phone number does not exist
    // https://github.com/motech-implementations/mim/issues/169
    @Test
    public void verifyIssue169() {
        Subscription subscription = sh.mksub(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));
        Subscriber subscriber = subscription.getSubscriber();

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_INVALIDNUMBER,
                FinalCallStatus.FAILED,
                "w1_1.wav",
                "w1_1",
                rh.hindiLanguage(),
                rh.delhiCircle()
        ));

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_INVALIDNUMBER,
                FinalCallStatus.FAILED,
                "w1_1.wav",
                "w1_1",
                rh.hindiLanguage(),
                rh.delhiCircle()
        ));

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_INVALIDNUMBER,
                FinalCallStatus.FAILED,
                "w1_1.wav",
                "w1_1",
                rh.hindiLanguage(),
                rh.delhiCircle()
        ));

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_INVALIDNUMBER,
                FinalCallStatus.FAILED,
                "w1_1.wav",
                "w1_1",
                rh.hindiLanguage(),
                rh.delhiCircle()
        ));

        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.INVALID_NUMBER, subscription.getDeactivationReason());
    }


    @Test
    public void verifySubscriptionCompletion() {

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        CsrHelper helper = new CsrHelper(timestamp, subscriptionService, subscriptionPackDataService,
                subscriberDataService, languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, districtService);

        helper.makeRecords(1, 3, 0, 0);

        for (CallSummaryRecordDto record : helper.getRecords()) {
            processCsr(record);
        }

        List<Subscription> subscriptions = subscriptionDataService.findByStatus(SubscriptionStatus.COMPLETED);
        assertEquals(3, subscriptions.size());
    }


    /**
     * To check that NMS shall not retry OBD message for which all OBD attempts(1 actual+3 retry) fails with
     * single message per week configuration.
     */
    @Test
    public void verifyFT140() {
        DateTime now = DateTime.now();

        // Create a record in the CallRetry table marked as "last try" and verify it is erased from the
        // CallRetry table
        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, now.minusDays(3),
                SubscriptionPackType.CHILD);

        String contentFileName = sh.getContentMessageFile(subscription, 0);
        String weekId = sh.getWeekId(subscription, 0);
        CallRetry retry = callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                CallStage.RETRY_LAST,
                contentFileName,
                weekId,
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                SubscriptionOrigin.MCTS_IMPORT
        ));


        Map<Integer, Integer> callStats = new HashMap<>();
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_NOANSWER,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()
        );

        processCsr(record);

        // There should be no calls to retry since the one above was the last try
        assertEquals(0, callRetryDataService.count());
    }


    /**
     * Verify callRetry record is deleted after a failed, but eventually successful message
     */
    @Test
    public void verifyNIP194() {
        DateTime now = DateTime.now();

        // Create a record in the CallRetry table marked as RETRY_2
        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, now.minusDays(3),
                SubscriptionPackType.CHILD);

        String contentFileName = sh.getContentMessageFile(subscription, 0);
        String weekId = sh.getWeekId(subscription, 0);
        CallRetry retry = callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                CallStage.RETRY_2,
                contentFileName,
                weekId,
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                SubscriptionOrigin.MCTS_IMPORT
        ));


        Map<Integer, Integer> callStats = new HashMap<>();
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_SUCCESS_CALL_CONNECTED,
                FinalCallStatus.SUCCESS,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()
        );

        processCsr(record);

        // There should be no calls to retry since the one above was the last try
        assertEquals(0, callRetryDataService.count());
    }


    @Test
    public void verifyFT144() {

        DateTime now = DateTime.now();

        // To check that NMS shall retry the OBD messages which failed as IVR did not attempt OBD for those messages.

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, now.minusDays(3),
                SubscriptionPackType.CHILD);
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        String weekId = sh.getWeekId(subscription, 0);
        CallRetry retry = callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                CallStage.RETRY_1,
                contentFileName,
                weekId,
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                SubscriptionOrigin.MCTS_IMPORT
        ));


        CallSummaryRecordDto record = new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_NOATTEMPT,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()
        );

        processCsr(record);


        // There should be one calls to retry since the one above was the last failed with No attempt
        assertEquals(1, callRetryDataService.count());

        List<CallRetry> retries = callRetryDataService.retrieveAll();

        assertEquals(subscription.getSubscriptionId(), retries.get(0).getSubscriptionId());
        assertEquals(CallStage.RETRY_2, retries.get(0).getCallStage());
    }



    @Test
    public void verifyFT149() {

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        // To check that NMS shall not retry the OBD messages which failed due to user number in dnd.

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),SubscriptionPackType.CHILD);
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        String weekId = sh.getWeekId(subscription, 0);


        Map<Integer, Integer> callStats = new HashMap<>();
        callStats.put(StatusCode.OBD_DNIS_IN_DND.getValue(), 1);
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_DNIS_IN_DND,
                FinalCallStatus.REJECTED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()
        );

        processCsr(record);

        // There should be no calls to retry since the one above was the last rejected with DND reason
        assertEquals(0, callRetryDataService.count());
        subscription = subscriptionService.getSubscription(subscription.getSubscriptionId());
        assertTrue(SubscriptionStatus.DEACTIVATED == subscription.getStatus());
    }

    @Test
    public void verifyFT150() {

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        // To check that NMS shall retry the OBD messages which failed due to OBD_FAILED_OTHERS.

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now());
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        String weekId = sh.getWeekId(subscription, 0);

        Map<Integer, Integer> callStats = new HashMap<>();
        callStats.put(StatusCode.OBD_FAILED_OTHERS.getValue(),1);
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_OTHERS,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()
        );

        processCsr(record);

        // There should be one call to retry since the one above call was rescheduled.
        assertEquals(1, callRetryDataService.count());
        List<CallRetry> retries = callRetryDataService.retrieveAll();

        assertEquals(subscription.getSubscriptionId(), retries.get(0).getSubscriptionId());
        assertEquals(CallStage.RETRY_1, retries.get(0).getCallStage());
    }

    @Test
    public void verifyFT141() {

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        // To check that NMS shall retry OBD message for which delivery fails for the first time with two message per
        // week configuration.

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),
                SubscriptionPackType.PREGNANCY);
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        String weekId = sh.getWeekId(subscription, 0);

        csrDataService.create(new CallSummaryRecord(
                subscription.getSubscriptionId(),
                contentFileName,
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                weekId,
                StatusCode.OBD_FAILED_BUSY,
                FinalCallStatus.FAILED,
                0
        ));

        CallSummaryRecordDto record = new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_BUSY,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()
        );

        processCsr(record);

        assertEquals(1, callRetryDataService.count());

        List<CallRetry> retries = callRetryDataService.retrieveAll();

        assertEquals(subscription.getSubscriptionId(), retries.get(0).getSubscriptionId());
        assertEquals(CallStage.RETRY_1, retries.get(0).getCallStage());
    }


    /**
     * To check that NMS shall retry OBD message for which first OBD retry fails
     * with single message per week configuration..
     */
    @Test
    public void verifyFT138() {
        DateTime now = DateTime.now();

        // Create a record in the CallRetry table marked as "retry_1" and verify it is updated as "retry_2" in
        // CallRetry table

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, now.minusDays(3));
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        String weekId = sh.getWeekId(subscription, 0);
        CallRetry retry = callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                CallStage.RETRY_1,
                contentFileName,
                weekId,
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                SubscriptionOrigin.MCTS_IMPORT
        ));


        Map<Integer, Integer> callStats = new HashMap<>();
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_NOANSWER,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()
        );

        processCsr(record);

        // There should be one calls to retry since the retry 1 was failed.
        assertEquals(1, callRetryDataService.count());

        List<CallRetry> retries = callRetryDataService.retrieveAll();

        assertEquals(subscription.getSubscriptionId(), retries.get(0).getSubscriptionId());
        assertEquals(CallStage.RETRY_2, retries.get(0).getCallStage());
    }


    /**
     * To check that NMS shall retry OBD message for which second OBD retry fails with
     * single message per week configuration.
     */
    @Test
    public void verifyFT139() {
        DateTime now = DateTime.now();

        // Create a record in the CallRetry table marked as "retry_2" and verify it is updated as "retry_last" in
        // CallRetry table

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, now.minusDays(3));
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        String weekId = sh.getWeekId(subscription, 0);
        CallRetry retry = callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                CallStage.RETRY_2,
                contentFileName,
                weekId,
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                SubscriptionOrigin.MCTS_IMPORT
        ));


        Map<Integer, Integer> callStats = new HashMap<>();
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_NOANSWER,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()
        );

        processCsr(record);

        // There should be one calls to retry since the retry 2 was failed.
        assertEquals(1, callRetryDataService.count());

        List<CallRetry> retries = callRetryDataService.retrieveAll();

        assertEquals(subscription.getSubscriptionId(), retries.get(0).getSubscriptionId());
        assertEquals(CallStage.RETRY_LAST, retries.get(0).getCallStage());
    }

    /**
     * To verify that beneficiary(via mcts import) will be  deactivated if he/she
     * has MSISDN number added to the DND database.
     */
    @Test
    public void verifyFT177() {

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(14));
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        String weekId = sh.getWeekId(subscription, 0);

        CallSummaryRecordDto csr = new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_DNIS_IN_DND,
                FinalCallStatus.REJECTED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()
        );

        processCsr(csr);

        // verify that subscription created via MCTS-import is still Deactivated with reason "do not disturb"
        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.DO_NOT_DISTURB, subscription.getDeactivationReason());
    }


    /*
    * NMS_FT_163
    * To verify pregnancyPack Pack created via IVR, shouldn't get deactivated due to reason DND.
    */
    @Test
    public void verifyFT163() {

        Subscription subscription = sh.mksub(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        String weekId = sh.getWeekId(subscription, 0);

        CallSummaryRecordDto csr = new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_DNIS_IN_DND,
                FinalCallStatus.REJECTED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()
        );

        processCsr(csr);

        // verify that subscription created via IVR is still Active
        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
    }


    /**
     * To verify that childPack beneficiary should not be  deactivated if the error “user
     * number does not exist” is not received for all failed delivery attempts during a scheduling
     * period for a message.
     */
    @Test
    public void verifyFT176() {

        Subscription subscription = sh.mksub(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        String weekId = sh.getWeekId(subscription, 0);

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_INVALIDNUMBER,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()));

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_INVALIDNUMBER,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()));

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_INVALIDNUMBER,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()));

        // verify that subscription is still Active
        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        // verify that call is rescheduled for next retry.
        assertEquals(1, callRetryDataService.count());

        List<CallRetry> retries = callRetryDataService.retrieveAll();

        assertEquals(subscription.getSubscriptionId(), retries.get(0).getSubscriptionId());
        assertEquals(CallStage.RETRY_LAST, retries.get(0).getCallStage());

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_SWITCHEDOFF,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()));

        // verify that subscription is still Active, it is not deactivated because call was not failed
        // due to invalid number for all retries.
        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
    }


    /**
     * To check that NMS shall not retry OBD message for which all OBD attempts(1 actual+1 retry) fails with
     * two message per week configuration.
     */
    @Test
    public void verifyFT142() {

        DateTime now = DateTime.now();

        // Create a record in the CallRetry table marked as "retry 1" and verify it is erased from the
        // CallRetry table

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, now.minusDays(3),
                SubscriptionPackType.PREGNANCY);
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        String weekId = sh.getWeekId(subscription, 0);
        CallRetry retry = callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                CallStage.RETRY_1,
                contentFileName,
                weekId,
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                SubscriptionOrigin.MCTS_IMPORT
        ));


        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_NOANSWER,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()));

        // There should be no calls to retry since the one above was the last try
        assertEquals(0, callRetryDataService.count());
    }


    /*
    * To verify that childPack Subscription should not be marked completed after just first retry.
    */
    @Test
    public void verifyFT187() {
        DateTime now = DateTime.now();

        int days = sh.childPack().getWeeks() * 7;
        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, now.minusDays(days),
                SubscriptionPackType.CHILD);
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        String weekId = sh.getWeekId(subscription, 0);

        callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                CallStage.RETRY_1,
                contentFileName,
                weekId,
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                SubscriptionOrigin.MCTS_IMPORT
        ));

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_SWITCHEDOFF,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()));

        // There should be one calls to retry since the retry 2 was failed.
        assertEquals(1, callRetryDataService.count());

        List<CallRetry> retries = callRetryDataService.retrieveAll();

        assertEquals(subscription.getSubscriptionId(), retries.get(0).getSubscriptionId());
        assertEquals(CallStage.RETRY_2, retries.get(0).getCallStage());

        // verify that subscription is still Active, as last message was not delivered successfully and retries
        // are left
        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
    }


    /**
     To verify that pregnancyPack beneficiary will be  deactivated if the error “user number does not exist” is
     received for all delivery attempts during a scheduling period for a message.
     */
    @Test
    public void verifyFT188() {

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),
                SubscriptionPackType.PREGNANCY);
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        String weekId = sh.getWeekId(subscription, 0);

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_INVALIDNUMBER,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()));

        // verify that subscription is still Active
        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        // verify that call is rescheduled for next retry.
        assertEquals(1, callRetryDataService.count());

        List<CallRetry> retries = callRetryDataService.retrieveAll();

        assertEquals(subscription.getSubscriptionId(), retries.get(0).getSubscriptionId());
        assertEquals(CallStage.RETRY_1, retries.get(0).getCallStage());

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_INVALIDNUMBER,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()));


        // verify that subscription is deactivated, call was failed due to invalid number for all attempts.
        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.INVALID_NUMBER, subscription.getDeactivationReason());
    }


    /**
     *To verify that pregnancyPack beneficiary should not be deactivated if the error “user number does
     *not exist” is not received for all failed delivery attempts during a scheduling period for a message.
     */
    @Test
    public void verifyFT189() {

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),
                SubscriptionPackType.PREGNANCY);
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        String weekId = sh.getWeekId(subscription, 0);

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_SWITCHEDOFF,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()));

        // verify that subscription is still Active
        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        // verify that call is rescheduled for next retry.
        assertEquals(1, callRetryDataService.count());

        List<CallRetry> retries = callRetryDataService.retrieveAll();

        assertEquals(subscription.getSubscriptionId(), retries.get(0).getSubscriptionId());
        assertEquals(CallStage.RETRY_1, retries.get(0).getCallStage());

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_FAILED_INVALIDNUMBER,
                FinalCallStatus.FAILED,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()));

        // verify that subscription is still Active, it is not deactivated because call was not failed
        // due to invalid number for all retries.
        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
    }


    /*
    *To verify 72Weeks Pack is marked completed after the Service Pack runs for its scheduled duration.
    */
    @Test
    public void verifyFT165() {
        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        int days = sh.pregnancyPack().getWeeks() * 7;
        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(days),
                SubscriptionPackType.PREGNANCY);
        int index = sh.getLastMessageIndex(subscription);
        String contentFileName = sh.getContentMessageFile(subscription, index);
        String weekId = sh.getWeekId(subscription, index);

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_SUCCESS_CALL_CONNECTED,
                FinalCallStatus.SUCCESS,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()));

        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertTrue(SubscriptionStatus.COMPLETED == subscription.getStatus());
    }

    /*
    * To verify 72Weeks Pack is marked completed after the Service Pack runs for its scheduled
    * duration including one retry.
    */
    @Test
    public void verifyFT167() {
        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        int days = sh.pregnancyPack().getWeeks() * 7;
        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(days),
                SubscriptionPackType.PREGNANCY);
        int index = sh.getLastMessageIndex(subscription);
        String contentFileName = sh.getContentMessageFile(subscription, index);
        String weekId = sh.getWeekId(subscription, index);

        callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                CallStage.RETRY_1,
                contentFileName,
                weekId,
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                SubscriptionOrigin.MCTS_IMPORT
        ));

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_SUCCESS_CALL_CONNECTED,
                FinalCallStatus.SUCCESS,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()));

        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertTrue(SubscriptionStatus.COMPLETED == subscription.getStatus());

        // verify call retry entry is also deleted from the database
        CallRetry retry = callRetryDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertNull(retry);
    }

    /*
    * To verify 48Weeks Pack is marked completed after the Service Pack runs for its scheduled
    * duration including one retry.
    */
    @Test
    public void verifyFT168() {
        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        int days = sh.childPack().getWeeks() * 7;
        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(days),
                SubscriptionPackType.CHILD);
        int index = sh.getLastMessageIndex(subscription);
        String contentFileName = sh.getContentMessageFile(subscription, index);
        String weekId = sh.getWeekId(subscription, index);

        callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                CallStage.RETRY_1,
                contentFileName,
                weekId,
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                SubscriptionOrigin.MCTS_IMPORT
        ));

        processCsr(new CallSummaryRecordDto(
                subscription,
                StatusCode.OBD_SUCCESS_CALL_CONNECTED,
                FinalCallStatus.SUCCESS,
                contentFileName,
                weekId,
                rh.hindiLanguage(),
                rh.delhiCircle()));

        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertTrue(SubscriptionStatus.COMPLETED == subscription.getStatus());

        // verify call retry entry is also deleted from the database
        CallRetry retry = callRetryDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertNull(retry);
    }


    @Test
    public void verifyNMS215() {

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        CsrHelper helper = new CsrHelper(timestamp, subscriptionService, subscriptionPackDataService,
                subscriberDataService, languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, districtService);

        helper.makeRecords(1, 0, 0, 0);

        for (CallSummaryRecordDto record : helper.getRecords()) {

        processCsr(record);

        }

        List<Subscription> subscriptions = subscriptionDataService.retrieveAll();
        assertEquals(1, subscriptions.size());
        assertEquals(false,subscriptions.get(0).getNeedsWelcomeMessageViaObd());
    }
}
