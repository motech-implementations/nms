package org.motechproject.nms.api.web.contract.kilkari;

import java.util.List;

/**
 * POST request data for 4.2.5 : http://<motech:port>/motech­platform­server/module/kilkari/inboxCallDetails
 */
public class InboxCallDetailsRequest {
    private Long callingNumber;
    private String operator;
    private String circle;
    private Long callId;
    private Long callStartTime;
    private Long callEndTime;
    private Integer callDurationInPulses;
    private Integer callStatus;
    private Integer callDisconnectReason;
    private List<InboxCallDetailsRequestCallData> content;
    private String failureReason;

    public InboxCallDetailsRequest() { }

    public InboxCallDetailsRequest(Long callingNumber, // NO CHECKSTYLE More than 7 parameters
                                   String operator, String circle, Long callId, Long callStartTime,
                                   Long callEndTime, Integer callDurationInPulses, Integer callStatus,
                                   Integer callDisconnectReason, List<InboxCallDetailsRequestCallData> content,
                                   String failureReason) {
        this.callingNumber = callingNumber;
        this.operator = operator;
        this.circle = circle;
        this.callId = callId;
        this.callStartTime = callStartTime;
        this.callEndTime = callEndTime;
        this.callDurationInPulses = callDurationInPulses;
        this.callStatus = callStatus;
        this.callDisconnectReason = callDisconnectReason;
        this.content = content;
        this.failureReason = failureReason;
    }

    public Long getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(Long callingNumber) {
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

    public Long getCallId() {
        return callId;
    }

    public void setCallId(Long callId) {
        this.callId = callId;
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
