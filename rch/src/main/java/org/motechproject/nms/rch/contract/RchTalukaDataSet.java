package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchTalukaRecord;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by vishnu on 27/6/18.
 */

@XmlRootElement(name = "NewDataSet")
@XmlAccessorType(XmlAccessType.NONE)
public class RchTalukaDataSet {

    private List<RchTalukaRecord> records;

    public List<RchTalukaRecord> getRecords() {
        return records;
    }

    @XmlElement(name = "Records")
    public void setRecords(List<RchTalukaRecord> records) {
        this.records = records;
    }
}
