package org.motechproject.nms.mobileacademy.domain.course;

/**
 * Contains details about end menu file and score files.
 */
public class ChapterContent {

    /**
     * Contains the details about the menu file to be played at the end of the chapter
     */
    private ChapterContentMenu menu;

    /**
     * This field contains information about the different files to be played at the end of chapter depending
     * upon the user’s score in the quiz.
     */
    private ChapterContentScore score;

    public ChapterContent() {
    }

    public ChapterContentMenu getMenu() {
        return menu;
    }

    public void setMenu(ChapterContentMenu menu) {
        this.menu = menu;
    }

    public ChapterContentScore getScore() {
        return score;
    }

    public void setScore(ChapterContentScore score) {
        this.score = score;
    }
}
