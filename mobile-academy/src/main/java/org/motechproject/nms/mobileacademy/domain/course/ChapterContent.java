package org.motechproject.nms.mobileacademy.domain.course;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Contains details about end menu file and score files.
 */
@Entity
public class ChapterContent {

    /**
     * Contains the details about the menu file to be played at the end of the chapter
     */
    @Field
    private ChapterContentMenu menu;

    /**
     * This field contains information about the different files to be played at the end of chapter depending
     * upon the user’s score in the quiz.
     */
    @Field
    private ChapterContentScore score;

    public ChapterContent() {
    }

    public ChapterContent(ChapterContentMenu menu, ChapterContentScore score) {
        this.menu = menu;
        this.score = score;
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
