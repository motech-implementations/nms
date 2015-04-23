package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.contract.BadRequest;
import org.motechproject.nms.api.web.exception.NotFoundException;
import org.motechproject.nms.props.domain.CallDisconnectReason;
import org.motechproject.nms.props.domain.CallStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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

    public static final long SMALLEST_10_DIGIT_NUMBER = 1000000000L;
    public static final long LARGEST_10_DIGIT_NUMBER  = 9999999999L;
    public static final long SMALLEST_15_DIGIT_NUMBER = 100000000000000L;
    public static final long LARGEST_15_DIGIT_NUMBER  = 999999999999999L;

    protected static boolean validateFieldPresent(StringBuilder errors, String fieldName, Object value) {
        if (value != null) {
            return true;
        }
        errors.append(String.format(NOT_PRESENT, fieldName));
        return false;
    }

    private static boolean validateField10Digits(StringBuilder errors, String fieldName, Long value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (value >= SMALLEST_10_DIGIT_NUMBER && value <= LARGEST_10_DIGIT_NUMBER) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    private static boolean validateField15Digits(StringBuilder errors, String fieldName, Long value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (value >= SMALLEST_15_DIGIT_NUMBER && value <= LARGEST_15_DIGIT_NUMBER) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    protected static boolean validateFieldCallStatus(StringBuilder errors, String fieldName, Integer value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (CallStatus.isValid(value)) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    protected static boolean validateFieldCallDisconnectReason(StringBuilder errors, String fieldName,
                                                               Integer value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (CallDisconnectReason.isValid(value)) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    protected StringBuilder validate(Long callingNumber, Long callId) {
        StringBuilder failureReasons = new StringBuilder();

        validateField10Digits(failureReasons, "callingNumber", callingNumber);
        validateField15Digits(failureReasons, "callId", callId);

        return failureReasons;
    }

    protected StringBuilder validate(Long callingNumber, Long callId, String operator, String circle) {
        StringBuilder failureReasons = validate(callingNumber, callId);

        validateFieldPresent(failureReasons, "operator", operator);
        validateFieldPresent(failureReasons, "circle", circle);

        return failureReasons;
    }


    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public BadRequest handleException(IllegalArgumentException e) {
        return new BadRequest(e.getMessage());
    }


    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public BadRequest handleException(NotFoundException e) {
        return new BadRequest(e.getMessage());
    }


    /**
     * Handles malformed JSON, returns a slightly more informative message than a generic HTTP-400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BadRequest handleException(HttpMessageNotReadableException e) {
        return new BadRequest(e.getMessage());
    }


}
