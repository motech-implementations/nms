package org.motechproject.nms.api.web.contract.mobileAcademy.course;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Lesson data object
 */
public class Lesson {

    @NotNull
    private String name;

    @NotNull
    @Valid
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
