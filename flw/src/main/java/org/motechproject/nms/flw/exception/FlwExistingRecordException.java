package org.motechproject.nms.flw.exception;

/**
 * Signals an issue with importing an FLW which already exits in database.
 */
public class FlwExistingRecordException extends Exception {

    public FlwExistingRecordException(String message) {
        super(message);
    }
}
