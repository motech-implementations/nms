package org.motechproject.nms.props.domain;

/**
 * Reason the call was disconnected, returned by IMI (the IVR provider)
 */
public enum CallDisconnectReason {
    NORMAL_DROP(1),
    VXML_RUNTIME_EXCEPTION(2),
    CONTENT_NOT_FOUND(3),
    USAGE_CAP_EXCEEDED(4),
    ERROR_IN_THE_API(5),
    SYSTEM_ERROR(6);

    private final int value;

    CallDisconnectReason(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean isValid(int i) {
        return (i >= NORMAL_DROP.getValue() && i <= SYSTEM_ERROR.getValue());
    }

    //NOTE: this only works if the values are consecutive
    public static CallDisconnectReason fromInt(int i) {
        if (isValid(i)) {
            return values()[i - NORMAL_DROP.getValue()];
        } else {
            throw new IllegalArgumentException(String.format("%d is an invalid CallDisconnectReason", i));
        }
    }
}
