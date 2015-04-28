package org.motechproject.nms.mobileacademy.domain.course;

/**
 * This will contain details about a particular lesson of a particular chapter.
 */
public class Lesson {

    /**
     * Specifies the name of the lesson in format of “Lesson<lessonId>”, where lessonId will be from 01 to 04.
     */
    private String name;

    /**
     * Contains details about actual content files to be played while playing a lesson.
     */
    private LessonContent content;

    public Lesson() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LessonContent getContent() {
        return content;
    }

    public void setContent(LessonContent content) {
        this.content = content;
    }
}
