package org.motechproject.nms.api.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.api.utils.CourseBuilder;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.SmsStatusRequest;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.Chapter;
import org.motechproject.nms.api.web.contract.mobileAcademy.sms.DeliveryInfo;
import org.motechproject.nms.api.web.contract.mobileAcademy.sms.DeliveryInfoNotification;
import org.motechproject.nms.api.web.contract.mobileAcademy.sms.DeliveryStatus;
import org.motechproject.nms.api.web.contract.mobileAcademy.sms.RequestData;
import org.motechproject.nms.api.web.validator.MobileAcademyValidator;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Unit test for course structure validation
 */
public class MobileAcademyValidatorUnitTest {

    private CourseResponse courseResponse;

    @Before
    public void setCourseResponse() {
        this.courseResponse = CourseBuilder.generateValidCourseResponse();
    }

    @Test
    public void TestValidCourseStructure() {

        assertNull(MobileAcademyValidator.validateCourseResponse(courseResponse));
    }

    @Test
    public void TestValidateChapterCount() {

        courseResponse.getChapters().add(new Chapter());
        String errorString = MobileAcademyValidator.validateCourseResponse(courseResponse);
        assertNotNull(errorString);
    }

    @Test
    public void TestValidateChapterNull() {

        courseResponse.setChapters(null);
        assertNotNull(MobileAcademyValidator.validateCourseResponse(courseResponse));
    }

    @Test
    public void TestValidateLessonNull() {

        courseResponse.getChapters().get(0).setLessons(null);
        assertNotNull(MobileAcademyValidator.validateCourseResponse(courseResponse));
    }

    @Test
    public void TestValidateCourseName() {

        assertFalse(MobileAcademyValidator.validateCourseFormat(courseResponse));
    }

    @Test
    public void TestSmsStatusClientCorrelatorNull() {
        SmsStatusRequest status = GenerateValidSmsStatus();
        status.getRequestData().getDeliveryInfoNotification().setClientCorrelator(null);
        String errors = MobileAcademyValidator.validateSmsStatus(status);
        assertNotNull(errors);
        assertTrue(errors.contains("clientCorrelator"));
    }

    @Test
    public void TestSmsStatusDeliveryStatusNull() {

        SmsStatusRequest status = GenerateValidSmsStatus();
        status.getRequestData().getDeliveryInfoNotification().getDeliveryInfo().setDeliveryStatus(null);
        String errors = MobileAcademyValidator.validateSmsStatus(status);
        assertNotNull(errors);
        assertTrue(errors.contains("deliveryStatus"));
    }

    @Test
    public void TestSmsStatusAddressFormatInvalid() {

        SmsStatusRequest status = GenerateValidSmsStatus();
        status.getRequestData().getDeliveryInfoNotification().getDeliveryInfo().setAddress("9876543210");
        String errors = MobileAcademyValidator.validateSmsStatus(status);
        assertNotNull(errors);
        assertTrue(errors.contains("address"));
    }

    @Test
    public void TestSmsStatusAddressFormatNull() {

        SmsStatusRequest status = GenerateValidSmsStatus();
        status.getRequestData().getDeliveryInfoNotification().getDeliveryInfo().setAddress("9876543210");
        String errors = MobileAcademyValidator.validateSmsStatus(status);
        assertNotNull(errors);
        assertTrue(errors.contains("address"));
    }

    private SmsStatusRequest GenerateValidSmsStatus() {
        SmsStatusRequest smsStatusRequest = new SmsStatusRequest();
        smsStatusRequest.setRequestData(new RequestData());
        smsStatusRequest.getRequestData().setDeliveryInfoNotification(new DeliveryInfoNotification());
        smsStatusRequest.getRequestData().getDeliveryInfoNotification().setClientCorrelator("FooBar");
        smsStatusRequest.getRequestData().getDeliveryInfoNotification().setDeliveryInfo(new DeliveryInfo());
        smsStatusRequest.getRequestData()
                .getDeliveryInfoNotification()
                .getDeliveryInfo()
                .setAddress("tel: 9876543219");
        smsStatusRequest.getRequestData()
                .getDeliveryInfoNotification()
                .getDeliveryInfo()
                .setDeliveryStatus(DeliveryStatus.DeliveredToTerminal);

        return smsStatusRequest;
    }
}
