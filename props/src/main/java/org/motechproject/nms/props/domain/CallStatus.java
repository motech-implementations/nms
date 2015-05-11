package org.motechproject.nms.props.domain;

/**
 * Call Status, describes how a phone call ended
 */
public enum CallStatus {
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

    public static CallStatus fromInt(int i) {
        if (isValid(i)) {
            return values()[i];
        } else {
            throw new IllegalArgumentException(String.format("%d is an invalid CallStatus", i));
        }
    }
}
