package org.motechproject.nms.api.web.exception;

public class NotFoundException extends IllegalStateException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(Exception ex, String message) {
        super(message, ex);
    }

}
