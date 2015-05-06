package org.motechproject.nms.mobileacademy.domain.course;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import java.util.List;

/**
 * This will contain details about a particular chapter.
 */
@Entity
public class Chapter extends MdsEntity {

    /**
     * Specifies the name of the chapter in format of “Chapter<chapterId>”, where
     * chapterId will be from 01 to 11.
     */
    @Field
    private String name;

    /**
     * Contains details about end menu file and score files.
     */
    @Field
    private ChapterContent content;

    /**
     * Specifies the list of lessons in a given chapter alongwith their details. The list will contain
     * four elements, one for each lesson.
     */
    @Field
    private List<Lesson> lessons;

    /**
     * This section contains information about various files to be played during the quiz.
     */
    @Field
    private Quiz quiz;

    public Chapter() {
    }

    public Chapter(String name, ChapterContent content, List<Lesson> lessons, Quiz quiz) {
        this.name = name;
        this.content = content;
        this.lessons = lessons;
        this.quiz = quiz;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChapterContent getContent() {
        return content;
    }

    public void setContent(ChapterContent content) {
        this.content = content;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
}
