package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchHealthBlockRecord;

import java.util.List;

/**
 * Created by vishnu on 13/7/18.
 * Updated by rakesh on 20/08/20
 */

public class RchHealthBlockDataSet {

    private List<RchHealthBlockRecord> records;

    public List<RchHealthBlockRecord> getRecords() {
        return records;
    }

    public void setRecords(List<RchHealthBlockRecord> records) {
        this.records = records;
    }
}
