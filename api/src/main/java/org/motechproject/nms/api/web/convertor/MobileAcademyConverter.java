package org.motechproject.nms.api.web.convertor;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.ChapterContentMenu;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.ChapterContentScore;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.LessonContentLesson;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.LessonContentMenu;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.QuestionContent;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.QuizContentMenu;
import org.motechproject.nms.mobileacademy.domain.Course;

import java.util.ArrayList;

/**
 * Translator helper module that converts from the service dto object to
 * API response object
 */
public final class MobileAcademyConverter {

    private static ModelMapper modelMapper;

    /**
     * Private constructor for static MA course converters
     */
    private MobileAcademyConverter() {

    }

    /**
     * Converts the course service dto to api response object
     * @param course course dto
     * @return CourseResponse API object
     */
    public static CourseResponse convertCourse(Course course) {

        CourseResponse response = new CourseResponse();
        response.setName(course.getName());
        response.setCourseVersion((int) course.getModificationDate().getMillis());
        response.setChapters(
                new ArrayList<org.motechproject.nms.api.web.contract.mobileAcademy.course.Chapter>());

        return response;
    }




    /**
     * Convert the course response api object to course service dto
     * @param courseResponse course response api object
     * @return course service dto
     */
    public static Course convertCourseResponse(CourseResponse courseResponse) {

        PropertyMap<ChapterContentMenu, org.motechproject.nms.mobileacademy.domain.course.ChapterContentMenu> chapterContentMap =
                new PropertyMap<ChapterContentMenu, org.motechproject.nms.mobileacademy.domain.course.ChapterContentMenu>() {
            @Override
            protected void configure() {
                map().setIdentifier(source.getId());
                map().setFile(source.getFiles());
            }
        };

        PropertyMap<ChapterContentScore, org.motechproject.nms.mobileacademy.domain.course.ChapterContentScore> chapterScoreMap = new PropertyMap<ChapterContentScore, org.motechproject.nms.mobileacademy.domain.course.ChapterContentScore>() {
            @Override
            protected void configure() {
                map().setIdentifier(source.getId());
                map().setFiles(source.getFiles());
            }
        };

        PropertyMap<LessonContentLesson, org.motechproject.nms.mobileacademy.domain.course.LessonContentLesson> lessonContentLessonMap = new PropertyMap<LessonContentLesson, org.motechproject.nms.mobileacademy.domain.course.LessonContentLesson>() {
            @Override
            protected void configure() {
                map().setIdentifier(source.getId());
                map().setFile(source.getFile());
            }
        };

        PropertyMap<LessonContentMenu, org.motechproject.nms.mobileacademy.domain.course.LessonContentMenu> lessonContentMenuMap = new PropertyMap<LessonContentMenu, org.motechproject.nms.mobileacademy.domain.course.LessonContentMenu>() {
            @Override
            protected void configure() {
                map().setIdentifier(source.getId());
                map().setFile(source.getFile());
            }
        };

        PropertyMap<QuizContentMenu, org.motechproject.nms.mobileacademy.domain.course.QuizContentMenu> quizMap = new PropertyMap<QuizContentMenu, org.motechproject.nms.mobileacademy.domain.course.QuizContentMenu>() {
            @Override
            protected void configure() {
                map().setIdentifier(source.getId());
                map().setFile(source.getFile());
            }
        };

        PropertyMap<QuestionContent, org.motechproject.nms.mobileacademy.domain.course.QuestionContent> questionMap = new PropertyMap<QuestionContent, org.motechproject.nms.mobileacademy.domain.course.QuestionContent>() {
            @Override
            protected void configure() {
                map().setIdentifier(source.getId());
                map().setQuestion(source.getQuestion());
                map().setCorrectAnswer(source.getCorrectAnswer());
                map().setWrongAnswer(source.getWrongAnswer());
            }
        };

        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
        modelMapper.addMappings(chapterContentMap);
        modelMapper.addMappings(chapterScoreMap);
        modelMapper.addMappings(lessonContentLessonMap);
        modelMapper.addMappings(lessonContentMenuMap);
        modelMapper.addMappings(quizMap);
        modelMapper.addMappings(questionMap);
        return modelMapper.map(courseResponse, Course.class);
    }
}
