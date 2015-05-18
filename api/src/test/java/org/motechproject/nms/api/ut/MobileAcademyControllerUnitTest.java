package org.motechproject.nms.api.ut;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.nms.api.web.BaseController;
import org.motechproject.nms.api.web.MobileAcademyController;
import org.motechproject.nms.api.web.contract.mobileAcademy.SaveBookmarkRequest;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for Mobile academy controller
 */
public class MobileAcademyControllerUnitTest {

    private MobileAcademyController mobileAcademyController;
    
    @Mock
    private MobileAcademyService mobileAcademyService;

    @Mock
    private EventRelay eventRelay;

    @Before
    public void setup() {
        mobileAcademyController = new MobileAcademyController(mobileAcademyService, eventRelay);
        initMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBookmarkNullCallingNumber() {
        SaveBookmarkRequest sb = new SaveBookmarkRequest();
        sb.setCallingNumber(null);
        sb.setCallId(BaseController.SMALLEST_15_DIGIT_NUMBER);
        mobileAcademyController.saveBookmarkWithScore(sb);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBookmarkMinCallingNumber() {
        SaveBookmarkRequest sb = new SaveBookmarkRequest();
        sb.setCallingNumber(999999999L);
        sb.setCallId(BaseController.SMALLEST_15_DIGIT_NUMBER);
        mobileAcademyController.saveBookmarkWithScore(sb);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBookmarkMaxCallingNumber() {
        SaveBookmarkRequest sb = new SaveBookmarkRequest();
        sb.setCallingNumber(10000000000L);
        sb.setCallId(BaseController.SMALLEST_15_DIGIT_NUMBER);
        mobileAcademyController.saveBookmarkWithScore(sb);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBookmarkNullCallId() {
        SaveBookmarkRequest sb = new SaveBookmarkRequest();
        sb.setCallingNumber(BaseController.SMALLEST_10_DIGIT_NUMBER);
        sb.setCallId(null);
        mobileAcademyController.saveBookmarkWithScore(sb);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBookmarkMinCallId() {
        SaveBookmarkRequest sb = new SaveBookmarkRequest();
        sb.setCallingNumber(BaseController.SMALLEST_10_DIGIT_NUMBER);
        sb.setCallId(99999999999999L);
        mobileAcademyController.saveBookmarkWithScore(sb);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTriggerBadCallingNumber() {
        long callingNumber = 1234;
        mobileAcademyController.sendNotification(callingNumber);
    }

}
