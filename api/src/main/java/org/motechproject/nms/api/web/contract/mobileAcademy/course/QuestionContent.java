package org.motechproject.nms.api.web.contract.mobileAcademy.course;

import javax.validation.constraints.NotNull;

/**
 * Question content
 */
public class QuestionContent {

    @NotNull
    private String id;

    @NotNull
    private String question;

    @NotNull
    private String correctAnswer;

    @NotNull
    private String wrongAnswer;

    public QuestionContent() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getWrongAnswer() {
        return wrongAnswer;
    }

    public void setWrongAnswer(String wrongAnswer) {
        this.wrongAnswer = wrongAnswer;
    }
}
