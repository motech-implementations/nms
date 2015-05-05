package org.motechproject.nms.api.web.contract.mobileAcademy;

import org.motechproject.nms.api.web.contract.mobileAcademy.course.Chapter;

import java.util.List;

/**
 * Response object for get course API 2.2.2
 */
public class CourseResponse {

    // course name
    private String name;

    // course version in epoch datetime format
    private int courseVersion;

    private List<Chapter> chapters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCourseVersion() {
        return courseVersion;
    }

    public void setCourseVersion(int courseVersion) {
        this.courseVersion = courseVersion;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }
}
