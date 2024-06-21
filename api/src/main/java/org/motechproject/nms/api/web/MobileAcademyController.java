
package org.motechproject.nms.api.web;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseVersionResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.GetBookmarkResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.SaveBookmarkRequest;
import org.motechproject.nms.api.web.contract.mobileAcademy.SmsStatusRequest;
import org.motechproject.nms.api.web.contract.mobileAcademy.sms.DeliveryInfo;
import org.motechproject.nms.api.web.converter.MobileAcademyConverter;
import org.motechproject.nms.api.web.validator.MobileAcademyValidator;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.dto.MaCourse;
import org.motechproject.nms.mobileacademy.exception.CourseNotCompletedException;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.motechproject.nms.props.service.LogHelper;
import org.motechproject.nms.rch.service.RchWebServiceFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;


/**
 * Mobile Academy controller
 */
@RequestMapping("mobileacademy")
@Controller
public class MobileAcademyController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileAcademyController.class);

    private static final String SMS_STATUS_SUBJECT = "nms.ma.sms.deliveryStatus";

    // TODO: take this stateIds from properties file
    public static final Set<Integer> stateIds = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(1, 2, 10, 20)));

    /**
     * MA service to handle all business logic
     */
    @Autowired
    private MobileAcademyService mobileAcademyService;

    @Autowired
    private FrontLineWorkerService frontLineWorkerService;

    @Autowired
    private RchWebServiceFacade rchWebServiceFacade;

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
    @Transactional(readOnly = true)
    @RequestMapping(
            value = "/course/{version}",
            method = RequestMethod.GET)
    @ResponseBody
    public CourseResponse getCourse(@PathVariable long version) {

        log("REQUEST: /mobileacademy/course/{}" , String.valueOf(version));

        MaCourse getCourse = mobileAcademyService.getCourse(version);

        if (getCourse == null) {
            LOGGER.error("No course found in database. Check course ingestion and name");
            throw new InternalError(String.format(NOT_FOUND, "course"));
        }

        CourseResponse response = MobileAcademyConverter.convertCourseDto(getCourse);

        if (response == null) {
            LOGGER.error("Failed dto mapping, check object mapping");
            throw new InternalError(String.format(INVALID, "CourseResponse"));
        }

        log("RESPONSE: /mobileacademy/course", response.toString());
        return response;
    }

    /**
     * Get the version of the course
     * @return Integer representing the timestamp since epoch
     * ***NOTE::*** THIS IS USED by the LOAD BALANCER and NAGIOS monitoring to certify server health,
     * do not delete, or be careful when you refactor!! With great power comes great responsibility!!
     */
    @Transactional(readOnly = true)
    @RequestMapping(
            value = "/courseVersion",
            method = RequestMethod.GET)
    @ResponseBody
    public CourseVersionResponse getCourseVersion() {

        log("REQUEST: /mobileacademy/courseVersion");

        CourseVersionResponse response = new CourseVersionResponse(mobileAcademyService.getCourseVersion());
        log("RESPONSE: /mobileacademy/courseVersion", response.toString());
        return response;
    }

    /**
     * Get bookmark for a user
     * @param callingNumber number of the caller
     * @param callId unique tracking id for the call
     * @return serialized json bookmark response
     */
    @Transactional(readOnly = true)
    @RequestMapping(
            value = "/bookmarkWithScore",
            method = RequestMethod.GET)
    @ResponseBody
    public GetBookmarkResponse getBookmarkWithScore(@RequestParam(required = false) Long callingNumber,
                                                    @RequestParam(required = false) String callId) {

        log("REQUEST: /mobileacademy/bookmarkWithScore", String.format("callingNumber=%s, callId=%s",
                LogHelper.obscure(callingNumber), callId));

        StringBuilder errors = new StringBuilder();
        validateField10Digits(errors, "callingNumber", callingNumber);
        if (errors.length() != 0) {
            throw new IllegalArgumentException(errors.toString());
        }

        validateCallId(errors, callId);
        if (errors.length() != 0) {
            throw new IllegalArgumentException(errors.toString());
        }

        MaBookmark bookmark = mobileAcademyService.getBookmark(callingNumber, callId);

        GetBookmarkResponse ret = MobileAcademyConverter.convertBookmarkDto(bookmark);
        log("RESPONSE: /mobileacademy/bookmarkWithScore", String.format("callId=%s, %s", callId, ret.toString()));
        return ret;
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

        log("REQUEST: /mobileacademy/bookmarkWithScore (POST)", LogHelper.nullOrString(bookmarkRequest));

        // validate bookmark
        if (bookmarkRequest == null) {
            throw new IllegalArgumentException(String.format(INVALID, "bookmarkRequest"));
        }

        // validate calling number
        StringBuilder errors = new StringBuilder();
        validateField10Digits(errors, "callingNumber", bookmarkRequest.getCallingNumber());
        if (errors.length() != 0) {
            throw new IllegalArgumentException(errors.toString());
        }

        // validate call id
        validateCallId(errors, bookmarkRequest.getCallId());
        if (errors.length() != 0) {
            throw new IllegalArgumentException(errors.toString());
        }

        // validate scores
        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(bookmarkRequest.getCallingNumber());
        Long flwId = flw.getId();
        if(stateIds.contains(flw.getState()) && validateMA2Scores(bookmarkRequest.getScoresByChapter(), bookmarkRequest.getBookmark())){
            MaBookmark bookmark = MobileAcademyConverter.convertSaveBookmarkRequest(bookmarkRequest, flwId);
            mobileAcademyService.setMA2Bookmark(bookmark);
            mobileAcademyService.processMA2Bookmark(flw , bookmark.getScoresByChapter());
        }
        else if (validateMAScores(bookmarkRequest.getScoresByChapter(), bookmarkRequest.getBookmark())) {
            MaBookmark bookmark = MobileAcademyConverter.convertSaveBookmarkRequest(bookmarkRequest, flwId);
            mobileAcademyService.setBookmark(bookmark);
        }
    }

    /**
     * Save sms status. The request mapping value is not ideal here but updating it since it would cost
     * more effort to do a CR with IMI at this point
     * @param smsDeliveryStatus sms delivery details
     * @return OK or exception
     */
    @Transactional
    @RequestMapping(
            value = "/sms/status/imi",
            method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void saveSmsStatus(@RequestBody SmsStatusRequest smsDeliveryStatus) {

        log("REQUEST: /mobileacademy/sms/status/imi (POST)", LogHelper.nullOrString(smsDeliveryStatus));

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

    @RequestMapping(value = "/readxmlFilesWithoutRejecting" , method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void addRchData(){
        log("REQUEST: /ops/readxmlFilesWithoutRejecting");
        String remoteLocation = "/usr/local/xmlUpdate";
        LOGGER.info("Calling API to readAllDataFromXMLFile. ");
        rchWebServiceFacade.readAllDataFromXMLFile(remoteLocation);
    }

    @Transactional
    @RequestMapping(
            value = "/notify",
            method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void sendNotification(@RequestBody Long flwId) {

        log("REQUEST: /mobileacademy/notify (POST)", String.format("flwId=%s", String.valueOf(flwId)));

        // done with validation
        try {
            mobileAcademyService.triggerCompletionNotification(flwId);
        } catch (CourseNotCompletedException cnc) {
            LOGGER.error("Could not send notification: " + cnc.toString());
            throw cnc;
        }
    }

    @RequestMapping(value = "/readBeneficiaryXmlFiles",
            method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void updateRchData(){
        log("REQUEST: /ops/readBeneficiaryXmlFiles");
        String remoteLocation = "/usr/local/ManualRCH/";
        rchWebServiceFacade.readBeneficiaryDataFromFile(remoteLocation);
    }

}
