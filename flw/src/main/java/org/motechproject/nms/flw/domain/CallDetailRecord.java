package org.motechproject.nms.flw.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.Persistent;
import java.util.Collections;
import java.util.List;

@Entity
public class CallDetailRecord {

    public static final int FIELD_SIZE_10 = 10;
    public static final int FIELD_SIZE_15 = 15;
    public static final int FIELD_SIZE_255 = 255;

    public CallDetailRecord() {
    }

    @Field
    private FrontLineWorker frontLineWorker;

    @Field
    private Service service;

    @Field
    @Column(length = FIELD_SIZE_10)
    private long callingNumber;

    @Field
    @Column(length = FIELD_SIZE_15)
    private long callId;

    @Field
    @Column(length = FIELD_SIZE_255)
    private String operator;

    @Field
    @Column(length = FIELD_SIZE_255)
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
    private CallStatus callStatus;

    @Field
    private CallDisconnectReason callDisconnectReason;

    @Field
    @Persistent(mappedBy = "callDetailRecord")
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

    public CallStatus getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(CallStatus callStatus) {
        this.callStatus = callStatus;
    }

    public void setCallStatus(int i) {

        this.callStatus = CallStatus.fromInt(i);
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
