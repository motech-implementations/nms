package org.motechproject.nms.mobileacademy.domain.course;

/**
 * Contains details about file to be played as the Quiz Header
 */
public class QuizContent {

    /**
     * This contains detail about the file to be played before the quiz
     */
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
