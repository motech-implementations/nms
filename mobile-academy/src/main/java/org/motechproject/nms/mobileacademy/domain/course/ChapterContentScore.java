package org.motechproject.nms.mobileacademy.domain.course;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import java.util.List;

/**
 * This field contains information about the different files to be played at the end of chapter depending
 * upon the user’s score in the quiz.
 */
@Entity
public class ChapterContentScore {

    /**
     * This is a id for the Score files of the chapter in the format ”Chapter<ChapterId>_Score”, where
     * chapterId varies from 01 to 11.
     */
    @Field
    private String identifier;

    /**
     * It contains list of audio files to be played at the time of completion of chapter
     * depending upon the score of user in quiz. For instance, first file in the list specifies the file
     * to be played if user has scored zero in quiz, Second file in the list has to be played if user
     * has scored one in quiz and so on
     */
    @Field
    private List<String> files;

    public ChapterContentScore() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
