package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.contract.KilkariResponseUser;
import org.motechproject.nms.api.web.contract.MobilAcademyUser;
import org.motechproject.nms.api.web.contract.MobileKunjiUser;
import org.motechproject.nms.api.web.contract.ResponseUser;
import org.motechproject.nms.language.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


@RequestMapping("/flw")
@Controller
public class UserController extends BaseController {

    public static final String MOBILE_ACADEMY = "mobileacademy";
    public static final String MOBILE_KUNJI = "mobilekunji";
    public static final String KILKARI = "kilkari";

    @Autowired
    private LanguageService languageService;

    @RequestMapping("/{serviceName}/user")
    @ResponseBody
    public ResponseUser user(@PathVariable String serviceName, @RequestParam String callingNumber,
                             @RequestParam String operator, @RequestParam String circle, @RequestParam String callId) {
        Set<String> languages = languageService.getCircleLanguages(circle);
        StringBuilder failureReasons = validate(callingNumber, operator, circle, callId);

        ResponseUser user = null;

        if (MOBILE_ACADEMY.equals(serviceName)) {
            user = new MobilAcademyUser();
        } else if (MOBILE_KUNJI.equals(serviceName)) {
            user = new MobileKunjiUser();
        } else if (KILKARI.equals(serviceName)) {
            user = new KilkariResponseUser();
            ((KilkariResponseUser)user).setSubscriptionPackList(new HashSet<>(Arrays.asList("pack123")));
        } else {
            failureReasons.append(String.format(INVALID, "serviceName"));
        }

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        return user;
    }
}
