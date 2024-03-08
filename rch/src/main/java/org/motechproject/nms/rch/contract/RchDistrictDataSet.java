package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchDistrictRecord;

import java.util.List;

/**
 * Created by vishnu on 13/7/18.
 * Updated by rakesh on 20/08/20
 */

public class RchDistrictDataSet {

    private List<RchDistrictRecord> records;

    public List<RchDistrictRecord> getRecords() {
        return records;
    }

    public void setRecords(List<RchDistrictRecord> records) {
        this.records = records;
    }
}
