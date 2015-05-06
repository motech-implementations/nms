package org.motechproject.nms.mobileacademy.domain.course;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

/**
 * Contains details about a particular question of the quiz
 */
@Entity
public class Question extends MdsEntity {

    /**
     * Specifies the name of question associated to a particular chapter in the format
     ”Question<QuestionId>”, where QuestionId varies from 01 to 04.
     */
    @Field
    private String name;

    /**
     * It specifies the DTMF input for correct answer to the given question.
     */
    @Field
    private int correctAnswerOption;

    /**
     * This contains details about various files to be played during the question.
     */
    @Field
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
