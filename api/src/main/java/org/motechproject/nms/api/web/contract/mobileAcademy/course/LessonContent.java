package org.motechproject.nms.api.web.contract.mobileAcademy.course;

/**
 * Lesson content
 */
public class LessonContent {

    private LessonContentLesson lesson;

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
