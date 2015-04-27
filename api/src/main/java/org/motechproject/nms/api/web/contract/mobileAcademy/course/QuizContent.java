package org.motechproject.nms.api.web.contract.mobileAcademy.course;

import java.util.List;

/**
 * Quiz content for get course request containing menu options and questions
 */
public class QuizContent {

    private QuizMenu menu;

    private List<Question> questions;

    public QuizContent(){
    }

    public QuizMenu getMenu() {
        return menu;
    }

    public void setMenu(QuizMenu menu) {
        this.menu = menu;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
