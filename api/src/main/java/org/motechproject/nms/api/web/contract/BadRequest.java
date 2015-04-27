package org.motechproject.nms.api.web.contract;

/**
 * Response body
 *
 * Returned by the base controller @ExceptionHandler error handlers.
 * A typical response body would look like: {"failureReason":"<callingNumber: Not Found>"}
 * Multiple errors are concatenated: {"failureReason":"<callStartTime: Not Present><callEndTime: Not Present>"}
 *
 */
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
