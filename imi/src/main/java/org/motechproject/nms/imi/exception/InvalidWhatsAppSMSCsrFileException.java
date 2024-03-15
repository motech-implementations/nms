package org.motechproject.nms.imi.exception;

import java.util.List;

public class InvalidWhatsAppSMSCsrFileException extends IllegalStateException{
    private List<String> messages;

    public InvalidWhatsAppSMSCsrFileException(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
}
