package org.motechproject.nms.mobileacademy.service.impl;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.config.core.constants.ConfigurationConstants;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mtraining.domain.ActivityRecord;
import org.motechproject.mtraining.domain.ActivityState;
import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.service.ActivityService;
import org.motechproject.mtraining.service.BookmarkService;
import org.motechproject.nms.mobileacademy.domain.CompletionRecord;
import org.motechproject.nms.mobileacademy.domain.NmsCourse;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.dto.MaCourse;
import org.motechproject.nms.mobileacademy.exception.CourseNotCompletedException;
import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.repository.NmsCourseDataService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.motechproject.nms.props.service.LogHelper;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    private static final String FINAL_BOOKMARK = "COURSE_COMPLETED";

    private static final String COURSE_COMPLETED = "nms.ma.course.completed";

    private static final String SCORES_KEY = "scoresByChapter";

    private static final String BOOKMARK_KEY = "bookmark";

    private static final String NOT_COMPLETE = "<%s: Course not complete>";

    private static final String COURSE_ENTITY_NAME = "MA.Course";

    private static final int CHAPTER_COUNT = 11;

    private static final int PASS_SCORE = 22;

    private static final int MILLIS_PER_SEC = 1000;

    /**
     * Bookmark service to get and set bookmarks
     */
    private BookmarkService bookmarkService;

    /**
     * Activity service to track user completion data
     */
    private ActivityService activityService;

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
                                    ActivityService activityService,
                                    NmsCourseDataService nmsCourseDataService,
                                    CompletionRecordDataService completionRecordDataService,
                                    EventRelay eventRelay,
                                    @Qualifier("maSettings") SettingsFacade settingsFacade,
                                    AlertService alertService) {
        this.bookmarkService = bookmarkService;
        this.activityService = activityService;
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
            alertService.create(COURSE_ENTITY_NAME, COURSE_NAME, "Could not find course", AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            throw new IllegalStateException("No course bootstrapped. Check deployment");
        }

        return mapCourseDomainToDto(course);
    }

    @Override
    public void setCourse(MaCourse courseDto) {

        if (courseDto == null) {
            LOGGER.error("Attempted to set null course, exiting operation");
            alertService.create(COURSE_ENTITY_NAME, "MaCourse", "Trying to set null MaCourse", AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            return;
        }

        setOrUpdateCourse(courseDto);
    }

    @Override
    public long getCourseVersion() {

        NmsCourse course = nmsCourseDataService.getCourseByName(COURSE_NAME);

        if (course == null) {
            alertService.create(COURSE_ENTITY_NAME, COURSE_NAME, "Could not find course", AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            throw new IllegalStateException("No course bootstrapped. Check deployment");
        }

        return course.getModificationDate().getMillis() / MILLIS_PER_SEC; //Unix epoch is represented in seconds
    }

    @Override
    public MaBookmark getBookmark(Long callingNumber, String callId) {

        Bookmark existingBookmark = bookmarkService.getLatestBookmarkByUserId(callingNumber.toString());

        if (existingBookmark != null) {
            MaBookmark toReturn = setMaBookmarkProperties(existingBookmark);
            toReturn.setCallId(callId);
            return toReturn;
        }

        return null;
    }

    @Override
    public MaBookmark getBookmarkOps(Long callingNumber) {
        LOGGER.debug("Retrieve bookmark by Ops");
        Bookmark existingBookmark = bookmarkService.getLatestBookmarkByUserId(callingNumber.toString());
        if (existingBookmark != null) {
            MaBookmark toReturn = new MaBookmark();
            toReturn.setCallingNumber(Long.parseLong(existingBookmark.getExternalId()));

            // default behavior to map the data
            if (existingBookmark.getProgress() != null) {
                Object bookmark = existingBookmark.getProgress().get(BOOKMARK_KEY);
                toReturn.setBookmark(bookmark == null ? null : bookmark.toString());
                toReturn.setScoresByChapter((Map<String, Integer>) existingBookmark.getProgress().get(SCORES_KEY));
            }
        }

        return null;
    }

    @Override
    public void setBookmark(MaBookmark saveBookmark) {

        if (saveBookmark == null) {
            LOGGER.error("Bookmark cannot be null, check request");
            throw new IllegalArgumentException("Invalid bookmark, cannot be null");
        }

        String callingNumber = saveBookmark.getCallingNumber().toString();
        Bookmark existingBookmark = bookmarkService.getLatestBookmarkByUserId(callingNumber);

        // write a new activity record if existing bookmark is null or
        // existing bookmark has no progress from earlier reset
        if (existingBookmark == null ||
                (existingBookmark.getProgress() != null && existingBookmark.getProgress().isEmpty())) {
            activityService.createActivity(
                    new ActivityRecord(callingNumber, null, null, null, DateTime.now(), null, ActivityState.STARTED));
        }

        if (existingBookmark == null) {
            // if no bookmarks exist for user
            LOGGER.info("No bookmarks found for user " + LogHelper.obscure(saveBookmark.getCallingNumber()));
            bookmarkService.createBookmark(setBookmarkProperties(saveBookmark, new Bookmark()));
        } else {

            // update the first bookmark
            LOGGER.info("Updating bookmark for user");
            bookmarkService.updateBookmark(setBookmarkProperties(saveBookmark, existingBookmark));
        }

        if (saveBookmark.getBookmark() != null
                && saveBookmark.getBookmark().equals(FINAL_BOOKMARK)
                && saveBookmark.getScoresByChapter() != null
                && saveBookmark.getScoresByChapter().size() == CHAPTER_COUNT) {

            LOGGER.debug("Found last bookmark and 11 scores. Starting evaluation & notification");
            // Create an activity record here since pass/fail counts as 1 try
            activityService.createActivity(
                    new ActivityRecord(callingNumber, null, null, null, null, DateTime.now(), ActivityState.COMPLETED));
            evaluateCourseCompletion(saveBookmark.getCallingNumber(), saveBookmark.getScoresByChapter());
        }
    }

    @Override
    public void triggerCompletionNotification(Long callingNumber) {

        final CompletionRecord cr = completionRecordDataService.findRecordByCallingNumber(callingNumber);

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

        // If this is running inside a transaction (which it probably always will), then send the event after
        // the db commit. Else, most likely in a test, send it right away
        // https://github.com/motech-implementations/mim/issues/518
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    sendEvent(cr.getCallingNumber());
                }
            });
        } else {
            sendEvent(cr.getCallingNumber());
        }
    }

    /**
     * Send event to notify
     * @param callingNumber calling number to notify
     */
    private void sendEvent(Long callingNumber) {

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

        String bookmark = fromBookmark.getBookmark();
        if (bookmark != null) {
            toBookmark.getProgress().put(BOOKMARK_KEY, bookmark);
        }

        return toBookmark;
    }

    // Map domain object to dto
    private MaBookmark setMaBookmarkProperties(Bookmark fromBookmark) {

        MaBookmark toReturn = new MaBookmark();
        toReturn.setCallingNumber(Long.parseLong(fromBookmark.getExternalId()));

        // default behavior to map the data
        if (fromBookmark.getProgress() != null) {
            Object bookmark = fromBookmark.getProgress().get(BOOKMARK_KEY);
            toReturn.setBookmark(bookmark == null ? null : bookmark.toString());
            toReturn.setScoresByChapter((Map<String, Integer>) fromBookmark.getProgress().get(SCORES_KEY));
        }

        // if the bookmark is final, reset it
        if (toReturn.getBookmark() != null && toReturn.getBookmark().equals(FINAL_BOOKMARK)) {
            LOGGER.debug("We need to reset bookmark to new state.");
            fromBookmark.setProgress(new HashMap<String, Object>());
            bookmarkService.updateBookmark(fromBookmark);

            toReturn.setScoresByChapter(null);
            toReturn.setBookmark(null);
        }

        return toReturn;
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
            LOGGER.debug("User with calling number: " + LogHelper.obscure(callingNumber) + " failed with score: " + totalScore);
            return;
        }

        // We know that they completed the course here. Start post-processing
        CompletionRecord cr = completionRecordDataService.findRecordByCallingNumber(callingNumber);

        if (cr == null) {
            LOGGER.debug("No existing completion record. Creating new record");
            cr = new CompletionRecord(callingNumber, totalScore);
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

    @MotechListener(subjects = {
            ConfigurationConstants.FILE_CHANGED_EVENT_SUBJECT,
            ConfigurationConstants.FILE_CREATED_EVENT_SUBJECT })
    @Transactional
    public void handleCourseChanges(MotechEvent event) {

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
            // TODO: validate the json format here
            course.setName(jo.get("name").toString());
            course.setContent(jo.get("chapters").toString());
            setOrUpdateCourse(course);
        }
        catch (Exception e) {
            LOGGER.error("Error while reading course json. Check file. Exception: " + e.toString());
            alertService.create(COURSE_ENTITY_NAME, "MaCourse", "Error reading course json", AlertType.CRITICAL, AlertStatus.NEW, 0, null);
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
        courseDto.setVersion(course.getModificationDate().getMillis() / MILLIS_PER_SEC);
        courseDto.setContent(course.getContent());
        return courseDto;
    }
}
