package org.motechproject.nms.props.domain;

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

    /**
     * Determines if an IMI failure code would be sent through a CSR
     *
     * @param value the failure code
     * @return true if this failure would be sent in a CSR, false otherwise
     */
    public static boolean summaryOnlyFailure(int value) {
        switch (fromInt(value)) {
            case OBD_DNIS_IN_DND:
            case OBD_FAILED_OTHERS:
            case OBD_FAILED_NOATTEMPT:
                return true;

            default:
                return false;
        }
    }
}
