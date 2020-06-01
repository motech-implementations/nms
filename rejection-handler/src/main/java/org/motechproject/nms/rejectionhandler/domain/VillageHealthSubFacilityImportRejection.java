package org.motechproject.nms.rejectionhandler.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

@Entity(tableName = "nms_village_health_sub_facility_rejects")
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
}
