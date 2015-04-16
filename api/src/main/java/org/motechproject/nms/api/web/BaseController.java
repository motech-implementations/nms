package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.exception.NotFoundException;
import org.motechproject.nms.api.web.contract.BadRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * BaseController
 */
public class BaseController {
    public static final String NOT_PRESENT = "<%s: Not Present>";
    public static final String INVALID = "<%s: Invalid>";
    public static final String NOT_FOUND = "<%s: Not Found>";
    public static final Pattern CALLING_NUMBER_PATTERN = Pattern.compile(
            "[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]");

    protected StringBuilder validate(String callingNumber, String callId) {
        StringBuilder failureReasons = new StringBuilder();

        if (callingNumber == null) {
            failureReasons.append(String.format(NOT_PRESENT, "callingNumber"));
        } else if (!CALLING_NUMBER_PATTERN.matcher(callingNumber).matches()) {
            failureReasons.append(String.format(INVALID, "callingNumber"));
        }

        if (callId == null) {
            failureReasons.append(String.format(NOT_PRESENT, "callId"));
        }

        return failureReasons;
    }

    protected StringBuilder validate(String callingNumber, String operator, String circle, String callId) {
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
