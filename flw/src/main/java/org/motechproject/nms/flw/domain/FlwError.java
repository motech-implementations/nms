package org.motechproject.nms.flw.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * Audit log object for tracking failed FLW updates
 */
@Entity(tableName = "nms_flw_errors")
public class FlwError {

    @Field
    private String mctsId;

    @Field
    private Long stateId;

    @Field
    private Long districtId;

    @Field
    private String error;

    public FlwError(String mctsId, Long stateId, Long districtId, String error) {
        this.mctsId = mctsId;
        this.stateId = stateId;
        this.districtId = districtId;
        this.error = error;
    }

    public String getMctsId() {
        return mctsId;
    }

    public void setMctsId(String mctsId) {
        this.mctsId = mctsId;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
