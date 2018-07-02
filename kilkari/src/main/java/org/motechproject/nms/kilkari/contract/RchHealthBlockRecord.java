package org.motechproject.nms.kilkari.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by vishnu on 29/6/18.
 */

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class RchHealthBlockRecord {

    private Long stateCode;
    private Long districtCode;
    private Long talukaCode;
    private Long healthBlockCode;
    private String healthBlockName;

    public String getHealthBlockName() {
        return healthBlockName;
    }

    @XmlElement(name = "HealthBlock_Name")
    public void setHealthBlockName(String healthBlockName) {
        this.healthBlockName = healthBlockName;
    }

    public Long getStateCode() {
        return stateCode;
    }

    @XmlElement(name = "State_Code")
    public void setStateCode(Long stateCode) {
        this.stateCode = stateCode;
    }

    public Long getDistrictCode() {
        return districtCode;
    }

    @XmlElement(name = "District_Code")
    public void setDistrictCode(Long districtCode) {
        this.districtCode = districtCode;
    }

    public Long getTalukaCode() {
        return talukaCode;
    }

    @XmlElement(name = "Taluka_Code")
    public void setTalukaCode(Long talukaCode) {
        this.talukaCode = talukaCode;
    }

    public Long getHealthBlockCode() {
        return healthBlockCode;
    }

    @XmlElement(name = "HealthBlock_Code")
    public void setHealthBlockCode(Long healthBlockCode) {
        this.healthBlockCode = healthBlockCode;
    }
}
