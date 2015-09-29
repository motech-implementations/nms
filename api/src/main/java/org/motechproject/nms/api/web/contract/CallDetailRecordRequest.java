package org.motechproject.nms.api.web.contract;

import java.util.Collections;
import java.util.List;

/**
 * Request body
 *
 * 2.2.6 Save CallDetails API
 * IVR shall invoke this API to send MA call details to MoTech.
 * /api/mobileacademy/callDetails
 *
 * 3.2.2 Save Call Details API
 * This API enables IVR to send call details to NMS_MoTech_MK. This data is further saved in NMS database and used
 *    for reporting purpose.
 * /api/mobilekunji/callDetails
 *
 */
public class CallDetailRecordRequest {
    private Long callingNumber;
    private String callId;
    private String operator;
    private String circle;
    private Long callStartTime;
    private Long callEndTime;
    private Integer callDurationInPulses;
    private Integer endOfUsagePromptCounter;
    private Boolean welcomeMessagePromptFlag;
    private Integer callStatus;
    private Integer callDisconnectReason;
    private List<CallContentRequest> content;

    public CallDetailRecordRequest() { }

    public Long getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(Long callingNumber) {
        this.callingNumber = callingNumber;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public Long getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(Long callStartTime) {
        this.callStartTime = callStartTime;
    }

    public Long getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(Long callEndTime) {
        this.callEndTime = callEndTime;
    }

    public Integer getCallDurationInPulses() {
        return callDurationInPulses;
    }

    public void setCallDurationInPulses(Integer callDurationInPulses) {
        this.callDurationInPulses = callDurationInPulses;
    }

    public Integer getEndOfUsagePromptCounter() {
        return endOfUsagePromptCounter;
    }

    public void setEndOfUsagePromptCounter(Integer endOfUsagePromptCounter) {
        this.endOfUsagePromptCounter = endOfUsagePromptCounter;
    }

    public Boolean getWelcomeMessagePromptFlag() {
        return welcomeMessagePromptFlag;
    }

    public void setWelcomeMessagePromptFlag(Boolean welcomeMessagePromptFlag) {
        this.welcomeMessagePromptFlag = welcomeMessagePromptFlag;
    }

    public Integer getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(Integer callStatus) {
        this.callStatus = callStatus;
    }

    public Integer getCallDisconnectReason() {
        return callDisconnectReason;
    }

    public void setCallDisconnectReason(Integer callDisconnectReason) {
        this.callDisconnectReason = callDisconnectReason;
    }

    public List<CallContentRequest> getContent() {
        if (null != content) {
            return content;
        }

        return Collections.emptyList();
    }

    public void setContent(List<CallContentRequest> content) {
        this.content = content;
    }

    // Must hide PII
    @Override
    public String toString() {
        return "CallDetailRecordRequest{" +
                "callingNumber=" + LogHelper.obscure(callingNumber) +
                ", callId=" + callId +
                ", operator='" + operator + '\'' +
                ", circle='" + circle + '\'' +
                ", callStartTime=" + callStartTime +
                ", callEndTime=" + callEndTime +
                ", callDurationInPulses=" + callDurationInPulses +
                ", endOfUsagePromptCounter=" + endOfUsagePromptCounter +
                ", welcomeMessagePromptFlag=" + welcomeMessagePromptFlag +
                ", callStatus=" + callStatus +
                ", callDisconnectReason=" + callDisconnectReason +
                ", content=" + content +
                '}';
    }
}
