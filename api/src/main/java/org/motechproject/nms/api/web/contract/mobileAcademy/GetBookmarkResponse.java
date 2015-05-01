package org.motechproject.nms.api.web.contract.mobileAcademy;

import java.util.Map;

/**
 * Bookmark response object for bookmarkWithScore API 2.2.3.2
 */

public class GetBookmarkResponse {

    // actual bookmark index in course
    private String bookmark;

    private Map<String, Integer> scoresByChapter;

    public GetBookmarkResponse() {
    }

    public String getBookmark() {
        return bookmark;
    }

    public void setBookmark(String bookmark) {
        this.bookmark = bookmark;
    }

    public Map<String, Integer> getScoresByChapter() {
        return scoresByChapter;
    }

    public void setScoresByChapter(Map<String, Integer> scoresByChapter) {
        this.scoresByChapter = scoresByChapter;
    }
}
