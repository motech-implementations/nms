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
public class RchVillageHealthSubFacilityRecord {

    private Long districtCode;
    private Long villageCode;
    private Long healthSubFacilityCode;
    private DateTime execDate;

    public Long getDistrictCode() {
        return districtCode;
    }

    @XmlElement(name = "District_ID")
    public void setDistrictCode(Long districtCode) {
        this.districtCode = districtCode;
    }

    public Long getVillageCode() {
        return villageCode;
    }

    @XmlElement(name = "Village_ID")
    public void setVillageCode(Long villageCode) {
        this.villageCode = villageCode;
    }

    public Long getHealthSubFacilityCode() {
        return healthSubFacilityCode;
    }

    @XmlElement(name = "HealthSubFacility_ID")
    public void setHealthSubFacilityCode(Long healthSubFacilityCode) {
        this.healthSubFacilityCode = healthSubFacilityCode;
    }

    public DateTime getExecDate() {
        return execDate;
    }

    @XmlElement(name = "Exec_Date")
    public void setExecDate(DateTime execDate) {
        this.execDate = execDate;
    }
}
