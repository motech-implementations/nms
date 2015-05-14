package org.motechproject.nms.kilkari.domain;

public enum StatusCode {
    OBD_SUCCESS_CALL_CONNECTED(1001),
    OBD_FAILED_NOATTEMPT(2000),
    OBD_FAILED_BUSY(2001),
    OBD_FAILED_NOANSWER(2002),
    OBD_FAILED_SWITCHEDOFF(2003),
    OBD_FAILED_INVALIDNUMBER(2004),
    OBD_FAILED_OTHERS(2005),
    OBD_DNIS_IN_DND(3001);

    private final int value;

    private StatusCode(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static StatusCode fromInt(int i) {
        for (StatusCode statusCode : values()) {
            if (statusCode.getValue() == i) {
                return statusCode;
            }
        }
        throw new IllegalArgumentException(String.format("%d is an invalid StatusCode", i));
    }
}
