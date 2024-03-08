package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchVillageHealthSubFacilityRecord;

import java.util.List;

/**
 * Created by vishnu on 13/7/18.
 * Updated by rakesh on 20/08/20
 */

public class RchVillageHealthSubFacilityDataSet {

    private List<RchVillageHealthSubFacilityRecord> records;

    public List<RchVillageHealthSubFacilityRecord> getRecords() {
        return records;
    }

    public void setRecords(List<RchVillageHealthSubFacilityRecord> records) {
        this.records = records;
    }
}
