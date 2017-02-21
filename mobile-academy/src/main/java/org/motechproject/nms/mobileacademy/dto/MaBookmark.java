package org.motechproject.nms.mobileacademy.dto;

import java.util.Map;

/**
 * Middle man dto to pass data from API to service layer
 */
public class MaBookmark {

    private Long flwId;

    private String callId;

    private String bookmark;

    private Map<String, Integer> scoresByChapter;

    public MaBookmark() {
    }

    public MaBookmark(Long flwId, String callId, String bookmark, Map<String, Integer> scoresByChapter) {

        this.flwId = flwId;
        this.callId = callId;
        this.bookmark = bookmark;
        this.scoresByChapter = scoresByChapter;
    }

    public Long getFlwId() {
        return flwId;
    }

    public void setFlwId(Long flwId) {
        this.flwId = flwId;
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

    public Map<String, Integer> getScoresByChapter() {
        return scoresByChapter;
    }

    public void setScoresByChapter(Map<String, Integer> scoresByChapter) {
        this.scoresByChapter = scoresByChapter;
    }
}
