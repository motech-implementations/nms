package org.motechproject.nms.api.ut;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.nms.api.utils.CourseBuilder;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.Chapter;;
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

}
