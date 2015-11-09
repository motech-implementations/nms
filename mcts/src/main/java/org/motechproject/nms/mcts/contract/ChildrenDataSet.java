package org.motechproject.nms.mcts.contract;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "NewDataSet")
@XmlAccessorType(XmlAccessType.NONE)
public class ChildrenDataSet {

    private List<ChildRecord> records;

    public List<ChildRecord> getRecords() {
        return records;
    }

    @XmlElement(name = "Records")
    public void setRecords(List<ChildRecord> records) {
        this.records = records;
    }
}