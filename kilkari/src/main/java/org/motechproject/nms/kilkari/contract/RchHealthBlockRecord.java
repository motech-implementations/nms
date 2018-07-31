package org.motechproject.nms.kilkari.contract;

import org.joda.time.DateTime;

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

    private Long districtCode;
    private String talukaCode;
    private Long healthBlockCode;
    private String healthBlockName;
    private DateTime execDate;

    public String getHealthBlockName() {
        return healthBlockName;
    }

    @XmlElement(name = "HealthBlock_Name")
    public void setHealthBlockName(String healthBlockName) {
        this.healthBlockName = healthBlockName;
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
}
