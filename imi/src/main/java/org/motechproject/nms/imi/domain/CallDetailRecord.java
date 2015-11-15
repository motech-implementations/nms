package org.motechproject.nms.imi.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Index;

/**
 * See NMS API - 4.4.3 CDR Detail File Format
 */
@Entity(tableName = "nms_imi_cdrs")
public class CallDetailRecord {

    @Field
    @Index //In an ideal world this would be unique, but there are lots of existing dupe rows in the production db
    private String requestId;

    @Field
    private Long msisdn;

    @Field
    private String callId;

    @Field
    private String attemptNo;

    @Field
    private String callStartTime;

    @Field
    private String callAnswerTime;

    @Field
    private String callEndTime;

    @Field
    private String callDurationInPulse;

    @Field
    private String callStatus;

    @Field
    private String languageLocationId;

    @Field
    private String contentFile;

    @Field
    private String msgPlayStartTime;

    @Field
    private String msgPlayEndTime;

    @Field
    private String circleId;

    @Field
    private String operatorId;

    @Field
    private String priority;

    @Field
    private String callDisconnectReason;

    @Field
    private String weekId;

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

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getAttemptNo() {
        return attemptNo;
    }

    public void setAttemptNo(String attemptNo) {
        this.attemptNo = attemptNo;
    }

    public String getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(String callStartTime) {
        this.callStartTime = callStartTime;
    }

    public String getCallAnswerTime() {
        return callAnswerTime;
    }

    public void setCallAnswerTime(String callAnswerTime) {
        this.callAnswerTime = callAnswerTime;
    }

    public String getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(String callEndTime) {
        this.callEndTime = callEndTime;
    }

    public String getCallDurationInPulse() {
        return callDurationInPulse;
    }

    public void setCallDurationInPulse(String callDurationInPulse) {
        this.callDurationInPulse = callDurationInPulse;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public String getLanguageLocationId() {
        return languageLocationId;
    }

    public void setLanguageLocationId(String languageLocationId) {
        this.languageLocationId = languageLocationId;
    }

    public String getContentFile() {
        return contentFile;
    }

    public void setContentFile(String contentFile) {
        this.contentFile = contentFile;
    }

    public String getMsgPlayStartTime() {
        return msgPlayStartTime;
    }

    public void setMsgPlayStartTime(String msgPlayStartTime) {
        this.msgPlayStartTime = msgPlayStartTime;
    }

    public String getMsgPlayEndTime() {
        return msgPlayEndTime;
    }

    public void setMsgPlayEndTime(String msgPlayEndTime) {
        this.msgPlayEndTime = msgPlayEndTime;
    }

    public String getCircleId() {
        return circleId;
    }

    public void setCircleId(String circleId) {
        this.circleId = circleId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCallDisconnectReason() {
        return callDisconnectReason;
    }

    public void setCallDisconnectReason(String callDisconnectReason) {
        this.callDisconnectReason = callDisconnectReason;
    }

    public String getWeekId() {
        return weekId;
    }

    public void setWeekId(String weekId) {
        this.weekId = weekId;
    }
}
