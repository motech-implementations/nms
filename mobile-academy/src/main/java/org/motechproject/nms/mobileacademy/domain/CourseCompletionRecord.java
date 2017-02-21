package org.motechproject.nms.mobileacademy.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

/**
 * Tracks the completion record for a given calling number
 */
@Entity(tableName = "nms_ma_course_completion_records")
public class CourseCompletionRecord extends MdsEntity {

    @Field
    private Long flwId;

    @Field
    private int score;

    @Field
    private String chapterWiseScores;

    @Field
    private boolean passed;

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


    public CourseCompletionRecord(long callingNumber, int score, String chapterWiseScores) {
        this(callingNumber, score, chapterWiseScores, false);
    }

    public CourseCompletionRecord(long callingNumber, int score, String chapterWiseScores, boolean sentNotification) {
        this(callingNumber, score, chapterWiseScores, false, sentNotification, 0);

    }

    public CourseCompletionRecord(Long flwId, int score, String chapterWiseScores, boolean passed, boolean sentNotification, int notificationRetryCount) {
        this.flwId = flwId;
        this.score = score;
        this.chapterWiseScores = chapterWiseScores;
        this.passed = passed;
        this.sentNotification = sentNotification;
        this.notificationRetryCount = notificationRetryCount;
    }

    public Long getFlwId() {
        return flwId;
    }

    public void setFlwId(Long flwId) {
        this.flwId = flwId;
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

    public String getChapterWiseScores() {
        return chapterWiseScores;
    }

    public void setChapterWiseScores(String chapterWiseScores) {
        this.chapterWiseScores = chapterWiseScores;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }
}
