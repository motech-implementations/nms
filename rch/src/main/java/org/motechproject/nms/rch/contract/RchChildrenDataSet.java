package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchChildRecord;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "NewDataSet")
@XmlAccessorType(XmlAccessType.NONE)
public class RchChildrenDataSet {

    private List<RchChildRecord> records;

    public List<RchChildRecord> getRecords() {
        return records;
    }

    @XmlElement(name = "Records")
    public void setRecords(List<RchChildRecord> records) {
        this.records = records;
    }
}
