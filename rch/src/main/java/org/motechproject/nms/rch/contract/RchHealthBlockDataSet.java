package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchHealthBlockRecord;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by vishnu on 2/7/18.
 */

@XmlRootElement(name = "NewDataSet")
@XmlAccessorType(XmlAccessType.NONE)
public class RchHealthBlockDataSet {

    private List<RchHealthBlockRecord> records;

    public List<RchHealthBlockRecord> getRecords() {
        return records;
    }

    @XmlElement(name = "Records")
    public void setRecords(List<RchHealthBlockRecord> records) {
        this.records = records;
    }
}
