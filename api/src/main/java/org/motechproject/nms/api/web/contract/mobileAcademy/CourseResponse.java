package org.motechproject.nms.api.web.contract.mobileAcademy;



import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;

/**
 * Response object for get course API 2.2.2
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    @NotNull
    @JsonRawValue
    private Object course;

    public Object getCourse() { return course;}

    public void setCourse(Object course) {this.course = course;}

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
