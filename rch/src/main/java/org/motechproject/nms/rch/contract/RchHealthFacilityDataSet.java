package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchHealthFacilityRecord;

import java.util.List;

/**
 * Created by vishnu on 13/7/18.
 * Updated by rakesh on 20/08/20
 */

public class RchHealthFacilityDataSet {

    private List<RchHealthFacilityRecord> records;

    public List<RchHealthFacilityRecord> getRecords() {
        return records;
    }

    public void setRecords(List<RchHealthFacilityRecord> records) {
        this.records = records;
    }
}
