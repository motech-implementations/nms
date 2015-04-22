package org.motechproject.nms.api.web.contract;

import java.util.Collections;
import java.util.List;

public class CallDetailRecordRequest {
    private String callingNumber;
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

    public String getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(String callingNumber) {
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
}
