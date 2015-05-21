package org.motechproject.nms.mobileacademy.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Tracks the completion record for a given calling number
 */
@Entity(tableName = "nms_ma_completion_records")
public class CompletionRecord {

    @Field
    @Min(value = 1000000000L, message = "callingNumber must be 10 digits")
    @Max(value = 9999999999L, message = "callingNumber must be 10 digits")
    @Column(length = 10)
    @Unique
    private long callingNumber;

    @Field
    private int score;

    @Field
    private boolean sentNotification;

    @Field
    private int completionCount;

    public CompletionRecord(long callingNumber, int score) {
        this.callingNumber = callingNumber;
        this.score = score;
        sentNotification = false;
        completionCount = 1;
    }

    public CompletionRecord(long callingNumber, int score, boolean sentNotification, int completionCount) {
        this.callingNumber = callingNumber;
        this.score = score;
        this.sentNotification = sentNotification;
        this.completionCount = completionCount;
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
}
