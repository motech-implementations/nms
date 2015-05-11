package org.motechproject.nms.mobileacademy.it;

import org.junit.Before;
import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.repository.BookmarkDataService;
import org.motechproject.nms.mobileacademy.domain.Course;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.repository.CourseDataService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    private CourseDataService courseDataService;

    @Inject
    private BookmarkDataService bookmarkDataService;

    private static String validCourseName = "MobileAcademyCourse";

    private static String invalidCourseName = "SampleCourse";

    @Before
    public void setupMobileAcademy() {

        courseDataService.deleteAll();
    }

    @Test
    public void testSetCourse() {

        addCourseHelper(invalidCourseName);
        Course retrieved = courseDataService.findCourseByName(invalidCourseName);
        assertNotNull(retrieved);
    }

    @Test
    public void testMobileAcademyServicePresent() throws Exception {
        assertNotNull(maService);
    }

    @Test
    public void testGetCourse() {

        addCourseHelper(validCourseName);
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
    public void testGetBookmarkEmpty() {

        assertNull(maService.getBookmark(0L, 1L));
    }

    private void addCourseHelper(String courseName) {
        Course newCourse = new Course();
        newCourse.setName(courseName);
        courseDataService.create(newCourse);
    }

}
