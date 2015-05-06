package org.motechproject.nms.api.web.converter;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.ValidationException;
import org.modelmapper.convention.MatchingStrategies;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.ChapterContentMenu;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.ChapterContentScore;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.LessonContentLesson;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.LessonContentMenu;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.QuestionContent;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.QuizContentMenu;
import org.motechproject.nms.mobileacademy.domain.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translator helper module that converts from the service dto object to
 * API response object
 */
public final class MobileAcademyConverter {

    private static ModelMapper modelMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileAcademyConverter.class);

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
    @SuppressWarnings("CPD-START")
    public static CourseResponse convertCourse(Course course) {

        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);

        PropertyMap<org.motechproject.nms.mobileacademy.domain.course.ChapterContentMenu, ChapterContentMenu> chapterContentMap =
                new PropertyMap<org.motechproject.nms.mobileacademy.domain.course.ChapterContentMenu, ChapterContentMenu>() {
                    @Override
                    protected void configure() {
                        map().setId(source.getIdentifier());
                        map().setFile(source.getFile());
                    }
        };

        PropertyMap<org.motechproject.nms.mobileacademy.domain.course.ChapterContentScore, ChapterContentScore> chapterScoreMap =
                new PropertyMap<org.motechproject.nms.mobileacademy.domain.course.ChapterContentScore, ChapterContentScore>() {
                    @Override
                    protected void configure() {
                        map().setId(source.getIdentifier());
                        map().setFiles(source.getFiles());
                    }
        };

        PropertyMap<org.motechproject.nms.mobileacademy.domain.course.LessonContentLesson, LessonContentLesson> lessonContentLessonMap =
                new PropertyMap<org.motechproject.nms.mobileacademy.domain.course.LessonContentLesson, LessonContentLesson>() {
                    @Override
                    protected void configure() {
                        map().setId(source.getIdentifier());
                        map().setFile(source.getFile());
                    }
        };

        PropertyMap<org.motechproject.nms.mobileacademy.domain.course.LessonContentMenu, LessonContentMenu> lessonContentMenuMap =
                new PropertyMap< org.motechproject.nms.mobileacademy.domain.course.LessonContentMenu, LessonContentMenu>() {
                    @Override
                    protected void configure() {
                        map().setId(source.getIdentifier());
                        map().setFile(source.getFile());
                    }
        };

        PropertyMap<org.motechproject.nms.mobileacademy.domain.course.QuizContentMenu, QuizContentMenu> quizMap =
                new PropertyMap<org.motechproject.nms.mobileacademy.domain.course.QuizContentMenu, QuizContentMenu>() {
                    @Override
                    protected void configure() {
                        map().setId(source.getIdentifier());
                        map().setFile(source.getFile());
                    }
        };

        PropertyMap<org.motechproject.nms.mobileacademy.domain.course.QuestionContent, QuestionContent> questionMap =
                new PropertyMap<org.motechproject.nms.mobileacademy.domain.course.QuestionContent, QuestionContent>() {
                    @Override
                    protected void configure() {
                        map().setId(source.getIdentifier());
                        map().setQuestion(source.getQuestion());
                        map().setCorrectAnswer(source.getCorrectAnswer());
                        map().setWrongAnswer(source.getWrongAnswer());
                    }
        };


        modelMapper.addMappings(chapterContentMap);
        modelMapper.addMappings(chapterScoreMap);
        modelMapper.addMappings(lessonContentLessonMap);
        modelMapper.addMappings(lessonContentMenuMap);
        modelMapper.addMappings(quizMap);
        modelMapper.addMappings(questionMap);

        // the validate checks if any of the source and target properties are unmapped
        try {
            modelMapper.validate();
            return modelMapper.map(course, CourseResponse.class);
        } catch (ValidationException ve) {
            LOGGER.error(ve.toString());
            return null;
        }
    }

    /**
     * Convert the course response api object to course service dto
     * @param courseResponse course response api object
     * @return course service dto
     */
    @SuppressWarnings("CPD-END")
    public static Course convertCourseResponse(CourseResponse courseResponse) {

        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);

        PropertyMap<ChapterContentMenu, org.motechproject.nms.mobileacademy.domain.course.ChapterContentMenu> chapterContentMap =
                new PropertyMap<ChapterContentMenu, org.motechproject.nms.mobileacademy.domain.course.ChapterContentMenu>() {
                    @Override
                    protected void configure() {
                        map().setIdentifier(source.getId());
                        map().setFile(source.getFile());
                    }
        };

        PropertyMap<ChapterContentScore, org.motechproject.nms.mobileacademy.domain.course.ChapterContentScore> chapterScoreMap =
                new PropertyMap<ChapterContentScore, org.motechproject.nms.mobileacademy.domain.course.ChapterContentScore>() {
                    @Override
                    protected void configure() {
                        map().setIdentifier(source.getId());
                        map().setFiles(source.getFiles());
                    }
        };

        PropertyMap<LessonContentLesson, org.motechproject.nms.mobileacademy.domain.course.LessonContentLesson> lessonContentLessonMap =
                new PropertyMap<LessonContentLesson, org.motechproject.nms.mobileacademy.domain.course.LessonContentLesson>() {
                    @Override
                    protected void configure() {
                        map().setIdentifier(source.getId());
                        map().setFile(source.getFile());
                    }
        };

        PropertyMap<LessonContentMenu, org.motechproject.nms.mobileacademy.domain.course.LessonContentMenu> lessonContentMenuMap =
                new PropertyMap<LessonContentMenu, org.motechproject.nms.mobileacademy.domain.course.LessonContentMenu>() {
                    @Override
                    protected void configure() {
                        map().setIdentifier(source.getId());
                        map().setFile(source.getFile());
                    }
        };

        PropertyMap<QuizContentMenu, org.motechproject.nms.mobileacademy.domain.course.QuizContentMenu> quizMap =
                new PropertyMap<QuizContentMenu, org.motechproject.nms.mobileacademy.domain.course.QuizContentMenu>() {
                    @Override
                    protected void configure() {
                        map().setIdentifier(source.getId());
                        map().setFile(source.getFile());
                    }
        };

        PropertyMap<QuestionContent, org.motechproject.nms.mobileacademy.domain.course.QuestionContent> questionMap =
                new PropertyMap<QuestionContent, org.motechproject.nms.mobileacademy.domain.course.QuestionContent>() {
                    @Override
                    protected void configure() {
                        map().setIdentifier(source.getId());
                        map().setQuestion(source.getQuestion());
                        map().setCorrectAnswer(source.getCorrectAnswer());
                        map().setWrongAnswer(source.getWrongAnswer());
                    }
        };

        modelMapper.addMappings(chapterContentMap);
        modelMapper.addMappings(chapterScoreMap);
        modelMapper.addMappings(lessonContentLessonMap);
        modelMapper.addMappings(lessonContentMenuMap);
        modelMapper.addMappings(quizMap);
        modelMapper.addMappings(questionMap);

        // the validate checks if any of the source and target properties are unmapped
        try {
            modelMapper.validate();
            return modelMapper.map(courseResponse, Course.class);
        } catch (ValidationException ve) {
            LOGGER.error(ve.toString());
            return null;
        }
    }
}
