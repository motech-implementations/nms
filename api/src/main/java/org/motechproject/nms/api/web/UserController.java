package org.motechproject.nms.api.web;

import org.joda.time.DateTime;
import org.motechproject.nms.api.web.contract.FrontLineWorkerUser;
import org.motechproject.nms.api.web.contract.KilkariResponseUser;
import org.motechproject.nms.api.web.contract.ResponseUser;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashSet;
import java.util.Set;


@Controller
public class UserController extends BaseController {

    public static final String MOBILE_ACADEMY = "mobileacademy";
    public static final String MOBILE_KUNJI = "mobilekunji";
    public static final String KILKARI = "kilkari";

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

    @RequestMapping("/{serviceName}/user")
    @ResponseBody
    public ResponseUser user(@PathVariable String serviceName, @RequestParam String callingNumber,
                             @RequestParam String operator, @RequestParam String circle, @RequestParam String callId)
            throws NotFoundException {
        StringBuilder failureReasons = validate(callingNumber, operator, circle, callId);
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        ResponseUser user = null;

        /*
        Make sure the url the user hit corresponds to a service we are expecting
         */
        if (!(MOBILE_ACADEMY.equals(serviceName) || MOBILE_KUNJI.equals(serviceName)) && !(KILKARI.equals(serviceName))) {
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
        if (null != language) {
            user.setDefaultLanguageLocationCode(language.getCode());
        }

        return user;
    }

    private ResponseUser getKilkariResponseUser(String callingNumber) throws NotFoundException {
        ResponseUser user;
        user = new KilkariResponseUser();
        Subscriber subscriber = kilkariService.getSubscriber(callingNumber);
        if (subscriber == null) {
            throw new NotFoundException(String.format(NOT_FOUND, "callingNumber"));
        }
        Set<Subscription> subscriptions = subscriber.getSubscriptions();
        Set<String> packs = new HashSet<>();
        for (Subscription subscription : subscriptions) {
            packs.add(subscription.getSubscriptionPack().getName());
        }
        ((KilkariResponseUser) user).setSubscriptionPackList(packs);
        return user;
    }

    private ResponseUser getFrontLineWorkerResponseUser(String serviceName, String callingNumber) {
        ResponseUser user;

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

        ((FrontLineWorkerUser) user).setCurrentUsageInPulses(serviceUsage.getUsageInPulses());
        ((FrontLineWorkerUser) user).setEndOfUsagePromptCounter(serviceUsage.getEndOfUsage());
        ((FrontLineWorkerUser) user).setWelcomePromptFlag(serviceUsage.getWelcomePrompt() > 0 ? true : false);

        ((FrontLineWorkerUser) user).setMaxAllowedUsageInPulses(serviceUsageCap.getMaxUsageInPulses());

        // TODO: During configuration sprint this value needs to be de-hardcoded
        ((FrontLineWorkerUser) user).setMaxAllowedEndOfUsagePrompt(2);

        return user;
    }
}
