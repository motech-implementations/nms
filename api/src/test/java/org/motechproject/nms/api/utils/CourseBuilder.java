package org.motechproject.nms.api.utils;

import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.mobileacademy.dto.MaCourse;

/**
 * Helper to generate a course response
 */
public final class CourseBuilder {

    public static CourseResponse generateValidCourseResponse() {
        CourseResponse response = new CourseResponse();
        response.setName("MobileAcademyCourse");
        response.setCourseVersion(20150526L);
        response.setChapters("[]");
        return response;
    }

    public static MaCourse generateValidCourseDto() {
        MaCourse course = new MaCourse();
        course.setName("MobileAcademyCourse");
        course.setVersion(20150526L); // random, supposed to be millis eventually
        course.setContent("[{}]");
        return course;
    }
}
