package org.motechproject.nms.mobileacademy.ut;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.repository.BookmarkDataService;
import org.motechproject.nms.mobileacademy.domain.Course;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.repository.CourseDataService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.motechproject.nms.mobileacademy.service.impl.MobileAcademyServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for Mobile Academy Service
 */
public class MobileAcademyServiceUnitTest {

    @Mock
    private MobileAcademyService mobileAcademyService;

    @Mock
    private CourseDataService courseDataService;

    @Mock
    private BookmarkDataService bookmarkDataService;

    @Mock
    private CompletionRecordDataService completionRecordDataService;

    @Before
    public void setup() {
        initMocks(this);
        mobileAcademyService = new MobileAcademyServiceImpl(
                bookmarkDataService, courseDataService, completionRecordDataService);
    }

    @Test
    public void getCourseTest() {
        Course currentCourse = new Course();
        when(courseDataService.findCourseByName("MobileAcademyCourse")).thenReturn(currentCourse);
        assertEquals(mobileAcademyService.getCourse(), currentCourse);
    }

    @Test
    public void getBookmarkTest() {
        Bookmark newBookmark = new Bookmark("55", "getBookmarkTest", null, null, null);

        when(bookmarkDataService.findBookmarksForUser(anyString()))
                .thenReturn(new ArrayList<Bookmark>(Arrays.asList(newBookmark)));

        MaBookmark mab = mobileAcademyService.getBookmark(55L, 10L);
        assertTrue(mab.getCallingNumber() == 55);
    }

    @Test
    public void setNewBookmarkTest() {
        MaBookmark mab = new MaBookmark(1234567890L, 123456789011121L, "Chapter1_Lesson1", null);

        when(bookmarkDataService.create(any(Bookmark.class))).thenReturn(new Bookmark());
        when(bookmarkDataService.findBookmarksForUser(anyString())).thenReturn(new ArrayList<Bookmark>());
        mobileAcademyService.setBookmark(mab);
    }

    @Test
    public void setUpdateBookmarkTest() {
        MaBookmark mab = new MaBookmark(1234567890L, 123456789011121L, "Chapter1_Lesson1", null);

        when(bookmarkDataService.create(any(Bookmark.class)))
                .thenReturn(new Bookmark());
        when(bookmarkDataService.findBookmarksForUser(anyString()))
                .thenReturn(new ArrayList<>(Arrays.asList(new Bookmark())));
        mobileAcademyService.setBookmark(mab);
    }

}
