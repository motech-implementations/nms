package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.props.domain.FinalCallStatus;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Map;

@Entity(tableName = "nms_kk_summary_records")
public class CallSummaryRecord {

    @Field
    @Unique
    private String requestId;

    @Field
    @Min(value = 1000000000L, message = "msisdn must be 10 digits")
    @Max(value = 9999999999L, message = "msisdn must be 10 digits")
    @Column(length = 10)
    private Long msisdn;

    @Field
    private String contentFileName;

    @Field
    private String weekId;

    @Field
    private String languageLocationCode;

    @Field
    private String circle;

    @Field
    private FinalCallStatus finalStatus;

    @Field
    private Map<Integer, Integer> statusStats;

    @Field
    @Min(value = 0, message = "percentPlayed must be >= 0")
    @Max(value = 100, message = "percentPlayed must be <= 100")
    private Integer percentPlayed;

    @Field
    private Integer callAttempts;

    @Field
    private Integer attemptedDayCount;

    public CallSummaryRecord() { }

    public CallSummaryRecord(String requestId, Long msisdn, // NO CHECKSTYLE More than 7 parameters
                             String contentFileName, String weekId, String languageLocationCode, String circle,
                             FinalCallStatus finalStatus, Map<Integer, Integer> statusStats,
                             Integer percentPlayed, Integer callAttempts, Integer attemptedDayCount) {
        this.requestId = requestId;
        this.msisdn = msisdn;
        this.contentFileName = contentFileName;
        this.weekId = weekId;
        this.languageLocationCode = languageLocationCode;
        this.circle = circle;
        this.finalStatus = finalStatus;
        this.statusStats = statusStats;
        this.percentPlayed = percentPlayed;
        this.callAttempts = callAttempts;
        this.attemptedDayCount = attemptedDayCount;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
        this.msisdn = msisdn;
    }

    public String getContentFileName() {
        return contentFileName;
    }

    public void setContentFileName(String contentFileName) {
        this.contentFileName = contentFileName;
    }

    public String getWeekId() {
        return weekId;
    }

    public void setWeekId(String weekId) {
        this.weekId = weekId;
    }

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public FinalCallStatus getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(FinalCallStatus finalStatus) {
        this.finalStatus = finalStatus;
    }

    public Map<Integer, Integer> getStatusStats() {
        return statusStats;
    }

    public void setStatusStats(Map<Integer, Integer> statusStats) {
        this.statusStats = statusStats;
    }

    public Integer getPercentPlayed() {
        return percentPlayed;
    }

    public void setPercentPlayed(Integer percentPlayed) {
        this.percentPlayed = percentPlayed;
    }

    public Integer getCallAttempts() {
        return callAttempts;
    }

    public void setCallAttempts(Integer callAttempts) {
        this.callAttempts = callAttempts;
    }

    public Integer getAttemptedDayCount() {
        return attemptedDayCount;
    }

    public void setAttemptedDayCount(Integer attemptedDayCount) {
        this.attemptedDayCount = attemptedDayCount;
    }

    @Override
    public String toString() {
        return "CallSummaryRecord{" +
                "requestId='" + requestId + '\'' +
                ", msisdn=" + msisdn +
                '}';
    }

}
