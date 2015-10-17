package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;
import org.motechproject.nms.props.domain.DayOfTheWeek;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Entity(tableName = "nms_kk_retry_records")
public class CallRetry extends MdsEntity {
    @Field
    @Unique
    private String subscriptionId;

    @Field
    @Min(value = 1000000000L, message = "msisdn must be 10 digits")
    @Max(value = 9999999999L, message = "msisdn must be 10 digits")
    @Column(length = 10)
    private Long msisdn;

    @Field
    private DayOfTheWeek dayOfTheWeek;

    @Field
    private CallStage callStage;

    @Field
    private String contentFileName;

    @Field
    private String weekId;

    @Field
    private String languageLocationCode;

    @Field
    private String circle;

    @Field
    private SubscriptionOrigin subscriptionOrigin;

    /**
     * the time at which this callRetry was created (extracted from a RequestId)
     * allows us to check for duplicate callRetry and end up with better CDR processing logic when dealing with
     * bad data from IMI
     */
    @Field
    private String timestamp;

    public CallRetry() { }

    public CallRetry(String subscriptionId, Long msisdn, //NO CHECKSTYLE More than 7 parameters
                     DayOfTheWeek dayOfTheWeek, CallStage callStage, String contentFileName, String weekId,
                     String languageLocationCode, String circle, SubscriptionOrigin subscriptionOrigin,
                     String timestamp) {
        this.subscriptionId = subscriptionId;
        this.msisdn = msisdn;
        this.dayOfTheWeek = dayOfTheWeek;
        this.callStage = callStage;
        this.contentFileName = contentFileName;
        this.weekId = weekId;
        this.languageLocationCode = languageLocationCode;
        this.circle = circle;
        this.subscriptionOrigin = subscriptionOrigin;
        this.timestamp = timestamp;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
        this.msisdn = msisdn;
    }

    public DayOfTheWeek getDayOfTheWeek() {
        return dayOfTheWeek;
    }

    public void setDayOfTheWeek(DayOfTheWeek dayOfTheWeek) {
        this.dayOfTheWeek = dayOfTheWeek;
    }

    public CallStage getCallStage() {
        return callStage;
    }

    public void setCallStage(CallStage callStage) {
        this.callStage = callStage;
    }

    public String getContentFileName() {
        return contentFileName;
    }

    public void setContentFileName(String contentFileName) {
        this.contentFileName = contentFileName;
    }

    public String getWeekId() {
        return weekId;
    }

    public void setWeekId(String weekId) {
        this.weekId = weekId;
    }

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public SubscriptionOrigin getSubscriptionOrigin() {
        return subscriptionOrigin;
    }

    public void setSubscriptionOrigin(SubscriptionOrigin subscriptionOrigin) {
        this.subscriptionOrigin = subscriptionOrigin;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "CallRetry{" +
                "subscriptionId='" + subscriptionId + '\'' +
                ", msisdn=" + msisdn +
                ", dayOfTheWeek=" + dayOfTheWeek +
                ", callStage=" + callStage +
                ", contentFileName='" + contentFileName + '\'' +
                ", weekId='" + weekId + '\'' +
                ", languageLocationCode='" + languageLocationCode + '\'' +
                ", circle='" + circle + '\'' +
                ", subscriptionOrigin='" + subscriptionOrigin + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
