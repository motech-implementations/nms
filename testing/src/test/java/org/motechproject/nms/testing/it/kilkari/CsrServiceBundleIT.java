package org.motechproject.nms.testing.it.kilkari;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.mds.config.SettingsService;
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
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.RequestId;
import org.motechproject.nms.props.domain.StatusCode;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
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
    EventRelay eventRelay;

    @Inject
    CsrService csrService;

    @Inject
    private SettingsService settingsService;

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;

    @Inject
    private SubscriptionDataService subscriptionDataService;

    @Inject
    private SubscriberDataService subscriberDataService;

    @Inject
    private LanguageDataService languageDataService;

    @Inject
    private CallRetryDataService callRetryDataService;

    @Inject
    private CallSummaryRecordDataService csrDataService;

    @Inject
    private CircleDataService circleDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DistrictDataService districtDataService;

    @Inject
    private TestingService testingService;


    private RegionHelper rh;
    private SubscriptionHelper sh;


    @Before
    public void setupTestData() {
        testingService.clearDatabase();

        rh = new RegionHelper(languageDataService, circleDataService, stateDataService,
                districtDataService);

        sh = new SubscriptionHelper(subscriptionService,
                subscriberDataService, subscriptionPackDataService, languageDataService, circleDataService,
                stateDataService, districtDataService);

        sh.childPack();
        sh.pregnancyPack();
        csrService.buildMessageDurationCache();
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


    @Test
    public void verifyServiceFunctional() {
        Subscription subscription = sh.mksub(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));

        CallSummaryRecordDto csr = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), "11112233445566"),
                subscription.getSubscriber().getCallingNumber(),
                "w1_1.wav",
                "w1_1",
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                FinalCallStatus.FAILED,
                makeStatsMap(StatusCode.OBD_FAILED_INVALIDNUMBER, 3),
                0,
                3
        );

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, csr);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);
    }


    // Deactivate if user phone number does not exist
    // https://github.com/motech-implementations/mim/issues/169
    @Test
    public void verifyIssue169() {
        Subscription subscription = sh.mksub(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));
        Subscriber subscriber = subscription.getSubscriber();

        csrDataService.create(new CallSummaryRecord(
                new RequestId(subscription.getSubscriptionId(), "11112233445566").toString(),
                subscription.getSubscriber().getCallingNumber(),
                "w1_1.wav",
                "w1_1",
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                FinalCallStatus.FAILED,
                makeStatsMap(StatusCode.OBD_FAILED_INVALIDNUMBER, 10),
                0,
                10,
                3
        ));

        callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                DayOfTheWeek.today(),
                CallStage.RETRY_LAST,
                "w1_1.wav",
                "w1_1",
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                SubscriptionOrigin.MCTS_IMPORT
        ));

        CallSummaryRecordDto csr = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), "11112233445566"),
                subscription.getSubscriber().getCallingNumber(),
                "w1_1.wav",
                "w1_1",
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                FinalCallStatus.FAILED,
                makeStatsMap(StatusCode.OBD_FAILED_INVALIDNUMBER, 3),
                0,
                3
        );

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, csr);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);

        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.INVALID_NUMBER, subscription.getDeactivationReason());
    }


    @Test
    public void verifySubscriptionCompletion() {

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        CsrHelper helper = new CsrHelper(timestamp, subscriptionService, subscriptionPackDataService,
                subscriberDataService, languageDataService, circleDataService, stateDataService,
                districtDataService);

        helper.makeRecords(1, 3, 0, 0);

        for (CallSummaryRecordDto record : helper.getRecords()) {
            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(CSR_PARAM_KEY, record);
            MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
            csrService.processCallSummaryRecord(motechEvent);
        }

        List<Subscription> subscriptions = subscriptionDataService.findByStatus(SubscriptionStatus.COMPLETED);
        assertEquals(3, subscriptions.size());
    }


    @Test
    public void verifyFT140() {
        /**
         * To check that NMS shall not retry OBD message for which all OBD attempts(1 actual+3 retry) fails with
         * single message per week configuration.
         */

            String timestamp = DateTime.now().toString(TIME_FORMATTER);

        // Create a record in the CallRetry table marked as "last try" and verify it is erased from the
        // CallRetry table

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(3),
                SubscriptionPackType.CHILD);
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        CallRetry retry = callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                DayOfTheWeek.today(),
                CallStage.RETRY_LAST,
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                SubscriptionOrigin.MCTS_IMPORT
        ));


        Map<Integer, Integer> callStats = new HashMap<>();
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), timestamp),
                subscription.getSubscriber().getCallingNumber(),
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                FinalCallStatus.FAILED,
                callStats,
                0,
                5
        );


        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, record);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);

        // There should be no calls to retry since the one above was the last try
        assertEquals(0, callRetryDataService.count());
    }


    @Test
    public void verifyFT144() {

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        // To check that NMS shall retry the OBD messages which failed as IVR did not attempt OBD for those messages.

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),SubscriptionPackType.CHILD);
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        CallRetry retry = callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                DayOfTheWeek.today(),
                CallStage.RETRY_1,
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                SubscriptionOrigin.MCTS_IMPORT
        ));


        Map<Integer, Integer> callStats = new HashMap<>();
        callStats.put(StatusCode.OBD_FAILED_NOATTEMPT.getValue(),1);
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), timestamp),
                subscription.getSubscriber().getCallingNumber(),
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                FinalCallStatus.FAILED,
                callStats,
                0,
                1
        );


        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, record);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);

        // There should be one calls to retry since the one above was the last failed with No attempt
        assertEquals(1, callRetryDataService.count());

        List<CallRetry> retries = callRetryDataService.retrieveAll();

        assertEquals(subscription.getSubscriptionId(), retries.get(0).getSubscriptionId());
        assertEquals(CallStage.RETRY_2, retries.get(0).getCallStage());
        assertEquals(DayOfTheWeek.today().nextDay(),retries.get(0).getDayOfTheWeek());

    }



    @Test
    public void verifyFT149() {

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        // To check that NMS shall not retry the OBD messages which failed due to user number in dnd.

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),SubscriptionPackType.CHILD);
        String contentFileName = sh.getContentMessageFile(subscription, 0);


        Map<Integer, Integer> callStats = new HashMap<>();
        callStats.put(StatusCode.OBD_DNIS_IN_DND.getValue(), 1);
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), timestamp),
                subscription.getSubscriber().getCallingNumber(),
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                FinalCallStatus.REJECTED,
                callStats,
                0,
                1
        );



        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, record);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);

        // There should be no calls to retry since the one above was the last rejected with DND reason
        assertEquals(0, callRetryDataService.count());
    }

    @Test
    public void verifyFT150() {

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        // To check that NMS shall retry the OBD messages which failed due to OBD_FAILED_OTHERS.

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now());
        String contentFileName = sh.getContentMessageFile(subscription, 0);

        Map<Integer, Integer> callStats = new HashMap<>();
        callStats.put(StatusCode.OBD_FAILED_OTHERS.getValue(),1);
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), timestamp),
                subscription.getSubscriber().getCallingNumber(),
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                FinalCallStatus.FAILED,
                callStats,
                0,
                1
        );


        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, record);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);

        // There should be one call to retry since the one above call was rescheduled.
        assertEquals(1, callRetryDataService.count());
        List<CallRetry> retries = callRetryDataService.retrieveAll();

        assertEquals(subscription.getSubscriptionId(), retries.get(0).getSubscriptionId());
        assertEquals(CallStage.RETRY_1, retries.get(0).getCallStage());
        assertEquals(DayOfTheWeek.today().nextDay(),retries.get(0).getDayOfTheWeek());
    }

    @Test
    public void verifyFT141() {

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        // To check that NMS shall retry OBD message for which delivery fails for the first time with two message per
        // week configuration.

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now(),
                SubscriptionPackType.PREGNANCY);
        String contentFileName = sh.getContentMessageFile(subscription, 0);

        csrDataService.create(new CallSummaryRecord(
                new RequestId(subscription.getSubscriptionId(), "11112233445566").toString(),
                subscription.getSubscriber().getCallingNumber(),
                "w1_1.wav",
                "w1_1",
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                FinalCallStatus.FAILED,
                makeStatsMap(StatusCode.OBD_FAILED_BUSY, 1),
                0,
                10,
                3
        ));
        Map<Integer, Integer> callStats = new HashMap<>();
        callStats.put(StatusCode.OBD_FAILED_BUSY.getValue(),1);
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), timestamp),
                subscription.getSubscriber().getCallingNumber(),
                contentFileName,
                "XXX",
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                FinalCallStatus.FAILED,
                callStats,
                0,
                3
        );


        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, record);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);




        assertEquals(1, callRetryDataService.count());

        List<CallRetry> retries = callRetryDataService.retrieveAll();

        assertEquals(subscription.getSubscriptionId(), retries.get(0).getSubscriptionId());
        assertEquals(CallStage.RETRY_1, retries.get(0).getCallStage());
        assertEquals(DayOfTheWeek.today().nextDay(),retries.get(0).getDayOfTheWeek());
    }


    @Test
    public void verifyFT138() {
        /**
         * To check that NMS shall retry OBD message for which first OBD retry fails
         * with single message per week configuration..
         */

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        // Create a record in the CallRetry table marked as "retry_1" and verify it is updated as "retry_2" in
        // CallRetry table

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(3));
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        CallRetry retry = callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                DayOfTheWeek.today(),
                CallStage.RETRY_1,
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                SubscriptionOrigin.MCTS_IMPORT
        ));


        Map<Integer, Integer> callStats = new HashMap<>();
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), timestamp),
                subscription.getSubscriber().getCallingNumber(),
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                FinalCallStatus.FAILED,
                callStats,
                0,
                5
        );


        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, record);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);

        // There should be one calls to retry since the retry 1 was failed.
        assertEquals(1, callRetryDataService.count());

        List<CallRetry> retries = callRetryDataService.retrieveAll();

        assertEquals(subscription.getSubscriptionId(), retries.get(0).getSubscriptionId());
        assertEquals(CallStage.RETRY_2, retries.get(0).getCallStage());
        assertEquals(DayOfTheWeek.today().nextDay(),retries.get(0).getDayOfTheWeek());
    }

    @Test
    public void verifyFT139() {
        /**
         * To check that NMS shall retry OBD message for which second OBD retry fails with
         * single message per week configuration.
         */

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        // Create a record in the CallRetry table marked as "retry_2" and verify it is updated as "retry_last" in
        // CallRetry table

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(3));
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        CallRetry retry = callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                DayOfTheWeek.today(),
                CallStage.RETRY_2,
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                SubscriptionOrigin.MCTS_IMPORT
        ));


        Map<Integer, Integer> callStats = new HashMap<>();
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), timestamp),
                subscription.getSubscriber().getCallingNumber(),
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                FinalCallStatus.FAILED,
                callStats,
                0,
                5
        );


        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, record);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);

        // There should be one calls to retry since the retry 2 was failed.
        assertEquals(1, callRetryDataService.count());

        List<CallRetry> retries = callRetryDataService.retrieveAll();

        assertEquals(subscription.getSubscriptionId(), retries.get(0).getSubscriptionId());
        assertEquals(CallStage.RETRY_LAST, retries.get(0).getCallStage());
        assertEquals(DayOfTheWeek.today().nextDay(),retries.get(0).getDayOfTheWeek());
    }


    /**
     * To verify that beneficiary(via mcts import) will be  deactivated if he/she
     * has MSISDN number added to the DND database.
     */
    @Test
    public void verifyFT177() {

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(14));

        CallSummaryRecordDto csr = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), "11112233445566"),
                subscription.getSubscriber().getCallingNumber(),
                sh.getContentMessageFile(subscription, 0),
                sh.getWeekId(subscription, 0),
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                FinalCallStatus.REJECTED,
                makeStatsMap(StatusCode.OBD_DNIS_IN_DND, 3),
                0,
                3
        );

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, csr);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);

        // verify that subscription created via MCTS-import is still Deactivated with reason "do not disturb"
        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.DO_NOT_DISTURB, subscription.getDeactivationReason());
    }


    /*
    * NMS_FT_163
    * To verify 72Weeks Pack created via IVR, shouldn't get deactivated due to reason DND.
    */
    @Test
    public void verifyFT163() {

        Subscription subscription2 = sh.mksub(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));

        CallSummaryRecordDto csr = new CallSummaryRecordDto(
                new RequestId(subscription2.getSubscriptionId(), "11112233445566"),
                subscription2.getSubscriber().getCallingNumber(),
                sh.getContentMessageFile(subscription2, 0),
                sh.getWeekId(subscription2, 0),
                rh.hindiLanguage().getCode(),
                rh.delhiCircle().getName(),
                FinalCallStatus.REJECTED,
                makeStatsMap(StatusCode.OBD_DNIS_IN_DND, 3),
                0,
                3
        );

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, csr);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);

        // verify that subscription created via IVR is still Active
        subscription2 = subscriptionDataService.findBySubscriptionId(subscription2.getSubscriptionId());
        assertEquals(SubscriptionStatus.ACTIVE, subscription2.getStatus());
    }


    /**
     * To check that NMS shall not retry OBD message for which all OBD attempts(1 actual+1 retry) fails with
     * two message per week configuration.
     */
    @Test
    public void verifyFT142() {

        String timestamp = DateTime.now().toString(TIME_FORMATTER);

        // Create a record in the CallRetry table marked as "retry 1" and verify it is erased from the
        // CallRetry table

        Subscription subscription = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(3),
                SubscriptionPackType.PREGNANCY);
        String contentFileName = sh.getContentMessageFile(subscription, 0);
        CallRetry retry = callRetryDataService.create(new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                DayOfTheWeek.today(),
                CallStage.RETRY_1,
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                SubscriptionOrigin.MCTS_IMPORT
        ));


        Map<Integer, Integer> callStats = new HashMap<>();
        CallSummaryRecordDto record = new CallSummaryRecordDto(
                new RequestId(subscription.getSubscriptionId(), timestamp),
                subscription.getSubscriber().getCallingNumber(),
                contentFileName,
                "XXX",
                "XXX",
                "XX",
                FinalCallStatus.FAILED,
                callStats,
                0,
                5
        );

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(CSR_PARAM_KEY, record);
        MotechEvent motechEvent = new MotechEvent(PROCESS_SUMMARY_RECORD_SUBJECT, eventParams);
        csrService.processCallSummaryRecord(motechEvent);

        // There should be no calls to retry since the one above was the last try
        assertEquals(0, callRetryDataService.count());
    }

    //todo: verify multiple days' worth of summary record aggregation
    //todo: verify more stuff I can't think of now
}
