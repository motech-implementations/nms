package org.motechproject.nms.mobileacademy.it;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.event.MotechEvent;
import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.domain.Course;
import org.motechproject.mtraining.domain.CourseUnitState;
import org.motechproject.mtraining.repository.BookmarkDataService;
import org.motechproject.mtraining.service.MTrainingService;
import org.motechproject.nms.mobileacademy.domain.CompletionRecord;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.exception.CourseNotCompletedException;
import org.motechproject.nms.mobileacademy.service.SmsNotificationService;
import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Verify that MobileAcademyService present, functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class MobileAcademyServiceBundleIT extends BasePaxIT {

    @Inject
    private MobileAcademyService maService;

    @Inject
    private MTrainingService mTrainingService;

    @Inject
    private BookmarkDataService bookmarkDataService;

    @Inject
    private CompletionRecordDataService completionRecordDataService;

    @Inject
    private SmsNotificationService smsNotificationService;

    private static String validCourseName = "MobileAcademyCourse";

    private static String invalidCourseName = "SampleCourse";

    @Before
    public void setupMobileAcademy() {

        List<Course> courses = mTrainingService.getCourseByName(invalidCourseName);
        if (courses != null) {
            for (Course currentCourse : courses) {
                mTrainingService.deleteCourse(currentCourse.getId());
            }
        }
        completionRecordDataService.deleteAll();
    }

    @Test
    public void testSetCourse() {

        addCourseHelper(invalidCourseName);
        List<Course> retrieved = mTrainingService.getCourseByName(invalidCourseName);
        assertNotNull(retrieved);
        assertTrue(retrieved.size() > 0);
    }

    @Test
    public void testMobileAcademyServicePresent() throws Exception {
        assertNotNull(maService);
    }

    @Test
    public void testGetCourse() {

        assertNotNull(maService.getCourse());
    }

    @Test
    public void testGetCourseVersion() {

        addCourseHelper(validCourseName);
        assertNotNull(maService.getCourseVersion());
        assertTrue(maService.getCourseVersion() > 0);
    }

    @Test
    public void testGetBookmark() {

        bookmarkDataService.create(new Bookmark("1", "1", "1", "1", new HashMap<String, Object>()));
        assertNotNull(maService.getBookmark(1L, 1L));
    }

    @Test
    public void testGetEmptyBookmark() {

        assertNull(maService.getBookmark(123L, 456L));
    }

    @Test
    public void testSetNewBookmark() {
        List<Bookmark> existing = bookmarkDataService.findBookmarksForUser("555");
        MaBookmark bookmark = new MaBookmark(555L, 666L, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser("555");
        assertTrue(added.size() == (existing.size() + 1));
    }

    @Test
    public void testSetExistingBookmark() {
        bookmarkDataService.deleteAll();
        MaBookmark bookmark = new MaBookmark(556L, 666L, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser("556");
        assertTrue(added.size() == 1);

        bookmark.setBookmark("Chapter3_Lesson2");
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Quiz1", 4);
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);

        MaBookmark retrieved = maService.getBookmark(556L, 666L);
        assertNotNull(retrieved.getBookmark());
        assertTrue(retrieved.getBookmark().equals("Chapter3_Lesson2"));
        assertNotNull(retrieved.getScoresByChapter());
        assertTrue(retrieved.getScoresByChapter().get("Quiz1") == 4);
    }

    @Test
    public void testSetLastBookmark() {
        bookmarkDataService.deleteAll();
        long callingNumber = 9876543210L;
        MaBookmark bookmark = new MaBookmark(callingNumber, 666L, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser("9876543210");
        assertTrue(added.size() == 1);

        bookmark.setBookmark("Chapter11_Quiz");
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), ((int) (Math.random() * 100)) % 5);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);
    }

    @Test
    public void testSetGetLastBookmark() {
        bookmarkDataService.deleteAll();
        long callingNumber = 9987654321L;
        MaBookmark bookmark = new MaBookmark(callingNumber, 666L, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser("9987654321");
        assertTrue(added.size() == 1);

        bookmark.setBookmark("Chapter11_Quiz");
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 1);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);

        MaBookmark retrieved = maService.getBookmark(callingNumber, 666L);
        assertNotNull(retrieved.getCallingNumber());
        assertNotNull(retrieved.getCallId());
        assertNotNull(retrieved.getBookmark());
        assertNotNull(retrieved.getScoresByChapter());
    }

    @Test
    public void testSetGetResetBookmark() {
        bookmarkDataService.deleteAll();
        long callingNumber = 9987654321L;
        MaBookmark bookmark = new MaBookmark(callingNumber, 666L, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser("9987654321");
        assertTrue(added.size() == 1);

        bookmark.setBookmark("Chapter11_Quiz");
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 4);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);

        MaBookmark retrieved = maService.getBookmark(callingNumber, 666L);
        assertNotNull(retrieved.getCallingNumber());
        assertNotNull(retrieved.getCallId());
        assertNull(retrieved.getBookmark());
        assertNull(retrieved.getScoresByChapter());
    }

    @Test
    public void testGetBookmarkEmpty() {

        assertNull(maService.getBookmark(0L, 1L));
    }

    @Test
    public void testTriggerNotificationSent() {
        bookmarkDataService.deleteAll();
        long callingNumber = 9876543210L;
        MaBookmark bookmark = new MaBookmark(callingNumber, 666L, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser(String.valueOf(callingNumber));
        assertTrue(added.size() == 1);

        bookmark.setBookmark("Chapter11_Quiz");
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 3);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);
        CompletionRecord cr = completionRecordDataService.findRecordByCallingNumber(callingNumber);
        assertNotNull(cr);
        assertEquals(cr.getCallingNumber(), callingNumber);
        assertEquals(cr.getScore(), 33);

    }

    @Test
    public void testTriggerNotificationNotSent() {
        bookmarkDataService.deleteAll();
        long callingNumber = 9876543211L;
        MaBookmark bookmark = new MaBookmark(callingNumber, 666L, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser(String.valueOf(callingNumber));
        assertTrue(added.size() == 1);

        bookmark.setBookmark("Chapter11_Quiz");
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 1);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);
        // null because we set a failing score
        assertNull(completionRecordDataService.findRecordByCallingNumber(callingNumber));
    }

    @Test
    public void testRetriggerNotification() {

        long callingNumber = 9876543211L;

        CompletionRecord cr = new CompletionRecord(callingNumber, 44, true, 1);
        completionRecordDataService.create(cr);

        maService.triggerCompletionNotification(callingNumber);
        cr = completionRecordDataService.findRecordByCallingNumber(callingNumber);
        assertFalse(cr.isSentNotification());
    }

    @Test(expected = CourseNotCompletedException.class)
    public void testRetriggerNotificationException() {

        long callingNumber = 9876543222L;
        maService.triggerCompletionNotification(callingNumber);
    }

    @Test
    public void testNotification() {

        long callingNumber = 9876543211L;
        MotechEvent event = new MotechEvent();
        event.getParameters().put("callingNumber", callingNumber);
        CompletionRecord cr = new CompletionRecord(callingNumber, 35, false, 1);
        completionRecordDataService.create(cr);
        smsNotificationService.sendSmsNotification(event);
        // TODO: cannot check the notification status yet since we don't have a real IMI url to hit
    }

    private void addCourseHelper(String courseName) {

        mTrainingService.createCourse(new Course(courseName, CourseUnitState.Active, "[]"));
    }

}
