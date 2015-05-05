package org.motechproject.nms.api.ut;

import org.junit.Test;
import org.motechproject.nms.api.utils.CourseBuilder;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.convertor.MobileAcademyConverter;
import org.motechproject.nms.mobileacademy.domain.Course;

import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for converting between MA dto and MA api response objects
 */
public class MobileAcademyConverterUnitTest {

    @Test
    public void TestCourseResponseConversion() {
        CourseResponse response = CourseBuilder.generateValidCourseResponse();
        Course course = MobileAcademyConverter.convertCourseResponse(response);
        assertNotNull(course);
    }

}
