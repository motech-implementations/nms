package org.motechproject.nms.api.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.api.utils.CourseBuilder;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.SmsStatusRequest;
import org.motechproject.nms.api.web.contract.mobileAcademy.sms.DeliveryInfo;
import org.motechproject.nms.api.web.contract.mobileAcademy.sms.DeliveryInfoNotification;
import org.motechproject.nms.api.web.contract.mobileAcademy.sms.DeliveryStatus;
import org.motechproject.nms.api.web.contract.mobileAcademy.sms.RequestData;
import org.motechproject.nms.api.web.validator.MobileAcademyValidator;

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
    public void TestValidateChapterNull() {

        courseResponse.setChapters(null);
        assertNotNull(MobileAcademyValidator.validateCourseResponse(courseResponse));
    }

    @Test
    public void TestSmsStatusClientCorrelatorNull() {
        SmsStatusRequest status = GenerateValidSmsStatus();
        status.getRequestData().getDeliveryInfoNotification().setClientCorrelator(null);
        String errors = MobileAcademyValidator.validateSmsStatus(status);
        assertNotNull(errors);
        assertTrue(errors.contains("ClientCorrelator"));
    }

    @Test
    public void TestSmsStatusDeliveryStatusNull() {

        SmsStatusRequest status = GenerateValidSmsStatus();
        status.getRequestData().getDeliveryInfoNotification().getDeliveryInfo().setDeliveryStatus(null);
        String errors = MobileAcademyValidator.validateSmsStatus(status);
        assertNotNull(errors);
        assertTrue(errors.contains("DeliveryStatus"));
    }

    @Test
    public void TestSmsStatusAddressFormatInvalid() {

        SmsStatusRequest status = GenerateValidSmsStatus();
        status.getRequestData().getDeliveryInfoNotification().getDeliveryInfo().setAddress("987654321");
        String errors = MobileAcademyValidator.validateSmsStatus(status);
        assertNotNull(errors);
        assertTrue(errors.contains("Address"));
    }

    @Test
    public void TestSmsStatusAddressValidNull() {

        SmsStatusRequest status = GenerateValidSmsStatus();
        status.getRequestData().getDeliveryInfoNotification().getDeliveryInfo().setAddress(null);
        String errors = MobileAcademyValidator.validateSmsStatus(status);
        assertNotNull(errors);
        assertTrue(errors.contains("Address"));
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
