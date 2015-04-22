package org.motechproject.nms.api.web.contract.mobileAcademy;

/**
 * Bookmark request object to save a bookmark 2.2.5
 */
public class BookmarkRequest {

    private String callingNumber;

    private String callId;

    private String bookmark;

    private String scoresByChapter;

    public String getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(String callingNumber) {
        this.callingNumber = callingNumber;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
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
