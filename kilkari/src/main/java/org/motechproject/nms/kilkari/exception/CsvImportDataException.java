package org.motechproject.nms.kilkari.exception;

public class CsvImportDataException extends CsvImportException {

    private static final long serialVersionUID = 7874543019201079606L;

    public CsvImportDataException(String message) {
        super(message);
    }

    public CsvImportDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
