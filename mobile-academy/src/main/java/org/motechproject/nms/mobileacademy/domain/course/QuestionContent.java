package org.motechproject.nms.mobileacademy.domain.course;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Internal question content class
 * * This contains details about various files to be played during the question.
 */
@Entity
public class QuestionContent {

    /**
     * Specifies the name of audio file to be played for the question.
     */
    @Field
    private String question;

    /**
     * 2.2.2.2.1 Not explain in spec, but probably used for id of the question
     */
    @Field
    private String identifier;

    /**
     * Specifies the name of audio file to be played if user has provided correct DTMF input
     * in answer to above question.
     */
    @Field
    private String correctAnswer;

    /**
     * Specifies the name of audio file to be played if user has not provided correct DTMF input
     * in answer to above question.
     */
    @Field
    private String wrongAnswer;

    public QuestionContent() {
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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
