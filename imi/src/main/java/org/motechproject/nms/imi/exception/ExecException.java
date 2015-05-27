package org.motechproject.nms.imi.exception;


public class ExecException extends Exception {
    public ExecException(String message) {
        super(message);
    }

    public ExecException(String message, Throwable cause) {
        super(message, cause);
    }
}
