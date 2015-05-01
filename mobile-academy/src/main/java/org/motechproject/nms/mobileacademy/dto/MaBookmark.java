package org.motechproject.nms.mobileacademy.dto;

import java.util.Map;

/**
 * Middle man dto to pass data from API to service layer
 */
public class MaBookmark {

    private Long callingNumber;

    private Long callId;

    private String bookmark;

    private Map<String, Integer> scoresByChapter;

    public MaBookmark() {
    }

    public MaBookmark(Long callingNumber, Long callId, String bookmark, Map<String, Integer> scoresByChapter) {

        this.callingNumber = callingNumber;
        this.callId = callId;
        this.bookmark = bookmark;
        this.scoresByChapter = scoresByChapter;
    }

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
