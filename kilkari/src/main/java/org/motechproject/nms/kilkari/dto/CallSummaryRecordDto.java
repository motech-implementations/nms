package org.motechproject.nms.kilkari.dto;

import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.RequestId;

import java.io.Serializable;
import java.util.Map;

public class CallSummaryRecordDto implements Serializable {
    private static final long serialVersionUID = -8391255985224161089L;
    private Integer callAttempts;
    private Integer secondsPlayed;
    private Map<Integer, Integer> statusStats;
    private FinalCallStatus finalStatus;
    private String circle;
    private String languageLocationCode;
    private String contentFileName;
    private Long msisdn;
    private String weekId;
    private RequestId requestId;

    public CallSummaryRecordDto() { }

    public CallSummaryRecordDto(RequestId requestId, Long msisdn, // NO CHECKSTYLE More than 7 parameters
                             String contentFileName, String weekId, String languageLocationCode, String circle,
                             FinalCallStatus finalStatus, Map<Integer, Integer> statusStats,
                             Integer secondsPlayed, Integer callAttempts) {
        this.requestId = requestId;
        this.msisdn = msisdn;
        this.contentFileName = contentFileName;
        this.weekId = weekId;
        this.languageLocationCode = languageLocationCode;
        this.circle = circle;
        this.finalStatus = finalStatus;
        this.statusStats = statusStats;
        this.secondsPlayed = secondsPlayed;
        this.callAttempts = callAttempts;
    }

    public Integer getCallAttempts() {
        return callAttempts;
    }

    public void setCallAttempts(Integer callAttempts) {
        this.callAttempts = callAttempts;
    }

    public Integer getSecondsPlayed() {
        return secondsPlayed;
    }

    public void setSecondsPlayed(Integer secondsPlayed) {
        this.secondsPlayed = secondsPlayed;
    }

    public Map<Integer, Integer> getStatusStats() {
        return statusStats;
    }

    public void setStatusStats(Map<Integer, Integer> statusStats) {
        this.statusStats = statusStats;
    }

    public FinalCallStatus getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(FinalCallStatus finalStatus) {
        this.finalStatus = finalStatus;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    public String getContentFileName() {
        return contentFileName;
    }

    public void setContentFileName(String contentFileName) {
        this.contentFileName = contentFileName;
    }

    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
        this.msisdn = msisdn;
    }

    public String getWeekId() {
        return weekId;
    }

    public void setWeekId(String weekId) {
        this.weekId = weekId;
    }

    public RequestId getRequestId() {
        return requestId;
    }

    public void setRequestId(RequestId requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "CallSummaryRecordDto{" +
                "requestId='" + requestId + '\'' +
                ", msisdn=" + msisdn +
                '}';
    }
}
