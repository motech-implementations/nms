package org.motechproject.nms.api.web;

import org.joda.time.DateTime;
import org.motechproject.nms.api.web.contract.FrontLineWorkerUser;
import org.motechproject.nms.api.web.contract.LanguageRequest;
import org.motechproject.nms.api.web.contract.ResponseUser;
import org.motechproject.nms.api.web.contract.kilkari.UserResponse;
import org.motechproject.nms.api.web.exception.NotFoundException;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.Service;
import org.motechproject.nms.flw.domain.ServiceUsage;
import org.motechproject.nms.flw.domain.ServiceUsageCap;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.service.ServiceUsageCapService;
import org.motechproject.nms.flw.service.ServiceUsageService;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.service.KilkariService;
import org.motechproject.nms.language.domain.Language;
import org.motechproject.nms.language.service.LanguageService;
import org.motechproject.nms.location.domain.District;
import org.motechproject.nms.location.domain.State;
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

    @Autowired
    private LanguageService languageService;

    @Autowired
    private KilkariService kilkariService;

    @Autowired
    private FrontLineWorkerService frontLineWorkerService;

    @Autowired
    private ServiceUsageService serviceUsageService;

    @Autowired
    private ServiceUsageCapService serviceUsageCapService;


    /**
     * 2.2.7.1  Set User Language Location Code
     *          http://<motech:port>/motech­platform­server/module/mobileacademy/languageLocationCode
     *
     * 3.2.3.1  Set User Language Location Code
     *          http://<motech:port>/motech­platform­server/module/mobilekunji/languageLocationCode
     *
     *          IVR shall invoke this API to provide user languageLocation preference to MOTECH.
     *
     */
    @RequestMapping(value = "/{serviceName}/languageLocationCode",
                    method = RequestMethod.POST,
                    headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    public void setUserLanguageLocationCode(@PathVariable String serviceName,
                                            @RequestBody LanguageRequest languageRequest) {
        Long callingNumber = languageRequest.getCallingNumber();
        Long callId = languageRequest.getCallId();
        Integer languageLocationCode = languageRequest.getLanguageLocationCode();

        StringBuilder failureReasons = validate(callingNumber, callId);
        validateFieldPresent(failureReasons, "languageLocationCode", languageRequest.getLanguageLocationCode());

        if (!(MOBILE_ACADEMY.equals(serviceName) || MOBILE_KUNJI.equals(serviceName))) {
            failureReasons.append(String.format(INVALID, "serviceName"));
        }

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        // TODO: If no FLW user is found we need to create one here #62
        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(callingNumber);
        if (null == flw) {
            throw new NotFoundException(String.format(NOT_FOUND, "callingNumber"));
        }

        Language language = languageService.getLanguageByCode(languageLocationCode);
        if (null == language) {
            throw new NotFoundException(String.format(NOT_FOUND, "languageLocationCode"));
        }

        flw.setLanguage(language);
        frontLineWorkerService.update(flw);
    }


    /**
     * 2.2.1.1 Get User Details
     *         http://<motech:port>/motech­patform­server/module/mobileacademy/user?callingNumber=9999999900
     *             &operator=A&circle=AP&callId=123456789012345

     * 3.2.1.1 Get User Details
     *         http://<motech:port>/motech­patform­server/module/mobilekunji/user?callingNumber=9999999900
     *             &operator=A&circle=AP&callId=234000011111111
     *             
     *             
     * IVR shall invoke this API when to retrieve details specific to the user identified by
     * callingNumber. In case user specific details are not available in the database, the API will
     * attempt to load system defaults based on the operator and circle provided.
     *
     */
    @RequestMapping("/{serviceName}/user") // NO CHECKSTYLE Cyclomatic Complexity
    @ResponseBody
    public ResponseUser getUserDetails(@PathVariable String serviceName,
                             @RequestParam(required = false) Long callingNumber,
                             @RequestParam(required = false) String operator,
                             @RequestParam(required = false) String circle,
                             @RequestParam(required = false) Long callId) {

        StringBuilder failureReasons = validate(callingNumber, callId, operator, circle);
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        ResponseUser user = null;

        /*
        Make sure the url the user hit corresponds to a service we are expecting
         */
        if (!(MOBILE_ACADEMY.equals(serviceName) || MOBILE_KUNJI.equals(serviceName) ||
                KILKARI.equals(serviceName))) {
            failureReasons.append(String.format(INVALID, "serviceName"));
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

    private ResponseUser getKilkariResponseUser(Long callingNumber) {
        UserResponse user = new UserResponse();
        Subscriber subscriber = kilkariService.getSubscriber(callingNumber);
        if (subscriber == null) {
            throw new NotFoundException(String.format(NOT_FOUND, "callingNumber"));
        }
        Set<Subscription> subscriptions = subscriber.getSubscriptions();
        Set<String> packs = new HashSet<>();
        for (Subscription subscription : subscriptions) {
            packs.add(subscription.getSubscriptionPack().getName());
        }
        user.setSubscriptionPackList(packs);
        return user;
    }

    private ResponseUser getFrontLineWorkerResponseUser(String serviceName, Long callingNumber) {
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
        }

        ServiceUsageCap serviceUsageCap = serviceUsageCapService.getServiceUsageCap(state, service);

        user.setCurrentUsageInPulses(serviceUsage.getUsageInPulses());
        user.setEndOfUsagePromptCounter(serviceUsage.getEndOfUsage());
        user.setWelcomePromptFlag(serviceUsage.getWelcomePrompt() > 0);

        user.setMaxAllowedUsageInPulses(serviceUsageCap.getMaxUsageInPulses());

        // TODO: During configuration sprint this value needs to be de-hardcoded
        user.setMaxAllowedEndOfUsagePrompt(2);

        return user;
    }
}
