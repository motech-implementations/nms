package org.motechproject.nms.rejectionhandler.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Unique;

@Entity(tableName = "nms_village_health_sub_facility_rejects")
@Unique(name = "UNIQUE_VILLAGE_HEALTH_SUB_FACILITY", members = { "villageCode", "healthSubFacilityCode" })
public class VillageHealthSubFacilityImportRejection {
    @Field
    private Long stateId;

    @Field
    private Long districtCode;

    @Field
    private Long villageCode;

    @Field
    private Long healthSubFacilityCode;

    @Field
    private DateTime execDate;

    @Field
    private Boolean accepted;

    @Field
    private String rejectionReason;

    @Field
    private String talukaCode;

    @Field
    private Long healthFacilityCode;

    @Field
    private String villageName;

    public Long getStateId() {    return stateId;    }

    public void setStateId(Long stateId) {     this.stateId = stateId;    }

    public Long getDistrictCode() {    return districtCode;   }

    public void setDistrictCode(Long districtCode) {    this.districtCode = districtCode;    }

    public Long getVillageCode() {    return villageCode;    }

    public void setVillageCode(Long villageCode) {     this.villageCode = villageCode;    }

    public Long getHealthSubFacilityCode() {     return healthSubFacilityCode;   }

    public void setHealthSubFacilityCode(Long healthSubFacilityCode) {     this.healthSubFacilityCode = healthSubFacilityCode;    }

    public DateTime getExecDate() {     return execDate;    }

    public void setExecDate(DateTime execDate) {    this.execDate = execDate;   }

    public Boolean getAccepted() {     return accepted;    }

    public void setAccepted(Boolean accepted) {    this.accepted = accepted;   }

    public String getRejectionReason() {     return rejectionReason;   }

    public void setRejectionReason(String rejectionReason) {   this.rejectionReason = rejectionReason;    }

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

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public VillageHealthSubFacilityImportRejection() {
    }

    public VillageHealthSubFacilityImportRejection(Long stateId, Long districtCode, Long villageCode, Long healthSubFacilityCode, Boolean accepted, String rejectionReason) {
        this.stateId = stateId;
        this.districtCode = districtCode;
        this.villageCode = villageCode;
        this.healthSubFacilityCode = healthSubFacilityCode;
        this.accepted = accepted;
        this.rejectionReason = rejectionReason;
    }
}
