package org.motechproject.nms.api.web.contract.mobileAcademy;

/**
 * Bookmark response object for bookmarkWithScore API 2.2.3.2
 */

public class BookmarkResponse {

    // actual bookmark index in course
    private String bookmark;

    // use score history by chapter
    // TODO: Probably be an object instead of simple string here
    private String scoresByChapter;

    public String getBookmark() {
        return bookmark;
    }

    public void setBookmark(String bookmark) {
        this.bookmark = bookmark;
    }

    public String getScoresByChapter() {
        return scoresByChapter;
    }

    public void setScoresByChapter(String scoresByChapter) {
        this.scoresByChapter = scoresByChapter;
    }
}
