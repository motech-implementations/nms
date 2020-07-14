package org.motechproject.nms.rejectionhandler.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Unique;

@Entity(tableName = "nms_health_sub_facility_rejects")
@Unique(name = "UNIQUE_STATE_HEALTH_SUB_FACILITY_CODE", members = { "stateId", "healthSubFacilityCode" })
public class HealthSubFacilityImportRejection {
    @Field
    private Long stateId;

    @Field
    private Long districtCode;

    @Field
    private String talukaCode;

    @Field
    private Long healthFacilityCode;

    @Field
    private Long healthSubFacilityCode;

    @Field
    private String healthSubFacilityName;

    @Field
    private DateTime execDate;

    @Field
    private Boolean accepted;

    @Field
    private String rejectionReason;

    public Long getStateId() {   return stateId;    }

    public void setStateId(Long stateId) {   this.stateId = stateId;    }

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

    public Long getHealthFacilityCode() {
        return healthFacilityCode;
    }

    public void setHealthFacilityCode(Long healthFacilityCode) {
        this.healthFacilityCode = healthFacilityCode;
    }

    public Long getHealthSubFacilityCode() {
        return healthSubFacilityCode;
    }

    public void setHealthSubFacilityCode(Long healthSubFacilityCode) {
        this.healthSubFacilityCode = healthSubFacilityCode;
    }

    public String getHealthSubFacilityName() {
        return healthSubFacilityName;
    }

    public void setHealthSubFacilityName(String healthSubFacilityName) {
        this.healthSubFacilityName = healthSubFacilityName;
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

    public HealthSubFacilityImportRejection() {
    }

    public HealthSubFacilityImportRejection(Long stateId, Long districtCode, String talukaCode, Long healthFacilityCode, Long healthSubFacilityCode, String healthSubFacilityName, Boolean accepted, String rejectionReason) {
        this.stateId = stateId;
        this.districtCode = districtCode;
        this.talukaCode = talukaCode;
        this.healthFacilityCode = healthFacilityCode;
        this.healthSubFacilityCode = healthSubFacilityCode;
        this.healthSubFacilityName = healthSubFacilityName;
        this.accepted = accepted;
        this.rejectionReason = rejectionReason;
    }
}
