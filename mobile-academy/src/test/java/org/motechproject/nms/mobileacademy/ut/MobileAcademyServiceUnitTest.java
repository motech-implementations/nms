package org.motechproject.nms.mobileacademy.ut;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.repository.BookmarkDataService;
import org.motechproject.nms.mobileacademy.domain.CompletionRecord;
import org.motechproject.nms.mobileacademy.domain.Course;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.exception.CourseNotCompletedException;
import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.repository.CourseDataService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.motechproject.nms.mobileacademy.service.impl.MobileAcademyServiceImpl;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
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

    @Mock
    private EventRelay eventRelay;

    private Validator validator;

    @Before
    public void setup() {
        initMocks(this);
        mobileAcademyService = new MobileAcademyServiceImpl(
                bookmarkDataService, courseDataService, completionRecordDataService, eventRelay);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
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

    @Test
    public void setLastBookmark() {
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), ((int) (Math.random() * 100)) % 5);
        }
        MaBookmark mab = new MaBookmark(1234567890L, 123456789011121L, "Chapter11_Quiz", scores);
        doNothing().when(eventRelay).sendEventMessage(any(MotechEvent.class));

        CompletionRecord cr = new CompletionRecord(1234567890L, 22, false, 1);
        when(completionRecordDataService.findRecordByCallingNumber(anyLong())).thenReturn(cr);
        mobileAcademyService.setBookmark(mab);
    }

    @Test
    public void setLastBookmarkFailingScore() {
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 0);
        }
        MaBookmark mab = new MaBookmark(1234567890L, 123456789011121L, "Chapter11_Quiz", scores);
        doNothing().when(eventRelay).sendEventMessage(any(MotechEvent.class));
        mobileAcademyService.setBookmark(mab);
    }

    @Test
    public void getLastBookmarkReset() {

        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 4);
        }

        Map<String, Object> progress = new HashMap<>();
        progress.put("scoresByChapter", scores);
        Bookmark newBookmark = new Bookmark("55", "getBookmarkTest", "Chapter11", "Quiz", progress);
        when(bookmarkDataService.findBookmarksForUser(anyString()))
                .thenReturn(new ArrayList<Bookmark>(Arrays.asList(newBookmark)));

        MaBookmark retreived = mobileAcademyService.getBookmark(55L, 56L);
        assertNull(retreived.getBookmark());
        assertNull(retreived.getScoresByChapter());
    }

    @Test
    public void getLastBookmarkNotReset() {
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 0);
        }

        Map<String, Object> progress = new HashMap<>();
        progress.put("scoresByChapter", scores);
        Bookmark newBookmark = new Bookmark("55", "getBookmarkTest", "Chapter11", "Quiz", progress);

        when(bookmarkDataService.findBookmarksForUser(anyString()))
                .thenReturn(new ArrayList<Bookmark>(Arrays.asList(newBookmark)));
        MaBookmark retreived = mobileAcademyService.getBookmark(55L, 56L);
        assertNotNull(retreived.getBookmark());
        assertNotNull(retreived.getScoresByChapter());
    }

    @Test
    public void testCallingNumberTooShort() {
        CompletionRecord cr = new CompletionRecord(1L, 22);
        Set<ConstraintViolation<CompletionRecord>> cv = validator.validateProperty(cr, "callingNumber");
        assertEquals(1, cv.size());
        assertEquals("callingNumber must be 10 digits", cv.iterator().next().getMessage());
    }

    @Test
    public void testCallingNumberTooLong() {
        CompletionRecord cr = new CompletionRecord(11111111111L, 22);
        Set<ConstraintViolation<CompletionRecord>> cv = validator.validateProperty(cr, "callingNumber");
        assertEquals(1, cv.size());
        assertEquals("callingNumber must be 10 digits", cv.iterator().next().getMessage());
    }

    @Test(expected = CourseNotCompletedException.class)
    public void testNotificationTriggerException() {
        when(completionRecordDataService.findRecordByCallingNumber(anyLong())).thenReturn(null);
        mobileAcademyService.triggerCompletionNotification(1234567890L);
    }

    @Test
    public void testNotificationTriggerValidNew() {
        CompletionRecord cr = new CompletionRecord(1234567890L, 22);
        when(completionRecordDataService.findRecordByCallingNumber(anyLong())).thenReturn(cr);
        mobileAcademyService.triggerCompletionNotification(1234567890L);
    }

    @Test
    public void testNotificationTriggerValidExisting() {
        CompletionRecord cr = new CompletionRecord(1234567890L, 22, true, 1);
        when(completionRecordDataService.findRecordByCallingNumber(anyLong())).thenReturn(cr);

        when(completionRecordDataService.update(any(CompletionRecord.class))).thenAnswer(
                new Answer<CompletionRecord>() {
                    @Override
                    public CompletionRecord answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();
                        return (CompletionRecord) args[0];
                    }
                }
        );

        mobileAcademyService.triggerCompletionNotification(1234567890L);
        assertFalse(cr.isSentNotification());
    }
}
