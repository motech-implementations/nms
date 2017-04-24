package org.motechproject.nms.kilkari.exception;


public class InvalidRegistrationIdException extends RuntimeException {

    private static final long serialVersionUID = -4281715964469401468L;

    public InvalidRegistrationIdException(String message) {
        super(message);
    }

    public InvalidRegistrationIdException(String message, Throwable t) {
        super(message, t);
    }
}