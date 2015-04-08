package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.contract.BadRequest;
import org.motechproject.nms.api.web.contract.KilkariResponseUser;
import org.motechproject.nms.api.web.contract.KilkariResponseUserExisting;
import org.motechproject.nms.api.web.contract.KilkariResponseUserNewUnknownLanguage;
import org.motechproject.nms.language.service.LanguageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


@RequestMapping("/kilkari")
@Controller
public class KilkariController {


    public static final String NOT_PRESENT = "<%s: Not Present>";
    public static final String INVALID = "<%s: Invalid>";
    public static final Pattern CALLING_NUMBER_PATTERN = Pattern.compile(
            "[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]");

    @Autowired
    private LanguageService languageService;


    @RequestMapping("/user")
    @ResponseBody
    public KilkariResponseUser user(@RequestParam String callingNumber, @RequestParam String operator, @RequestParam String circle,
                       @RequestParam String callId) {
        Set<String> languages = languageService.getCircleLanguages(circle);
        StringBuilder failureReasons = new StringBuilder();

        if (callingNumber == null) {
            failureReasons.append(String.format(NOT_PRESENT, "callingNumber"));
        } else if (!CALLING_NUMBER_PATTERN.matcher(callingNumber).matches()) {
            failureReasons.append(String.format(INVALID, "callingNumber"));
        }
        if (operator == null) {
            failureReasons.append(String.format(NOT_PRESENT, "operator"));
        }
        if (circle == null) {
            failureReasons.append(String.format(NOT_PRESENT, "circle"));
        }
        if (callId == null) {
            failureReasons.append(String.format(NOT_PRESENT, "callId"));
        }

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        if (languages.size() > 0) {
            return new KilkariResponseUserExisting(circle, languages.iterator().next(),
                    new HashSet<String>(Arrays.asList("pack123")));
        } else {
            return new KilkariResponseUserNewUnknownLanguage(circle, "??");
        }
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public BadRequest handleException(Exception e) throws IOException {
        return new BadRequest(e.getMessage());
    }
}
