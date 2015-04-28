package org.motechproject.nms.mobileacademy.domain.course;

/**
 *  This contains detail about the file to be played before the quiz
 */
public class QuizContentMenu {

    /**
     * This is a id for the quiz header to be played. The format is ”Chapter<ChapterId>_QuizHeader>”,
     * where chapterId varies from 01 to 11.
     */
    private String id;

    /**
     * Specifies the name of audio file to be played at the start of the quiz
     */
    private String file;

    public QuizContentMenu() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
