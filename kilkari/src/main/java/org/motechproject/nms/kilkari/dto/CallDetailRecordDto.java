package org.motechproject.nms.kilkari.dto;

import org.joda.time.DateTime;
import org.motechproject.nms.props.domain.CallDisconnectReason;
import org.motechproject.nms.props.domain.RequestId;
import org.motechproject.nms.props.domain.StatusCode;

import java.io.Serializable;

public class CallDetailRecordDto implements Serializable {
    private static final long serialVersionUID = 4499560266083597513L;
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

    @Override //NO CHECKSTYLE Cyclomatic Complexity
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CallDetailRecordDto that = (CallDetailRecordDto) o;

        if (!requestId.equals(that.requestId)) {
            return false;
        }
        if (!msisdn.equals(that.msisdn)) {
            return false;
        }
        if (callAnswerTime != null ? !callAnswerTime.equals(that.callAnswerTime) : that.callAnswerTime != null) {
            return false;
        }
        if (msgPlayDuration != null ? !msgPlayDuration.equals(that.msgPlayDuration) : that.msgPlayDuration != null) {
            return false;
        }
        if (statusCode != that.statusCode) {
            return false;
        }
        if (languageLocationId != null ? !languageLocationId.equals(that.languageLocationId) : that.languageLocationId != null) {
            return false;
        }
        if (contentFile != null ? !contentFile.equals(that.contentFile) : that.contentFile != null) {
            return false;
        }
        if (circleId != null ? !circleId.equals(that.circleId) : that.circleId != null) {
            return false;
        }
        if (operatorId != null ? !operatorId.equals(that.operatorId) : that.operatorId != null) {
            return false;
        }
        if (callDisconnectReason != that.callDisconnectReason) {
            return false;
        }
        return !(weekId != null ? !weekId.equals(that.weekId) : that.weekId != null);

    }

    @Override
    public int hashCode() {
        int result = requestId.hashCode();
        result = 31 * result + msisdn.hashCode();
        result = 31 * result + (callAnswerTime != null ? callAnswerTime.hashCode() : 0);
        result = 31 * result + (msgPlayDuration != null ? msgPlayDuration.hashCode() : 0);
        result = 31 * result + (statusCode != null ? statusCode.hashCode() : 0);
        result = 31 * result + (languageLocationId != null ? languageLocationId.hashCode() : 0);
        result = 31 * result + (contentFile != null ? contentFile.hashCode() : 0);
        result = 31 * result + (circleId != null ? circleId.hashCode() : 0);
        result = 31 * result + (operatorId != null ? operatorId.hashCode() : 0);
        result = 31 * result + (callDisconnectReason != null ? callDisconnectReason.hashCode() : 0);
        result = 31 * result + (weekId != null ? weekId.hashCode() : 0);
        return result;
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
