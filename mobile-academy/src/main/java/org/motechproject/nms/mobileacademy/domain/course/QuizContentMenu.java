package org.motechproject.nms.mobileacademy.domain.course;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 *  This contains detail about the file to be played before the quiz
 */
@Entity
public class QuizContentMenu {

    /**
     * This is a id for the quiz header to be played. The format is ”Chapter<ChapterId>_QuizHeader>”,
     * where chapterId varies from 01 to 11.
     */
    @Field
    private String identifier;

    /**
     * Specifies the name of audio file to be played at the start of the quiz
     */
    @Field
    private String file;

    public QuizContentMenu() {
    }

    public QuizContentMenu(String identifier, String file) {
        this.identifier = identifier;
        this.file = file;
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
