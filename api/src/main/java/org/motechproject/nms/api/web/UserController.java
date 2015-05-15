package org.motechproject.nms.api.web;

import org.joda.time.DateTime;
import org.motechproject.nms.api.web.contract.FlwUserResponse;
import org.motechproject.nms.api.web.contract.UserResponse;
import org.motechproject.nms.api.web.contract.kilkari.KilkariUserResponse;
import org.motechproject.nms.api.web.exception.NotAuthorizedException;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.Service;
import org.motechproject.nms.flw.domain.ServiceUsage;
import org.motechproject.nms.flw.domain.ServiceUsageCap;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.service.ServiceUsageCapService;
import org.motechproject.nms.flw.service.ServiceUsageService;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.service.CircleService;
import org.motechproject.nms.region.service.LanguageLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Controller
public class UserController extends BaseController {

    public static final String CIRCLE = "circle";
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
    private LanguageLocationService languageLocationService;

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
    public UserResponse getUserDetails(@PathVariable String serviceName,
                             @RequestParam(required = false) Long callingNumber,
                             @RequestParam(required = false) String operator,
                             @RequestParam(required = false) String circle,
                             @RequestParam(required = false) Long callId) {

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
            user = getFrontLineWorkerResponseUser(serviceName, callingNumber);
        }

        /*
        Kilkari in the house!
         */
        if (KILKARI.equals(serviceName)) {
            user = getKilkariResponseUser(callingNumber);
        }

        LanguageLocation defaultLanguageLocation = null;
        if (circleObj != null) {
            defaultLanguageLocation = languageLocationService.getDefaultForCircle(circleObj);
        }

        // If no circle was provided, or if the provided circle doesn't have a default language, use the national
        if (defaultLanguageLocation == null) {
            defaultLanguageLocation = languageLocationService.getNationalDefaultLanguageLocation();
        }

        if (defaultLanguageLocation != null && user != null) {
            user.setDefaultLanguageLocationCode(defaultLanguageLocation.getCode());
        }

        // If the user does not have a language location code we want to return the allowed language location
        // codes for the provided circle, or all if no circle was provided
        List<LanguageLocation> languageLocations = new ArrayList<>();
        if (user.getLanguageLocationCode() == null && circleObj != null) {
            languageLocations = languageLocationService.getAllForCircle(circleObj);

            // If there is only one language set that as the users language.  I would prefer if we instead
            // returned the 1 element allowedLanguages array and had the IVR just skip prompting the user
            // but that would result in two API calls without a prompt being played and that could
            // be too long of a delay.  So instead we create or update the FLW in the get user api call.  bleh.
//  This is an open question in an email thread with IMI.  My preference is for the VXML to just call set language

            if (false && languageLocations.size() == 1) {
                if (MOBILE_ACADEMY.equals(serviceName) || MOBILE_KUNJI.equals(serviceName)) {
                    updateFLWWithLanguage(callingNumber, languageLocations.get(0));
                }

                user.setLanguageLocationCode(languageLocations.get(0).getCode());
            }
        }

        if (user.getLanguageLocationCode() == null && circleObj == null) {
            languageLocations = languageLocationService.getAll();
        }

        if (languageLocations.size() > 0) {
            List<String> allowedLanguageLocations = new ArrayList<>();
            for (LanguageLocation languageLocation : languageLocations) {
                allowedLanguageLocations.add(languageLocation.getCode());
            }
            user.setAllowedLanguageLocationCodes(allowedLanguageLocations);
        }

        return user;
    }

    private void updateFLWWithLanguage(Long callingNumber, LanguageLocation languageLocation) {
        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(callingNumber);
        if (flw == null) {
            flw = new FrontLineWorker(callingNumber);
        }

        flw.setLanguageLocation(languageLocation);

        // MOTECH-1667 added to get an upsert method included
        if (flw.getId() == null) {
            frontLineWorkerService.add(flw);
        } else {
            frontLineWorkerService.update(flw);
        }
    }

    private UserResponse getKilkariResponseUser(Long callingNumber) {
        KilkariUserResponse user = new KilkariUserResponse();
        Set<String> packs = new HashSet<>();

        Subscriber subscriber = subscriberService.getSubscriber(callingNumber);
        if (subscriber != null) {
            Set<Subscription> subscriptions = subscriber.getSubscriptions();
            for (Subscription subscription : subscriptions) {
                packs.add(subscription.getSubscriptionPack().getName());
            }

            LanguageLocation subscriberLanguageLocation = subscriber.getLanguageLocation();
            if (subscriberLanguageLocation != null) {
                user.setLanguageLocationCode(subscriberLanguageLocation.getCode());
            }
        }
        user.setSubscriptionPackList(packs);

        return user;
    }

    private UserResponse getFrontLineWorkerResponseUser(String serviceName, Long callingNumber) {
        FlwUserResponse user = new FlwUserResponse();

        Service service = null;

        if (MOBILE_ACADEMY.equals(serviceName)) {
            service = Service.MOBILE_ACADEMY;
        }

        if (MOBILE_KUNJI.equals(serviceName)) {
            service = Service.MOBILE_KUNJI;
        }

        ServiceUsage serviceUsage = new ServiceUsage(null, service, 0, 0, 0, DateTime.now());
        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(callingNumber);

        State state = null;
        if (null != flw) {
            LanguageLocation languageLocation = flw.getLanguageLocation();
            if (null != languageLocation) {
                user.setLanguageLocationCode(languageLocation.getCode());
            }

            serviceUsage = serviceUsageService.getCurrentMonthlyUsageForFLWAndService(flw, service);

            District district = flw.getDistrict();
            if (null != district) {
                state = district.getState();
            }

            if (!frontLineWorkerAuthorizedForAccess(flw)) {
                throw new NotAuthorizedException(String.format(NOT_AUTHORIZED, CALLING_NUMBER));
            }
        }

        ServiceUsageCap serviceUsageCap = serviceUsageCapService.getServiceUsageCap(state, service);

        user.setCurrentUsageInPulses(serviceUsage.getUsageInPulses());
        user.setEndOfUsagePromptCounter(serviceUsage.getEndOfUsage());
        user.setWelcomePromptFlag(serviceUsage.getWelcomePrompt() > 0);

        user.setMaxAllowedUsageInPulses(serviceUsageCap.getMaxUsageInPulses());

        // TODO: #38 During configuration sprint this value needs to be de-hardcoded
        user.setMaxAllowedEndOfUsagePrompt(2);

        return user;
    }

}
