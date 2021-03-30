package org.motechproject.nms.rejectionhandler.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Unique;

@Entity(tableName = "nms_village_rejects")
@Unique(name = "UNIQUE_STATE_VILLAGE_CODE_SVID", members = { "stateId", "villageCode", "svid" })
public class VillageImportRejection {
    @Field
    private Long stateId;

    @Field
    private Long districtCode;

    @Field
    private String talukaCode;

    @Field
    private Long villageCode;

    @Field
    private String villageName;

    @Field
    private DateTime execDate;

    @Field
    private Boolean accepted;

    @Field
    private String rejectionReason;

    @Field
    private long svid;

    @Field
    private Long mddsCode;

    @Field
    private Long state_code;

    public Long getStateId() {    return stateId;    }

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

    public Long getVillageCode() {
        return villageCode;
    }

    public void setVillageCode(Long villageCode) {
        this.villageCode = villageCode;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
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

    public VillageImportRejection() {
    }

    public long getSvid() {
        return svid;
    }

    public void setSvid(long svid) {
        this.svid = svid;
    }

    public Long getState_code() {
        return state_code;
    }

    public void setState_code(Long state_code) {
        this.state_code = state_code;
    }

    public VillageImportRejection(Long stateId, Long districtCode, String talukaCode, Long villageCode, String villageName, Boolean accepted, String rejectionReason) {
        this.stateId = stateId;
        this.districtCode = districtCode;
        this.talukaCode = talukaCode;
        this.villageCode = villageCode;
        this.villageName = villageName;
        this.accepted = accepted;
        this.rejectionReason = rejectionReason;
    }
}
