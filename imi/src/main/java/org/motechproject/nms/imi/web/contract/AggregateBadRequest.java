package org.motechproject.nms.imi.web.contract;

import java.util.List;

public class AggregateBadRequest {
    private List<String> failureReasons;

    public AggregateBadRequest(List<String> failureReasons) {
        this.failureReasons = failureReasons;
    }

    public List<String> getFailureReasons() {
        return failureReasons;
    }

    public void setFailureReasons(List<String> failureReasons) {
        this.failureReasons = failureReasons;
    }
}
