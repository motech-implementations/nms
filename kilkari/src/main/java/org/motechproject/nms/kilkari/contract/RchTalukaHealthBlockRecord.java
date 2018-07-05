package org.motechproject.nms.kilkari.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by vishnu on 3/7/18.
 */

@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class RchTalukaHealthBlockRecord {

    private Long stateCode;
    private Long talukaCode;
    private Long healthBlockCode;
    private String talukaName;

    public String getTalukaName() {
        return talukaName;
    }

    @XmlElement(name = "Taluka_Name")
    public void setTalukaName(String talukaName) {
        this.talukaName = talukaName;
    }

    public Long getStateCode() {
        return stateCode;
    }

    @XmlElement(name = "State_Code")
    public void setStateCode(Long stateCode) {
        this.stateCode = stateCode;
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
