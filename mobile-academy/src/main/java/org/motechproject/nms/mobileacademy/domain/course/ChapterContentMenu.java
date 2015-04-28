package org.motechproject.nms.mobileacademy.domain.course;

/**
 * Contains the details about the menu file to be played at the end of the chapter
 */
public class ChapterContentMenu {

    /**
     * This is id for the End menu file of the chapter in the format”Chapter<ChapterId>_EndMenu”, where
     * chapterId varies from 01 to 11.
     */
    private String id;

    /**
     * Name of audio file to be played at the end of chapter for prompting the user to either repeat
     * the chapter or go to next chapter.
     */
    private String file;

    public ChapterContentMenu() {
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
