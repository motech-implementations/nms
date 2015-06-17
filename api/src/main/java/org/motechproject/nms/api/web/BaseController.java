package org.motechproject.nms.api.web;

import org.joda.time.DateTime;
import org.motechproject.nms.api.web.contract.BadRequest;
import org.motechproject.nms.api.web.exception.NotAuthorizedException;
import org.motechproject.nms.api.web.exception.NotDeployedException;
import org.motechproject.nms.api.web.exception.NotFoundException;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.service.WhitelistService;
import org.motechproject.nms.props.domain.CallDisconnectReason;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.props.service.PropertyService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * BaseController
 *
 * Common data & helper methods
 * All error handlers
 *
 */
public class BaseController {
    public static final String MOBILE_ACADEMY = "mobileacademy";
    public static final String MOBILE_KUNJI = "mobilekunji";
    public static final String KILKARI = "kilkari";

    public static final String NOT_PRESENT = "<%s: Not Present>";
    public static final String INVALID = "<%s: Invalid>";
    public static final String NOT_FOUND = "<%s: Not Found>";
    public static final String NOT_AUTHORIZED = "<%s: Not Authorized>";
    public static final String NOT_DEPLOYED = "<%s: Not Deployed In State>";

    public static final long SMALLEST_10_DIGIT_NUMBER = 1000000000L;
    public static final long LARGEST_10_DIGIT_NUMBER  = 9999999999L;
    public static final long SMALLEST_15_DIGIT_NUMBER = 100000000000000L;
    public static final long LARGEST_15_DIGIT_NUMBER  = 999999999999999L;
    public static final int MAX_LENGTH_255 = 255;

    public static final String CALLING_NUMBER = "callingNumber";

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

    @Autowired
    private WhitelistService whitelistService;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private FrontLineWorkerService frontLineWorkerService;


    protected static boolean validateFieldPresent(StringBuilder errors, String fieldName, Object value) {
        if (value != null) {
            return true;
        }
        errors.append(String.format(NOT_PRESENT, fieldName));
        return false;
    }

    protected static boolean validateFieldString(StringBuilder errors, String fieldName, String value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (value.length() > 0) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    protected static boolean validateFieldPositiveLong(StringBuilder errors, String fieldName, Long value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (value >= 0) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    protected static boolean validateField10Digits(StringBuilder errors, String fieldName, Long value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (value >= SMALLEST_10_DIGIT_NUMBER && value <= LARGEST_10_DIGIT_NUMBER) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    protected static boolean validateField15Digits(StringBuilder errors, String fieldName, Long value) {
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
        if (FinalCallStatus.isValidEnumValue(value)) {
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
        if (CallDisconnectReason.isValidEnumValue(value)) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    protected boolean validateFieldExactLength(StringBuilder errors, String fieldName, String value, int length) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (value.length() != length) {
            errors.append(String.format(INVALID, fieldName));
            return false;
        }
        return true;
    }

    protected boolean validateRequiredFieldMaxLength(StringBuilder errors, String fieldName, String value, int length) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }

        return validateFieldMaxLength(errors, fieldName, value, length);
    }

    protected boolean validateFieldMaxLength(StringBuilder errors, String fieldName, String value, int length) {
        if (value != null && value.length() > length) {
            errors.append(String.format(INVALID, fieldName));
            return false;
        }
        return true;
    }

    protected StringBuilder validate(Long callingNumber, Long callId) {
        StringBuilder failureReasons = new StringBuilder();

        validateField10Digits(failureReasons, "callingNumber", callingNumber);
        validateField15Digits(failureReasons, "callId", callId);

        return failureReasons;
    }

    protected StringBuilder validate(Long callingNumber, Long callId, String operator, String circle) {
        StringBuilder failureReasons = validate(callingNumber, callId);

        validateRequiredFieldMaxLength(failureReasons, "operator", operator, MAX_LENGTH_255);

        validateFieldMaxLength(failureReasons, "circle", circle, MAX_LENGTH_255);

        return failureReasons;
    }

    protected DateTime epochToDateTime(long epoch) {
        return new DateTime(epoch);
    }

    protected State getStateForFrontLineWorker(FrontLineWorker flw, Circle circle) {
        State state = frontLineWorkerService.getState(flw);

        if (state == null && circle != null) {
            state = getStateFromCircle(circle);
        }

        return state;
    }




    protected State getStateFromCircle(Circle circle) {

        State state = null;

        if (circle != null) {
            List<State> states = circle.getStates();

            if (states.size() == 1) {
                state = states.get(0);
            }

        }

        return state;
    }

    protected boolean frontLineWorkerAuthorizedForAccess(FrontLineWorker flw, State state) {
        return whitelistService.numberWhitelistedForState(state, flw.getContactNumber());
    }


    protected boolean checkForServiceDeployedInMultiplestate(Service service, Circle circle) {

        List<State> states = null;

        if (null == circle) {
            return true;
        }

        states = circle.getStates();
        // Check for multiple state in same circle
        if (states != null && states.size() != 0) {

            //iterate each state and check deployment status
            for (State temp : states) {
                //If service is deployed in any  state, handle it as deployed service for all states
                if (propertyService.isServiceDeployedInState(service, temp)){
                    return true;
                }

                return false;

            }
        }
        return true;

    }

    protected boolean serviceDeployedInUserState(Service service, State state, Circle circle) {
        // If I don't have a state for the FLW let them continue further
        if (state == null) {

            return checkForServiceDeployedInMultiplestate(service, circle);
        }

        return propertyService.isServiceDeployedInState(service, state);
    }

    @ExceptionHandler(NotAuthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public BadRequest handleException(NotAuthorizedException e) {
        return new BadRequest(e.getMessage());
    }

    @ExceptionHandler(NotDeployedException.class)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    @ResponseBody
    public BadRequest handleException(NotDeployedException e) {
        return new BadRequest(e.getMessage());
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


    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public BadRequest handleException(NullPointerException e) {
        LOGGER.error("Internal Server Error", e);
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

    protected Service getServiceFromName(String serviceName) {
        Service service = null;

        if (MOBILE_ACADEMY.equals(serviceName)) {
            service = Service.MOBILE_ACADEMY;
        }

        if (MOBILE_KUNJI.equals(serviceName)) {
            service = Service.MOBILE_KUNJI;
        }

        return service;
    }
}
