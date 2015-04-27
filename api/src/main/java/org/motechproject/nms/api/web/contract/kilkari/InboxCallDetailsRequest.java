package org.motechproject.nms.api.web.contract.kilkari;

import java.util.Set;

/**
 * Request body
 *
 * 4.2.5 Save Inbox Call Details
 * IVR shall invoke this API to send the call detail information corresponding to the Inbox access inbound call
 *    for which inbox message(s) is played.
 * /api/kilkari/inboxCallDetails
 *
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
    private Set<CallDataRequest> content;

    public InboxCallDetailsRequest() { }

    public InboxCallDetailsRequest(Long callingNumber, // NO CHECKSTYLE More than 7 parameters
                                   String operator, String circle, Long callId, Long callStartTime,
                                   Long callEndTime, Integer callDurationInPulses, Integer callStatus,
                                   Integer callDisconnectReason, Set<CallDataRequest> content) {
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

    public Set<CallDataRequest> getContent() {
        return content;
    }

    public void setContent(Set<CallDataRequest> content) {
        this.content = content;
    }
}
