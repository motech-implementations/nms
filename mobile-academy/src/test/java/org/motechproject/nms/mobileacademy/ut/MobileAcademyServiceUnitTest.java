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
import org.motechproject.nms.mobileacademy.domain.CourseCompletionRecord;
import org.motechproject.nms.mobileacademy.domain.NmsCourse;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.exception.CourseNotCompletedException;
import org.motechproject.nms.mobileacademy.repository.CourseCompletionRecordDataService;
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

import javax.validation.Validation;
import javax.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
    private CourseCompletionRecordDataService courseCompletionRecordDataService;

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
                nmsCourseDataService, activityDataService, courseCompletionRecordDataService, frontLineWorkerService, eventRelay, mtrainingModuleActivityRecordAuditDataService, settingsFacade, alertService);
        courseNotificationService = new CourseNotificationServiceImpl(smsNotificationService,
                    settingsFacade, activityService, schedulerService, courseCompletionRecordDataService, alertService,
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

        FrontLineWorker flw = new FrontLineWorker(1234567890L);
        flw.setId(55L);
        when(bookmarkService.getLatestBookmarkByUserId(anyString()))
                .thenReturn(newBookmark);
        when(frontLineWorkerService.getByContactNumber(anyLong())).thenReturn(flw);

        MaBookmark mab = mobileAcademyService.getBookmark(1234567890L, VALID_CALL_ID);
        assertTrue(mab.getFlwId() == 55L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setBookmarkNullTest() {

        mobileAcademyService.setBookmark(null);
    }

    @Test
    public void setNewBookmarkTest() {
        MaBookmark mab = new MaBookmark(123456L, VALID_CALL_ID, "Chapter1_Lesson1", null);

        FrontLineWorker flw = new FrontLineWorker(1234567890L);
        flw.setId(123456L);
        when(bookmarkService.createBookmark(any(Bookmark.class))).thenReturn(new Bookmark());
        when(bookmarkService.getLatestBookmarkByUserId(anyString())).thenReturn(null);
        when(frontLineWorkerService.getById(anyLong())).thenReturn(flw);
        mobileAcademyService.setBookmark(mab);
    }

    @Test
    public void setUpdateBookmarkTest() {
        MaBookmark mab = new MaBookmark(123456L, VALID_CALL_ID, "Chapter1_Lesson1", null);

        FrontLineWorker flw = new FrontLineWorker(1234567890L);
        flw.setId(123456L);
        when(bookmarkService.createBookmark(any(Bookmark.class)))
                .thenReturn(new Bookmark());
        when(bookmarkService.getLatestBookmarkByUserId(anyString()))
                .thenReturn(new Bookmark());
        when(frontLineWorkerService.getById(anyLong())).thenReturn(flw);
        mobileAcademyService.setBookmark(mab);
    }

    @Test
    public void setLastBookmark() {
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), ((int) (Math.random() * 100)) % 5);
        }
        MaBookmark mab = new MaBookmark(123456L, VALID_CALL_ID, "COURSE_COMPLETED", scores);
        doNothing().when(eventRelay).sendEventMessage(any(MotechEvent.class));

        CourseCompletionRecord ccr = new CourseCompletionRecord(123456L, 22, scores.toString(), false);
        List<CourseCompletionRecord> records = new ArrayList<>();
        records.add(ccr);
        when(courseCompletionRecordDataService.findByFlwId(anyLong())).thenReturn(records);
        FrontLineWorker flw = new FrontLineWorker(1234567890L);
        flw.setId(123456L);
        when(frontLineWorkerService.getById(anyLong())).thenReturn(flw);
        mobileAcademyService.setBookmark(mab);
    }

    @Test
    public void setLastBookmarkFailingScore() {
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 0);
        }
        MaBookmark mab = new MaBookmark(123456L, VALID_CALL_ID, "COURSE_COMPLETED", scores);
        doNothing().when(eventRelay).sendEventMessage(any(MotechEvent.class));
        FrontLineWorker flw = new FrontLineWorker(1234567890L);
        flw.setId(123456L);
        when(frontLineWorkerService.getById(anyLong())).thenReturn(flw);
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
        FrontLineWorker flw = new FrontLineWorker(1234567890L);
        flw.setId(55L);
        when(frontLineWorkerService.getByContactNumber(anyLong())).thenReturn(flw);
        MaBookmark retrieved = mobileAcademyService.getBookmark(1234567890L, VALID_CALL_ID);
        assertNull(retrieved.getBookmark());
        assertNull(retrieved.getScoresByChapter());
    }

    @Test
    public void testStatusUpdateNotification() {
        MotechEvent event = new MotechEvent();
        event.getParameters().put("address", "tel: 9876543210");
        event.getParameters().put("deliveryStatus", "DeliveredToTerminal");
        CourseCompletionRecord ccr = new CourseCompletionRecord(123456L, 34, "score", true);
        ccr.setModificationDate(DateTime.now());
        assertNull(ccr.getLastDeliveryStatus());

        List<CourseCompletionRecord> records = new ArrayList<>();
        records.add(ccr);
        when(courseCompletionRecordDataService.findByFlwId(anyLong())).thenReturn(records);
        when(settingsFacade.getProperty(anyString())).thenReturn("1");
        FrontLineWorker flw = new FrontLineWorker(1234567890L);
        flw.setFlwId("123456");
        when(frontLineWorkerService.getByContactNumber(anyLong())).thenReturn(flw);
        courseNotificationService.updateSmsStatus(event);
        assertTrue(ccr.getLastDeliveryStatus().equals("DeliveredToTerminal"));
    }

    @Test
    public void testStatusUpdateNotificationRetry() {
        MotechEvent event = new MotechEvent();
        event.getParameters().put("address", "tel: 9876543210");
        event.getParameters().put("deliveryStatus", "DeliveryImpossible");
        CourseCompletionRecord ccr = new CourseCompletionRecord(123456L, 34, "score", true);
        ccr.setModificationDate(DateTime.now().minusDays(1));
        assertNull(ccr.getLastDeliveryStatus());
        assertEquals(0, ccr.getNotificationRetryCount());

        List<CourseCompletionRecord> records = new ArrayList<>();
        records.add(ccr);
        when(courseCompletionRecordDataService.findByFlwId(anyLong())).thenReturn(records);
        when(settingsFacade.getProperty(anyString())).thenReturn("1");
        doNothing().when(schedulerService).safeScheduleRepeatingJob(any(RepeatingSchedulableJob.class));
        when(frontLineWorkerService.getById(anyLong())).thenReturn(getFrontLineWorker());
        when(frontLineWorkerService.getByContactNumber(anyLong())).thenReturn(getFrontLineWorker());
        when(frontLineWorkerService.getById(anyLong())).thenReturn(getFrontLineWorker());
        courseNotificationService.updateSmsStatus(event);
        assertTrue(ccr.getLastDeliveryStatus().equals("DeliveryImpossible"));
        assertEquals(1, ccr.getNotificationRetryCount());
    }

    private FrontLineWorker getFrontLineWorker() {
        FrontLineWorker flw = new FrontLineWorker("Unit Test Babu", 12L);
        State state = new State("TN", 333L);
        District district = new District();
        district.setState(state);
        district.setCode(444L);
        state.setDistricts(new HashSet<>(Arrays.asList(district)));
        flw.setId(123456L);
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
        CourseCompletionRecord ccr = new CourseCompletionRecord(123456L, 34, "score", true, true, 1);
        ccr.setModificationDate(DateTime.now().minusDays(1));
        assertNull(ccr.getLastDeliveryStatus());
        assertEquals(1, ccr.getNotificationRetryCount());

        List<CourseCompletionRecord> records = new ArrayList<>();
        records.add(ccr);
        when(courseCompletionRecordDataService.findByFlwId(anyLong())).thenReturn(records);
        when(settingsFacade.getProperty(anyString())).thenReturn("1");
        doNothing().when(schedulerService).safeScheduleRepeatingJob(any(RepeatingSchedulableJob.class));
        FrontLineWorker flw = new FrontLineWorker(1234567890L);
        flw.setId(123456L);
        when(frontLineWorkerService.getById(anyLong())).thenReturn(flw);
        when(frontLineWorkerService.getByContactNumber(anyLong())).thenReturn(flw);
        when(frontLineWorkerService.getById(anyLong())).thenReturn(flw);
        courseNotificationService.updateSmsStatus(event);
        assertTrue(ccr.getLastDeliveryStatus().equals("DeliveryImpossible"));
        assertEquals(1, ccr.getNotificationRetryCount());
    }

    @Test
    public void testStatusUpdateNotificationScheduler() {
        MotechEvent event = new MotechEvent();
        event.getParameters().put("address", "tel: 9876543210");
        event.getParameters().put("deliveryStatus", "DeliveryImpossible");
        CourseCompletionRecord ccr = new CourseCompletionRecord(123456L, 34, "score", true);
        ccr.setModificationDate(DateTime.now());
        assertNull(ccr.getLastDeliveryStatus());
        assertEquals(0, ccr.getNotificationRetryCount());

        List<CourseCompletionRecord> records = new ArrayList<>();
        records.add(ccr);
        when(courseCompletionRecordDataService.findByFlwId(anyLong())).thenReturn(records);
        when(settingsFacade.getProperty(anyString())).thenReturn("1");
        doNothing().when(schedulerService).safeScheduleRepeatingJob(any(RepeatingSchedulableJob.class));
        FrontLineWorker flw = new FrontLineWorker(1234567890L);
        flw.setId(123456L);
        when(frontLineWorkerService.getById(anyLong())).thenReturn(flw);
        when(frontLineWorkerService.getByContactNumber(anyLong())).thenReturn(flw);
        when(frontLineWorkerService.getById(anyLong())).thenReturn(flw);
        courseNotificationService.updateSmsStatus(event);
        assertTrue(ccr.getLastDeliveryStatus().equals("DeliveryImpossible"));
        assertEquals(0, ccr.getNotificationRetryCount());
    }

    @Test(expected = CourseNotCompletedException.class)
    public void testNotificationTriggerException() {
        when(courseCompletionRecordDataService.findByFlwId(anyLong())).thenReturn(null);
        mobileAcademyService.triggerCompletionNotification(123456L);
    }

    @Test
    public void testNotificationTriggerValidNew() {
        CourseCompletionRecord ccr = new CourseCompletionRecord(123456L, 22, "score");
        List<CourseCompletionRecord> records = new ArrayList<>();
        records.add(ccr);
        when(courseCompletionRecordDataService.findByFlwId(anyLong())).thenReturn(records);
        mobileAcademyService.triggerCompletionNotification(123456L);
        mobileAcademyService.triggerCompletionNotification(123456L);
        assertFalse(ccr.isSentNotification());
    }

    @Test
    public void testNotificationTriggerValidExisting() {
        CourseCompletionRecord ccr = new CourseCompletionRecord(123456L, 22, "score", true);
        List<CourseCompletionRecord> records = new ArrayList<>();
        records.add(ccr);
        when(courseCompletionRecordDataService.findByFlwId(anyLong())).thenReturn(records);

        when(courseCompletionRecordDataService.update(any(CourseCompletionRecord.class))).thenAnswer(
                new Answer<CourseCompletionRecord>() {
                    @Override
                    public CourseCompletionRecord answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();
                        return (CourseCompletionRecord) args[0];
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
