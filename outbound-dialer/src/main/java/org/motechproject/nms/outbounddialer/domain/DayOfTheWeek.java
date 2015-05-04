package org.motechproject.nms.outbounddialer.domain;

import org.joda.time.DateTime;

public enum DayOfTheWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    public static DayOfTheWeek today() {
        return values()[DateTime.now().getDayOfWeek() - 1];
    }
}
