package org.motechproject.nms.imi.exception;

public class InternalException extends IllegalStateException {
    public InternalException(String message) {
        super(message);
    }

    public InternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
