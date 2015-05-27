package org.motechproject.nms.testing.it.imi;


import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
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

public class SubscriptionHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionHelper.class);

    private SubscriptionService subscriptionService;
    private SubscriberDataService subscriberDataService;
    private LanguageDataService languageDataService;
    private LanguageLocationDataService languageLocationDataService;
    private CircleDataService circleDataService;
    private StateDataService stateDataService;
    private DistrictDataService districtDataService;


    public SubscriptionHelper(SubscriptionService subscriptionService,
                              SubscriberDataService subscriberDataService,
                              LanguageDataService languageDataService,
                              LanguageLocationDataService languageLocationDataService,
                              CircleDataService circleDataService,
                              StateDataService stateDataService,
                              DistrictDataService districtDataService) {

        this.subscriptionService = subscriptionService;
        this.subscriberDataService = subscriberDataService;
        this.languageDataService = languageDataService;
        this.languageLocationDataService = languageLocationDataService;
        this.circleDataService = circleDataService;
        this.stateDataService = stateDataService;
        this.districtDataService = districtDataService;
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


    public int getRandomMessageIndex(Subscription sub) {
        return (int) (Math.random() * sub.getSubscriptionPack().getMessages().size());
    }


    public String getWeekId(Subscription sub, int index) {
        return sub.getSubscriptionPack().getMessages().get(index).getWeekId();
    }


    public String getLanguageLocationCode(Subscription sub) {
        return ((LanguageLocation) subscriberDataService.getDetachedField(
                sub.getSubscriber(),"languageLocation")).getCode();
    }


    public String getCircle(Subscription sub) {
        return ((Circle) subscriberDataService.getDetachedField(
                sub.getSubscriber(),"circle")).getName();
    }


    public String getContentMessageFile(Subscription sub, int index) {
        return sub.getSubscriptionPack().getMessages().get(index).getMessageFileName();
    }

    public Subscription mksub(SubscriptionOrigin origin, DateTime startDate) {
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
}
