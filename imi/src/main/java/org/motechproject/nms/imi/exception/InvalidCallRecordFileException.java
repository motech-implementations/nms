package org.motechproject.nms.imi.exception;

import java.util.List;

public class InvalidCallRecordFileException extends IllegalStateException {
    private List<String> messages;

    public InvalidCallRecordFileException(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
}
