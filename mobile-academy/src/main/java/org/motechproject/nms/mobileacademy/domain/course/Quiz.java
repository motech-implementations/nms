package org.motechproject.nms.mobileacademy.domain.course;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import java.util.List;

/**
 * This section contains information about various files to be played during the quiz.
 */
@Entity
public class Quiz {

    /**
     * Specifies the name of quiz associated to a particular chapter in the format "Quiz"
     */
    @Field
    private String name;

    /**
     * Contains details about file to be played as the Quiz Header
     */
    @Field
    private QuizContent content;

    /**
     * Contains list of questions to be played after user has listened to all four lessons in a chapter.
     * The list will contain four elements, one for each question.
     */
    @Field
    private List<Question> questions;

    public Quiz() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public QuizContent getContent() {
        return content;
    }

    public void setContent(QuizContent content) {
        this.content = content;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
