package org.motechproject.nms.kilkari.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Record of Weekly Calls Not Answered Deactivated Numbers
 */
@Entity(tableName = "nms_weekly_calls_not_answered_msisdn")
public class WeeklyCallsNotAnsweredMsisdnRecord {

    @Field
    @Unique
    @Min(value = 1000000000L, message = "callingNumber must be 10 digits")
    @Max(value = 9999999999L, message = "callingNumber must be 10 digits")
    @Column(length = 10, allowsNull = "false")
    private Long callingNumber;

    @Field
    private DateTime deativationDate;

    public WeeklyCallsNotAnsweredMsisdnRecord() {
    }

    public WeeklyCallsNotAnsweredMsisdnRecord(Long callingNumber, DateTime deativationDate) {
        this.callingNumber = callingNumber;
        this.deativationDate = deativationDate;
    }

    public Long getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(Long callingNumber) {
        this.callingNumber = callingNumber;
    }

    public DateTime getDeativationDate() {
        return deativationDate;
    }

    public void setDeativationDate(DateTime deativationDate) {
        this.deativationDate = deativationDate;
    }
}