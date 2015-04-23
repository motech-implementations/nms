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

import java.util.regex.Pattern;

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

    public static final Pattern NUMERIC_PATTERN = Pattern.compile("[1-9][0-9]*|0");
    public static final Pattern NUMERIC_PATTERN_10 = Pattern.compile("[1-9][0-9]{9}");
    public static final Pattern NUMERIC_PATTERN_15 = Pattern.compile("[1-9][0-9]{14}");

    protected static boolean validateFieldPresent(StringBuilder errors, String fieldName, String value) {
        if (value != null) {
            return true;
        }
        errors.append(String.format(NOT_PRESENT, fieldName));
        return false;
    }

    private static boolean validateFieldNumericPattern(Pattern pattern, StringBuilder errors, String fieldName,
                                                String value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (pattern.matcher(value).matches()) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    protected static boolean validateFieldNumeric(StringBuilder errors, String fieldName, String value) {
        return validateFieldNumericPattern(NUMERIC_PATTERN, errors, fieldName, value);
    }

    protected static boolean validateFieldNumeric10(StringBuilder errors, String fieldName, String value) {
        return validateFieldNumericPattern(NUMERIC_PATTERN_10, errors, fieldName, value);
    }

    protected static boolean validateFieldNumeric15(StringBuilder errors, String fieldName, String value) {
        return validateFieldNumericPattern(NUMERIC_PATTERN_15, errors, fieldName, value);
    }

    protected static boolean validateFieldCallStatus(StringBuilder errors, String fieldName, String value) {
        if (!validateFieldNumericPattern(NUMERIC_PATTERN, errors, fieldName, value)) {
            return false;
        }
        if (CallStatus.isValid(Integer.parseInt(value))) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    protected static boolean validateFieldCallDisconnectReason(StringBuilder errors, String fieldName, String value) {
        if (!validateFieldNumericPattern(NUMERIC_PATTERN, errors, fieldName, value)) {
            return false;
        }
        if (CallDisconnectReason.isValid(Integer.parseInt(value))) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    protected StringBuilder validate(String callingNumber, String callId) {
        StringBuilder failureReasons = new StringBuilder();

        validateFieldNumeric10(failureReasons, "callingNumber", callingNumber);
        validateFieldNumeric15(failureReasons, "callId", callId);

        return failureReasons;
    }

    protected StringBuilder validate(String callingNumber, String operator, String circle, String callId) {
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
