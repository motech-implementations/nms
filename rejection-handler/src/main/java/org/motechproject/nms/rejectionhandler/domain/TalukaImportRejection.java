package org.motechproject.nms.rejectionhandler.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

@Entity(tableName = "nms_taluka_rejects")
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
}
