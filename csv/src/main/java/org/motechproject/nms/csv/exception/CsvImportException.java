package org.motechproject.nms.csv.exception;

public class CsvImportException extends RuntimeException {

    private static final long serialVersionUID = -6278097435395933061L;

    public CsvImportException(String message) {
        super(message);
    }

    public CsvImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
