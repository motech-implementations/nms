package org.motechproject.nms.mcts.exception;

public class MctsWebServiceExeption extends RuntimeException{

    private static final long serialVersionUID = 6752594116166118889L;

    public MctsWebServiceExeption(String message, Throwable t) {
        super(message, t);
    }
}
