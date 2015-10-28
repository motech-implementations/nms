package org.motechproject.nms.kilkari.exception;

public class InvalidReferanceDateException extends RuntimeException {

    private static final long serialVersionUID = -4281715964469401467L;

    public InvalidReferanceDateException(String message) {
        super(message);
    }

    public InvalidReferanceDateException(String message, Throwable t) {
        super(message, t);
    }
}
