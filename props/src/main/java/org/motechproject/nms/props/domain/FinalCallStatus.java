package org.motechproject.nms.props.domain;

/**
 * Call Status, describes how a phone call ended
 */
public enum FinalCallStatus {
    INVALID, // 0
    SUCCESS, // 1
    FAILED, // 2
    REJECTED; // 3

    public static boolean isValid(int i) {
        return (i >= 1 && i < values().length);
    }

    public int getValue() {
        return ordinal();
    }

    public static FinalCallStatus fromInt(int i) {
        if (isValid(i)) {
            return values()[i];
        } else {
            throw new IllegalArgumentException(String.format("%d is an invalid FinalCallStatus", i));
        }
    }

    public static FinalCallStatus fromStatusCode(StatusCode statusCode) {
        switch (statusCode) {
            case OBD_SUCCESS_CALL_CONNECTED:
                return SUCCESS;
            case OBD_DNIS_IN_DND:
                return REJECTED;
            default:
                return FAILED;
        }
    }
}
