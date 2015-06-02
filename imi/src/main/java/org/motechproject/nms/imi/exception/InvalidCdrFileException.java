package org.motechproject.nms.imi.exception;

import java.util.List;

public class InvalidCdrFileException extends IllegalStateException {
    private List<String> messages;

    public InvalidCdrFileException(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
}
