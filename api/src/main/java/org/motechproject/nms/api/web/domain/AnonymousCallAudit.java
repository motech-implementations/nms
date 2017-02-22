package org.motechproject.nms.api.web.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Audit record for Anonymous call details import
 */

@Entity(tableName = "nms_anonymous_call_details_audit")
public class AnonymousCallAudit {

    @Field
    private DateTime dateTimeNow;

    @Field
    private String circle;

    @Field
    private Long callingNumber;

    public AnonymousCallAudit(DateTime dateTimeNow, String circle, Long callingNumber) {
        this.dateTimeNow = dateTimeNow;
        this.circle = circle;
        this.callingNumber = callingNumber;
    }

    public DateTime getDateTimeNow() {
        return dateTimeNow;
    }

    public void setDateTimeNow(DateTime dateTimeNow) {
        this.dateTimeNow = dateTimeNow;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public Long getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(Long callingNumber) {
        this.callingNumber = callingNumber;
    }
}
