package org.motechproject.nms.imi.exception;

public class InvalidCsrException extends IllegalStateException {
    public InvalidCsrException(String message) {
        super(message);
    }

    public InvalidCsrException(Throwable e) {
        super(e);
    }
}
