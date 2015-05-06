package org.motechproject.nms.api.utils;

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
import org.motechproject.nms.api.web.contract.mobileAcademy.course.QuestionContent;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.Quiz;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.QuizContent;
import org.motechproject.nms.api.web.contract.mobileAcademy.course.QuizContentMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper to generate a course response
 */
public final class CourseBuilder {

    public static CourseResponse generateValidCourseResponse() {
        CourseResponse response = new CourseResponse();
        response.setName("MobileAcademyCourse");

        List<Chapter> chapters = new ArrayList<>();
        for(int i = 1; i < 12; i++) {
            Chapter currentChapter = new Chapter();
            currentChapter.setName("Chapter" + i);
            currentChapter.setQuiz(generateValidQuiz("Quiz_" + i));
            currentChapter.setContent(generateValidChapterContent());
            currentChapter.setLessons(generateValidLessons());
            chapters.add(currentChapter);
        }
        response.setChapters(chapters);

        return response;
    }

    public static Quiz generateValidQuiz(String quizName) {
        Quiz quiz = new Quiz();
        quiz.setName(quizName);
        QuizContent qc = new QuizContent();

        QuizContentMenu qcm = new QuizContentMenu();
        qcm.setFile("foo.bar");
        qcm.setId("qcm.id");
        qc.setMenu(qcm);

        List<Question> questions = new ArrayList<>();
        for (int i = 1; i < 5; i++) {

            questions.add(generateValidQuestion("Question_" + i));
        }
        qc.setQuestions(questions);
        quiz.setContent(qc);

        return quiz;
    }

    public static Question generateValidQuestion(String name) {
        Question newQuestion = new Question();
        newQuestion.setName(name);
        newQuestion.setCorrectAnswerOption(1);

        QuestionContent qc = new QuestionContent();
        qc.setCorrectAnswer("correct.wav");
        qc.setWrongAnswer("wrong.wav");
        qc.setId("foobar_" + name);
        qc.setQuestion("question.wav");

        newQuestion.setContent(qc);
        return newQuestion;
    }

    public static ChapterContent generateValidChapterContent() {

        ChapterContent cc = new ChapterContent();

        ChapterContentMenu ccm = new ChapterContentMenu();
        ccm.setId("ccm.id");
        ccm.setFile("ccm.file");
        cc.setMenu(ccm);

        ChapterContentScore ccs = new ChapterContentScore();
        ccs.setId("ccs.id");
        ccs.setFiles(new ArrayList<String>(
                Arrays.asList("file1", "file2", "file3", "file4", "file5")
        ));
                cc.setScore(ccs);

        return cc;
    }

    public static List<Lesson> generateValidLessons() {

        List<Lesson> lessons = new ArrayList<>();
        for (int i = 0; i < 4; i++) {

            Lesson currentLesson = new Lesson();
            currentLesson.setName(String.format("Lesson<%d>", i + 1));
            currentLesson.setContent(generateValidLessonContent());
            lessons.add(currentLesson);
        }

        return lessons;
    }

    public static LessonContent generateValidLessonContent() {
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
