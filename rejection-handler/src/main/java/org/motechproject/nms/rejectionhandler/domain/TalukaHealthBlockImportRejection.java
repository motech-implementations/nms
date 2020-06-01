package org.motechproject.nms.rejectionhandler.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

@Entity(tableName = "nms_taluka_health_block_rejects")
public class TalukaHealthBlockImportRejection {
    @Field
    private Long stateId;

    @Field
    private String talukaCode;

    @Field
    private Long healthBlockCode;

    @Field
    private DateTime execDate;

    @Field
    private Boolean accepted;

    @Field
    private String rejectionReason;

    public Long getStateId() {    return stateId;   }

    public void setStateId(Long stateId) {     this.stateId = stateId;    }

    public String getTalukaCode() {      return talukaCode;    }

    public void setTalukaCode(String talukaCode) {      this.talukaCode = talukaCode;    }

    public Long getHealthBlockCode() {      return healthBlockCode;    }

    public void setHealthBlockCode(Long healthBlockCode) {      this.healthBlockCode = healthBlockCode;    }

    public DateTime getExecDate() {     return execDate;    }

    public void setExecDate(DateTime execDate) {      this.execDate = execDate;    }

    public Boolean getAccepted() {    return accepted;    }

    public void setAccepted(Boolean accepted) {      this.accepted = accepted;    }

    public String getRejectionReason() {      return rejectionReason;    }

    public void setRejectionReason(String rejectionReason) {    this.rejectionReason = rejectionReason;    }
}
