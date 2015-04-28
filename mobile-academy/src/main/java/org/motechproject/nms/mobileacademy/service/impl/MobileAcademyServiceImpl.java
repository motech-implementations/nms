package org.motechproject.nms.mobileacademy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.NotImplementedException;
import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.repository.BookmarkDataService;
import org.motechproject.nms.mobileacademy.domain.CallDetail;
import org.motechproject.nms.mobileacademy.domain.Course;

import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.repository.CourseDataService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Simple implementation of the {@link MobileAcademyService} interface.
 */
@Service("mobileAcademyService")
public class MobileAcademyServiceImpl implements MobileAcademyService {

    /**
     * Bookmark data service
     */
    private BookmarkDataService bookmarkDataService;

    /**
     * Course data service
     */
    private CourseDataService courseDataService;

    @Override
    public Course getCourse() {

        // TODO: Make this configurable
        return courseDataService.findCourseByName("MobileAcademyCourse");
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
    public Integer getCourseVersion() {

        return 1;
    }

    @Override
    public MaBookmark getBookmark(Long callingNumber, Long callId) {

        List<Bookmark> bookmarks = bookmarkDataService.findBookmarksForUser(callingNumber.toString());
        if (CollectionUtils.isEmpty(bookmarks)) {
            return null;
        } else {
            Bookmark existingBookmark = bookmarks.get(0);
            MaBookmark toReturn = new MaBookmark();
            toReturn.setCallingNumber(Long.parseLong(existingBookmark.getExternalId()));
            toReturn.setCallId((Long) existingBookmark.getProgress().get("callId"));
            toReturn.setScoresByChapter((Map<String, Integer>) existingBookmark.getProgress()
                    .get("scoresByChapter"));
            toReturn.setBookmark(existingBookmark.getChapterIdentifier() + "_" +
                    existingBookmark.getLessonIdentifier());
            return toReturn;
        }
    }

    @Override
    public void setBookmark(MaBookmark saveBookmark) {

        List<Bookmark> existing = bookmarkDataService.findBookmarksForUser(saveBookmark.getCallingNumber().toString());

        if (CollectionUtils.isEmpty(existing)) {
            // if no bookmarks exist for user
            bookmarkDataService.create(setProperties(saveBookmark, new Bookmark()));
        } else {
            // we found a list (usually only 1) and update it
            bookmarkDataService.update(setProperties(saveBookmark, existing.get(0)));
        }
    }

    private Bookmark setProperties(MaBookmark fromBookmark, Bookmark toBookmark) {
        toBookmark.setExternalId(fromBookmark.getCallingNumber().toString());
        toBookmark.getProgress().put("callId", fromBookmark.getCallId());
        toBookmark.setChapterIdentifier(fromBookmark.getBookmark().split("_")[0]);
        toBookmark.setLessonIdentifier(fromBookmark.getBookmark().split("_")[1]);
        toBookmark.getProgress().put("scoresByChapter", fromBookmark.getScoresByChapter());
        return toBookmark;
    }

    @Override
    public void saveCallDetails(CallDetail callDetail) {

        // placeholder for void until this gets implemented
        throw new NotImplementedException();
    }

}
