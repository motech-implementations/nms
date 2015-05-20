package org.motechproject.nms.scp.service.exception;

public class SecureCopyException extends Exception {
    public SecureCopyException(String message) {
        super(message);
    }

    public SecureCopyException(String message, Throwable cause) {
        super(message, cause);
    }
}
