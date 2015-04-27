package org.motechproject.nms.api.web.contract.mobileAcademy.course;

/**
 * Question metadata
 */
public class Question {

    private String name;

    private String correctAnswerOption;

    private QuestionContent content;

    public Question() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCorrectAnswerOption() {
        return correctAnswerOption;
    }

    public void setCorrectAnswerOption(String correctAnswerOption) {
        this.correctAnswerOption = correctAnswerOption;
    }

    public QuestionContent getContent() {
        return content;
    }

    public void setContent(QuestionContent content) {
        this.content = content;
    }
}
