package org.motechproject.nms.api.web.contract.mobileAcademy.course;

import java.util.List;

/**
 * Chapter inner class for course response
 */
public class Chapter {

    private String name;

    private ChapterContent content;

    private List<Lesson> lessons;

    public Chapter() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChapterContent getContent() {
        return content;
    }

    public void setContent(ChapterContent content) {
        this.content = content;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }
}
