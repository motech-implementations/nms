package org.motechproject.nms.testing.it.ma;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.event.MotechEvent;
import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.repository.ActivityDataService;
import org.motechproject.mtraining.repository.BookmarkDataService;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.mobileacademy.domain.CompletionRecord;
import org.motechproject.nms.mobileacademy.domain.NmsCourse;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.dto.MaCourse;
import org.motechproject.nms.mobileacademy.exception.CourseNotCompletedException;
import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.repository.NmsCourseDataService;
import org.motechproject.nms.mobileacademy.service.CourseNotificationService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
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
    MobileAcademyService maService;

    @Inject
    BookmarkDataService bookmarkDataService;

    @Inject
    ActivityDataService activityDataService;

    @Inject
    CompletionRecordDataService completionRecordDataService;

    @Inject
    NmsCourseDataService nmsCourseDataService;

    @Inject
    CourseNotificationService courseNotificationService;

    @Inject
    LanguageDataService languageDataService;

    @Inject
    StateDataService stateDataService;

    @Inject
    CircleDataService circleDataService;

    @Inject
    DistrictService districtService;

    @Inject
    FrontLineWorkerService frontLineWorkerService;

    @Inject
    TestingService testingService;

    private static final String VALID_COURSE_NAME = "MobileAcademyCourse";

    private static final String FINAL_BOOKMARK = "COURSE_COMPLETED";


    @Before
    public void setupMobileAcademy() {

        completionRecordDataService.deleteAll();
        activityDataService.deleteAll();
        bookmarkDataService.deleteAll();
        nmsCourseDataService.deleteAll();
        testingService.clearDatabase();
    }

    @Test
    public void testSetCourseNoUpdate() throws IOException {
        setupMaCourse();

        NmsCourse originalCourse = nmsCourseDataService.getCourseByName(VALID_COURSE_NAME);
        MaCourse copyCourse = new MaCourse(originalCourse.getName(), originalCourse.getModificationDate().getMillis(), originalCourse.getContent());
        maService.setCourse(copyCourse);

        // verify that modified time (version) didn't change
        assertEquals(nmsCourseDataService.getCourseByName(VALID_COURSE_NAME).getModificationDate(),
                originalCourse.getModificationDate());
    }

    @Test
    public void testSetCourseUpdate() throws IOException {
        setupMaCourse();
        NmsCourse originalCourse = nmsCourseDataService.getCourseByName(VALID_COURSE_NAME);
        String courseContent = originalCourse.getContent();
        MaCourse copyCourse = new MaCourse(originalCourse.getName(), originalCourse.getModificationDate().getMillis(), originalCourse.getContent() + "foo");
        maService.setCourse(copyCourse);

        // verify that modified time (version) did change
        assertNotEquals(nmsCourseDataService.getCourseByName(VALID_COURSE_NAME).getModificationDate(),
                originalCourse.getModificationDate());
        originalCourse.setContent(courseContent);
        nmsCourseDataService.update(originalCourse);
    }

    @Test
    public void testNoCoursePresent() throws IOException {
        setupMaCourse();
        NmsCourse originalCourse = nmsCourseDataService.getCourseByName(VALID_COURSE_NAME);
        nmsCourseDataService.delete(originalCourse);
        assertNull(nmsCourseDataService.getCourseByName(VALID_COURSE_NAME));

        try {
            maService.getCourse();
        } catch (IllegalStateException is) {
            assertTrue(is.toString().contains("No course bootstrapped. Check deployment"));
        }

        nmsCourseDataService.create(new NmsCourse(originalCourse.getName(), originalCourse.getContent()));
    }

    @Test
    public void testMobileAcademyServicePresent() throws Exception {
        assertNotNull(maService);
    }

    @Test
    public void testGetCourse() throws IOException {
        setupMaCourse();
        assertNotNull(maService.getCourse());
    }

    @Test
    public void testGetCourseVersion() throws IOException {
        setupMaCourse();
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
    public void testSetNullBookmark() {
        try {
            maService.setBookmark(null);
            throw new IllegalStateException("This test expected an IllegalArgumentException");
        } catch (IllegalArgumentException ia) {
            assertTrue(ia.toString().contains("cannot be null"));
        }
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

        long callingNumber = 9876543210L;
        MaBookmark bookmark = new MaBookmark(callingNumber, 666L, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser("9876543210");
        assertTrue(added.size() == 1);

        bookmark.setBookmark(FINAL_BOOKMARK);
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), ((int) (Math.random() * 100)) % 5);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);
    }

    @Test
    public void testCompletionCount() {

        long callingNumber = 9876543210L;
        int completionCountBefore = activityDataService.findRecordsForUser(String.valueOf(callingNumber)).size();
        MaBookmark bookmark = new MaBookmark(callingNumber, 666L, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser("9876543210");
        assertTrue(added.size() == 1);

        bookmark.setBookmark(FINAL_BOOKMARK);
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), ((int) (Math.random() * 100)) % 5);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);
        int completionCountAfter = activityDataService.findRecordsForUser(String.valueOf(callingNumber)).size();

        assertEquals(completionCountBefore + 1, completionCountAfter);
    }

    @Test
    public void testSetGetLastBookmark() {

        long callingNumber = 9987654321L;
        MaBookmark bookmark = new MaBookmark(callingNumber, 666L, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser("9987654321");
        assertTrue(added.size() == 1);

        bookmark.setBookmark(FINAL_BOOKMARK);
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 1);
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
    public void testSetGetResetBookmark() {

        long callingNumber = 9987654321L;
        MaBookmark bookmark = new MaBookmark(callingNumber, 666L, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser("9987654321");
        assertTrue(added.size() == 1);

        bookmark.setBookmark(FINAL_BOOKMARK);
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
    public void testTriggerNotificationSent() {

        long callingNumber = 9876543210L;
        MaBookmark bookmark = new MaBookmark(callingNumber, 666L, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser(String.valueOf(callingNumber));
        assertTrue(added.size() == 1);

        bookmark.setBookmark(FINAL_BOOKMARK);
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

        long callingNumber = 9876543211L;
        MaBookmark bookmark = new MaBookmark(callingNumber, 666L, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser(String.valueOf(callingNumber));
        assertTrue(added.size() == 1);

        bookmark.setBookmark(FINAL_BOOKMARK);
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
        // Setup language/location and flw for notification
        FrontLineWorker flw = frontLineWorkerService.getByContactNumber(2111113333L);
        if (flw != null) {
            flw.setStatus(FrontLineWorkerStatus.INVALID);
            frontLineWorkerService.update(flw);
            frontLineWorkerService.delete(flw);
        }

        createLanguageLocationData();
        State sampleState = stateDataService.findByCode(1L);
        Language language = languageDataService.findByCode("50");
        flw = new FrontLineWorker("Test Worker", 2111113333L);
        flw.setLanguage(language);
        flw.setState(sampleState);
        flw.setDistrict(sampleState.getDistricts().get(0));
        frontLineWorkerService.add(flw);
        flw = frontLineWorkerService.getByContactNumber(2111113333L);
        assertNotNull(flw);

        long callingNumber = 2111113333L;
        MotechEvent event = new MotechEvent();
        event.getParameters().put("callingNumber", callingNumber);
        event.getParameters().put("smsContent", "FooBar");
        CompletionRecord cr = new CompletionRecord(callingNumber, 35, false, 1);
        completionRecordDataService.create(cr);
        courseNotificationService.sendSmsNotification(event);
        // TODO: cannot check the notification status yet since we don't have a real IMI url to hit
    }

    private void createLanguageLocationData() {
        Language ta = languageDataService.findByCode("50");
        if (ta == null) {
            ta = languageDataService.create(new Language("50", "hin"));
        }

        State state = stateDataService.findByCode(1L);

        if (state == null) {
            District district = new District();
            district.setName("District 1");
            district.setRegionalName("District 1");
            district.setLanguage(ta);
            district.setCode(1L);

            state = new State();
            state.setName("State 1");
            state.setCode(1L);
            state.getDistricts().add(district);
            stateDataService.create(state);
        }
    }

    /**
     * setup MA course structure from nmsCourse.json file.
     */
    private JSONObject setupMaCourse() throws IOException {
        MaCourse course = new MaCourse();
        String jsonText = IOUtils
                .toString(getFileInputStream("nmsCourse.json"));
        JSONObject jo = new JSONObject(jsonText);
        course.setName(jo.get("name").toString());
        course.setContent(jo.get("chapters").toString());
        nmsCourseDataService.create(new NmsCourse(course.getName(), course
                .getContent()));
        return jo;
    }

    private InputStream getFileInputStream(String fileName) {
        try {
            return new FileInputStream(new File(Thread.currentThread()
                    .getContextClassLoader().getResource(fileName).getPath()));
        } catch (IOException io) {
            return null;
        }
    }

}
