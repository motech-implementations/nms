package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Track msisdn update of a kilkari User
 */
@Entity(tableName = "nms_subscriber_msisdn_tracker")
public class SubscriberMsisdnTracker {

    @Field
    private Long motherId;

    @Field
    @Min(value = 1000000000L, message = "callingNumber must be 10 digits")
    @Max(value = 9999999999L, message = "callingNumber must be 10 digits")
    @Column(length = 10, allowsNull = "true")
    private Long oldCallingNumber;

    @Field
    @Min(value = 1000000000L, message = "callingNumber must be 10 digits")
    @Max(value = 9999999999L, message = "callingNumber must be 10 digits")
    @Column(length = 10, allowsNull = "false")
    private Long newCallingNumber;

    public SubscriberMsisdnTracker(Long motherId, Long oldCallingNumber, Long newCallingNumber) {
        this.motherId = motherId;
        this.oldCallingNumber = oldCallingNumber;
        this.newCallingNumber = newCallingNumber;
    }

    public Long getMotherId() {
        return motherId;
    }

    public void setMotherId(Long motherId) {
        this.motherId = motherId;
    }

    public Long getOldCallingNumber() {
        return oldCallingNumber;
    }

    public void setOldCallingNumber(Long oldCallingNumber) {
        this.oldCallingNumber = oldCallingNumber;
    }

    public Long getNewCallingNumber() {
        return newCallingNumber;
    }

    public void setNewCallingNumber(Long newCallingNumber) {
        this.newCallingNumber = newCallingNumber;
    }
}
