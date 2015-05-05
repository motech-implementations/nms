package org.motechproject.nms.api.ut;

import org.junit.Test;
import org.motechproject.nms.api.utils.CourseBuilder;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.Chapter;;
import org.motechproject.nms.api.web.validator.MobileAcademyValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.util.Set;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Unit test for course structure validation
 */
public class MobileAcademyValidatorUnitTest {

    @Test
    public void TestValidCourseStructure() {

        CourseResponse courseResponse = CourseBuilder.generateValidCourseResponse();
        assertNull(MobileAcademyValidator.ValidateCourseResponse(courseResponse));
    }

    @Test
    public void TestValidateChapterCount() {

        CourseResponse courseResponse = CourseBuilder.generateValidCourseResponse();
        courseResponse.getChapters().add(new Chapter());
        String errorString = MobileAcademyValidator.ValidateCourseResponse(courseResponse);
        assertNotNull(errorString);
    }

    @Test
    public void TestValidateChapterNull() {

        CourseResponse courseResponse = CourseBuilder.generateValidCourseResponse();
        courseResponse.setChapters(null);
        assertNotNull(MobileAcademyValidator.ValidateCourseResponse(courseResponse));
    }

    @Test
    public void TestValidateLessonNull() {
        CourseResponse courseResponse = CourseBuilder.generateValidCourseResponse();
        courseResponse.getChapters().get(0).setLessons(null);
        assertNotNull(MobileAcademyValidator.ValidateCourseResponse(courseResponse));
    }

}
