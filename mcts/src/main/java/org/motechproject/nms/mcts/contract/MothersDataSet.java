package org.motechproject.nms.mcts.contract;

import org.motechproject.nms.kilkari.contract.MotherRecord;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "NewDataSet")
@XmlAccessorType(XmlAccessType.NONE)
public class MothersDataSet {

    private List<MotherRecord> records;

    public List<MotherRecord> getRecords() {
        return records;
    }

    @XmlElement(name = "Records")
    public void setRecords(List<MotherRecord> records) {
        this.records = records;
    }
}
