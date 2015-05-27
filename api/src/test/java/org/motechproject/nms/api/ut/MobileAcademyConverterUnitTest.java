package org.motechproject.nms.api.ut;

import org.junit.Test;
import org.motechproject.nms.api.utils.CourseBuilder;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.converter.MobileAcademyConverter;
import org.motechproject.nms.mobileacademy.dto.MaCourse;

import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for converting between MA dto and MA api response objects
 */
public class MobileAcademyConverterUnitTest {

    @Test
    public void TestCourseConversion() {

        // ideally this course would be generated like the call above, but not enough time now
        MaCourse course = MobileAcademyConverter.convertCourseResponse(CourseBuilder.generateValidCourseResponse());
        CourseResponse response = MobileAcademyConverter.convertCourse(course);
        assertNotNull(course);
    }

    // TODO: more tests when the json course ingestion is completed.

}
