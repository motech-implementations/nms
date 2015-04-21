package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.exception.NotFoundException;
import org.motechproject.nms.api.web.contract.BadRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

/**
 * BaseController
 */
public class BaseController {
    public static final String MOBILE_ACADEMY = "mobileacademy";
    public static final String MOBILE_KUNJI = "mobilekunji";
    public static final String KILKARI = "kilkari";

    public static final String NOT_PRESENT = "<%s: Not Present>";
    public static final String INVALID = "<%s: Invalid>";
    public static final String NOT_FOUND = "<%s: Not Found>";

    public static final int CALLING_NUMBER_LENGTH = 10;
    public static final int CALL_ID_LENGTH = 15;

    protected StringBuilder validate(Long callingNumber, Long callId) {
        StringBuilder failureReasons = new StringBuilder();

        if (callingNumber == null) {
            failureReasons.append(String.format(NOT_PRESENT, "callingNumber"));
        } else if (CALLING_NUMBER_LENGTH != String.valueOf(callingNumber).length()) {
            failureReasons.append(String.format(INVALID, "callingNumber"));
        }

        if (callId == null) {
            failureReasons.append(String.format(NOT_PRESENT, "callId"));
        } else if (CALL_ID_LENGTH != String.valueOf(callId).length()) {
            failureReasons.append(String.format(INVALID, "callId"));
        }

        return failureReasons;
    }

    protected StringBuilder validate(Long callingNumber, String operator, String circle, Long callId) {
        StringBuilder failureReasons = validate(callingNumber, callId);

        if (operator == null) {
            failureReasons.append(String.format(NOT_PRESENT, "operator"));
        }

        if (circle == null) {
            failureReasons.append(String.format(NOT_PRESENT, "circle"));
        }

        return failureReasons;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public BadRequest handleException(IllegalArgumentException e) throws IOException {
        return new BadRequest(e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public BadRequest handleException(NotFoundException e) throws IOException {
        return new BadRequest(e.getMessage());
    }
}
