package org.motechproject.nms.kilkari.contract;

import com.fasterxml.jackson.annotation.JsonProperty;

/**Created by vishnu on 27/6/18.
 * update by rakesh on 20/08/20.
 */

public class RchTalukaHealthBlockRecord {

    @JsonProperty("TalukaID")
    private String talukaCode;

    @JsonProperty("HealthBlockID")
    private Long healthBlockCode;

    @JsonProperty("TalukaName")
    private String talukaName;

    @JsonProperty("DistrictID")
    private Long districtCode;


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

    public String getTalukaName() {
        return talukaName;
    }

    public void setTalukaName(String talukaName) {
        this.talukaName = talukaName;
    }

    public Long getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(Long districtCode) {
        this.districtCode = districtCode;
    }
}
