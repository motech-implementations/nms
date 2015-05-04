package org.motechproject.nms.api.web.contract.mobileAcademy.course;

/**
 * Question metadata
 */
public class Question {

    private String name;

    private int correctAnswerOption;

    private QuestionContent content;

    public Question() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCorrectAnswerOption() {
        return correctAnswerOption;
    }

    public void setCorrectAnswerOption(int correctAnswerOption) {
        this.correctAnswerOption = correctAnswerOption;
    }

    public QuestionContent getContent() {
        return content;
    }

    public void setContent(QuestionContent content) {
        this.content = content;
    }
}
