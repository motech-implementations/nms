package org.motechproject.nms.kilkari.contract;

import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by vishnu on 27/6/18.
 */
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class RchTalukaRecord {

    private Long districtCode;
    private String talukaCode;
    private String talukaName;
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

    public String getTalukaName() {
        return talukaName;
    }

    @XmlElement(name = "Taluka_Name")
    public void setTalukaName(String talukaName) {
        this.talukaName = talukaName;
    }

    public DateTime getExecDate() {
        return execDate;
    }

    @XmlElement(name = "Exec_Date")
    public void setExecDate(DateTime execDate) {
        this.execDate = execDate;
    }
}
