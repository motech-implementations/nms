package org.motechproject.nms.rejectionhandler.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Unique;

@Entity(tableName = "nms_district_rejects")
@Unique(name = "UNIQUE_STATE_DISTRICT_CODE", members = { "stateId", "districtCode" })
public class DistrictImportRejection {
    @Field
    private Long stateId;

    @Field
    private Long districtCode;

    @Field
    private String districtName;

    @Field
    private DateTime execDate;

    @Field
    private Boolean accepted;

    @Field
    private String rejectionReason;

    public Long getStateId() {     return stateId;    }

    public void setStateId(Long stateId) {      this.stateId = stateId;    }

    public Long getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(Long districtCode) {
        this.districtCode = districtCode;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
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

    public DistrictImportRejection() {
    }

    public DistrictImportRejection(Long stateId, Long districtCode, String districtName, Boolean accepted, String rejectionReason) {
        this.stateId = stateId;
        this.districtCode = districtCode;
        this.districtName = districtName;
        this.accepted = accepted;
        this.rejectionReason = rejectionReason;
    }
}
