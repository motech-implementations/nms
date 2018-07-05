package org.motechproject.nms.rch.contract;

import org.motechproject.nms.kilkari.contract.RchTalukaHealthBlockRecord;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by vishnu on 3/7/18.
 */

@XmlRootElement(name = "NewDataSet")
@XmlAccessorType(XmlAccessType.NONE)
public class RchTalukaHealthBlockDataSet {


    private List<RchTalukaHealthBlockRecord> records;


    public List<RchTalukaHealthBlockRecord> getRecords() {
        return records;
    }

    @XmlElement(name = "Records")
    public void setRecords(List<RchTalukaHealthBlockRecord> records) {
        this.records = records;
    }
}
