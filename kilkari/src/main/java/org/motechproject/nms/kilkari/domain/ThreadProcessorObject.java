package org.motechproject.nms.kilkari.domain;

import java.util.Map;

/**
 * Created by beehyv on 25/4/18.
 */
public class ThreadProcessorObject {

    private Map<String, Object> rejectedBeneficiaries;
    private Map<String, Object> rejectionStatus;
    private Integer recordsProcessed;

    public Map<String, Object> getRejectedBeneficiaries() {
        return rejectedBeneficiaries;
    }

    public void setRejectedBeneficiaries(Map<String, Object> rejectedBeneficiaries) {
        this.rejectedBeneficiaries = rejectedBeneficiaries;
    }

    public Map<String, Object> getRejectionStatus() {
        return rejectionStatus;
    }

    public void setRejectionStatus(Map<String, Object> rejectionStatus) {
        this.rejectionStatus = rejectionStatus;
    }

    public Integer getRecordsProcessed() {
        return recordsProcessed;
    }

    public void setRecordsProcessed(Integer recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }
}
