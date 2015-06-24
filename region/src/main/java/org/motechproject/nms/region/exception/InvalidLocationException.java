package org.motechproject.nms.region.exception;

/**
 * Exception class to throw for invalid location codes
 */
public class InvalidLocationException extends Exception {

    public InvalidLocationException(String message) {
        super(message);
    }

}
