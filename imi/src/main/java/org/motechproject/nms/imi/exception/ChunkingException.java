package org.motechproject.nms.imi.exception;

public class ChunkingException extends RuntimeException {
    public ChunkingException(String m, Throwable t) {
        super(m, t);
    }
}
