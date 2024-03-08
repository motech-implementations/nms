package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchHealthSubFacilityRecord;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by vishnu on 13/7/18.
 * Updated by rakesh on 20/08/20
 */

@XmlAccessorType(XmlAccessType.NONE)
public class RchHealthSubFacilityDataSet {

    private List<RchHealthSubFacilityRecord> records;

    public List<RchHealthSubFacilityRecord> getRecords() {
        return records;
    }

    public void setRecords(List<RchHealthSubFacilityRecord> records) {
        this.records = records;
    }
}
