package org.motechproject.nms.api.web;

import org.joda.time.DateTime;
import org.motechproject.nms.api.web.contract.FrontLineWorkerUser;
import org.motechproject.nms.api.web.contract.LanguageRequest;
import org.motechproject.nms.api.web.contract.ResponseUser;
import org.motechproject.nms.api.web.contract.kilkari.UserResponse;
import org.motechproject.nms.api.web.exception.NotAuthorizedException;
import org.motechproject.nms.api.web.exception.NotFoundException;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.Service;
import org.motechproject.nms.flw.domain.ServiceUsage;
import org.motechproject.nms.flw.domain.ServiceUsageCap;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.service.ServiceUsageCapService;
import org.motechproject.nms.flw.service.ServiceUsageService;
import org.motechproject.nms.flw.service.WhitelistService;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.service.KilkariService;
import org.motechproject.nms.language.domain.Language;
import org.motechproject.nms.language.service.LanguageService;
import org.motechproject.nms.location.domain.District;
import org.motechproject.nms.location.domain.State;
import org.motechproject.nms.location.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashSet;
import java.util.Set;


@Controller
public class UserController extends BaseController {

    public static final String CALLING_NUMBER = "callingNumber";
    public static final String LANGUAGE_LOCATION_CODE = "languageLocationCode";
    public static final String SERVICE_NAME = "serviceName";

    @Autowired
    private LanguageService languageService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private KilkariService kilkariService;

    @Autowired
    private FrontLineWorkerService frontLineWorkerService;

    @Autowired
    private ServiceUsageService serviceUsageService;

    @Autowired
    private ServiceUsageCapService serviceUsageCapService;

    @Autowired
    private WhitelistService whitelistService;

    @RequestMapping(value = "/{serviceName}/languageLocationCode",
                    method = RequestMethod.POST,
                    headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    public void setUserLanguage(@PathVariable String serviceName, @RequestBody LanguageRequest languageRequest)
            throws NotFoundException {
        String callingNumber = languageRequest.getCallingNumber();
        String callId = languageRequest.getCallId();
        String languageLocationCode = languageRequest.getLanguageLocationCode();

        StringBuilder failureReasons = validate(callingNumber, callId);
        validateFieldNumeric(failureReasons, LANGUAGE_LOCATION_CODE, languageRequest.getLanguageLocationCode());

        if (null == languageLocationCode) {
            failureReasons.append(String.format(NOT_PRESENT, LANGUAGE_LOCATION_CODE));
        }

        if (!(MOBILE_ACADEMY.equals(serviceName) || MOBILE_KUNJI.equals(serviceName))) {
            failureReasons.append(String.format(INVALID, SERVICE_NAME));
        }

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(callingNumber);
        if (flw == null) {
            flw = new FrontLineWorker(callingNumber);
        }

        Language language = languageService.getLanguageByCode(Integer.parseInt(languageLocationCode));
        if (null == language) {
            throw new NotFoundException(String.format(NOT_FOUND, LANGUAGE_LOCATION_CODE));
        }

        flw.setLanguage(language);

        if (!frontLineWorkerAuthorizedForAccess(flw)) {
            throw new NotAuthorizedException(String.format(NOT_AUTHORIZED, CALLING_NUMBER));
        }

        // MOTECH-1667 added to get an upsert method included
        if (flw.getId() == null) {
            frontLineWorkerService.add(flw);
        } else {
            frontLineWorkerService.update(flw);
        }
    }


    @RequestMapping("/{serviceName}/user") // NO CHECKSTYLE Cyclomatic Complexity
    @ResponseBody
    public ResponseUser user(@PathVariable String serviceName,
                             @RequestParam(required = false) String callingNumber,
                             @RequestParam(required = false) String operator,
                             @RequestParam(required = false) String circle,
                             @RequestParam(required = false) String callId)
            throws NotFoundException {

        StringBuilder failureReasons = validate(callingNumber, operator, circle, callId);
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        ResponseUser user = null;

        /*
        Make sure the url the user hit corresponds to a service we are expecting
         */
        if (!(MOBILE_ACADEMY.equals(serviceName) || MOBILE_KUNJI.equals(serviceName) ||
                KILKARI.equals(serviceName))) {
            failureReasons.append(String.format(INVALID, SERVICE_NAME));
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

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Language language = languageService.getDefaultCircleLanguage(circle);
        if (language != null && user != null) {
            user.setDefaultLanguageLocationCode(language.getCode());
        }

        return user;
    }

    private ResponseUser getKilkariResponseUser(String callingNumber) throws NotFoundException {
        UserResponse user = new UserResponse();
        Subscriber subscriber = kilkariService.getSubscriber(callingNumber);
        if (subscriber == null) {
            throw new NotFoundException(String.format(NOT_FOUND, CALLING_NUMBER));
        }
        Set<Subscription> subscriptions = subscriber.getSubscriptions();
        Set<String> packs = new HashSet<>();
        for (Subscription subscription : subscriptions) {
            packs.add(subscription.getSubscriptionPack().getName());
        }
        user.setSubscriptionPackList(packs);
        return user;
    }

    private ResponseUser getFrontLineWorkerResponseUser(String serviceName, String callingNumber) {
        FrontLineWorkerUser user;

        Service service = null;

        if (MOBILE_ACADEMY.equals(serviceName)) {
            service = Service.MOBILE_ACADEMY;
        }

        if (MOBILE_KUNJI.equals(serviceName)) {
            service = Service.MOBILE_KUNJI;
        }

        user = new FrontLineWorkerUser();
        ServiceUsage serviceUsage = new ServiceUsage(null, service, 0, 0, 0, DateTime.now());
        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(callingNumber);

        State state = null;
        if (null != flw) {
            Language language = flw.getLanguage();
            if (null != language) {
                user.setLanguageLocationCode(language.getCode());
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

    private boolean frontLineWorkerAuthorizedForAccess(FrontLineWorker flw) {
        District district = flw.getDistrict();
        State state = null;

        if (district != null) {
            state = district.getState();
        }

        if (state == null) {
            state = locationService.getStateForLanguage(flw.getLanguage());
        }

        return whitelistService.numberWhitelistedForState(state, flw.getContactNumber());
    }
}
