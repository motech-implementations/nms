package org.motechproject.nms.mobileacademy.domain.course;

/**
 * Created by kosh on 4/28/15.
 */
public class LessonContent {

    /**
     * Contains details about actual content files to be played while playing a lesson.
     */
    private LessonContentLesson lesson;

    /**
     * Contains the details about the menu file to be played at the end of the lesson.
     */
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
