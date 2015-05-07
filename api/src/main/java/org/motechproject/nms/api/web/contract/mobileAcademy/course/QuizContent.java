package org.motechproject.nms.api.web.contract.mobileAcademy.course;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Quiz content for get course request containing menu options and questions
 */
public class QuizContent {

    @NotNull
    @Valid
    private QuizContentMenu menu;

    @NotNull
    @Size(min = 4, max = 4)
    @Valid
    private List<Question> questions;

    public QuizContent() {
    }

    public QuizContentMenu getMenu() {
        return menu;
    }

    public void setMenu(QuizContentMenu menu) {
        this.menu = menu;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
