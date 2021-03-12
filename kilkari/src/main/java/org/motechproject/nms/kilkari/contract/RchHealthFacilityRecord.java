package org.motechproject.nms.kilkari.contract;

import com.fasterxml.jackson.annotation.JsonProperty;


/**Created by vishnu on 27/6/18.
 * update by rakesh on 20/08/20.
 */

public class RchHealthFacilityRecord {

    @JsonProperty("DistrictID")
    private Long districtCode;

    @JsonProperty("TalukaID")
    private String talukaCode;

    @JsonProperty("HealthBlockID")
    private Long healthBlockCode;

    @JsonProperty("HealtfacilityID")
    private Long healthFacilityCode;

    @JsonProperty("HealtfacilityName")
    private String healthFacilityName;

    @JsonProperty("HealtfacilityType")
    private Long healthfacilityType;


    public Long getHealthBlockCode() {
        return healthBlockCode;
    }

    public void setHealthBlockCode(Long healthBlockCode) {
        this.healthBlockCode = healthBlockCode;
    }

    public Long getHealthFacilityCode() {
        return healthFacilityCode;
    }

    public void setHealthFacilityCode(Long healthFacilityCode) {
        this.healthFacilityCode = healthFacilityCode;
    }

    public String getHealthFacilityName() {
        return healthFacilityName;
    }

    public void setHealthFacilityName(String healthFacilityName) {
        this.healthFacilityName = healthFacilityName;
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

    public Long getHealthfacilityType() {
        return healthfacilityType;
    }

    public void setHealthfacilityType(Long healthfacilityType) {
        this.healthfacilityType = healthfacilityType;
    }
}
