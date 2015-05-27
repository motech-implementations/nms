package org.motechproject.nms.mobileacademy.service.impl;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.config.core.constants.ConfigurationConstants;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.service.BookmarkService;
import org.motechproject.nms.mobileacademy.domain.CompletionRecord;
import org.motechproject.nms.mobileacademy.domain.NmsCourse;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.dto.MaCourse;
import org.motechproject.nms.mobileacademy.exception.CourseNotCompletedException;
import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.repository.NmsCourseDataService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple implementation of the {@link MobileAcademyService} interface.
 */
@Service("mobileAcademyService")
public class MobileAcademyServiceImpl implements MobileAcademyService {

    private static final String COURSE_CONTENT_FILE = "nmsCourse.json";

    private static final String COURSE_NAME = "MobileAcademyCourse";

    private static final String FINAL_BOOKMARK = "Chapter11_Quiz";

    private static final String COURSE_COMPLETED = "nms.ma.course.completed";

    private static final String SCORES_KEY = "scoresByChapter";

    private static final String NOT_COMPLETE = "<%s: Course not complete>";

    private static final int CHAPTER_COUNT = 11;

    private static final int PASS_SCORE = 22;

    /**
     * Bookmark service to get and set bookmarks
     */
    private BookmarkService bookmarkService;

    /**
     * Completion record data service
     */
    private CompletionRecordDataService completionRecordDataService;

    /**
     * NMS course data service
     */
    private NmsCourseDataService nmsCourseDataService;

    /**
     * Eventing system for course completion processing
     */
    private EventRelay eventRelay;

    /**
     * Used to retrieve course data
     */
    private SettingsFacade settingsFacade;

