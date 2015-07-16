package org.motechproject.nms.api.web;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.nms.api.web.contract.LogHelper;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseVersionResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.GetBookmarkResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.SaveBookmarkRequest;
import org.motechproject.nms.api.web.contract.mobileAcademy.SmsStatusRequest;
import org.motechproject.nms.api.web.contract.mobileAcademy.sms.DeliveryInfo;
import org.motechproject.nms.api.web.converter.MobileAcademyConverter;
import org.motechproject.nms.api.web.validator.MobileAcademyValidator;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.dto.MaCourse;
import org.motechproject.nms.mobileacademy.exception.CourseNotCompletedException;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;


/**
 * Mobile Academy controller
 */
@RequestMapping("mobileacademy")
@Controller
public class MobileAcademyController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileAcademyController.class);

    private static final String SMS_STATUS_SUBJECT = "nms.ma.sms.deliveryStatus";

    /**
     * MA service to handle all business logic
     */
    @Autowired
    private MobileAcademyService mobileAcademyService;

    /**
     * Event relay service to handle async notifications
     */
    private EventRelay eventRelay;

    // Default constructor for CGLIB generation
    public MobileAcademyController() {
        super();
    }
    /**
     * Constructor for controller
     * @param mobileAcademyService mobile academy service
     * @param eventRelay event relay service
     */
    @Autowired
    public MobileAcademyController(MobileAcademyService mobileAcademyService, EventRelay eventRelay) {
        this.mobileAcademyService = mobileAcademyService;
        this.eventRelay = eventRelay;
    }

    /**
     *
     * 2.2.2.1 Get MA Course – Request
     *
     * Get course
     * @return course response object
     */
    @RequestMapping(
            value = "/course",
            method = RequestMethod.GET)
    @ResponseBody
    public CourseResponse getCourse() {

        log("/mobileacademy/course");

        MaCourse getCourse = mobileAcademyService.getCourse();

        if (getCourse == null) {
            LOGGER.error("No course found in database. Check course ingestion and name");
            throw new InternalError(String.format(NOT_FOUND, "course"));
        }

        CourseResponse response = MobileAcademyConverter.convertCourseDto(getCourse);

        if (response != null) {
            return response;
        } else {
            LOGGER.error("Failed dto mapping, check object mapping");
            throw new InternalError(String.format(INVALID, "CourseResponse"));
        }

    }

    /**
     * Get the version of the course
     * @return Integer representing the timestamp since epoch
     */
    @RequestMapping(
            value = "/courseVersion",
            method = RequestMethod.GET)
    @ResponseBody
    public CourseVersionResponse getCourseVersion() {

        log("/mobileacademy/courseVersion");

        return new CourseVersionResponse(mobileAcademyService.getCourseVersion());
    }

    /**
     * Get bookmark for a user
     * @param callingNumber number of the caller
     * @param callId unique tracking id for the call
     * @return serialized json bookmark response
     */
    @RequestMapping(
            value = "/bookmarkWithScore",
            method = RequestMethod.GET)
    @ResponseBody
    public GetBookmarkResponse getBookmarkWithScore(@RequestParam Long callingNumber,
                                                 @RequestParam Long callId) {

        log("/mobileacademy/bookmarkWithScore (GET)", String.format("callingNumber=%s, callId=%s",
                LogHelper.obscure(callingNumber), callId));

        StringBuilder errors = new StringBuilder();
        validateField10Digits(errors, "callingNumber", callingNumber);
        if (errors.length() != 0) {
            throw new IllegalArgumentException(errors.toString());
        }

        validateField15Digits(errors, "callId", callId);
        if (errors.length() != 0) {
            throw new IllegalArgumentException(errors.toString());
        }

        MaBookmark bookmark = mobileAcademyService.getBookmark(callingNumber, callId);
        return MobileAcademyConverter.convertBookmarkDto(bookmark);
    }

    /**
     * Save a bookmark for a user
     * @param bookmarkRequest info about the user for bookmark save
     * @return OK or exception
     */
    @RequestMapping(
            value = "/bookmarkWithScore",
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void saveBookmarkWithScore(@RequestBody SaveBookmarkRequest bookmarkRequest) {

        log("/mobileacademy/bookmarkWithScore (POST)", LogHelper.nullOrString(bookmarkRequest));

        if (bookmarkRequest == null) {
            throw new IllegalArgumentException(String.format(INVALID, "bookmarkRequest"));
        }

        StringBuilder errors = new StringBuilder();
        validateField10Digits(errors, "callingNumber", bookmarkRequest.getCallingNumber());
        if (errors.length() != 0) {
            throw new IllegalArgumentException(errors.toString());
        }

        validateField15Digits(errors, "callId", bookmarkRequest.getCallId());
        if (errors.length() != 0) {
            throw new IllegalArgumentException(errors.toString());
        }

        MaBookmark bookmark = MobileAcademyConverter.convertSaveBookmarkRequest(bookmarkRequest);
        mobileAcademyService.setBookmark(bookmark);
    }

    /**
     * Save sms
     * @param smsDeliveryStatus sms delivery details
     * @return OK or exception
     */
    @RequestMapping(
            value = "/smsdeliverystatus",
            method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void saveSmsStatus(@RequestBody SmsStatusRequest smsDeliveryStatus) {

        log("/mobileacademy/smsdeliverystatus (POST)", LogHelper.nullOrString(smsDeliveryStatus));

        String errors = MobileAcademyValidator.validateSmsStatus(smsDeliveryStatus);

        if (errors != null) {
            throw new IllegalArgumentException(errors);
        }

        //TODO: should this be refactored into IMI module or sms module?
        // we updated the completion record. Start event message to trigger notification workflow
        DeliveryInfo deliveryInfo = smsDeliveryStatus.getRequestData()
                .getDeliveryInfoNotification().getDeliveryInfo();
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("address", deliveryInfo.getAddress());
        eventParams.put("deliveryStatus", deliveryInfo.getDeliveryStatus().toString());
        MotechEvent motechEvent = new MotechEvent(SMS_STATUS_SUBJECT, eventParams);
        eventRelay.sendEventMessage(motechEvent);
        LOGGER.debug("Sent event message to process completion notification");
    }

    @RequestMapping(
            value = "/notify",
            method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void sendNotification(@RequestBody Long callingNumber) {

        log("/mobileacademy/notify", String.format("callingNumber=%s", LogHelper.obscure(callingNumber)));

        StringBuilder errors = new StringBuilder();
        validateField10Digits(errors, "callingNumber", callingNumber);
        if (errors.length() != 0) {
            throw new IllegalArgumentException(errors.toString());
        }

        // done with validation
        try {
            mobileAcademyService.triggerCompletionNotification(callingNumber);
        } catch (CourseNotCompletedException cnc) {
            LOGGER.error("Could not send notification: " + cnc.toString());
            throw cnc;
        }
    }

}
