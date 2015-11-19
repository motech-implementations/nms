package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

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

    @Field
    private String targetFiletimestamp;

    @Field
    private Integer invalidNumberCount;

    public CallRetry() { }

    public CallRetry(String subscriptionId, Long msisdn, //NO CHECKSTYLE More than 7 parameters
                     CallStage callStage, String contentFileName, String weekId, String languageLocationCode,
                     String circle, SubscriptionOrigin subscriptionOrigin, String targetFiletimestamp,
                     Integer invalidNumberCount) {
        this.subscriptionId = subscriptionId;
        this.msisdn = msisdn;
        this.callStage = callStage;
        this.contentFileName = contentFileName;
        this.weekId = weekId;
        this.languageLocationCode = languageLocationCode;
        this.circle = circle;
        this.subscriptionOrigin = subscriptionOrigin;
        this.targetFiletimestamp = targetFiletimestamp;
        this.invalidNumberCount = invalidNumberCount;
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

    public String getTargetFiletimestamp() {
        return targetFiletimestamp;
    }

    public void setTargetFiletimestamp(String targetFiletimestamp) {
        this.targetFiletimestamp = targetFiletimestamp;
    }

    public Integer getInvalidNumberCount() {
        return invalidNumberCount;
    }

    public void setInvalidNumberCount(Integer invalidNumberCount) {
        this.invalidNumberCount = invalidNumberCount;
    }
}
