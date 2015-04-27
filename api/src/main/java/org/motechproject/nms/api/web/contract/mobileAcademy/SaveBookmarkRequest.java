package org.motechproject.nms.api.web.contract.mobileAcademy;

import java.util.Map;

/**
 * Bookmark request object to save a bookmark 2.2.5
 */
public class SaveBookmarkRequest {

    private Long callingNumber;

    private Long callId;

    private String bookmark;

    private Map<String, Integer> scoresByChapter;

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

    public Map<String, Integer> getScoresByChapter() {
        return scoresByChapter;
    }

    public void setScoresByChapter(Map<String, Integer> scoresByChapter) {
        this.scoresByChapter = scoresByChapter;
    }
}
