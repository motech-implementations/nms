package org.motechproject.nms.mobileacademy.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Course object containing all the course related information and metadata
 */
public class MaCourse {

    @JsonProperty("name")
    private String name;

    @JsonProperty("courseVersion")
    private Long version;

    @JsonProperty("chapters")
    private String content;

    public MaCourse() {
    }

    public MaCourse(String name, Long version, String content) {
        this.name = name;
        this.version = version;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
