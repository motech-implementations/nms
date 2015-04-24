package org.motechproject.nms.api.web.exception;

public class NotAuthorizedException extends IllegalStateException {
    public NotAuthorizedException(String message) {
            super(message);
        }

    public NotAuthorizedException(Exception ex, String message) {
            super(message, ex);
        }
}
