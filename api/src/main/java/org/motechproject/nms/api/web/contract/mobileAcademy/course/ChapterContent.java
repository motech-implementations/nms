package org.motechproject.nms.api.web.contract.mobileAcademy.course;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Inner class for chapter content for course response
 */
public class ChapterContent {

    @NotNull
    @Valid
    private ChapterContentMenu menu;

    @NotNull
    @Valid
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
