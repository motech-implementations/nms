package org.motechproject.nms.mobileacademy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.repository.BookmarkDataService;
import org.motechproject.nms.mobileacademy.domain.CompletionRecord;
import org.motechproject.nms.mobileacademy.domain.Course;

import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.repository.CompletionRecordDataService;
import org.motechproject.nms.mobileacademy.repository.CourseDataService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple implementation of the {@link MobileAcademyService} interface.
 */
@Service("mobileAcademyService")
public class MobileAcademyServiceImpl implements MobileAcademyService {

    private static final String FINAL_BOOKMARK = "Chapter11_Quiz";

    private static final int CHAPTER_COUNT = 11;

    private static final int PASS_SCORE = 22;

    /**
     * Bookmark data service
     */
    private BookmarkDataService bookmarkDataService;

    /**
     * Completion record data service
     */
    private CompletionRecordDataService completionRecordDataService;

    /**
     * Course data service
     */
    private CourseDataService courseDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileAcademyServiceImpl.class);

    @Autowired
    public MobileAcademyServiceImpl(BookmarkDataService bookmarkDataService,
                                    CourseDataService courseDataService,
                                    CompletionRecordDataService completionRecordDataService) {
        this.bookmarkDataService = bookmarkDataService;
        this.courseDataService = courseDataService;
        this.completionRecordDataService = completionRecordDataService;
    }

    @Override
    public Course getCourse() {

        // Make this course name configurable
        String lookupName = "MobileAcademyCourse";
        return courseDataService.findCourseByName(lookupName);
    }

    @Override
    public void setCourse(Course course) {
        Course existing = courseDataService.findCourseByName(course.getName());

        if (existing == null) {
            courseDataService.create(course);
        } else {
            course.setId(existing.getId());
            courseDataService.update(course);
        }
    }

    @Override
    public int getCourseVersion() {

        Course course = getCourse();
        if (course != null) {
            DateTime version = course.getModificationDate();
            return (int) version.getMillis();
        } else {
            // return -1 and let the caller handle the upstream response
            return -1;
        }
    }

    @Override
    public MaBookmark getBookmark(Long callingNumber, Long callId) {

        List<Bookmark> bookmarks = bookmarkDataService.findBookmarksForUser(callingNumber.toString());
        if (CollectionUtils.isEmpty(bookmarks)) {
            return null;
        } else {

            if (bookmarks.size() > 1) {
                LOGGER.debug("Found more than 1 instance of valid bookmark, picking top");
            }

            Bookmark existingBookmark = bookmarks.get(0);
            MaBookmark toReturn = new MaBookmark();
            toReturn.setCallingNumber(Long.parseLong(existingBookmark.getExternalId()));
            toReturn.setCallId(callId);

            if (existingBookmark.getProgress() != null) {
                toReturn.setScoresByChapter((Map<String, Integer>) existingBookmark.getProgress()
                        .get("scoresByChapter"));
                toReturn.setBookmark(existingBookmark.getChapterIdentifier() + "_" +
                        existingBookmark.getLessonIdentifier());
            } else {
                toReturn.setScoresByChapter(null);
                toReturn.setBookmark(null);
            }


            return toReturn;
        }
    }

    @Override
    public void setBookmark(MaBookmark saveBookmark) {

        String callingNumber = saveBookmark.getCallingNumber().toString();
        List<Bookmark> existing = bookmarkDataService.findBookmarksForUser(callingNumber);

        if (CollectionUtils.isEmpty(existing)) {
            // if no bookmarks exist for user
            LOGGER.info("No bookmarks found for user " + callingNumber);
            bookmarkDataService.create(setBookmarkProperties(saveBookmark, new Bookmark()));
        } else {
            // error check
            if (existing.size() > 1) {
                LOGGER.error("Found more than 1 bookmark for calling number. This should never be possible.");
                LOGGER.error("Contact dev team about calling number: " + callingNumber);
            }

            // update the first bookmark
            LOGGER.info("Updating the first bookmark for user");
            bookmarkDataService.update(setBookmarkProperties(saveBookmark, existing.get(0)));
        }

        if (saveBookmark.getBookmark().equals(FINAL_BOOKMARK)
                && saveBookmark.getScoresByChapter().size() == CHAPTER_COUNT) {

            evaluateCourseCompletion(saveBookmark.getCallingNumber(), saveBookmark.getScoresByChapter());
        }
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
            toBookmark.getProgress().put("scoresByChapter", fromBookmark.getScoresByChapter());
        }

        if (fromBookmark.getBookmark() != null) {
            toBookmark.setChapterIdentifier(fromBookmark.getBookmark().split("_")[0]);
            toBookmark.setLessonIdentifier(fromBookmark.getBookmark().split("_")[1]);
        }

        return toBookmark;
    }

    private void evaluateCourseCompletion(Long callingNumber, Map<String, Integer> scores) {

        int totalScore = 0;
        for (int chapterCount = 1; chapterCount <= CHAPTER_COUNT; chapterCount++) {

            totalScore += scores.get(String.valueOf(chapterCount));
        }

        if (totalScore >= PASS_SCORE) {

            CompletionRecord cr = new CompletionRecord(callingNumber, totalScore);

            // TODO: check for existing records first
            completionRecordDataService.create(cr);
        }

    }

}
