package org.motechproject.nms.outbounddialer.repository;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;


/**
 * Models the Kilkari beneficiaries, ie: the IVR call recipients
 */
@Entity
public class Beneficiary {
    @Field
    private Long msisdn;
    @Field
    private DayOfTheWeek defaultCallDay;
    @Field
    private DayOfTheWeek nextCallDay;
    @Field
    private String weekId;
    @Field
    private BeneficiaryStatus status;
    @Field
    private String languageLocationCode;
    @Field
    private DateTime expectedDeliveryDate;
    @Field
    private CallStage stage;

    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
        this.msisdn = msisdn;
    }

    public DayOfTheWeek getDefaultCallDay() {
        return defaultCallDay;
    }

    public void setDefaultCallDay(DayOfTheWeek defaultCallDay) {
        this.defaultCallDay = defaultCallDay;
    }

    public DayOfTheWeek getNextCallDay() {
        return nextCallDay;
    }

    public void setNextCallDay(DayOfTheWeek nextCallDay) {
        this.nextCallDay = nextCallDay;
    }

    public String getWeekId() {
        return weekId;
    }

    public void setWeekId(String weekId) {
        this.weekId = weekId;
    }

    public BeneficiaryStatus getStatus() {
        return status;
    }

    public void setStatus(BeneficiaryStatus status) {
        this.status = status;
    }

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    public DateTime getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(DateTime expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public CallStage getStage() {
        return stage;
    }

    public void setStage(CallStage stage) {
        this.stage = stage;
    }
}
