package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.contract.KilkariResponseUser;
import org.motechproject.nms.api.web.contract.MobileAcademyUser;
import org.motechproject.nms.api.web.contract.MobileKunjiUser;
import org.motechproject.nms.api.web.contract.ResponseUser;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.service.KilkariService;
import org.motechproject.nms.language.domain.Language;
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

    @RequestMapping("/{serviceName}/user")
    @ResponseBody
    public ResponseUser user(@PathVariable String serviceName, @RequestParam String callingNumber,
                             @RequestParam String operator, @RequestParam String circle, @RequestParam String callId) {
        List<Language> languages = languageService.getCircleLanguages(circle);
        StringBuilder failureReasons = validate(callingNumber, operator, circle, callId);

        ResponseUser user = null;

        if (MOBILE_ACADEMY.equals(serviceName)) {
            user = new MobileAcademyUser();
        } else if (MOBILE_KUNJI.equals(serviceName)) {
            user = new MobileKunjiUser();
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

        if (languages.size() > 0) {
            user.setLanguageLocationCode(languages.get(0).getCode());
        }

        return user;
    }
}
