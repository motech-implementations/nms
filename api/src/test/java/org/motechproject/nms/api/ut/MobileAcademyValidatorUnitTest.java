package org.motechproject.nms.api.ut;

import org.junit.Test;
import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.Chapter;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.ChapterContent;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.ChapterContentMenu;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.ChapterContentScore;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.Lesson;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.LessonContent;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.LessonContentLesson;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.LessonContentMenu;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.Question;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.Quiz;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.QuizContent;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.QuizContentMenu;
import org.motechproject.nms.api.web.validator.MobileAcademyValidator;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Unit test for course structure validation
 */
public class MobileAcademyValidatorUnitTest {

    @Test
    public void TestValidCourseStructure() {

        CourseResponse courseResponse = generateValidCourseResponse();
        assertTrue(MobileAcademyValidator.validateCourseStructure(courseResponse));
    }

    @Test
    public void TestInvalidCourseStructure() {

        CourseResponse courseResponse = generateValidCourseResponse();
        courseResponse.getChapters().add(new Chapter());
        assertFalse(MobileAcademyValidator.validateCourseStructure(courseResponse));
    }

    private CourseResponse generateValidCourseResponse() {
        CourseResponse response = new CourseResponse();
        response.setName("MobileAcademyCourse");

        List<Chapter> chapters = new ArrayList<>();
        for(int i = 0; i < 11; i++) {
            Chapter currentChapter = new Chapter();
            currentChapter.setName("Chapter" + (i + 1));
            currentChapter.setQuiz(generateValidQuiz());
            currentChapter.setContent(generateValidChapterContent());
            currentChapter.setLessons(generateValidLessons());
            chapters.add(currentChapter);
        }
        response.setChapters(chapters);

        return response;
    }

    private Quiz generateValidQuiz() {
        Quiz quiz = new Quiz();
        QuizContent qc = new QuizContent();
        QuizContentMenu qcm = new QuizContentMenu();
        qcm.setFile("foo.bar");
        qcm.setId("qcm.id");
        List<Question> questions = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            questions.add(new Question());
        }

        qc.setQuestions(questions);
        quiz.setContent(qc);
        return quiz;
    }

    private ChapterContent generateValidChapterContent() {

        ChapterContent cc = new ChapterContent();

        ChapterContentMenu ccm = new ChapterContentMenu();
        ccm.setId("ccm.id");
        ccm.setFiles("ccm.file");
        cc.setMenu(ccm);

        ChapterContentScore ccs = new ChapterContentScore();
        ccs.setId("ccs.id");
        ccs.setFiles(new ArrayList<String>());
        cc.setScore(ccs);

        return cc;
    }

    private List<Lesson> generateValidLessons() {

        List<Lesson> lessons = new ArrayList<>();
        for (int i = 0; i < 4; i++) {

            Lesson currentLesson = new Lesson();
            currentLesson.setName(String.format("Lesson<%d>", i + 1));
            currentLesson.setContent(generateValidLessonContent());
            lessons.add(currentLesson);
        }

        return lessons;
    }

    private LessonContent generateValidLessonContent() {
        LessonContent lc = new LessonContent();

        LessonContentMenu lcm = new LessonContentMenu();
        lcm.setId("lcm.id");
        lcm.setFile("lcm.file");
        lc.setMenu(lcm);

        LessonContentLesson lcl = new LessonContentLesson();
        lcl.setFile("lcl.file");
        lcl.setId("lcl.id");
        lc.setLesson(lcl);

        return lc;
    }

}
