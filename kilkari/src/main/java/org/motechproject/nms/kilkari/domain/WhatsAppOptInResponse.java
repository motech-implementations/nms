package org.motechproject.nms.kilkari.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum WhatsAppOptInResponse {
    OPTED(1),
    NOT_OPTED(2),
    NO_RESPONSE(0);

    private final int value;

    WhatsAppOptInResponse(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @JsonCreator
    public static WhatsAppOptInResponse fromValue(int value) {
        for (WhatsAppOptInResponse response : WhatsAppOptInResponse.values()) {
            if (response.value == value) {
                return response;
            }
        }
        return NO_RESPONSE; // Default to NO_RESPONSE if the value doesn't match any enum constant
    }
}

