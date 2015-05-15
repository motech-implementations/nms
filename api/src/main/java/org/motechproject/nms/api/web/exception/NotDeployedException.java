package org.motechproject.nms.api.web.exception;

public class NotDeployedException extends IllegalStateException {

    public NotDeployedException(String message) {
        super(message);
    }

    public NotDeployedException(Exception ex, String message) {
        super(message, ex);
    }

}
