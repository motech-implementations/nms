package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchVillageRecord;

import java.util.List;

/**
 * Created by vishnu on 13/7/18.
 * Updated by rakesh on 20/08/20
 */

public class RchVillageDataSet {

    private List<RchVillageRecord> records;

    public List<RchVillageRecord> getRecords() {
        return records;
    }

    public void setRecords(List<RchVillageRecord> records) {
        this.records = records;
    }
}
