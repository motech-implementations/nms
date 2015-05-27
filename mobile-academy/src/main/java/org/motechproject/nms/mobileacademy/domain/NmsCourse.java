package org.motechproject.nms.mobileacademy.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;

/**
 * Course entity to store the contents
 */
@Entity(tableName = "nms_ma_course")
public class NmsCourse extends MdsEntity {

    @Field
    @Unique
    private String name;

    @Field
    @Column(length = 38912) // 38kb to be sure that we can fit the content
    private String content;

    public NmsCourse() {
    }

    public NmsCourse(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
