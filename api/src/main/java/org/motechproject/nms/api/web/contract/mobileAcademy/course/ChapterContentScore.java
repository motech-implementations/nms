package org.motechproject.nms.api.web.contract.mobileAcademy.course;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Chapter content score
 */
public class ChapterContentScore {

    @NotNull
    private String id;

    /**
     * This stores the file to play for scores ranging from 0-4
     */
    @NotNull
    @Size(min = 5, max = 5)
    @Valid
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
