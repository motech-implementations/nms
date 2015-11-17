package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.StatusCode;

import javax.jdo.annotations.Unique;

@Entity(tableName = "nms_kk_summary_records")
public class CallSummaryRecord {

    @Field
    @Unique
    private String subscriptionId;

    @Field
    private String contentFileName;

    @Field
    private String languageCode;

    @Field
    private String circleName;

    @Field
    private String weekId;

    @Field
    private StatusCode statusCode;

    @Field
    private FinalCallStatus finalStatus;

    /**
     * Number of times the status for this call was OBD_FAILED_INVALIDNUMBER for this weekId
     */
    @Field
    private Integer invalidNumberCount;

    public CallSummaryRecord() { }

    public CallSummaryRecord(String subscriptionId, String contentFileName, String languageCode, String circleName,
                             String weekId, StatusCode statusCode, FinalCallStatus finalStatus,
                             Integer invalidNumberCount) {
        this.subscriptionId = subscriptionId;
        this.contentFileName = contentFileName;
        this.languageCode = languageCode;
        this.circleName = circleName;
        this.weekId = weekId;
        this.statusCode = statusCode;
        this.finalStatus = finalStatus;
        this.invalidNumberCount = invalidNumberCount;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getContentFileName() {
        return contentFileName;
    }

    public void setContentFileName(String contentFileName) {
        this.contentFileName = contentFileName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getCircleName() {
        return circleName;
    }

    public void setCircleName(String circleName) {
        this.circleName = circleName;
    }

    public void setSubscriptionId(String requestId) {
        this.subscriptionId = requestId;
    }

    public String getWeekId() {
        return weekId;
    }

    public void setWeekId(String weekId) {
        this.weekId = weekId;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public FinalCallStatus getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(FinalCallStatus finalStatus) {
        this.finalStatus = finalStatus;
    }

    public Integer getInvalidNumberCount() {
        return invalidNumberCount;
    }

    public void setInvalidNumberCount(Integer invalidNumberCount) {
        this.invalidNumberCount = invalidNumberCount;
    }
}
