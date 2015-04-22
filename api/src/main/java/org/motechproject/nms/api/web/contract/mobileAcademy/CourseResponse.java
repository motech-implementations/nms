package org.motechproject.nms.api.web.contract.mobileAcademy;

/**
 * Response object for get Course API 2.2.2
 */
public class CourseResponse {

    // course name
    private String name;

    // course version in epoch datetime format
    private Long courseVersion;

    // actual deep copy of course
    // TODO : This should probably be a course object that gets serialized than just string
    private String course;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCourseVersion() {
        return courseVersion;
    }

    public void setCourseVersion(Long courseVersion) {
        this.courseVersion = courseVersion;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }
}
