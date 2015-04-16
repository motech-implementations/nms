package org.motechproject.nms.api.web.exception;

public class NotFoundException extends Exception {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(Exception ex, String message) {
        super(message, ex);
    }

}
