package org.motechproject.nms.api.web.convertor;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.mobileacademy.domain.Course;

/**
 * Translator helper module that converts from the service dto object to
 * API response object
 */
public final class MobileAcademyConverter {

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
    public static CourseResponse convertCourse(Course course) {

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper.map(course, CourseResponse.class);
    }
}
