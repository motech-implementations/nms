package org.motechproject.nms.rch.exception;

/**
 * Created by beehyvsc on 6/6/17.
 */
public class RchFileManipulationException extends Exception {

    public RchFileManipulationException(String message) {
        super(message);
    }

    public RchFileManipulationException(String message, Throwable t) {
        super(message, t);
    }
}
