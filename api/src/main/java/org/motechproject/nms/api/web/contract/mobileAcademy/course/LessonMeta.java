package org.motechproject.nms.api.web.contract.mobileAcademy.course;

/**
 * Lesson metadata
 */
public class LessonMeta {

    private String id;

    private String file;

    public LessonMeta() {
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
