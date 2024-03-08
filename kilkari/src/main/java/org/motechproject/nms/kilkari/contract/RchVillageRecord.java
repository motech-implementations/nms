package org.motechproject.nms.kilkari.contract;

import com.fasterxml.jackson.annotation.JsonProperty;

/**Created by vishnu on 27/6/18.
 * update by rakesh on 20/08/20.
 */

public class RchVillageRecord {

    @JsonProperty("DistrictCode")
    private Long districtCode;

    @JsonProperty("SubDistrict_Code")
    private String talukaCode;

    @JsonProperty("villegeCode")
    private Long villageCode;

    @JsonProperty("VillegeName")
    private String villageName;

    @JsonProperty("MDDS_Code")
    private Long MDDS_Code;

    @JsonProperty("StateCode")
    private Long StateCode;

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

    public Long getMDDS_Code() {
        return MDDS_Code;
    }

    public void setMDDS_Code(Long MDDS_Code) {
        this.MDDS_Code = MDDS_Code;
    }

    public Long getStateCode() {
        return StateCode;
    }

    public void setStateCode(Long stateCode) {
        StateCode = stateCode;
    }
}
