package org.motechproject.nms.api.web;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.motechproject.nms.api.web.contract.BadRequest;
import org.motechproject.nms.api.web.exception.NotAuthorizedException;
import org.motechproject.nms.api.web.exception.NotDeployedException;
import org.motechproject.nms.api.web.exception.NotFoundException;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.service.WhitelistService;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.props.domain.CallDisconnectReason;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.props.service.PropertyService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.service.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

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

    public static final String IVR_INTERACTION_LOG = "IVR INTERACTION: %s";

    public static final long SMALLEST_10_DIGIT_NUMBER = 1000000000L;
    public static final long LARGEST_10_DIGIT_NUMBER  = 9999999999L;
    public static final long SMALLEST_15_DIGIT_NUMBER = 100000000000000L;
    public static final long LARGEST_15_DIGIT_NUMBER  = 999999999999999L;
    public static final int CALL_ID_LENGTH = 25;
    public static final long MILLIS = 1000;
    public static final int MAX_LENGTH_255 = 255;
    public static final int MA_MIN_SCORE = 0;
    public static final int MA_MAX_SCORE = 4;
    public static final int MA2_MIN_SCORE = 0;
    public static final int MA2_MAX_SCORE = 1;
    public static final String CALLING_NUMBER = "callingNumber";

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);
    public static final String LOG_RESPONSE_FORMAT = "RESPONSE: %s";

    @Autowired
    private WhitelistService whitelistService;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private FrontLineWorkerService frontLineWorkerService;

    @Autowired
    private StateService stateService;

    protected static void log(final String endpoint, final String s) {
        LOGGER.info(IVR_INTERACTION_LOG.format(endpoint) + (StringUtils.isBlank(s) ? "" : " : " + s));
    }

    protected static void log(final String endpoint) {
        log(endpoint, null);
    }

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

    protected static boolean validateDeactivationReason(StringBuilder errors, String fieldName, String value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        DeactivationReason reason = DeactivationReason.valueOf(value);
        if (reason.equals(DeactivationReason.WEEKLY_CALLS_NOT_ANSWERED) || reason.equals(DeactivationReason.LOW_LISTENERSHIP) || reason.equals(DeactivationReason.mMITRA)) {
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

    protected static boolean validateCallId(StringBuilder errors, String value) {

        if (value == null || value.isEmpty()) {
            errors.append(String.format(NOT_PRESENT, "callId"));
            return false;
        }

        if (value.length() == CALL_ID_LENGTH) {
            return true;
        }
        errors.append(String.format(INVALID, "callId"));
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

    protected static boolean validateFieldGfStatus(StringBuilder errors, String fieldName, String value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (("Active").equals(value) || ("Inactive").equals(value)) {
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

    protected StringBuilder validate(Long callingNumber, String callId) {
        StringBuilder failureReasons = new StringBuilder();

        validateField10Digits(failureReasons, "callingNumber", callingNumber);
        validateCallId(failureReasons, callId);

        return failureReasons;
    }

    protected StringBuilder validate(Long callingNumber, String callId, String operator, String circle) {
        StringBuilder failureReasons = validate(callingNumber, callId);

        validateFieldMaxLength(failureReasons, "operator", operator, MAX_LENGTH_255);

        validateFieldMaxLength(failureReasons, "circle", circle, MAX_LENGTH_255);

        return failureReasons;
    }

    protected DateTime epochToDateTime(long epoch) {
        return new DateTime(epoch * MILLIS); // epoch time sent by IVR is in secs
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
            Set<State> states = stateService.getAllInCircle(circle);

            if (states.size() == 1) {
                state = states.iterator().next();
            }
        }

        return state;
    }

    protected boolean frontLineWorkerAuthorizedForAccess(FrontLineWorker flw, State state) {
        return whitelistService.numberWhitelistedForState(state, flw.getContactNumber());
    }

    /**
     * Check if the service is deployed for a given circle
     * @param service service to check
     * @param circle circle of the caller
     * @return true if circle is null/unknown, or
     * true if no states in circle or
     * true if one state in circle is authorized, otherwise
     * false if none of the states in circle are deployed
     */
    protected boolean serviceDeployedInCircle(Service service, Circle circle) {

        if (circle == null) {
            return true;
        }

        Set<State> states = stateService.getAllInCircle(circle);
        if (states == null || states.isEmpty()) { // No state available
            return true;
        }

        for (State currentState : states) { // multiple states, false if undeployed in all states
            if (serviceDeployedInUserState(service, currentState)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if the service is deployed for a given state
     * @param service service to check
     * @param state (geographical)state of the user
     * @return true, if state is null or
     * true, if state is deployed
     * false, if state is not deployed
     */
    protected boolean serviceDeployedInUserState(Service service, State state) {

        return propertyService.isServiceDeployedInState(service, state);
    }

    protected  boolean validateMAScores(Map<String, Integer> scores, String bookmark) {
        if (scores != null) {
            for (Integer currentScore : scores.values()) {
                if (currentScore < MA_MIN_SCORE || currentScore > MA_MAX_SCORE) {
                    throw new IllegalArgumentException(String.format(INVALID, "scoresByChapter"));
                }
            }
            for (String key : scores.keySet()){
                if(bookmark!= null && !bookmark.isEmpty()) {
                    if(key.equals("0") && !bookmark.startsWith("Chapter01_")){
                        throw new IllegalArgumentException(String.format(INVALID, "scoresByChapter"));
                    }
                }
            }
        }
        return true;
    }


    protected  boolean validateMA2Scores(Map<String, Integer> scores, String bookmark) {
        if (scores != null) {
            for (Integer currentScore : scores.values()) {
                if (currentScore < MA2_MIN_SCORE || currentScore > MA2_MAX_SCORE) {
                    throw new IllegalArgumentException(String.format(INVALID, "scoresByChapter"));
                }
            }
            for (String key : scores.keySet()){
                if(bookmark!= null && !bookmark.isEmpty()) {
                    if(key.equals("0") && !bookmark.startsWith("Module0")) {
                        throw new IllegalArgumentException(String.format(INVALID, "scoresByChapter"));
                    }
                }
            }

            if(bookmark.contains("StartMenu") && (bookmark.contains("01") && scores.size()!=0) ||
                    (bookmark.contains("02") && scores.size()!=25) ||
                    (bookmark.contains("03") && scores.size()!=50 ) ){
                LOGGER.debug("Size of score map is mismatching");
                throw new IllegalArgumentException(String.format(INVALID, "scoresByChapter"));
            }
            else if(bookmark.contains("Quiz") && bookmark.length()==15){ //Module02_Quiz24
                int moduleNo = Integer.parseInt(bookmark.substring(7,8));
                int chapter = Integer.parseInt(bookmark.substring(13,15));
                int allowedSize = chapter>12 ? ((moduleNo-1)*25 + chapter-1) : ((moduleNo-1)*25 + chapter);
                if(scores.size()!=allowedSize){
                    throw new IllegalArgumentException(String.format(INVALID, "scoresByChapter"));
                }
            }
            else if(bookmark.contains("Transition") && bookmark.length()==28){ //Module02_Lesson24_Transition
                int moduleNo = Integer.parseInt(bookmark.substring(7,8));
                int chapter = Integer.parseInt(bookmark.substring(15,17));
                int allowedSize = chapter>12 ? ((moduleNo-1)*25 + chapter) : ((moduleNo-1)*25 + chapter +1);
                if(scores.size()!=allowedSize){
                    throw new IllegalArgumentException(String.format(INVALID, "scoresByChapter"));
                }
            }
            else if(bookmark.contains("Lesson") && bookmark.length()==17){ //Module02_Lesson24
                int moduleNo = Integer.parseInt(bookmark.substring(7,8));
                int chapter = Integer.parseInt(bookmark.substring(15,17));
                int allowedSize = chapter>12 ? ((moduleNo-1)*25 + chapter-1) : ((moduleNo-1)*25 + chapter);
                if(scores.size()!=allowedSize){
                    throw new IllegalArgumentException(String.format(INVALID, "scoresByChapter"));
                }
            }
            else if(bookmark.contains("FeedBack") && bookmark.length()==19){ //Module01_Feedback01
                int moduleNo = Integer.parseInt(bookmark.substring(7,8));
                int allowedSize = (moduleNo-1)*25 + 12;
                if(scores.size()!=allowedSize){
                    throw new IllegalArgumentException(String.format(INVALID, "scoresByChapter"));
                }
            }
            else{
                throw new IllegalArgumentException(String.format(INVALID, "scoresByChapter"));
            }
        }
        return true;
    }


    @ExceptionHandler(NotAuthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public BadRequest handleException(NotAuthorizedException e, HttpServletRequest request) {
        log(String.format(LOG_RESPONSE_FORMAT, request.getRequestURI()), e.getMessage());
        return new BadRequest(e.getMessage());
    }

    @ExceptionHandler(NotDeployedException.class)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    @ResponseBody
    public BadRequest handleException(NotDeployedException e, HttpServletRequest request) {
        log(String.format(LOG_RESPONSE_FORMAT, request.getRequestURI()), e.getMessage());
        return new BadRequest(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public BadRequest handleException(IllegalArgumentException e, HttpServletRequest request) {
        log(String.format(LOG_RESPONSE_FORMAT, request.getRequestURI()), e.getMessage());
        return new BadRequest(e.getMessage());
    }


    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public BadRequest handleException(NotFoundException e, HttpServletRequest request) {
        log(String.format(LOG_RESPONSE_FORMAT, request.getRequestURI()), e.getMessage());
        return new BadRequest(e.getMessage());
    }


    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public BadRequest handleException(NullPointerException e, HttpServletRequest request) {
        log(String.format(LOG_RESPONSE_FORMAT, request.getRequestURI()), e.getMessage());
        LOGGER.error("Internal Server Error", e);
        return new BadRequest(e.getMessage());
    }


    /**
     * Handles malformed JSON, returns a slightly more informative message than a generic HTTP-400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BadRequest handleException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log(String.format(LOG_RESPONSE_FORMAT, request.getRequestURI()), e.getMessage());
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
