package org.motechproject.nms.kilkari.contract;

import com.fasterxml.jackson.annotation.JsonProperty;

/**Created by vishnu on 27/6/18.
 * update by rakesh on 20/08/20.
 */

public class RchHealthBlockRecord {

    @JsonProperty("DistrictID")
    private Long districtCode;

    @JsonProperty("TalukaID")
    private String talukaCode;

    @JsonProperty("HealthBlockID")
    private Long healthBlockCode;

    @JsonProperty("HealthBlockName")
    private String healthBlockName;

    @JsonProperty("MDDSCode")
    private Long MDDS_Code;


    public String getHealthBlockName() {
        return healthBlockName;
    }

    public void setHealthBlockName(String healthBlockName) {
        this.healthBlockName = healthBlockName;
    }

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

    public Long getHealthBlockCode() {
        return healthBlockCode;
    }

    public void setHealthBlockCode(Long healthBlockCode) {
        this.healthBlockCode = healthBlockCode;
    }

    public Long getMDDS_Code() {
        return MDDS_Code;
    }

    public void setMDDS_Code(Long MDDS_Code) {
        this.MDDS_Code = MDDS_Code;
    }
}
