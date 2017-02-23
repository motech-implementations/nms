package org.motechproject.nms.api.web.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Audit record for Inactive Job Flw call details
 */

@Entity(tableName = "nms_inactive_job_call_audit")
public class InactiveJobCallAudit {

    @Field
    private DateTime dateTimeNow;

    @Field
    private String flwId;

    @Field
    private String mctsFlwId;

    @Field
    private Long callingNumber;

    public InactiveJobCallAudit(DateTime dateTimeNow, String flwId, String mctsFlwId, Long callingNumber) {
        this.dateTimeNow = dateTimeNow;
        this.flwId = flwId;
        this.mctsFlwId = mctsFlwId;
        this.callingNumber = callingNumber;
    }

    public DateTime getDateTimeNow() {
        return dateTimeNow;
    }

    public void setDateTimeNow(DateTime dateTimeNow) {
        this.dateTimeNow = dateTimeNow;
    }

    public String getFlwId() {
        return flwId;
    }

    public void setFlwId(String flwId) {
        this.flwId = flwId;
    }

    public String getMctsFlwId() {
        return mctsFlwId;
    }

    public void setMctsFlwId(String mctsFlwId) {
        this.mctsFlwId = mctsFlwId;
    }

    public Long getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(Long callingNumber) {
        this.callingNumber = callingNumber;
    }
}
