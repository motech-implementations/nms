package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchTalukaRecord;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by vishnu on 13/7/18.
 * Updated by rakesh on 20/08/20
 */

public class RchTalukaDataSet {

    private List<RchTalukaRecord> records;

    public List<RchTalukaRecord> getRecords() {
        return records;
    }

    public void setRecords(List<RchTalukaRecord> records) {
        this.records = records;
    }
}
