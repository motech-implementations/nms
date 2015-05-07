package org.motechproject.nms.mobileacademy.domain.course;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

/**
 * Contains the details about the menu file to be played at the end of the lesson.
 */
@Entity
public class LessonContentMenu extends MdsEntity {

    /**
     * This is a id for the End menu file of the lesson in the format
     * ”Chapter<ChapterId>_LessonEndMenu<LessonId>”, where chapterId varies from 01 to 11 and
     * LessonId varies from 01 to 04.
     */
    @Field
    private String identifier;

    /**
     * Name of audio file to be played at the end of lesson for prompting the user to either repeat
     * the lesson or go to next lesson.
     */
    @Field
    private String file;

    public LessonContentMenu() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
