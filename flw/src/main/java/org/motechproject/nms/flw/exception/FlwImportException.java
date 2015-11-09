package org.motechproject.nms.flw.exception;

/**
 * Signals an issue with importing an FLW.
 */
public class FlwImportException extends RuntimeException {

    private static final long serialVersionUID = 4526536536032174107L;

    public FlwImportException(String message) {
        super(message);
    }

    public FlwImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
