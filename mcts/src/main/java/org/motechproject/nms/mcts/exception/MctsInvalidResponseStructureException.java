package org.motechproject.nms.mcts.exception;

public class MctsInvalidResponseStructureException extends RuntimeException {

    private static final long serialVersionUID = 7742271787121615166L;

    public MctsInvalidResponseStructureException(String message) {
        super(message);
    }

    public MctsInvalidResponseStructureException(String message, Throwable t) {
        super(message, t);
    }
}
