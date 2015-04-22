package org.motechproject.nms.api.web.contract.mobileAcademy;

/**
 * Get the version of the MA course
 */
public class CourseVersionResponse {

    private int courseVersion;

    public int getCourseVersion() {
        return courseVersion;
    }

    public void setCourseVersion(int courseVersion) {
        this.courseVersion = courseVersion;
    }
}