    /**
     * Used for alerting
     */
    private AlertService alertService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileAcademyServiceImpl.class);

    @Autowired
    public MobileAcademyServiceImpl(BookmarkService bookmarkService,
                                    NmsCourseDataService nmsCourseDataService,
                                    CompletionRecordDataService completionRecordDataService,

                                    EventRelay eventRelay,
                                    @Qualifier("maSettings") SettingsFacade settingsFacade,
                                    AlertService alertService) {
        this.bookmarkService = bookmarkService;
        this.nmsCourseDataService = nmsCourseDataService;
        this.completionRecordDataService = completionRecordDataService;
        this.eventRelay = eventRelay;
        this.settingsFacade = settingsFacade;
        this.alertService = alertService;
        bootstrapCourse();
    }

    @Override
    public MaCourse getCourse() {

        NmsCourse course = nmsCourseDataService.getCourseByName(COURSE_NAME);

        if (course == null) {
            alertService.create("mTraining.Course", COURSE_NAME, "Could not find course", AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            throw new IllegalStateException("No course bootstrapped. Check deployment");
        }

        return mapCourseDomainToDto(course);
    }

    @Override
    public void setCourse(MaCourse courseDto) {

        if (courseDto == null) {
            LOGGER.error("Attempted to set null course, exiting operation");
            alertService.create("MA.Course", "MaCourse", "Trying to set null MaCourse", AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            return;
        }

        setOrUpdateCourse(courseDto);
    }

    @Override
    public long getCourseVersion() {

        NmsCourse course = nmsCourseDataService.getCourseByName(COURSE_NAME);

        if (course == null) {
            alertService.create("mTraining.Course", COURSE_NAME, "Could not find course", AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            throw new IllegalStateException("No course bootstrapped. Check deployment");
        }

        return course.getModificationDate().getMillis();
    }

    @Override
    public MaBookmark getBookmark(Long callingNumber, Long callId) {

        Bookmark existingBookmark = bookmarkService.getLatestBookmarkByUserId(callingNumber.toString());

        if (existingBookmark != null) {
            MaBookmark toReturn = setMaBookmarkProperties(existingBookmark);
            toReturn.setCallId(callId);
            return toReturn;
        }

        return null;
    }

    @Override
    public void setBookmark(MaBookmark saveBookmark) {

        String callingNumber = saveBookmark.getCallingNumber().toString();
        Bookmark existingBookmark = bookmarkService.getLatestBookmarkByUserId(callingNumber);

        if (existingBookmark == null) {
            // if no bookmarks exist for user
            LOGGER.info("No bookmarks found for user " + callingNumber);
            bookmarkService.createBookmark(setBookmarkProperties(saveBookmark, new Bookmark()));
        } else {

            // update the first bookmark
            LOGGER.info("Updating bookmark for user");
            bookmarkService.updateBookmark(setBookmarkProperties(saveBookmark, existingBookmark));
        }

        if (saveBookmark.getBookmark() != null
                && saveBookmark.getScoresByChapter() != null
                && saveBookmark.getBookmark().equals(FINAL_BOOKMARK)
                && saveBookmark.getScoresByChapter().size() == CHAPTER_COUNT) {

            LOGGER.debug("Found last bookmark and 11 scores. Starting evaluation & notification");
            evaluateCourseCompletion(saveBookmark.getCallingNumber(), saveBookmark.getScoresByChapter());
        }
    }

    @Override
    public void triggerCompletionNotification(Long callingNumber) {

        CompletionRecord cr = completionRecordDataService.findRecordByCallingNumber(callingNumber);

        // No completion record found, fail notification
        if (cr == null) {
            throw new CourseNotCompletedException(String.format(NOT_COMPLETE, String.valueOf(callingNumber)));
        }

        // reset notification status on the completion record and try again

        if (cr.isSentNotification()) {
            LOGGER.debug("Found existing completion record, resetting and trying again");
            cr.setSentNotification(false);
            completionRecordDataService.update(cr);
        }

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("callingNumber", callingNumber);
        MotechEvent motechEvent = new MotechEvent(COURSE_COMPLETED, eventParams);
        eventRelay.sendEventMessage(motechEvent);
        LOGGER.debug("Sent event message to process completion notification");
    }

    // Map the dto to the domain object
    private Bookmark setBookmarkProperties(MaBookmark fromBookmark, Bookmark toBookmark) {

        toBookmark.setExternalId(fromBookmark.getCallingNumber().toString());

        if (toBookmark.getProgress() == null) {
            toBookmark.setProgress(new HashMap<String, Object>());
        }
        toBookmark.getProgress().put("callId", fromBookmark.getCallId());

        // This guarantees that we always update to the latest scores
        if (fromBookmark.getScoresByChapter() != null) {
            toBookmark.getProgress().put(SCORES_KEY, fromBookmark.getScoresByChapter());
        }

        if (fromBookmark.getBookmark() != null) {
            toBookmark.setChapterIdentifier(fromBookmark.getBookmark().split("_")[0]);
            toBookmark.setLessonIdentifier(fromBookmark.getBookmark().split("_")[1]);
        }

        return toBookmark;
    }

    // Map domain object to dto
    private MaBookmark setMaBookmarkProperties(Bookmark fromBookmark) {

        MaBookmark toReturn = new MaBookmark();
        toReturn.setCallingNumber(Long.parseLong(fromBookmark.getExternalId()));

        String bookmark = fromBookmark.getChapterIdentifier() + "_" + fromBookmark.getLessonIdentifier();
        Map<String, Integer> scores = getScores(fromBookmark);

        // default behavior to map the data
        toReturn.setBookmark(bookmark);
        toReturn.setScoresByChapter(scores);

        // if the bookmark is final and scores pass, reset it
        if (bookmark.equals(FINAL_BOOKMARK) && getTotalScore(scores) >= PASS_SCORE) {
            LOGGER.debug("We need to reset bookmark to new state.");
            toReturn.setScoresByChapter(null);
            toReturn.setBookmark(null);
        }

        return toReturn;
    }

    /**
     * Given a bookmark, get the scores map for it
     * @param bookmark bookmark object
     * @return map of course-score from the bookmark
     * @throws ClassCastException
     */
    private Map<String, Integer> getScores(Bookmark bookmark) {

        if (bookmark != null && bookmark.getProgress() != null) {
            return (Map<String, Integer>) bookmark.getProgress().get(SCORES_KEY);
        }

        return null;
    }

    /**
     * Helper method to check whether a course meets completion criteria
     * @param callingNumber calling number of flw
     * @param scores scores in quiz
     */
    private void evaluateCourseCompletion(Long callingNumber, Map<String, Integer> scores) {

        int totalScore = getTotalScore(scores);
        if (getTotalScore(scores) < PASS_SCORE) {
            // nothing to do
            LOGGER.debug("User with calling number: " + callingNumber + " failed with score: " + totalScore);
            return;
        }

        // We know that they completed the course here. Start post-processing
        CompletionRecord cr = completionRecordDataService.findRecordByCallingNumber(callingNumber);

        if (cr == null) {
            cr = new CompletionRecord(callingNumber, totalScore, false, 1);
            completionRecordDataService.create(cr);
        } else {
            LOGGER.debug("Found existing completion record, updating it");
            int completionCount = cr.getCompletionCount();
            cr.setCompletionCount(completionCount + 1);
            cr.setScore(totalScore);
            completionRecordDataService.update(cr);
        }

        // we updated the completion record. Start event message to trigger notification workflow
        triggerCompletionNotification(callingNumber);
    }

    /**
     * Get total scores from all chapters
     * @param scoresByChapter scores by chapter
     * @return total score
     */
    private static int getTotalScore(Map<String, Integer> scoresByChapter) {

        if (scoresByChapter == null) {
            return 0;
        }

        int totalScore = 0;
        for (int chapterCount = 1; chapterCount <= CHAPTER_COUNT; chapterCount++) {

            totalScore += scoresByChapter.get(String.valueOf(chapterCount));
        }

        return totalScore;
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @MotechListener(subjects = {
            ConfigurationConstants.FILE_CHANGED_EVENT_SUBJECT,
            ConfigurationConstants.FILE_CREATED_EVENT_SUBJECT })
    private void handleCourseChanges(MotechEvent event) {

        String filePath = (String) event.getParameters().get(ConfigurationConstants.FILE_PATH);

        if (filePath.contains(COURSE_CONTENT_FILE)) {
            LOGGER.debug("Got notification for course data change, reloading course");
            bootstrapCourse();
        } else {
            LOGGER.debug("Course file not in path, back to sleep: " + filePath);
        }

    }

    private void bootstrapCourse() {
        MaCourse course = new MaCourse();
        try (InputStream is = settingsFacade.getRawConfig(COURSE_CONTENT_FILE)) {
            String jsonText = IOUtils.toString(is);
            JSONObject jo = new JSONObject(jsonText);
            course.setName(jo.get("name").toString());
            course.setContent(jo.get("chapters").toString());
            setOrUpdateCourse(course);
        }
        catch (Exception e) {
            LOGGER.error("Error while reading course json. Check file. Exception: " + e.toString());
            alertService.create("MA.Course", "MaCourse", "Error reading course json", AlertType.CRITICAL, AlertStatus.NEW, 0, null);
        }
    }

    private void setOrUpdateCourse(MaCourse courseDto) {
        NmsCourse existing = nmsCourseDataService.getCourseByName(courseDto.getName());

        if (existing == null) {
            nmsCourseDataService.create(new NmsCourse(courseDto.getName(), courseDto.getContent()));
            LOGGER.debug("Successfully created new course");
            return;
        }

        if (existing.getContent().equals(courseDto.getContent())) {
            LOGGER.debug("Found no changes in course data, dropping update");
        } else {
            existing.setContent(courseDto.getContent());
            nmsCourseDataService.update(existing);
            LOGGER.debug("Found updated to course data and did the needful");
        }
    }

    private MaCourse mapCourseDomainToDto(NmsCourse course) {

        MaCourse courseDto = new MaCourse();
        courseDto.setName(course.getName());
        courseDto.setVersion(course.getModificationDate().getMillis());
        courseDto.setContent(course.getContent());
        return courseDto;
    }
}
