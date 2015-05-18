package org.motechproject.nms.flw.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.props.domain.CallDisconnectReason;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.Service;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.Persistent;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.List;

@Entity(tableName = "nms_call_detail_records")
public class CallDetailRecord {

    public CallDetailRecord() {
    }

    @Field
    private FrontLineWorker frontLineWorker;

    @Field
    private Service service;

    @Field
    @Min(value = 1000000000L, message = "callingNumber must be 10 digits")
    @Max(value = 9999999999L, message = "callingNumber must be 10 digits")
    @Column(length = 10)
    private long callingNumber;

    @Field
    @Column(length = 15)
    private long callId;

    @Field
    @Column(length = 255)
    private String operator;

    @Field
    @Column(length = 255)
    private String circle;

    @Field
    private DateTime callStartTime;

    @Field
    private DateTime callEndTime;

    @Field
    private int callDurationInPulses;

    @Field
    private int endOfUsagePromptCounter;

    @Field
    private Boolean welcomePrompt;

    @Field
    private FinalCallStatus finalCallStatus;

    @Field
    private CallDisconnectReason callDisconnectReason;

    @Field
    @Persistent(mappedBy = "callDetailRecord")
    @Cascade(delete = true)
    @Order(extensions = @Extension(vendorName = "datanucleus", key = "list-ordering", value = "id ASC"))
    private List<CallContent> content;

    public FrontLineWorker getFrontLineWorker() {
        return frontLineWorker;
    }

    public void setFrontLineWorker(FrontLineWorker frontLineWorker) {
        this.frontLineWorker = frontLineWorker;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public long getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(long callingNumber) {
        this.callingNumber = callingNumber;
    }

    public long getCallId() {
        return callId;
    }

    public void setCallId(long callId) {
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

    public Boolean getWelcomePrompt() {
        return welcomePrompt;
    }

    public void setWelcomePrompt(Boolean welcomePrompt) {
        this.welcomePrompt = welcomePrompt;
    }

    public FinalCallStatus getFinalCallStatus() {
        return finalCallStatus;
    }

    public void setFinalCallStatus(FinalCallStatus finalCallStatus) {
        this.finalCallStatus = finalCallStatus;
    }

    public void setCallStatus(int i) {

        this.finalCallStatus = FinalCallStatus.fromInt(i);
    }

    public CallDisconnectReason getCallDisconnectReason() {
        return callDisconnectReason;
    }

    public void setCallDisconnectReason(CallDisconnectReason callDisconnectReason) {
        this.callDisconnectReason = callDisconnectReason;
    }

    public void setCallDisconnectReason(int i) {
        this.callDisconnectReason = CallDisconnectReason.fromInt(i);
    }

    public List<CallContent> getContent() {
        if (content == null) {
            return Collections.emptyList();
        }

        return content;
    }

    public void setContent(List<CallContent> content) {
        this.content = content;
    }
}
