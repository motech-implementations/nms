package org.motechproject.nms.api.web;

import org.apache.commons.lang.NotImplementedException;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseVersionResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.GetBookmarkResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.SaveBookmarkRequest;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * Mobile Academy controller
 */
@RequestMapping("mobileacademy")
@Controller
public class MobileAcademyController extends BaseController {

    /**
     * MA service to handle all business logic
     */
    private MobileAcademyService mobileAcademyService;

    /**
     * Constructor for controller
     * @param mobileAcademyService mobile academy service
     */
    @Autowired
    public MobileAcademyController(MobileAcademyService mobileAcademyService) {
        this.mobileAcademyService = mobileAcademyService;
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

        // TODO: hook up to get course in MA service
        return new CourseResponse();
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
    public GetBookmarkResponse getBookmarkWithScore(@RequestParam Long callingNumber,
                                                 @RequestParam Long callId) {

        return new GetBookmarkResponse();
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
    public void saveBookmarkWithScore(@RequestBody SaveBookmarkRequest bookmarkRequest) {

        Long callingNumber = bookmarkRequest.getCallingNumber();
        if (callingNumber == null || callingNumber < SMALLEST_10_DIGIT_NUMBER || callingNumber > LARGEST_10_DIGIT_NUMBER ) {
            throw new IllegalArgumentException(String.format(INVALID, "callingNumber"));
        }
        if (bookmarkRequest.getCallId() == null || bookmarkRequest.getCallId() < SMALLEST_15_DIGIT_NUMBER) {
            throw new IllegalArgumentException(String.format(INVALID, "callId"));
        }

        MaBookmark bookmark = new MaBookmark(bookmarkRequest.getCallingNumber(), bookmarkRequest.getCallId(),
                bookmarkRequest.getBookmark(), bookmarkRequest.getScoresByChapter());
        mobileAcademyService.setBookmark(bookmark);
    }

    /**
     * Save sms
     * @param smsDeliveryStatusRequest sms delivery details
     * @return OK or exception
     */
    @RequestMapping(
            value = "/smsdelivery",
            method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void saveSmsStatus(@RequestBody String smsDeliveryStatusRequest) {

        // placeholder for void returns
        // TBD in Sprint 2: https://github.com/motech-implementations/mim/issues/150 and will be implemented
        // using the SMS module
        throw new NotImplementedException();
    }

}
