package org.motechproject.nms.props.domain;

public enum WhatsAppOptInStatusCode {
    NO_INPUT(0),
    OPTED_FOR_WHATSAPP(1),
    OPTED_FOR_IVR(2),
    INVALID_INPUT(3),
    NULL(5);

    private final int value;

    public int getValue() {
        return value;
    }

    private WhatsAppOptInStatusCode(final int value) {
        this.value = value;
    }

    public static WhatsAppOptInStatusCode fromValue(int i) {
        for (WhatsAppOptInStatusCode statusCode : values()) {
            if (statusCode.getValue()==i) {
                return statusCode;
            }
        }
        throw new IllegalArgumentException(String.format("%d is an invalid StatusCode", i));
    }

    public static int getCodeByName(String name) {
        for (WhatsAppOptInStatusCode status : values()) {
            if (status.name().equals(name)) {
                return status.getValue();
            }
        }
        throw new IllegalArgumentException("No enum constant with name: " + name);
    }
}
