package org.motechproject.nms.mobileacademy.domain.course;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Contains details about file to be played as the Quiz Header
 */
@Entity
public class QuizContent {

    /**
     * This contains detail about the file to be played before the quiz
     */
    @Field
    private QuizContentMenu menu;

    public QuizContent() {
    }

    public QuizContentMenu getMenu() {
        return menu;
    }

    public void setMenu(QuizContentMenu menu) {
        this.menu = menu;
    }
}
