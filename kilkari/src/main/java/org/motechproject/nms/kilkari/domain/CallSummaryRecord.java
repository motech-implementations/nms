package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.StatusCode;

import javax.jdo.annotations.Unique;

@Entity(tableName = "nms_kk_summary_records")
public class CallSummaryRecord {

    //
    // NOTE: we repurposed this field to contain a subscriptionId, see lookupAndFixOldCsr
    //
    @Field
    @Unique
    private String requestId;

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

    public CallSummaryRecord() { }

    public CallSummaryRecord(String subscriptionId, String contentFileName, String languageCode, String circleName,
                             String weekId, StatusCode statusCode, FinalCallStatus finalStatus) {
        this.requestId = subscriptionId;
        this.contentFileName = contentFileName;
        this.languageCode = languageCode;
        this.circleName = circleName;
        this.weekId = weekId;
        this.statusCode = statusCode;
        this.finalStatus = finalStatus;
    }

    public String getRequestId() {
        return requestId;
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

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

}
