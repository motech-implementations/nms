package org.motechproject.nms.flw.domain;

import org.codehaus.jackson.annotate.JsonBackReference;
import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

@SuppressWarnings("PMD.UnusedPrivateField")
@Entity(tableName = "nms_call_content")
public class CallContent {

    @Field
    private Long id; //NOPMD UnusedPrivateField

    @Field
    @JsonBackReference
    private CallDetailRecord callDetailRecord;

    @Field
    private String type;

    @Field
    private String mobileKunjiCardCode;

    @Field
    private String contentName;

    @Field
    private String contentFile;

    @Field
    private DateTime startTime;

    @Field
    private DateTime endTime;

    @Field
    private Boolean completionFlag;

    @Field
    private Boolean correctAnswerEntered;

    public CallDetailRecord getCallDetailRecord() {
        return callDetailRecord;
    }

    public void setCallDetailRecord(CallDetailRecord callDetailRecord) {
        this.callDetailRecord = callDetailRecord;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMobileKunjiCardCode() {
        return mobileKunjiCardCode;
    }

    public void setMobileKunjiCardCode(String mobileKunjiCardCode) {
        this.mobileKunjiCardCode = mobileKunjiCardCode;
    }

    public String getContentName() {
        return contentName;
    }

    public void setContentName(String contentName) {
        this.contentName = contentName;
    }

    public String getContentFile() {
        return contentFile;
    }

    public void setContentFile(String contentFile) {
        this.contentFile = contentFile;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getCompletionFlag() {
        return completionFlag;
    }

    public void setCompletionFlag(Boolean completionFlag) {
        this.completionFlag = completionFlag;
    }

    public Boolean getCorrectAnswerEntered() {
        return correctAnswerEntered;
    }

    public void setCorrectAnswerEntered(Boolean correctAnswerEntered) {
        this.correctAnswerEntered = correctAnswerEntered;
    }
}
