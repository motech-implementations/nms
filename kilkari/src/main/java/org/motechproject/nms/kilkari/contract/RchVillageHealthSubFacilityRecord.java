package org.motechproject.nms.kilkari.contract;

import com.fasterxml.jackson.annotation.JsonProperty;


/**Created by vishnu on 27/6/18.
 * update by rakesh on 20/08/20.
 */

public class RchVillageHealthSubFacilityRecord {

    @JsonProperty("VillageID")
    private Long villageCode;

    @JsonProperty("HealtSubfacilityID")
    private Long healthSubFacilityCode;

    @JsonProperty("DistrictID")
    private Long districtCode;

    @JsonProperty("TalukaID")
    private String talukaCode;

    @JsonProperty("HealtfacilityID")
    private Long healthFacilityCode;

    @JsonProperty("VillageName")
    private String villageName;

    public Long getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(Long districtCode) {
        this.districtCode = districtCode;
    }

    public Long getVillageCode() {
        return villageCode;
    }

    public void setVillageCode(Long villageCode) {
        this.villageCode = villageCode;
    }

    public Long getHealthSubFacilityCode() {
        return healthSubFacilityCode;
    }

    public void setHealthSubFacilityCode(Long healthSubFacilityCode) {
        this.healthSubFacilityCode = healthSubFacilityCode;
    }

    public String getTalukaCode() {
        return talukaCode;
    }

    public void setTalukaCode(String talukaCode) {
        this.talukaCode = talukaCode;
    }

    public Long getHealthFacilityCode() {
        return healthFacilityCode;
    }

    public void setHealthFacilityCode(Long healthFacilityCode) {
        this.healthFacilityCode = healthFacilityCode;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }
}
