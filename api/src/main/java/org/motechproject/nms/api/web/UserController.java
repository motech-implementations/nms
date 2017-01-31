package org.motechproject.nms.api.web;

import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.nms.api.web.contract.FlwUserResponse;
import org.motechproject.nms.api.web.contract.UserResponse;
import org.motechproject.nms.api.web.contract.kilkari.KilkariUserResponse;
import org.motechproject.nms.api.web.domain.AnonymousCallAudit;
import org.motechproject.nms.api.web.exception.NotAuthorizedException;
import org.motechproject.nms.api.web.exception.NotDeployedException;
import org.motechproject.nms.api.web.repository.AnonymousCallAuditDataService;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.domain.ServiceUsage;
import org.motechproject.nms.flw.domain.ServiceUsageCap;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.service.ServiceUsageCapService;
import org.motechproject.nms.flw.service.ServiceUsageService;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.props.service.LogHelper;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.service.CircleService;
import org.motechproject.nms.region.service.LanguageService;
import org.motechproject.nms.region.service.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashSet;
import java.util.Set;


@Controller
public class UserController extends BaseController {

    public static final String SERVICE_NAME = "serviceName";

    @Autowired
    private SubscriberService subscriberService;

    @Autowired
    private FrontLineWorkerService frontLineWorkerService;

    @Autowired
    private ServiceUsageService serviceUsageService;

    @Autowired
    private ServiceUsageCapService serviceUsageCapService;

    @Autowired
    private CircleService circleService;

    @Autowired
    private LanguageService languageService;

    @Autowired
    private StateService stateService;

    @Autowired
    private AnonymousCallAuditDataService anonymousCallAuditDataService;


    /**
     * 2.2.1 Get User Details API
     * IVR shall invoke this API when to retrieve details specific to the user identified by callingNumber.
     * In case user specific details are not available in the database, the API will attempt to load system
     * defaults based on the operator and circle provided.
     * /api/mobileacademy/user?callingNumber=9999999900&operator=A&circle=AP&callId=123456789012345

     * 3.2.1 Get User Details API
     * IVR shall invoke this API when to retrieve details specific to the user identified by callingNumber.
     * In case user specific details are not available in the database, the API will attempt to load system
     * defaults based on the operator and circle provided.
     * /api/mobilekunji/user?callingNumber=9999999900&operator=A&circle=AP&callId=234000011111111
     *
     */
    @RequestMapping("/{serviceName}/user") // NO CHECKSTYLE Cyclomatic Complexity
    @ResponseBody
    @Transactional(noRollbackFor = NotAuthorizedException.class)
    public UserResponse getUserDetails(@PathVariable String serviceName,
                             @RequestParam(required = false) Long callingNumber,
                             @RequestParam(required = false) String operator,
                             @RequestParam(required = false) String circle,
                             @RequestParam(required = false) String callId) {

        log(String.format("REQUEST: /%s/user", serviceName), String.format(
                "callingNumber=%s, callId=%s, operator=%s, circle=%s",
                LogHelper.obscure(callingNumber), callId, operator, circle));

        StringBuilder failureReasons = validate(callingNumber, callId, operator, circle);
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Circle circleObj = circleService.getByName(circle);

        UserResponse user = null;

        /*
        Make sure the url the user hit corresponds to a service we are expecting
         */
        if (!(MOBILE_ACADEMY.equals(serviceName) || MOBILE_KUNJI.equals(serviceName) ||
                KILKARI.equals(serviceName))) {
            failureReasons.append(String.format(INVALID, SERVICE_NAME));
        }

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        /*
        Handle the FLW services
         */
        if (MOBILE_ACADEMY.equals(serviceName) || MOBILE_KUNJI.equals(serviceName)) {
            user = getFrontLineWorkerResponseUser(serviceName, callingNumber, circleObj);
        }

        /*
        Kilkari in the house!
         */
        if (KILKARI.equals(serviceName)) {
            if (!validateKilkariServiceAvailability(callingNumber, circleObj)) {
                throw new NotDeployedException(String.format(NOT_DEPLOYED, Service.KILKARI));
            }
            user = getKilkariResponseUser(callingNumber);
        }

        Language defaultLanguage = null;
        if (circleObj != null) {
            defaultLanguage = circleObj.getDefaultLanguage();
        }

        // If no circle was provided, or if the provided circle doesn't have a default language, use the national
        if (defaultLanguage == null) {
            defaultLanguage = languageService.getNationalDefaultLanguage();
        }

        if (defaultLanguage != null && user != null) {
            user.setDefaultLanguageLocationCode(defaultLanguage.getCode());
        }

        // If the user does not have a language location code we want to return the allowed language location
        // codes for the provided circle, or all if no circle was provided
        Set<Language> languages = new HashSet<>();
        if (user.getLanguageLocationCode() == null && circleObj != null) {
            languages = languageService.getAllForCircle(circleObj);
        }

        if (user.getLanguageLocationCode() == null && circleObj == null) {
            languages = languageService.getAll();
        }

        if (languages.size() > 0) {
            Set<String> allowedLanguageLocations = new HashSet<>();
            for (Language language : languages) {
                allowedLanguageLocations.add(language.getCode());
            }
            user.setAllowedLanguageLocationCodes(allowedLanguageLocations);
        }

        log(String.format("RESPONSE: /%s/user", serviceName), String.format("callId=%s, %s", callId, user.toString()));
        return user;
    }

