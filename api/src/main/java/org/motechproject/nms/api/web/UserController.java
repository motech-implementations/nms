package org.motechproject.nms.api.web;

import org.joda.time.DateTime;
import org.motechproject.nms.api.web.contract.KilkariResponseUser;
import org.motechproject.nms.api.web.contract.FrontLineWorkerUser;
import org.motechproject.nms.api.web.contract.ResponseUser;
import org.motechproject.nms.flw.domain.Service;
import org.motechproject.nms.flw.domain.ServiceUsage;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.service.KilkariService;
import org.motechproject.nms.language.domain.Language;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.language.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashSet;
import java.util.List;
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

    @RequestMapping("/{serviceName}/user")
    @ResponseBody
    public ResponseUser user(@PathVariable String serviceName, @RequestParam String callingNumber,
                             @RequestParam String operator, @RequestParam String circle, @RequestParam String callId) {
        Language language = languageService.getDefaultCircleLanguage(circle);
        StringBuilder failureReasons = validate(callingNumber, operator, circle, callId);

        ResponseUser user = null;

        if (MOBILE_ACADEMY.equals(serviceName) || MOBILE_KUNJI.equals(serviceName)) {
            /**
             * Common
             *   languageLocationCode - If user is located this is the language code they selected
             *   defaultLanguageLocationCode - Default language location code set for circle
             *
             * MA
             *   currentUsageInPulses - No. of pulses consumed for MA service
             *   endOfUsagePromptCounter - Indicates no. of times end of usage message has been played to user.
             *
             * MK
             *   currentUsageInPulses - No. of pulses consumed for MK service
             *   endOfUsagePromptCounter - Indicates no. of times end of usage message has been played to user.
             *   welcomePromptFlag - Indicates welcome prompt is already played or not
             *
             * Global Config
             *   maxAllowedUsageInPulses - Indicates maximum allowed usage (in pulses) for a user. -1 if uncapped
             *   maxAllowedEndOfUsagePrompt - Max number of times the End Of Usage prompt shall be played to the user in
             *                                a month (currently 2 according to SRS)
             */

            Service service = null;

            if (MOBILE_ACADEMY.equals(serviceName)) {
                service = Service.MOBILE_ACADEMY;
            }

            if (MOBILE_KUNJI.equals(serviceName)) {
                service = Service.MOBILE_KUNJI;
            }

            // Load the FLW
            user = new FrontLineWorkerUser();
            ServiceUsage serviceUsage = new ServiceUsage(null, service, 0, 0, 0, DateTime.now());
            FrontLineWorker flw = frontLineWorkerService.getByContactNumber(callingNumber);

            if (null != flw) {
                user.setLanguageLocationCode(flw.getLanguage().getCode());
                serviceUsage = frontLineWorkerService.getCurrentMonthlyUsageForService(flw, service);
            }

            ((FrontLineWorkerUser) user).setCurrentUsageInPulses(serviceUsage.getUsageInPulses());
            ((FrontLineWorkerUser) user).setEndOfUsagePromptCounter(serviceUsage.getEndOfUsage());
            ((FrontLineWorkerUser) user).setWelcomePromptFlag(serviceUsage.getWelcomePrompt() > 0 ? true : false);

        } else if (KILKARI.equals(serviceName)) {
            user = new KilkariResponseUser();
            List<SubscriptionPack> subscriptionPacks = kilkariService.getSubscriberPacks(callingNumber);
            Set<String> packs = new HashSet<>();
            for (SubscriptionPack subscriptionPack : subscriptionPacks) {
                packs.add(subscriptionPack.getName());
            }
            ((KilkariResponseUser) user).setSubscriptionPackList(packs);
        } else {
            failureReasons.append(String.format(INVALID, "serviceName"));
        }

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        if (null != language) {
            user.setDefaultLanguageLocationCode(language.getCode());
        }

        return user;
    }
}
