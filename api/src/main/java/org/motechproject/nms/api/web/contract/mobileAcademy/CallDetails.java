package org.motechproject.nms.api.web.contract.mobileAcademy;

import java.util.List;

/**
 * Call details object to track the completion of an MA call
 */
public class CallDetails {

    private Long callingNumber;

    private Long callId;

    private String operator;

    private String circle;

    private int callStartTime;

    private int callEndTime;

    private int callDurationInPulses;

    private int endOfUsagePromptCounter;

    private int callStatus;

    private int callDisconnectReason;

    private List<CallData> content;

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

    public int getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(int callStartTime) {
        this.callStartTime = callStartTime;
    }

    public int getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(int callEndTime) {
        this.callEndTime = callEndTime;
    }

    public int getCallDurationInPulses() {
        return callDurationInPulses;
    }

    public void setCallDurationInPulses(int callDurationInPulses) {
        this.callDurationInPulses = callDurationInPulses;
    }

    public int getEndOfUsagePromptCounter() {
        return endOfUsagePromptCounter;
    }

    public void setEndOfUsagePromptCounter(int endOfUsagePromptCounter) {
        this.endOfUsagePromptCounter = endOfUsagePromptCounter;
    }

    public int getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(int callStatus) {
        this.callStatus = callStatus;
    }

    public int getCallDisconnectReason() {
        return callDisconnectReason;
    }

    public void setCallDisconnectReason(int callDisconnectReason) {
        this.callDisconnectReason = callDisconnectReason;
    }

    public List<CallData> getContent() {
        return content;
    }

    public void setContent(List<CallData> content) {
        this.content = content;
    }
}
