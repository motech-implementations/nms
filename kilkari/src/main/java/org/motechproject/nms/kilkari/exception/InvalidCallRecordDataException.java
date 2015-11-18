package org.motechproject.nms.kilkari.exception;

public class InvalidCallRecordDataException extends RuntimeException {
    public InvalidCallRecordDataException(String m, Throwable t) {
        super(m, t);
    }
    public InvalidCallRecordDataException(String message) {
        super(message);
    }
}