    private boolean validateKilkariServiceAvailability(Long callingNumber, Circle circle) { // NO CHECKSTYLE Cyclomatic Complexity
        Subscriber subscriber = subscriberService.getSubscriber(callingNumber);

        // 1. Check for existing subscriber and if mcts data is available
        // 2. If not mcts data available, use circle information for existing subscriber
        // 3. If existing subscriber has no circle available or is new subscriber, use circle passed from imi

        // check for subscriber and mcts location data
        if (subscriber != null) {

            MctsMother mother = subscriber.getMother();
            if (mother != null) {
                return serviceDeployedInUserState(Service.KILKARI, mother.getState());
            }

            MctsChild child = subscriber.getChild();
            if (child != null) {
                return serviceDeployedInUserState(Service.KILKARI, child.getState());
            }
        }

        // Try to validate from circle since we don't have MCTS data for state. Choose circle from subscriber or
        // passed from IMI as last resort
        Circle currentCircle = (subscriber != null && subscriber.getCircle() != null) ?
                subscriber.getCircle() : circle;
        if (currentCircle == null) { // No circle available
            return true;
        }

        Set<State> states = stateService.getAllInCircle(currentCircle);
        if (states == null || states.isEmpty()) { // No state available
            return true;
        }

        if (states.size() == 1) { // only one state available
            return serviceDeployedInUserState(Service.KILKARI, states.iterator().next());
        }

        for (State currentState : states) { // multiple states, false if undeployed in all states
            if (serviceDeployedInUserState(Service.KILKARI, currentState)) {
                return true;
            }
        }
        return false;
    }

    private UserResponse getKilkariResponseUser(Long callingNumber) {
        Subscriber subscriber = subscriberService.getSubscriber(callingNumber);
        KilkariUserResponse kilkariUserResponse = new KilkariUserResponse();
        Set<String> packs = new HashSet<>();

        if (subscriber != null) {

            Set<Subscription> subscriptions = subscriber.getSubscriptions();
            for (Subscription subscription : subscriptions) {
                if ((subscription.getStatus() == SubscriptionStatus.ACTIVE) ||
                        (subscription.getStatus() == SubscriptionStatus.PENDING_ACTIVATION)) {
                    packs.add(subscription.getSubscriptionPack().getName());
                }
            }
            kilkariUserResponse.setSubscriptionPackList(packs);

            Language subscriberLanguage = subscriber.getLanguage();
            if (subscriberLanguage != null) {
                kilkariUserResponse.setLanguageLocationCode(subscriberLanguage.getCode());
            }
        }

        kilkariUserResponse.setSubscriptionPackList(packs);
        return kilkariUserResponse;
    }

    private UserResponse getFrontLineWorkerResponseUser(String serviceName, Long callingNumber, Circle circle) {
        FlwUserResponse user = new FlwUserResponse();
        Service service = getServiceFromName(serviceName);
        ServiceUsage serviceUsage = new ServiceUsage(null, service, 0, 0, false);
        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(callingNumber);

        State state = getStateForFrontLineWorker(flw, circle);

        if (state != null) {
            if (!serviceDeployedInUserState(service, state)) {
                throw new NotDeployedException(String.format(NOT_DEPLOYED, service));
            }
        } else {
            // If we have no state for the user see if the service is deployed in at least one state in the circle
            if (!serviceDeployedInCircle(service, circle)) {
                throw new NotDeployedException(String.format(NOT_DEPLOYED, service));
            }
        }

        if (MOBILE_ACADEMY.equals(serviceName)) {
            // make sure that flw is authorized to use MA
            restrictAnonymousMAUserCheck(flw, callingNumber, circle);
        }

        if (flw != null) {
            Language language = flw.getLanguage();
            if (null != language) {
                user.setLanguageLocationCode(language.getCode());
            }

            serviceUsage = serviceUsageService.getCurrentMonthlyUsageForFLWAndService(flw, service);

            if (!frontLineWorkerAuthorizedForAccess(flw, state)) {
                throw new NotAuthorizedException(String.format(NOT_AUTHORIZED, CALLING_NUMBER));
            }
        }

        ServiceUsageCap serviceUsageCap = serviceUsageCapService.getServiceUsageCap(state, service);
        user.setCurrentUsageInPulses(serviceUsage.getUsageInPulses());
        user.setEndOfUsagePromptCounter(serviceUsage.getEndOfUsage());
        user.setWelcomePromptFlag(serviceUsage.getWelcomePrompt());
        user.setMaxAllowedUsageInPulses(serviceUsageCap.getMaxUsageInPulses());
        user.setMaxAllowedEndOfUsagePrompt(2);

        return user;
    }

    private void restrictAnonymousMAUserCheck(FrontLineWorker flw, Long callingNumber, Circle circle) {

        if (flw == null || flw.getStatus() == FrontLineWorkerStatus.ANONYMOUS ||
                flw.getMctsFlwId() == null || flw.getMctsFlwId().isEmpty()) {
            // New requirement - https://applab.atlassian.net/projects/NMS/issues/NMS-325 - Block anonymous FLWs
            // if flw is null here, we don't already have a record from MCTS. return 403
            // We might have a non-null flw with anonymous status from earlier calls, if so, still return 403 and
            // force them to come through MCTS

            createAnonymousCallAuditRecordData(callingNumber, circle);
            throw new NotAuthorizedException(String.format(NOT_AUTHORIZED, CALLING_NUMBER));
        }
    }

    private void createAnonymousCallAuditRecordData(Long callingNumber, Circle circle) {
        if (circle == null) {
            anonymousCallAuditDataService.create(new AnonymousCallAudit(DateUtil.now(), null, callingNumber));
        } else {
            anonymousCallAuditDataService.create(new AnonymousCallAudit(DateUtil.now(), circle.getName(), callingNumber));
        }
    }

}
