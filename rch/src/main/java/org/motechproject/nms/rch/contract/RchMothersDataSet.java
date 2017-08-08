package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchMotherRecord;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "NewDataSet")
@XmlAccessorType(XmlAccessType.NONE)
public class RchMothersDataSet {

    private List<RchMotherRecord> records;

    public List<RchMotherRecord> getRecords() {
        return records;
    }

    @XmlElement(name = "Records")
    public void setRecords(List<RchMotherRecord> records) {
        this.records = records;
    }
}
