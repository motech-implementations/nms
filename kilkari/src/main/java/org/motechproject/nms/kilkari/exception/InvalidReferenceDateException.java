package org.motechproject.nms.kilkari.exception;

public class InvalidReferenceDateException extends RuntimeException {

    private static final long serialVersionUID = -4281715964469401467L;

    public InvalidReferenceDateException(String message) {
        super(message);
    }

    public InvalidReferenceDateException(String message, Throwable t) {
        super(message, t);
    }
}
