package org.motechproject.nms.tracking.exception;

public class TrackChangesException extends Exception {

    private static final long serialVersionUID = -6662426955871119037L;

    public TrackChangesException(String message) {
        super(message);
    }

    public TrackChangesException(String message, Throwable cause) {
        super(message, cause);
    }
}
