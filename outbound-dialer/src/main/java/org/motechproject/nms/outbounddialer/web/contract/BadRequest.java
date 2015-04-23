package main.java.org.motechproject.nms.outbounddialer.web.contract;

public class BadRequest {
    private String failureReason;

    public BadRequest(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
