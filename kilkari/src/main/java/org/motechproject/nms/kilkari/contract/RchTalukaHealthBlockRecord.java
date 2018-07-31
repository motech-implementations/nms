package org.motechproject.nms.kilkari.contract;

import org.joda.time.DateTime;

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

    private String talukaCode;
    private Long healthBlockCode;
    private DateTime execDate;

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

    public DateTime getExecDate() {
        return execDate;
    }

    @XmlElement(name = "Exec_Date")
    public void setExecDate(DateTime execDate) {
        this.execDate = execDate;
    }
}
