package org.motechproject.nms.api.web.contract.mobileAcademy.course;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Chapter inner class for course response
 */
public class Chapter {

    @NotNull
    private String name;

    @NotNull
    @Valid
    private ChapterContent content;

    @NotNull
    @Size(min = 4, max = 4)
    @Valid
    private List<Lesson> lessons;

    @NotNull
    @Valid
    private Quiz quiz;

    public Chapter() {
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
