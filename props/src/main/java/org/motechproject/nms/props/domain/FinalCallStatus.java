package org.motechproject.nms.props.domain;

/**
 * Call Status, describes how a phone call ended
 */
public enum FinalCallStatus {
    SUCCESS(1),
    FAILED(2),
    REJECTED(3);

    private final int value;
    private static final int SU = 1;
    private static final int FA = 2;
    private static final int RE = 3;

    FinalCallStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean isValidEnumValue(int i) {
        return (i == SU || i == FA || i == RE);
    }

    public static FinalCallStatus fromInt(int i) {
        if (i == SU) { return SUCCESS; }
        if (i == FA) { return FAILED; }
        if (i == RE) { return REJECTED; }
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
