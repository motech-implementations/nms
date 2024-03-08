package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchTalukaHealthBlockRecord;

import java.util.List;

/**
 * Created by vishnu on 13/7/18.
 * Updated by rakesh on 20/08/20
 */
public class RchTalukaHealthBlockDataSet {


    private List<RchTalukaHealthBlockRecord> records;


    public List<RchTalukaHealthBlockRecord> getRecords() {
        return records;
    }

    public void setRecords(List<RchTalukaHealthBlockRecord> records) {
        this.records = records;
    }
}
