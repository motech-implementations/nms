package org.motechproject.nms.imi.web.contract;

import java.util.List;

public class AggregateBadRequest {
    private List<String> failureReason;

    public AggregateBadRequest(List<String> failureReason) {
        this.failureReason = failureReason;
    }

    public List<String> getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(List<String> failureReason) {
        this.failureReason = failureReason;
    }
}
