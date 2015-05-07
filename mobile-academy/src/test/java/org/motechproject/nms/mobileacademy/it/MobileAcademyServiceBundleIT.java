package org.motechproject.nms.mobileacademy.it;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.repository.BookmarkDataService;
import org.motechproject.nms.mobileacademy.domain.Course;
import org.motechproject.nms.mobileacademy.repository.CourseDataService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.util.HashMap;

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
    public void testGetBookmark() throws Exception {

        bookmarkDataService.create(new Bookmark("1", "1", "1", "1", new HashMap<String, Object>()));
        assertNotNull(maService.getBookmark(1L, 1L));
    }

    @Test
    public void testGetBookmarkEmpty() throws Exception {
        assertNull(maService.getBookmark(0L, 1L));
    }

    private void addCourseHelper(String courseName) {
        Course newCourse = new Course();
        newCourse.setName(courseName);
        courseDataService.create(newCourse);
    }

}
