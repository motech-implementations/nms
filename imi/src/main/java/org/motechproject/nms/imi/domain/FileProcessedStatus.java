package org.motechproject.nms.imi.domain;

import org.codehaus.jackson.annotate.JsonCreator;

/**
 * File processing status, provided by IVR
 */
public enum FileProcessedStatus {
    FILE_PROCESSED_SUCCESSFULLY(8000, "FILE_PROCESSED_SUCCESSFULLY"),
    FILE_NOT_ACCESSIBLE(8001, "FILE_NOT_ACCESSIBLE"),
    FILE_CHECKSUM_ERROR(8002, "FILE_CHECKSUM_ERROR"),
    FILE_RECORDSCOUNT_ERROR(8003, "FILE_RECORDSCOUNT_ERROR"),
    FILE_OUTSIDE_SOCIAL_HOURS(8004, "FILE_OUTSIDE_SOCIAL_HOURS"),
    FILE_ERROR_IN_FILE_FORMAT(8005, "FILE_ERROR_IN_FILE_FORMAT");

    private final int value;
    private final String name;


    FileProcessedStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }


    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static boolean isValidEnumValue(int i) {
        return (i >= FILE_PROCESSED_SUCCESSFULLY.getValue() && i <= FILE_ERROR_IN_FILE_FORMAT.getValue());
    }

    @JsonCreator
    public static FileProcessedStatus fromInt(int i) {
        if (isValidEnumValue(i)) {
            return values()[i - FILE_PROCESSED_SUCCESSFULLY.getValue()];
        } else {
            throw new IllegalArgumentException(String.format("%d is an invalid FileProcessedStatus", i));
        }
    }
}
