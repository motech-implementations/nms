package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Record of Weekly Calls Not Answered Deactivated Numbers
 */
@Entity(tableName = "nms_blocked_msisdn")
public class BlockedMsisdnRecord {

    @Field
    @Unique
    @Min(value = 1000000000L, message = "callingNumber must be 10 digits")
    @Max(value = 9999999999L, message = "callingNumber must be 10 digits")
    @Column(length = 10, allowsNull = "false")
    private Long callingNumber;

    @Field
    @NotNull
    private DeactivationReason deactivationReason;

    public BlockedMsisdnRecord() {
    }

    public BlockedMsisdnRecord(Long callingNumber, DeactivationReason deactivationReason) {
        this.callingNumber = callingNumber;
        this.deactivationReason = deactivationReason;
    }

    public Long getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(Long callingNumber) {
        this.callingNumber = callingNumber;
    }

    public DeactivationReason getDeactivationReason() {
        return deactivationReason;
    }

    public void setDeactivationReason(DeactivationReason deactivationReason) {
        this.deactivationReason = deactivationReason;
    }
}