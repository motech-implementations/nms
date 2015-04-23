package org.motechproject.nms.api.web.contract.mobileAcademy;

/**
 * Bookmark request object to save a bookmark 2.2.5
 */
public class BookmarkRequest {

    private Long callingNumber;

    private Long callId;

    private String bookmark;

    private String scoresByChapter;

    public Long getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(Long callingNumber) {
        this.callingNumber = callingNumber;
    }

    public Long getCallId() {
        return callId;
    }

    public void setCallId(Long callId) {
        this.callId = callId;
    }

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
