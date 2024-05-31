package org.motechproject.nms.api.web.converter;

import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.GetBookmarkResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.SaveBookmarkRequest;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
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
    public static CourseResponse convertCourseDto(MaCourse course) {

        LOGGER.debug("Converting course dto to response contract");
        CourseResponse response = new CourseResponse();
        response.setName(course.getName());
        response.setCourseVersion(course.getVersion());
        response.setCourse(course.getCourse());
        response.setChapters(course.getContent());
        return response;
    }

    /**
     * Convert course response api object back to course service dto (used by tests)
     * @param courseResponse course response object
     * @return course dto
     */
    public static MaCourse convertCourseResponse(CourseResponse courseResponse) {
        MaCourse course = new MaCourse();
        course.setName(courseResponse.getName());
        course.setVersion(courseResponse.getCourseVersion());
        course.setContent(courseResponse.getChapters().toString());
        return course;
    }

    /**
     * Convert bookmark dto to api response object
     * @param bookmark bookmark dto
     * @return api response object
     */
    public static GetBookmarkResponse convertBookmarkDto(MaBookmark bookmark) {
        GetBookmarkResponse response = new GetBookmarkResponse();
        if (bookmark != null) {
            response.setBookmark(bookmark.getBookmark());
            response.setScoresByChapter(bookmark.getScoresByChapter());
        }
        return response;
    }

    /**
     * Convert api request object to bookmark dto
     * @param saveBookmarkRequest api request object
     * @return bookmark dto
     */
    public static MaBookmark convertSaveBookmarkRequest(SaveBookmarkRequest saveBookmarkRequest, Long flwId) {
        MaBookmark bookmark = new MaBookmark();
        bookmark.setFlwId(flwId);
        bookmark.setCallId(saveBookmarkRequest.getCallId());
        bookmark.setBookmark(saveBookmarkRequest.getBookmark());
        bookmark.setScoresByChapter(saveBookmarkRequest.getScoresByChapter());
        return bookmark;
    }
}
