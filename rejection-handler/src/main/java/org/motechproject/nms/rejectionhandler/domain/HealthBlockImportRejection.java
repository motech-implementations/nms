package org.motechproject.nms.rejectionhandler.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Unique;

@Entity(tableName = "nms_health_block_rejects")
@Unique(name = "UNIQUE_STATE_HEALTH_BLOCK_CODE", members = { "stateId", "healthBlockCode" })
public class HealthBlockImportRejection {

    @Field
    private Long stateId;

    @Field
    private Long districtCode;

    @Field
    private String talukaCode;

    @Field
    private Long healthBlockCode;

    @Field
    private String healthBlockName;

    @Field
    private DateTime execDate;

    @Field
    private Boolean accepted;

    @Field
    private String rejectionReason;

    @Field
    private Long mddsCode;

    public Long getStateId() {    return stateId;   }

    public void setStateId(Long stateId) {     this.stateId = stateId;    }

    public Long getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(Long districtCode) {
        this.districtCode = districtCode;
    }

    public String getTalukaCode() {
        return talukaCode;
    }

    public void setTalukaCode(String talukaCode) {
        this.talukaCode = talukaCode;
    }

    public Long getHealthBlockCode() {
        return healthBlockCode;
    }

    public void setHealthBlockCode(Long healthBlockCode) {
        this.healthBlockCode = healthBlockCode;
    }

    public String getHealthBlockName() {
        return healthBlockName;
    }

    public void setHealthBlockName(String healthBlockName) {
        this.healthBlockName = healthBlockName;
    }

    public DateTime getExecDate() {
        return execDate;
    }

    public void setExecDate(DateTime execDate) {
        this.execDate = execDate;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Long getMddsCode() {
        return mddsCode;
    }

    public void setMddsCode(Long mddsCode) {
        this.mddsCode = mddsCode;
    }

    public HealthBlockImportRejection() {
    }

    public HealthBlockImportRejection(Long stateId, Long districtCode, String talukaCode, Long healthBlockCode, String healthBlockName, Boolean accepted, String rejectionReason) {
        this.stateId = stateId;
        this.districtCode = districtCode;
        this.talukaCode = talukaCode;
        this.healthBlockCode = healthBlockCode;
        this.healthBlockName = healthBlockName;
        this.accepted = accepted;
        this.rejectionReason = rejectionReason;
    }
}
