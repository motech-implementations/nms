package org.motechproject.nms.flw.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Audit record for update of anonymous flw to active
 */
@Entity(tableName = "nms_flw_status_update_audit")
public class FlwStatusUpdateAudit {

    @Field
    private DateTime importDate;

    @Field
    private String  flwId;

    @Field
    private String mctsFlwId;

    @Field
    private Long contactNumber;

    @Field
    private UpdateStatusType updateStatusType;


    public FlwStatusUpdateAudit(DateTime importDate, String flwId, String mctsFlwId, Long contactNumber, UpdateStatusType updateStatusType) {
        this.importDate = importDate;
        this.flwId = flwId;
        this.mctsFlwId = mctsFlwId;
        this.contactNumber = contactNumber;
        this.updateStatusType = updateStatusType;
    }

    public UpdateStatusType getUpdateStatusType() {
        return updateStatusType;
    }

    public void setUpdateStatusType(UpdateStatusType updateStatusType) {
        this.updateStatusType = updateStatusType;
    }

    public DateTime getImportDate() {
        return importDate;
    }

    public void setImportDate(DateTime importDate) {
        this.importDate = importDate;
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

    public Long getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(Long contactNumber) {
        this.contactNumber = contactNumber;
    }
}
