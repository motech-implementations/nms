package org.motechproject.nms.rejectionhandler.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Unique;

@Entity(tableName = "nms_taluka_rejects")
@Unique(name = "UNIQUE_STATE_TALUKA_CODE", members = { "stateId", "talukaCode" })
public class TalukaImportRejection {
    @Field
    private Long stateId;

    @Field
    private Long districtCode;

    @Field
    private String talukaCode;

    @Field
    private String talukaName;

    @Field
    private DateTime execDate;

    @Field
    private Boolean accepted;

    @Field
    private String rejectionReason;

    @Field
    private Long stateCode;

    @Field
    private Long mddsCode;

    public Long getStateId() {    return stateId;   }

    public void setStateId(Long stateId) {     this.stateId = stateId;    }

    public Long getDistrictCode() {      return districtCode;    }

    public void setDistrictCode(Long districtCode) {     this.districtCode = districtCode;    }

    public String getTalukaCode() {      return talukaCode;    }

    public void setTalukaCode(String talukaCode) {      this.talukaCode = talukaCode;    }

    public String getTalukaName() {     return talukaName;    }

    public void setTalukaName(String talukaName) {    this.talukaName = talukaName;    }

    public DateTime getExecDate() {     return execDate;    }

    public void setExecDate(DateTime execDate) {     this.execDate = execDate;    }

    public Boolean getAccepted() {      return accepted;    }

    public void setAccepted(Boolean accepted) {     this.accepted = accepted;    }

    public String getRejectionReason() {     return rejectionReason;    }

    public void setRejectionReason(String rejectionReason) {     this.rejectionReason = rejectionReason;    }

    public Long getStateCode() {
        return stateCode;
    }

    public void setStateCode(Long stateCode) {
        this.stateCode = stateCode;
    }

    public Long getMddsCode() {
        return mddsCode;
    }

    public void setMddsCode(Long mddsCode) {
        this.mddsCode = mddsCode;
    }

    public TalukaImportRejection() {
    }

    public TalukaImportRejection(Long stateId, Long districtCode, String talukaCode, String talukaName, Boolean accepted, String rejectionReason) {
        this.stateId = stateId;
        this.districtCode = districtCode;
        this.talukaCode = talukaCode;
        this.talukaName = talukaName;
        this.accepted = accepted;
        this.rejectionReason = rejectionReason;
    }
}
