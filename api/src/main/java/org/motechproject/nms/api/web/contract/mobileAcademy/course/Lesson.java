package org.motechproject.nms.api.web.contract.mobileAcademy.course;

/**
 * Lesson data object
 */
public class Lesson {

    private String name;

    private LessonContent content;

    public Lesson() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LessonContent getContent() {
        return content;
    }

    public void setContent(LessonContent content) {
        this.content = content;
    }
}
