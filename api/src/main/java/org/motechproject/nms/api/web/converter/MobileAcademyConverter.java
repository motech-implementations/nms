package org.motechproject.nms.api.web.converter;

import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.mobileacademy.dto.MaCourse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translator helper module that converts from the service dto object to
 * API response object
 */
public final class MobileAcademyConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileAcademyConverter.class);

    /**
     * Private constructor for static MA course converters
     */
    private MobileAcademyConverter() {

    }

    /**
     * Converts the course service dto to api response object
     * @param course course dto
     * @return CourseResponse API object
     */
    public static CourseResponse convertCourse(MaCourse course) {

        LOGGER.debug("Converting course dto to response contract");
        CourseResponse response = new CourseResponse();
        response.setName(course.getName());
        response.setCourseVersion(course.getVersion());
        response.setChapters(course.getContent());
        return response;
    }

    public static MaCourse convertCourseResponse(CourseResponse courseResponse) {
        MaCourse course = new MaCourse();
        course.setName(courseResponse.getName());
        course.setVersion(courseResponse.getCourseVersion());
        course.setContent(courseResponse.getChapters().toString());
        return course;
    }

}
