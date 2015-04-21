package org.motechproject.nms.api.web.contract.kilkari;

import java.util.List;

/**
 * POST request data for 4.2.5 : http://<motech:port>/motech­platform­server/module/kilkari/inboxCallDetails
 */
public class InboxCallDetailsRequest {
    private String callingNumber;
    private String operator;
    private String circle;
    private String callId;
    private String callStartTime;
    private String callEndTime;
    private String callDurationInPulses;
    private String callStatus;
    private String callDisconnectReason;
    private List<InboxCallDetailsRequestCallData> content;
    private String failureReason;

    public String getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(String callingNumber) {
        this.callingNumber = callingNumber;
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

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(String callStartTime) {
        this.callStartTime = callStartTime;
    }

    public String getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(String callEndTime) {
        this.callEndTime = callEndTime;
    }

    public String getCallDurationInPulses() {
        return callDurationInPulses;
    }

    public void setCallDurationInPulses(String callDurationInPulses) {
        this.callDurationInPulses = callDurationInPulses;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public String getCallDisconnectReason() {
        return callDisconnectReason;
    }

    public void setCallDisconnectReason(String callDisconnectReason) {
        this.callDisconnectReason = callDisconnectReason;
    }

    public List<InboxCallDetailsRequestCallData> getContent() {
        return content;
    }

    public void setContent(List<InboxCallDetailsRequestCallData> content) {
        this.content = content;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
