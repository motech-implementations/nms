package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchHealthFacilityRecord;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by vishnu on 13/7/18.
 */

@XmlRootElement(name = "NewDataSet")
@XmlAccessorType(XmlAccessType.NONE)
public class RchHealthFacilityDataSet {

    private List<RchHealthFacilityRecord> records;

    public List<RchHealthFacilityRecord> getRecords() {
        return records;
    }

    @XmlElement(name = "Records")
    public void setRecords(List<RchHealthFacilityRecord> records) {
        this.records = records;
    }
}
