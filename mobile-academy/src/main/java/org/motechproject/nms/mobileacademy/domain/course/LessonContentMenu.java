package org.motechproject.nms.mobileacademy.domain.course;

/**
 * Contains the details about the menu file to be played at the end of the lesson.
 */
public class LessonContentMenu {

    /**
     * This is a id for the End menu file of the lesson in the format
     * ”Chapter<ChapterId>_LessonEndMenu<LessonId>”, where chapterId varies from 01 to 11 and
     * LessonId varies from 01 to 04.
     */
    private String id;

    /**
     * Name of audio file to be played at the end of lesson for prompting the user to either repeat
     * the lesson or go to next lesson.
     */
    private String file;

    public LessonContentMenu() {
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
