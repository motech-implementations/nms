package org.motechproject.nms.mcts.exception;

public class MctsImportConfigurationException extends RuntimeException {

    private static final long serialVersionUID = -2052867145063443369L;

    public MctsImportConfigurationException(String message) {
        super(message);
    }

    public MctsImportConfigurationException(String message, Throwable t) {
        super(message, t);
    }
}
