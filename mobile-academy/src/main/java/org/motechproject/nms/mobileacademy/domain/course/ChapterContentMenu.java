package org.motechproject.nms.mobileacademy.domain.course;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Contains the details about the menu file to be played at the end of the chapter
 */
@Entity
public class ChapterContentMenu {

    /**
     * This is id for the End menu file of the chapter in the format”Chapter<ChapterId>_EndMenu”, where
     * chapterId varies from 01 to 11.
     */
    @Field
    private String identifier;

    /**
     * Name of audio file to be played at the end of chapter for prompting the user to either repeat
     * the chapter or go to next chapter.
     */
    @Field
    private String file;

    public ChapterContentMenu() {
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
