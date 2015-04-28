package org.motechproject.nms.mobileacademy.domain.course;

/**
 * Contains details about actual content files to be played while playing a lesson.
 */
public class LessonContentLesson {

    /**
     * This is a id for the Content file of the lesson in the format”Chapter<ChapterId>_Lesson<LessonId>”,
     * where ChapterId varies from 01 to 11 and LessonId varies from 01 to 04.
     */
    private String id;

    /**
     * Name of audio file to be played containing actual audio content for the lesson.
     */
    private String file;

    public LessonContentLesson() {
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
