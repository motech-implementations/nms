package org.motechproject.nms.props.domain;

/**
 * Call Status, describes how a phone call ended
 */
public enum FinalCallStatus {
    SUCCESS(1),
    FAILED(2),
    REJECTED(3);

    private final int value;

    FinalCallStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean isValidEnumValue(int i) {
        return (i == 1 || i ==2 || i == 3);
    }

    public static FinalCallStatus fromInt(int i) {
        if (i == 1) { return SUCCESS; }
        if (i == 2) { return FAILED; }
        if (i == 3) { return REJECTED; }
        throw new IllegalArgumentException(String.format("%d is an invalid FinalCallStatus", i));
    }

    public static FinalCallStatus fromStatusCode(StatusCode code) {
        switch (code) {
            case OBD_SUCCESS_CALL_CONNECTED:
                return SUCCESS;
            case OBD_DNIS_IN_DND:
                return REJECTED;
            default:
                return FAILED;
        }
    }
}
