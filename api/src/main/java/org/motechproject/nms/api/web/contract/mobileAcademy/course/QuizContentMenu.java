package org.motechproject.nms.api.web.contract.mobileAcademy.course;

import javax.validation.constraints.NotNull;

/**
 * Quiz menu object to store quiz menu metadata
 */
public class QuizContentMenu {

    @NotNull
    private String id;

    @NotNull
    private String file;

    public QuizContentMenu() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
