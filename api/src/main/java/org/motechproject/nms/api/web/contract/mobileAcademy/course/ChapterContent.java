package org.motechproject.nms.api.web.contract.mobileAcademy.course;

/**
 * Inner class for chapter content for course response
 */
public class ChapterContent {

    private ChapterContentMenu menu;

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
