package org.motechproject.nms.mobileacademy.domain.course;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Lesson content class
 */
@Entity
public class LessonContent {

    /**
     * Contains details about actual content files to be played while playing a lesson.
     */
    @Field
    private LessonContentLesson lesson;

    /**
     * Contains the details about the menu file to be played at the end of the lesson.
     */
    @Field
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
