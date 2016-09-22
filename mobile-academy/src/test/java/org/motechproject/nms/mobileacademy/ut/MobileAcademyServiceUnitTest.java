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
import org.motechproject.mtraining.domain.ActivityRecord;
import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.repository.ActivityDataService;
import org.motechproject.mtraining.service.ActivityService;
import org.motechproject.mtraining.service.BookmarkService;
import org.motechproject.mtraining.service.MTrainingService;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.imi.service.SmsNotificationService;
import org.motechproject.nms.mobileacademy.domain.CompletionRecord;
import org.motechproject.nms.mobileacademy.domain.NmsCourse;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.exception.CourseNotCompletedException;
import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.repository.MtrainingModuleActivityRecordAuditDataService;
import org.motechproject.nms.mobileacademy.repository.NmsCourseDataService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.motechproject.nms.mobileacademy.service.impl.CourseNotificationServiceImpl;
import org.motechproject.nms.mobileacademy.service.impl.MobileAcademyServiceImpl;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.scheduler.contract.RepeatingSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

    private static final String VALID_CALL_ID = "1234567890123456789012345";

    @Mock
    private MobileAcademyService mobileAcademyService;

    @Mock
    private MTrainingService mTrainingService;

    @Mock
    private BookmarkService bookmarkService;

    @Mock
    private ActivityService activityService;

    @Mock
    private FrontLineWorkerService frontLineWorkerService;

    @Mock
    private NmsCourseDataService nmsCourseDataService;

    @Mock
    private CompletionRecordDataService completionRecordDataService;

    @Mock
    private ActivityDataService activityDataService;

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

    @Mock
    private MotechSchedulerService schedulerService;

    @Mock
    private DistrictDataService districtDataService;

    @Mock
    private MtrainingModuleActivityRecordAuditDataService mtrainingModuleActivityRecordAuditDataService;

    private Validator validator;

    @Before
    public void setup() {
        initMocks(this);
        nmsCourseDataService.deleteAll();
        when(settingsFacade.getRawConfig("nmsCourse.json")).thenReturn(getFileInputStream("nmsCourseTest.json"));
        mobileAcademyService = new MobileAcademyServiceImpl(bookmarkService, activityService,
                nmsCourseDataService, completionRecordDataService, activityDataService, eventRelay, mtrainingModuleActivityRecordAuditDataService, settingsFacade, alertService);
        courseNotificationService = new CourseNotificationServiceImpl(completionRecordDataService,
                smsNotificationService, settingsFacade, activityService, schedulerService, alertService,
                frontLineWorkerService, districtDataService);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        when(activityService.createActivity(any(ActivityRecord.class))).thenReturn(new ActivityRecord());
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

        MaBookmark mab = mobileAcademyService.getBookmark(55L, VALID_CALL_ID);
        assertTrue(mab.getCallingNumber() == 55);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setBookmarkNullTest() {

        mobileAcademyService.setBookmark(null);
    }

    @Test
    public void setNewBookmarkTest() {
        MaBookmark mab = new MaBookmark(1234567890L, VALID_CALL_ID, "Chapter1_Lesson1", null);

        when(bookmarkService.createBookmark(any(Bookmark.class))).thenReturn(new Bookmark());
        when(bookmarkService.getLatestBookmarkByUserId(anyString())).thenReturn(null);
        mobileAcademyService.setBookmark(mab);
    }

    @Test
    public void setUpdateBookmarkTest() {
        MaBookmark mab = new MaBookmark(1234567890L, VALID_CALL_ID, "Chapter1_Lesson1", null);

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
        MaBookmark mab = new MaBookmark(1234567890L, VALID_CALL_ID, "COURSE_COMPLETED", scores);
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
        MaBookmark mab = new MaBookmark(1234567890L, VALID_CALL_ID, "COURSE_COMPLETED", scores);
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
        progress.put("bookmark", "COURSE_COMPLETED");
        Bookmark newBookmark = new Bookmark("55", "getBookmarkTest", null, null, progress);
        when(bookmarkService.getLatestBookmarkByUserId(anyString()))
                .thenReturn(newBookmark);

        MaBookmark retrieved = mobileAcademyService.getBookmark(55L, VALID_CALL_ID);
        assertNull(retrieved.getBookmark());
        assertNull(retrieved.getScoresByChapter());
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
        cr.setModificationDate(DateTime.now());
        assertNull(cr.getLastDeliveryStatus());

        when(completionRecordDataService.findRecordByCallingNumber(anyLong())).thenReturn(cr);
        when(settingsFacade.getProperty(anyString())).thenReturn("1");
        courseNotificationService.updateSmsStatus(event);
        assertTrue(cr.getLastDeliveryStatus().equals("DeliveredToTerminal"));
    }

    @Test
    public void testStatusUpdateNotificationRetry() {
        MotechEvent event = new MotechEvent();
        event.getParameters().put("address", "tel: 9876543210");
        event.getParameters().put("deliveryStatus", "DeliveryImpossible");
        CompletionRecord cr = new CompletionRecord(9876543210L, 34, true, 1);
        cr.setModificationDate(DateTime.now().minusDays(1));
        assertNull(cr.getLastDeliveryStatus());
        assertEquals(0, cr.getNotificationRetryCount());

        when(completionRecordDataService.findRecordByCallingNumber(anyLong())).thenReturn(cr);
        when(settingsFacade.getProperty(anyString())).thenReturn("1");
        doNothing().when(schedulerService).safeScheduleRepeatingJob(any(RepeatingSchedulableJob.class));
        when(frontLineWorkerService.getByContactNumber(anyLong())).thenReturn(getFrontLineWorker());
        courseNotificationService.updateSmsStatus(event);
        assertTrue(cr.getLastDeliveryStatus().equals("DeliveryImpossible"));
        assertEquals(1, cr.getNotificationRetryCount());
    }

    private FrontLineWorker getFrontLineWorker() {
        FrontLineWorker flw = new FrontLineWorker("Unit Test Babu", 12L);
        State state = new State("TN", 333L);
        District district = new District();
        district.setState(state);
        district.setCode(444L);
        state.setDistricts(new HashSet<>(Arrays.asList(district)));
        flw.setState(state);
        flw.setDistrict(district);
        flw.setLanguage(new Language("hin", "Hindi"));
        return flw;
    }

    @Test
    public void testStatusUpdateNotificationMaxNoRetry() {
        MotechEvent event = new MotechEvent();
        event.getParameters().put("address", "tel: 9876543210");
        event.getParameters().put("deliveryStatus", "DeliveryImpossible");
        CompletionRecord cr = new CompletionRecord(9876543210L, 34, true, 1, 1);
        cr.setModificationDate(DateTime.now().minusDays(1));
        assertNull(cr.getLastDeliveryStatus());
        assertEquals(1, cr.getNotificationRetryCount());

        when(completionRecordDataService.findRecordByCallingNumber(anyLong())).thenReturn(cr);
        when(settingsFacade.getProperty(anyString())).thenReturn("1");
        doNothing().when(schedulerService).safeScheduleRepeatingJob(any(RepeatingSchedulableJob.class));
        courseNotificationService.updateSmsStatus(event);
        assertTrue(cr.getLastDeliveryStatus().equals("DeliveryImpossible"));
        assertEquals(1, cr.getNotificationRetryCount());
    }

    @Test
    public void testStatusUpdateNotificationScheduler() {
        MotechEvent event = new MotechEvent();
        event.getParameters().put("address", "tel: 9876543210");
        event.getParameters().put("deliveryStatus", "DeliveryImpossible");
        CompletionRecord cr = new CompletionRecord(9876543210L, 34, true, 1);
        cr.setModificationDate(DateTime.now());
        assertNull(cr.getLastDeliveryStatus());
        assertEquals(0, cr.getNotificationRetryCount());

        when(completionRecordDataService.findRecordByCallingNumber(anyLong())).thenReturn(cr);
        when(settingsFacade.getProperty(anyString())).thenReturn("1");
        doNothing().when(schedulerService).safeScheduleRepeatingJob(any(RepeatingSchedulableJob.class));
        courseNotificationService.updateSmsStatus(event);
        assertTrue(cr.getLastDeliveryStatus().equals("DeliveryImpossible"));
        assertEquals(0, cr.getNotificationRetryCount());
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
