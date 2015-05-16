package org.motechproject.nms.kilkari.dto;

import org.joda.time.DateTime;
import org.motechproject.nms.props.domain.CallDisconnectReason;
import org.motechproject.nms.props.domain.RequestId;
import org.motechproject.nms.props.domain.StatusCode;

public class CallDetailRecordDto {

    private RequestId requestId;
    private Long msisdn;
    private DateTime callAnswerTime;
    private Integer msgPlayDuration;
    private StatusCode statusCode;
    private String languageLocationId;
    private String contentFile;
    private String circleId;
    private String operatorId;
    private CallDisconnectReason callDisconnectReason;
    private String weekId;

    public CallDetailRecordDto() { }

    public RequestId getRequestId() {
        return requestId;
    }

    public void setRequestId(RequestId requestId) {
        this.requestId = requestId;
    }

    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
        this.msisdn = msisdn;
    }

    public DateTime getCallAnswerTime() {
        return callAnswerTime;
    }

    public void setCallAnswerTime(DateTime callAnswerTime) {
        this.callAnswerTime = callAnswerTime;
    }

    public Integer getMsgPlayDuration() {
        return msgPlayDuration;
    }

    public void setMsgPlayDuration(Integer msgPlayDuration) {
        this.msgPlayDuration = msgPlayDuration;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
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

    public CallDisconnectReason getCallDisconnectReason() {
        return callDisconnectReason;
    }

    public void setCallDisconnectReason(CallDisconnectReason callDisconnectReason) {
        this.callDisconnectReason = callDisconnectReason;
    }

    public String getWeekId() {
        return weekId;
    }

    public void setWeekId(String weekId) {
        this.weekId = weekId;
    }

    @Override
    public String toString() {
        return "CallDetailRecordDto{" +
                "requestId=" + requestId +
                ", msisdn=" + msisdn +
                ", callAnswerTime=" + callAnswerTime +
                ", msgPlayDuration=" + msgPlayDuration +
                ", statusCode=" + statusCode +
                ", languageLocationId='" + languageLocationId + '\'' +
                ", contentFile='" + contentFile + '\'' +
                ", circleId='" + circleId + '\'' +
                ", operatorId='" + operatorId + '\'' +
                ", callDisconnectReason=" + callDisconnectReason +
                ", weekId='" + weekId + '\'' +
                '}';
    }
}
