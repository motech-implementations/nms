package org.motechproject.nms.kilkari.contract;

import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by vishnu on 13/7/18.
 */

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class RchHealthFacilityRecord {

    private Long districtCode;
    private String talukaCode;
    private Long healthBlockCode;
    private Long healthFacilityCode;
    private String healthFacilityName;
    private DateTime execDate;

    public String getHealthFacilityName() {
        return healthFacilityName;
    }

    @XmlElement(name = "HealthFacility_Name")
    public void setHealthFacilityName(String healthFacilityName) {
        this.healthFacilityName = healthFacilityName;
    }

    public Long getDistrictCode() {
        return districtCode;
    }

    public DateTime getExecDate() {
        return execDate;
    }

    @XmlElement(name = "Exec_Date")
    public void setExecDate(DateTime execDate) {
        this.execDate = execDate;
    }

    @XmlElement(name = "District_ID")
    public void setDistrictCode(Long districtCode) {
        this.districtCode = districtCode;
    }

    public String getTalukaCode() {
        return talukaCode;
    }

    @XmlElement(name = "Taluka_ID")
    public void setTalukaCode(String talukaCode) {
        this.talukaCode = talukaCode;
    }

    public Long getHealthBlockCode() {
        return healthBlockCode;
    }

    @XmlElement(name = "HealthBlock_ID")
    public void setHealthBlockCode(Long healthBlockCode) {
        this.healthBlockCode = healthBlockCode;
    }

    public Long getHealthFacilityCode() {
        return healthFacilityCode;
    }

    @XmlElement(name = "HealthFacility_ID")
    public void setHealthFacilityCode(Long healthFacilityCode) {
        this.healthFacilityCode = healthFacilityCode;
    }
}
