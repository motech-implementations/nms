package org.motechproject.nms.outbounddialer.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

@Entity(tableName = "nms_call_retry")
public class CallRetry {
    @Field
    private String subscriptionId;

    @Field
    private Long msisdn;

    @Field
    private DayOfTheWeek dayOfTheWeek;

    @Field
    private CallStage callStage;

    @Field
    private String languageLocationCode;

    //todo: more data...

    public CallRetry() { }

    public CallRetry(String subscriptionId, Long msisdn, DayOfTheWeek dayOfTheWeek, CallStage callStage, String languageLocationCode) {
        this.subscriptionId = subscriptionId;
        this.msisdn = msisdn;
        this.dayOfTheWeek = dayOfTheWeek;
        this.callStage = callStage;
        this.languageLocationCode = languageLocationCode;
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

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }
}
