package org.motechproject.nms.mobileacademy.domain.course;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Contains details about actual content files to be played while playing a lesson.
 */
@Entity
public class LessonContentLesson {

    /**
     * This is a id for the Content file of the lesson in the format”Chapter<ChapterId>_Lesson<LessonId>”,
     * where ChapterId varies from 01 to 11 and LessonId varies from 01 to 04.
     */
    @Field
    private String identifier;

    /**
     * Name of audio file to be played containing actual audio content for the lesson.
     */
    @Field
    private String file;

    public LessonContentLesson() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String id) {
        this.identifier = identifier;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
