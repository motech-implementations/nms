package org.motechproject.nms.rch.exception;

public class RchImportConfigurationException extends RuntimeException {

    public RchImportConfigurationException(String message) {
        super(message);
    }

    public RchImportConfigurationException(String message, Throwable t) {
        super(message, t);
    }
}
