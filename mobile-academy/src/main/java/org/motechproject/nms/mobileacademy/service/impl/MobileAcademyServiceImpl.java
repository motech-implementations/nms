package org.motechproject.nms.mobileacademy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.NotImplementedException;
import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.repository.BookmarkDataService;
import org.motechproject.nms.mobileacademy.domain.CallDetail;
import org.motechproject.nms.mobileacademy.domain.Course;

import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Simple implementation of the {@link MobileAcademyService} interface.
 */
@Service("mobileAcademyService")
public class MobileAcademyServiceImpl implements MobileAcademyService {

    private BookmarkDataService bookmarkDataService;

    @Override
    public Course getCourse() {
        return new Course();
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

        Bookmark update;

        // if no bookmarks exist for user
        if (CollectionUtils.isEmpty(existing)) {
            update = new Bookmark();
        } else { // we found a set (usually only 1) and update it
            update = existing.get(0);
        }

        update.setExternalId(saveBookmark.getCallingNumber().toString());
        update.getProgress().put("callId", saveBookmark.getCallId());
        update.setChapterIdentifier(saveBookmark.getBookmark().split("_")[0]);
        update.setLessonIdentifier(saveBookmark.getBookmark().split("_")[1]);
        update.getProgress().put("scoresByChapter", saveBookmark.getScoresByChapter());

        bookmarkDataService.update(update);
    }

    @Override
    public void saveCallDetails(CallDetail callDetail) {

        // placeholder for void until this gets implemented
        throw new NotImplementedException();
    }

}
