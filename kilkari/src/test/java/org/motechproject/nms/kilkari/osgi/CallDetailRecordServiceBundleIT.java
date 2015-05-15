package org.motechproject.nms.kilkari.osgi;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.mds.config.SettingsService;
import org.motechproject.nms.kilkari.domain.CallDetailRecord;
import org.motechproject.nms.kilkari.domain.CallRetry;
import org.motechproject.nms.kilkari.domain.CallStage;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.StatusCode;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.CallDetailRecordDataService;
import org.motechproject.nms.kilkari.repository.CallRetryDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.service.CallDetailRecordService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.CallStatus;
import org.motechproject.nms.props.domain.DayOfTheWeek;
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
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class CallDetailRecordServiceBundleIT extends BasePaxIT {

    private static final String PROCESS_CDR = "nms.imi.kk.process_cdr";
    private static final String IMI_SERVICE_ID = "some_service_id"; //todo: look into that more closely

    @Inject
    EventRelay eventRelay;

    @Inject
    CallDetailRecordService cdrService;

    @Inject
    private SettingsService settingsService;

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private SubscriptionDataService subscriptionDataService;

    @Inject
    private SubscriberDataService subscriberDataService;

    @Inject
    private LanguageDataService languageDataService;

    @Inject
    private CallRetryDataService callRetryDataService;

    @Inject
    private CallDetailRecordDataService callDetailRecordDataService;

    @Inject
    private AlertService alertService;

    @Inject
    private LanguageLocationDataService languageLocationDataService;

    @Inject
    private CircleDataService circleDataService;

    @Inject
    private StateDataService stateDataService;

    @Inject
    private DistrictDataService districtDataService;


    @Before
    public void cleanupDatabase() {
        subscriptionService.deleteAll();
        subscriberDataService.deleteAll();
        languageLocationDataService.deleteAll();
        languageDataService.deleteAll();
        districtDataService.deleteAll();
        stateDataService.deleteAll();
        circleDataService.deleteAll();
        callRetryDataService.deleteAll();
    }


    @Test
    public void testServicePresent() {
        assertTrue(cdrService != null);
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
        getLogger().debug("Created subscription {}", subscription.toString());
        return subscription;
    }


    CallDetailRecord makeCdr() {
        Subscription subscription = makeSubscription(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));
        CallDetailRecord cdr = new CallDetailRecord(
                new RequestId(subscription.getSubscriptionId(), "foo.csv").toString(),
                IMI_SERVICE_ID,
                subscription.getSubscriber().getCallingNumber(),
                null,
                0,
                null,
                subscription.getSubscriptionPack().getMessages().get(2).getMessageFileName(),
                subscription.getSubscriptionPack().getMessages().get(2).getWeekId(),
                makeLanguageLocation().getCode(),
                makeCircle().getName(),
                CallStatus.SUCCESS,
                StatusCode.OBD_SUCCESS_CALL_CONNECTED,
                1);

        return cdr;
    }


    @Test
    public void verifyServiceFunctional() {
        CallDetailRecord cdr = makeCdr();
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("CDR", cdr);
        MotechEvent motechEvent = new MotechEvent(PROCESS_CDR, eventParams);
        eventRelay.sendEventMessage(motechEvent);
    }


    // https://github.com/motech-implementations/mim/issues/169
    @Test
    public void verifyIssue169() {

        Subscription subscription = makeSubscription(SubscriptionOrigin.IVR, DateTime.now().minusDays(14));

        CallRetry callRetry = new CallRetry(
                subscription.getSubscriptionId(),
                subscription.getSubscriber().getCallingNumber(),
                DayOfTheWeek.today(),
                CallStage.RETRY_LAST,
                "w1_m1.wav",
                "w1_1",
                subscription.getSubscriber().getLanguageLocation().getCode(),
                subscription.getSubscriber().getCircle().getName(),
                "I");
        callRetryDataService.create(callRetry);

        RequestId requestId = new RequestId(subscription.getSubscriptionId(), "file.csv");
        for (int i=0 ; i<3 ; i++) {
            callDetailRecordDataService.create(new CallDetailRecord(
                    requestId.toString(),
                    IMI_SERVICE_ID,
                    subscription.getSubscriber().getCallingNumber(),
                    null,
                    0,
                    null,
                    "w1_m1.wav",
                    "w1_1",
                    subscription.getSubscriber().getLanguageLocation().getCode(),
                    subscription.getSubscriber().getCircle().getName(),
                    CallStatus.FAILED,
                    StatusCode.OBD_FAILED_INVALIDNUMBER,
                    1));
        }

        CallDetailRecord cdr = new CallDetailRecord(
                requestId.toString(),
                IMI_SERVICE_ID,
                subscription.getSubscriber().getCallingNumber(),
                null,
                0,
                null,
                "w1_m1.wav",
                "w1_1",
                subscription.getSubscriber().getLanguageLocation().getCode(),
                subscription.getSubscriber().getCircle().getName(),
                CallStatus.FAILED,
                StatusCode.OBD_FAILED_INVALIDNUMBER,
                1);

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("CDR", cdr);
        MotechEvent motechEvent = new MotechEvent(PROCESS_CDR, eventParams);
        cdrService.processCallDetailRecord(motechEvent);

        subscription = subscriptionDataService.findBySubscriptionId(subscription.getSubscriptionId());
        assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
        assertEquals(DeactivationReason.INVALID_NUMBER, subscription.getDeactivationReason());
    }


    //todo: much more
}
