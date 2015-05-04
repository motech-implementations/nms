package org.motechproject.nms.api.web.convertor;

import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.mobileacademy.domain.Course;

/**
 * Translator helper module that converts from the service dto object to
 * API response object
 */
public final class MobileAcademyConvertor {

    /**
     * Private constructor for static MA course convertors
     */
    private MobileAcademyConvertor() {

    }

    /**
     * Converts the course service dto to api response object
     * @param course course dto
     * @return CourseResponse API object
     */
    public static CourseResponse convertCourse(Course course) {

        return new CourseResponse();
    }
}
