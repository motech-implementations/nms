package org.motechproject.nms.api.web.validator;

import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.Chapter;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.Lesson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Validator helper class for API request and response
 */
public final class MobileAcademyValidator {

    private static final int CHAPTER_COUNT = 11;

    private static final int LESSON_COUNT = 4;

    private static final int QUESTION_COUNT = 4;

    private static final String CHAPTER_NAME_FORMAT = "Chapter<%d>";

    private static final String CHAPTER_END_MENU_FORMAT = "Chapter<%d>_EndMenu";

    private static final String CHAPTER_SCORE_FORMAT = "Chapter<%d>_Score";

    private static final String LESSON_NAME_FORMAT = "Lesson<%d>";

    private static final String LESSON_CONTENT_ID_FORMAT = "Chapter<%d>_Lesson<%d>";

    private static final String LESSON_CONTENT_MENU_ID_FORMAT = "Chapter<%d>_LessonEndMenu<LessonId>";

    private static final String FORMAT_ERROR = " does not meet format requirements ";

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileAcademyValidator.class);

    /**
     * Private constructor for static validation helpers
     */
    private MobileAcademyValidator() {

    }

    public static boolean validateCourseStructure(CourseResponse courseResponse) {

        if (courseResponse.getChapters().size() != CHAPTER_COUNT) {
            LOGGER.error("Course does not meet chapter count size");
            return false;
        }

        for (Chapter chapter : courseResponse.getChapters()) {
            if (chapter.getLessons().size() != LESSON_COUNT) {
                LOGGER.error(chapter.getName() + " does not meet lesson count size");
                return false;
            }

            if (chapter.getQuiz().getContent().getQuestions().size() != QUESTION_COUNT) {
                LOGGER.error(chapter.getQuiz().getName() + " does not meet question count size, ");
                return false;
            }
        }

        return true;
    }

    public static boolean validateCourseFormat(CourseResponse courseResponse) {

        int i = 1;
        for (Chapter chapter : courseResponse.getChapters()) {

            if (chapter.getName() != String.format(CHAPTER_NAME_FORMAT, i)) {
                LOGGER.error(chapter.getName() + FORMAT_ERROR + CHAPTER_NAME_FORMAT);
                return false;
            }

            String endMenu = chapter.getContent().getMenu().getId();
            if (endMenu != String.format(CHAPTER_END_MENU_FORMAT, i)) {
                LOGGER.error(endMenu + FORMAT_ERROR + CHAPTER_END_MENU_FORMAT);
                return false;
            }

            String chapterScore = chapter.getContent().getScore().getId();
            if (chapterScore != String.format(CHAPTER_SCORE_FORMAT, i)) {
                LOGGER.error(endMenu + FORMAT_ERROR + CHAPTER_END_MENU_FORMAT);
                return false;
            }


            int l = 1;
            for (Lesson lesson : chapter.getLessons()) {

                String lessonName = lesson.getName();
                if (lessonName != String.format(LESSON_NAME_FORMAT, l)) {
                    LOGGER.error(lessonName + FORMAT_ERROR + LESSON_NAME_FORMAT);
                    return false;
                }

                String lessonContentId = lesson.getContent().getLesson().getId();
                if (lessonContentId != String.format(LESSON_CONTENT_ID_FORMAT, i, l)) {
                    LOGGER.error(lessonContentId + FORMAT_ERROR + LESSON_CONTENT_ID_FORMAT);
                    return false;
                }

                String lessonContentMenuId = lesson.getContent().getMenu().getId();
                if (lessonContentMenuId != String.format(LESSON_CONTENT_MENU_ID_FORMAT, i, l)) {

                    LOGGER.error(lessonContentMenuId + FORMAT_ERROR + LESSON_CONTENT_MENU_ID_FORMAT);
                    return false;
                }

                l++;
            }

            i++;
        }

        return true;
    }
}
