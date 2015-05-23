package org.motechproject.nms.api.web.contract.mobileAcademy;

/**
 * API response object for getting course version
 */
public class CourseVersionResponse {

    private long courseVersion;

    public CourseVersionResponse() {
    }

    public CourseVersionResponse(long courseVersion) {

        this.courseVersion = courseVersion;
    }

    public Long getCourseVersion() {
        return courseVersion;
    }

    public void setCourseVersion(Long courseVersion) {
        this.courseVersion = courseVersion;
    }
}
