package org.motechproject.nms.api.web.contract.mobileAcademy.course;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Lesson content
 */
public class LessonContent {

    @NotNull
    @Valid
    private LessonContentLesson lesson;

    @NotNull
    @Valid
    private LessonContentMenu menu;

    public LessonContent() {
    }

    public LessonContentLesson getLesson() {
        return lesson;
    }

    public void setLesson(LessonContentLesson lesson) {
        this.lesson = lesson;
    }

    public LessonContentMenu getMenu() {
        return menu;
    }

    public void setMenu(LessonContentMenu menu) {
        this.menu = menu;
    }
}
