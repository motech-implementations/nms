package org.motechproject.nms.api.web.contract.mobileAcademy.course;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Quiz object in course response containing quiz content
 */
public class Quiz {

    @NotNull
    private String name;

    @NotNull
    @Valid
    private QuizContent content;

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
}
