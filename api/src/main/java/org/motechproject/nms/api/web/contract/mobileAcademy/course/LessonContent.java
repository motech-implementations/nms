package org.motechproject.nms.api.web.contract.mobileAcademy.course;

/**
 * Lesson content
 */
public class LessonContent {

    private LessonMeta lesson;

    private LessonContentMenu menu;

    public LessonContent() {
    }

    public LessonMeta getLesson() {
        return lesson;
    }

    public void setLesson(LessonMeta lesson) {
        this.lesson = lesson;
    }

    public LessonContentMenu getMenu() {
        return menu;
    }

    public void setMenu(LessonContentMenu menu) {
        this.menu = menu;
    }
}
