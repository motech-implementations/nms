package org.motechproject.nms.mobileacademy.domain.course;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

/**
 * This will contain details about a particular lesson of a particular chapter.
 */
@Entity
public class Lesson extends MdsEntity {

    /**
     * Specifies the name of the lesson in format of “Lesson<lessonId>”, where lessonId will be from 01 to 04.
     */
    @Field
    private String name;

    /**
     * Contains details about actual content files to be played while playing a lesson.
     */
    @Field
    private LessonContent content;

    public Lesson() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LessonContent getContent() {
        return content;
    }

    public void setContent(LessonContent content) {
        this.content = content;
    }
}
