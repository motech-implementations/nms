package org.motechproject.nms.api.web.contract.mobileAcademy.course;

/**
 * Lesson content lesson metadata
 */
public class LessonContentLesson {

    private String id;

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
