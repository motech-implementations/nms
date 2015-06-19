package org.motechproject.nms.mobileacademy.ut;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.service.BookmarkService;
import org.motechproject.mtraining.service.MTrainingService;
import org.motechproject.nms.imi.service.SmsNotificationService;
import org.motechproject.nms.mobileacademy.domain.CompletionRecord;
import org.motechproject.nms.mobileacademy.domain.NmsCourse;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.exception.CourseNotCompletedException;
import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.repository.NmsCourseDataService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.motechproject.nms.mobileacademy.service.impl.CourseNotificationServiceImpl;
import org.motechproject.nms.mobileacademy.service.impl.MobileAcademyServiceImpl;
import org.motechproject.server.config.SettingsFacade;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
    private MTrainingService mTrainingService;

    @Mock
    private BookmarkService bookmarkService;

    @Mock
    private NmsCourseDataService nmsCourseDataService;

    @Mock
    private CompletionRecordDataService completionRecordDataService;

    @Mock
    private EventRelay eventRelay;

    @Mock
    private CourseNotificationServiceImpl courseNotificationService;

    @Mock
    private SmsNotificationService smsNotificationService;

    @Mock
    private SettingsFacade settingsFacade;

    @Mock
    private AlertService alertService;

    private Validator validator;

    @Before
    public void setup() {
        initMocks(this);
        nmsCourseDataService.deleteAll();
        when(settingsFacade.getRawConfig("nmsCourse.json")).thenReturn(getFileInputStream("nmsCourseTest.json"));
        mobileAcademyService = new MobileAcademyServiceImpl(bookmarkService, nmsCourseDataService,
                completionRecordDataService, eventRelay, settingsFacade, alertService);
        courseNotificationService = new CourseNotificationServiceImpl(completionRecordDataService,
                smsNotificationService);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void getCourseTest() {

        NmsCourse newCourse = new NmsCourse("MobileAcademyCourse", "[]");
        newCourse.setModificationDate(DateTime.now());
        nmsCourseDataService.create(newCourse);
        when(nmsCourseDataService.getCourseByName("MobileAcademyCourse")).thenReturn(newCourse);
        assertTrue(mobileAcademyService.getCourse().getContent().equals(newCourse.getContent()));
    }

    @Test
    public void getBookmarkTest() {
        Bookmark newBookmark = new Bookmark("55", "getBookmarkTest", null, null, null);

        when(bookmarkService.getLatestBookmarkByUserId(anyString()))
                .thenReturn(newBookmark);

        MaBookmark mab = mobileAcademyService.getBookmark(55L, 10L);
        assertTrue(mab.getCallingNumber() == 55);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setBookmarkNullTest() {

        mobileAcademyService.setBookmark(null);
    }

    @Test
    public void setNewBookmarkTest() {
        MaBookmark mab = new MaBookmark(1234567890L, 123456789011121L, "Chapter1_Lesson1", null);

        when(bookmarkService.createBookmark(any(Bookmark.class))).thenReturn(new Bookmark());
        when(bookmarkService.getLatestBookmarkByUserId(anyString())).thenReturn(null);
        mobileAcademyService.setBookmark(mab);
    }

    @Test
    public void setUpdateBookmarkTest() {
        MaBookmark mab = new MaBookmark(1234567890L, 123456789011121L, "Chapter1_Lesson1", null);

        when(bookmarkService.createBookmark(any(Bookmark.class)))
                .thenReturn(new Bookmark());
        when(bookmarkService.getLatestBookmarkByUserId(anyString()))
                .thenReturn(new Bookmark());
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
        when(bookmarkService.getLatestBookmarkByUserId(anyString()))
                .thenReturn(newBookmark);

        MaBookmark retreived = mobileAcademyService.getBookmark(55L, 56L);
        assertNull(retreived.getBookmark());
        assertNull(retreived.getScoresByChapter());
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

    @Test
    public void testStatusUpdateNotification() {
        MotechEvent event = new MotechEvent();
        event.getParameters().put("address", "tel: 9876543210");
        event.getParameters().put("deliveryStatus", "DeliveredToTerminal");
        CompletionRecord cr = new CompletionRecord(9876543210L, 34, true, 1);
        assertNull(cr.getLastDeliveryStatus());

        when(completionRecordDataService.findRecordByCallingNumber(anyLong())).thenReturn(cr);
        courseNotificationService.updateSmsStatus(event);
        assertTrue(cr.getLastDeliveryStatus().equals("DeliveredToTerminal"));
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
        mobileAcademyService.triggerCompletionNotification(1234567890L);
        assertFalse(cr.isSentNotification());
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

    }

    private InputStream getFileInputStream(String fileName) {

        try {
            return new FileInputStream(
                    new File(
                            Thread.currentThread()
                                    .getContextClassLoader()
                                    .getResource("nmsCourseTest.json")
                                    .getPath()));
        } catch (IOException io) {
            return null;
        }
    }
}
