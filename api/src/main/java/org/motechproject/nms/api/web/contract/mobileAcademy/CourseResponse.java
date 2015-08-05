package org.motechproject.nms.api.web.contract.mobileAcademy;

import org.codehaus.jackson.annotate.JsonRawValue;

import javax.validation.constraints.NotNull;

/**
 * Response object for get course API 2.2.2
 */
public class CourseResponse {

    // course name
    @NotNull
    private String name;

    // course version in epoch datetime format
    @NotNull
    private long courseVersion;

    @NotNull
    @JsonRawValue
    private Object chapters;

    public CourseResponse() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCourseVersion() {
        return courseVersion;
    }

    public void setCourseVersion(long courseVersion) {
        this.courseVersion = courseVersion;
    }

    public Object getChapters() {
        return chapters;
    }

    public void setChapters(Object chapters) {
        this.chapters = chapters;
    }

    @Override
    public String toString() {
        return "CourseResponse{" +
                "name='" + name + '\'' +
                ", courseVersion=" + courseVersion +
                '}';
    }
}
