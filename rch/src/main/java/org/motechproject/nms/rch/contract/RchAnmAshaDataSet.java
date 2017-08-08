package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchAnmAshaRecord;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "NewDataSet")
@XmlAccessorType(XmlAccessType.NONE)
public class RchAnmAshaDataSet {

    private List<RchAnmAshaRecord> records;

    public List<RchAnmAshaRecord> getRecords() {
        return records;
    }

    @XmlElement(name = "Records")
    public void setRecords(List<RchAnmAshaRecord> records) {
        this.records = records;
    }
}
