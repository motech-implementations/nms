package org.motechproject.nms.imi.service.contract;


import java.util.List;

public class ProcessResult {
    private int successCount;
    private List<String> errors;

    public ProcessResult(int successCount, List<String> errors) {
        this.successCount = successCount;
        this.errors = errors;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "ProcessResult{" +
                "successCount=" + successCount +
                ", errors=" + errors +
                '}';
    }
}
