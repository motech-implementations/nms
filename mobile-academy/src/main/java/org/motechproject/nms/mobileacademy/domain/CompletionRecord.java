package org.motechproject.nms.mobileacademy.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Tracks the completion record for a given calling number
 */
@Entity(tableName = "nms_ma_completion_records")
public class CompletionRecord extends MdsEntity {

    @Field
    @Min(value = 1000000000L, message = "callingNumber must be 10 digits")
    @Max(value = 9999999999L, message = "callingNumber must be 10 digits")
    @Column(length = 10)
    @Unique
    private long callingNumber;

    @Field
    private int score;

    @Field
    private int completionCount;

    @Field
    private boolean sentNotification;

    @Field
    private String lastDeliveryStatus;

    @Field
    private String smsReferenceNumber;

    /**
     * Note, this is the number of additional times to try on top of the original send notification request
     */
    @Field
    private int notificationRetryCount;

    public CompletionRecord(long callingNumber, int score) {
        this(callingNumber, score, false, 1);
    }

    public CompletionRecord(long callingNumber, int score, boolean sentNotification, int completionCount) {
        this(callingNumber, score, sentNotification, completionCount, 0);
    }

    public CompletionRecord(long callingNumber, int score, boolean sentNotification, int completionCount, int notificationRetryCount) {
        this.callingNumber = callingNumber;
        this.score = score;
        this.sentNotification = sentNotification;
        this.completionCount = completionCount;
        this.notificationRetryCount = notificationRetryCount;
    }

    public long getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(long callingNumber) {
        this.callingNumber = callingNumber;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isSentNotification() {
        return sentNotification;
    }

    public void setSentNotification(boolean sentNotification) {
        this.sentNotification = sentNotification;
    }

    public int getCompletionCount() {
        return completionCount;
    }

    public void setCompletionCount(int completionCount) {
        this.completionCount = completionCount;
    }

    public String getLastDeliveryStatus() {
        return lastDeliveryStatus;
    }

    public void setLastDeliveryStatus(String lastDeliveryStatus) {
        this.lastDeliveryStatus = lastDeliveryStatus;
    }

    public int getNotificationRetryCount() {
        return notificationRetryCount;
    }

    public void setNotificationRetryCount(int notificationRetryCount) {
        this.notificationRetryCount = notificationRetryCount;
    }

    public String getSmsReferenceNumber() {
        return smsReferenceNumber;
    }

    public void setSmsReferenceNumber(String smsReferenceNumber) {
        this.smsReferenceNumber = smsReferenceNumber;
    }
}
