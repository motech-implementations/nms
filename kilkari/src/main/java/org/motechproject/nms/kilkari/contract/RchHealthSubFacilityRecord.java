package org.motechproject.nms.kilkari.contract;

import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by beehyv on 18/7/18.
 */
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class RchHealthSubFacilityRecord {

    private Long districtCode;
    private String talukaCode;
    private Long healthFacilityCode;
    private Long healthSubFacilityCode;
    private String healthSubFacilityName;
    private DateTime execDate;

    public Long getDistrictCode() {
        return districtCode;
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

    public Long getHealthFacilityCode() {
        return healthFacilityCode;
    }

    @XmlElement(name = "HealthFacility_ID")
    public void setHealthFacilityCode(Long healthFacilityCode) {
        this.healthFacilityCode = healthFacilityCode;
    }

    public Long getHealthSubFacilityCode() {
        return healthSubFacilityCode;
    }

    @XmlElement(name = "HealthSubFacility_ID")
    public void setHealthSubFacilityCode(Long healthSubFacilityCode) {
        this.healthSubFacilityCode = healthSubFacilityCode;
    }

    public String getHealthSubFacilityName() {
        return healthSubFacilityName;
    }

    @XmlElement(name = "HealthSubFacility_Name")
    public void setHealthSubFacilityName(String healthSubFacilityName) {
        this.healthSubFacilityName = healthSubFacilityName;
    }

    public DateTime getExecDate() {
        return execDate;
    }

    @XmlElement(name = "Exec_Date")
    public void setExecDate(DateTime execDate) {
        this.execDate = execDate;
    }
}
