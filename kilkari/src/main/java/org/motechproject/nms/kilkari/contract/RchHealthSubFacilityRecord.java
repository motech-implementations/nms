package org.motechproject.nms.kilkari.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**Created by vishnu on 27/6/18.
 * update by rakesh on 20/08/20.
 */

public class RchHealthSubFacilityRecord {

    @JsonProperty("DistrictID")
    private Long districtCode;

    @JsonProperty("TalukaID")
    private String talukaCode;

    @JsonProperty("HealtfacilityID")
    private Long healthFacilityCode;

    @JsonProperty("HealtSubfacilityID")
    private Long healthSubFacilityCode;

    @JsonProperty("HealthSubCentreName")
    private String healthSubFacilityName;


    public Long getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(Long districtCode) {
        this.districtCode = districtCode;
    }

    public String getHealthSubFacilityName() {
        return healthSubFacilityName;
    }

    public void setHealthSubFacilityName(String healthSubFacilityName) {
        this.healthSubFacilityName = healthSubFacilityName;
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



}
