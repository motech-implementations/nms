package org.motechproject.nms.mcts.exception;

public class MctsWebServiceException extends RuntimeException{

    private static final long serialVersionUID = 6752594116166118889L;

    public MctsWebServiceException(String message, Throwable t) {
        super(message, t);
    }
}
