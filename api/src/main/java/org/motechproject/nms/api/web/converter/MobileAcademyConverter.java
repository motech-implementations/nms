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

        modelMapper.addMappings(getCourseChapterContentMap());
        modelMapper.addMappings(getCourseChapterScoreMap());
        modelMapper.addMappings(getCourseLessonContentLessonMap());
        modelMapper.addMappings(getCourseLessonContentMenuMap());
        modelMapper.addMappings(getCourseQuizMap());
        modelMapper.addMappings(getCourseQuestionMap());

        // the validate checks if any of the source and target properties are unmapped
        try {
            modelMapper.validate();
            return modelMapper.map(course, CourseResponse.class);
        } catch (ValidationException ve) {
            LOGGER.error(ve.toString());
            return null;
        }
    }

    private static PropertyMap<org.motechproject.nms.mobileacademy.domain.course.QuestionContent, QuestionContent> getCourseQuestionMap() {
        return new PropertyMap<org.motechproject.nms.mobileacademy.domain.course.QuestionContent, QuestionContent>() {
            @Override
            protected void configure() {
                map().setId(source.getIdentifier());
                map().setQuestion(source.getQuestion());
                map().setCorrectAnswer(source.getCorrectAnswer());
                map().setWrongAnswer(source.getWrongAnswer());
            }
};
    }

    private static PropertyMap<org.motechproject.nms.mobileacademy.domain.course.QuizContentMenu, QuizContentMenu> getCourseQuizMap() {
        return new PropertyMap<org.motechproject.nms.mobileacademy.domain.course.QuizContentMenu, QuizContentMenu>() {
            @Override
            protected void configure() {
                map().setId(source.getIdentifier());
                map().setFile(source.getFile());
            }
};
    }

    private static PropertyMap<org.motechproject.nms.mobileacademy.domain.course.LessonContentMenu, LessonContentMenu> getCourseLessonContentMenuMap() {
        return new PropertyMap<org.motechproject.nms.mobileacademy.domain.course.LessonContentMenu, LessonContentMenu>() {
            @Override
            protected void configure() {
                map().setId(source.getIdentifier());
                map().setFile(source.getFile());
            }
};
    }

    private static PropertyMap<org.motechproject.nms.mobileacademy.domain.course.LessonContentLesson, LessonContentLesson> getCourseLessonContentLessonMap() {
        return new PropertyMap<org.motechproject.nms.mobileacademy.domain.course.LessonContentLesson, LessonContentLesson>() {
            @Override
            protected void configure() {
                map().setId(source.getIdentifier());
                map().setFile(source.getFile());
            }
};
    }

    private static PropertyMap<org.motechproject.nms.mobileacademy.domain.course.ChapterContentScore, ChapterContentScore> getCourseChapterScoreMap() {
        return new PropertyMap<org.motechproject.nms.mobileacademy.domain.course.ChapterContentScore, ChapterContentScore>() {
            @Override
            protected void configure() {
                map().setId(source.getIdentifier());
                map().setFiles(source.getFiles());
            }
};
    }

    private static PropertyMap<org.motechproject.nms.mobileacademy.domain.course.ChapterContentMenu, ChapterContentMenu> getCourseChapterContentMap() {
        return new PropertyMap<org.motechproject.nms.mobileacademy.domain.course.ChapterContentMenu, ChapterContentMenu>() {
            @Override
            protected void configure() {
                map().setId(source.getIdentifier());
                map().setFile(source.getFile());
            }
};
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

        // Add model mapping for each sub-class that cannot be merged automatically
        modelMapper.addMappings(getCourseMap());
        modelMapper.addMappings(getChapterContentMenuMap());
        modelMapper.addMappings(getChapterScoreMap());
        modelMapper.addMappings(getLessonContentLessonMap());
        modelMapper.addMappings(getLessonContentMenuMap());
        modelMapper.addMappings(getQuizContentMap());
        modelMapper.addMappings(getQuestionContentMap());

        // the validate checks if any of the source and target properties are unmapped
        try {
            modelMapper.validate();
            return modelMapper.map(courseResponse, Course.class);
        } catch (ValidationException ve) {
            LOGGER.error(ve.toString());
            return null;
        }
    }

    private static PropertyMap<QuestionContent, org.motechproject.nms.mobileacademy.domain.course.QuestionContent> getQuestionContentMap() {
        return new PropertyMap<QuestionContent, org.motechproject.nms.mobileacademy.domain.course.QuestionContent>() {
            @Override
            protected void configure() {
                map().setIdentifier(source.getId());
                map().setQuestion(source.getQuestion());
                map().setCorrectAnswer(source.getCorrectAnswer());
                map().setWrongAnswer(source.getWrongAnswer());
                skip().setId(null);
                skip().setCreationDate(null);
                skip().setModificationDate(null);
                skip().setCreator(null);
                skip().setModifiedBy(null);
                skip().setOwner(null);
            }
};
    }

    private static PropertyMap<QuizContentMenu, org.motechproject.nms.mobileacademy.domain.course.QuizContentMenu> getQuizContentMap() {
        return new PropertyMap<QuizContentMenu, org.motechproject.nms.mobileacademy.domain.course.QuizContentMenu>() {
            @Override
            protected void configure() {
                map().setIdentifier(source.getId());
                map().setFile(source.getFile());
                skip().setId(null);
                skip().setCreationDate(null);
                skip().setModificationDate(null);
                skip().setCreator(null);
                skip().setModifiedBy(null);
                skip().setOwner(null);
            }
};
    }

    private static PropertyMap<LessonContentMenu, org.motechproject.nms.mobileacademy.domain.course.LessonContentMenu> getLessonContentMenuMap() {
        return new PropertyMap<LessonContentMenu, org.motechproject.nms.mobileacademy.domain.course.LessonContentMenu>() {
            @Override
            protected void configure() {
                map().setIdentifier(source.getId());
                map().setFile(source.getFile());
                skip().setId(null);
                skip().setCreationDate(null);
                skip().setModificationDate(null);
                skip().setCreator(null);
                skip().setModifiedBy(null);
                skip().setOwner(null);
            }
};
    }

    private static PropertyMap<LessonContentLesson, org.motechproject.nms.mobileacademy.domain.course.LessonContentLesson> getLessonContentLessonMap() {
        return new PropertyMap<LessonContentLesson, org.motechproject.nms.mobileacademy.domain.course.LessonContentLesson>() {
            @Override
            protected void configure() {
                map().setIdentifier(source.getId());
                map().setFile(source.getFile());
                skip().setId(null);
                skip().setCreationDate(null);
                skip().setModificationDate(null);
                skip().setCreator(null);
                skip().setModifiedBy(null);
                skip().setOwner(null);
            }
};
    }

    private static PropertyMap<ChapterContentScore, org.motechproject.nms.mobileacademy.domain.course.ChapterContentScore> getChapterScoreMap() {
        return new PropertyMap<ChapterContentScore, org.motechproject.nms.mobileacademy.domain.course.ChapterContentScore>() {
            @Override
            protected void configure() {
                map().setIdentifier(source.getId());
                map().setFiles(source.getFiles());
                skip().setId(null);
                skip().setCreationDate(null);
                skip().setModificationDate(null);
                skip().setCreator(null);
                skip().setModifiedBy(null);
                skip().setOwner(null);
            }
};
    }

    private static PropertyMap<ChapterContentMenu, org.motechproject.nms.mobileacademy.domain.course.ChapterContentMenu> getChapterContentMenuMap() {
        return new PropertyMap<ChapterContentMenu, org.motechproject.nms.mobileacademy.domain.course.ChapterContentMenu>() {
            @Override
            protected void configure() {
                map().setIdentifier(source.getId());
                map().setFile(source.getFile());
                skip().setId(null);
                skip().setCreationDate(null);
                skip().setModificationDate(null);
                skip().setCreator(null);
                skip().setModifiedBy(null);
                skip().setOwner(null);
            }
        };
    }

    private static PropertyMap<CourseResponse, Course> getCourseMap() {
        return new PropertyMap<CourseResponse, Course>() {
            @Override
            protected void configure() {
                skip().setId(null);
                skip().setCreationDate(null);
                skip().setModificationDate(null);
                skip().setCreator(null);
                skip().setModifiedBy(null);
                skip().setOwner(null);
            }
        };
    }

}
