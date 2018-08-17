package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchVillageHealthSubFacilityRecord;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by beehyv on 18/7/18.
 */
@XmlRootElement(name = "NewDataSet")
@XmlAccessorType(XmlAccessType.NONE)
public class RchVillageHealthSubFacilityDataSet {

    private List<RchVillageHealthSubFacilityRecord> records;

    public List<RchVillageHealthSubFacilityRecord> getRecords() {
        return records;
    }

    @XmlElement(name = "Records")
    public void setRecords(List<RchVillageHealthSubFacilityRecord> records) {
        this.records = records;
    }
}
