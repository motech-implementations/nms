package org.motechproject.nms.flw.exception;

/**
 * Signals an issue with importing an FLW which already exits in database.
 */
public class GfStatusInactiveException extends Exception {

    public GfStatusInactiveException(String message) {
        super(message);
    }
}