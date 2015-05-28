package org.motechproject.nms.kilkari.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.HashSet;
import java.util.Set;

/**
 * Information sent to MOTECH by the IVR system when a Kilkari subscriber calls their inbox
 */
@Entity(tableName = "nms_inbox_call_details")
public class InboxCallDetailRecord {
    @Field
    @Min(value = 1000000000L, message = "callingNumber must be 10 digits")
    @Max(value = 9999999999L, message = "callingNumber must be 10 digits")
    @Column(length = 10)
    private Long callingNumber;

    @Field
    private String operator;

    @Field
    private String circle;

    @Field
    private Long callId;

    @Field
    private DateTime callStartTime;

    @Field
    private DateTime callEndTime;

    @Field
    private Integer callDurationInPulses;

    @Field
    private Integer callStatus;

    @Field
    private Integer callDisconnectReason;

    @Field
    private Set<InboxCallData> content;

    public InboxCallDetailRecord() {
        content = new HashSet<>();
    }

    public InboxCallDetailRecord(Long callingNumber, String operator, String circle, // NO CHECKSTYLE More than 7 parameters
                                 Long callId, DateTime callStartTime, DateTime callEndTime, Integer callDurationInPulses,
                                 Integer callStatus, Integer callDisconnectReason, Set<InboxCallData> content) {
        this();
        this.callingNumber = callingNumber;
        this.operator = operator;
        this.circle = circle;
        this.callId = callId;
        this.callStartTime = callStartTime;
        this.callEndTime = callEndTime;
        this.callDurationInPulses = callDurationInPulses;
        this.callStatus = callStatus;
        this.callDisconnectReason = callDisconnectReason;
        if (content != null) {
            this.content = content;
        }
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

    public DateTime getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(DateTime callStartTime) {
        this.callStartTime = callStartTime;
    }

    public DateTime getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(DateTime callEndTime) {
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

    public Set<InboxCallData> getContent() {
        return content;
    }

    public void setContent(Set<InboxCallData> content) {
        this.content = content;
    }

    @Override //NO CHECKSTYLE Cyclomatic Complexity
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InboxCallDetailRecord that = (InboxCallDetailRecord) o;

        if (!callingNumber.equals(that.callingNumber)) {
            return false;
        }
        if (!operator.equals(that.operator)) {
            return false;
        }
        if (!circle.equals(that.circle)) {
            return false;
        }
        if (!callId.equals(that.callId)) {
            return false;
        }
        if (!callStartTime.equals(that.callStartTime)) {
            return false;
        }
        if (!callEndTime.equals(that.callEndTime)) {
            return false;
        }
        if (!callDurationInPulses.equals(that.callDurationInPulses)) {
            return false;
        }
        if (!callStatus.equals(that.callStatus)) {
            return false;
        }
        if (!callDisconnectReason.equals(that.callDisconnectReason)) {
            return false;
        }
        return !(content != null ? !content.equals(that.content) : that.content != null);

    }

    @Override //NO CHECKSTYLE Cyclomatic Complexity
    public int hashCode() {
        int result = (callingNumber != null ? callingNumber.hashCode() : 0);
        result = 31 * result + (operator != null ? operator.hashCode() : 0);
        result = 31 * result + (circle != null ? circle.hashCode() : 0);
        result = 31 * result + (callId != null ? callId.hashCode() : 0);
        result = 31 * result + (callStartTime != null ? callStartTime.hashCode() : 0);
        result = 31 * result + (callEndTime != null ? callEndTime.hashCode() : 0);
        result = 31 * result + (callDurationInPulses != null ? callDurationInPulses.hashCode() : 0);
        result = 31 * result + (callStatus != null ? callStatus.hashCode() : 0);
        result = 31 * result + (callDisconnectReason != null ? callDisconnectReason.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
