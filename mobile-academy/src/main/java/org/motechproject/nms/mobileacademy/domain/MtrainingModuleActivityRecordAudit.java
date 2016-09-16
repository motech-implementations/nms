package org.motechproject.nms.mobileacademy.domain;



import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Audit table to track changes in MTRAINING_MODULE_ACTIVITYRECORD
 */
@Entity(tableName = "MTRAINING_MODULE_ACTIVITYRECORD_Audit")
public class MtrainingModuleActivityRecordAudit extends MdsEntity {

    @Field
    @Min(value = 1000000000L, message = "callingNumber must be 10 digits")
    @Max(value = 9999999999L, message = "callingNumber must be 10 digits")
    @Column(length = 10)
    @Unique
    private long existingMsisdn;

    @Field
    @Min(value = 1000000000L, message = "callingNumber must be 10 digits")
    @Max(value = 9999999999L, message = "callingNumber must be 10 digits")
    @Column(length = 10)
    @Unique
    private long newMsisdn;

    public MtrainingModuleActivityRecordAudit(long existingMsisdn, long newMsisdn) {
        this.existingMsisdn = existingMsisdn;
        this.newMsisdn = newMsisdn;
    }

    public long getExistingMsisdn() {
        return existingMsisdn;
    }

    public void setExistingMsisdn(long existingMsisdn) {
        this.existingMsisdn = existingMsisdn;
    }

    public long getNewMsisdn() {
        return newMsisdn;
    }

    public void setNewMsisdn(long newMsisdn) {
        this.newMsisdn = newMsisdn;
    }
}
