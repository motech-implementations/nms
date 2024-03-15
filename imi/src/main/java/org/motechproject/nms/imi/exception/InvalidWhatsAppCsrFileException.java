package org.motechproject.nms.imi.exception;

import java.util.List;

public class InvalidWhatsAppCsrFileException extends IllegalStateException{
    private List<String> messages;

    public InvalidWhatsAppCsrFileException(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
}
