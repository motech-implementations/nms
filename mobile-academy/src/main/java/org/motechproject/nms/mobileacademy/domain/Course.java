package org.motechproject.nms.mobileacademy.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;
import org.motechproject.nms.mobileacademy.domain.course.Chapter;

import javax.jdo.annotations.Unique;
import java.util.List;

/**
 * Mobile Academy course entity
 */
@Entity(tableName = "nms_ma_course")
public class Course extends MdsEntity {

    @Field
    @Unique
    private String name;

    @Field
    private List<Chapter> chapters;

    public Course() {
    }

    public Course(String name, List<Chapter> chapters) {
        this.name = name;
        this.chapters = chapters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }
}
