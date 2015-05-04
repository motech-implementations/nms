package org.motechproject.nms.api.web.contract.mobileAcademy.course;

import java.util.List;

/**
 * Chapter content score
 */
public class ChapterContentScore {

    private String id;

    private List<String> files;

    public ChapterContentScore() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
