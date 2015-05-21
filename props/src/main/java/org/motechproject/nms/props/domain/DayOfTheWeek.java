package org.motechproject.nms.props.domain;

import org.joda.time.DateTime;

public enum DayOfTheWeek {
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SUNDAY(7);

    private final int value;

    DayOfTheWeek(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    //NOTE: this only works if the values are consecutive
    public static DayOfTheWeek fromInt(int i) {
        if (i >= MONDAY.getValue() && i <= SUNDAY.getValue()) {
            return values()[i - MONDAY.getValue()];
        } else {
            throw new IllegalArgumentException(String.format("%d is an invalid DayOfTheWeek", i));
        }
    }

    public static DayOfTheWeek today() {
        return fromInt(DateTime.now().getDayOfWeek());
    }

    public static DayOfTheWeek fromDateTime(DateTime dt) {
        return fromInt(dt.getDayOfWeek());
    }

    public DayOfTheWeek nextDay() {
        if (value == SUNDAY.getValue()) {
            return MONDAY;
        }
        return values()[value];
    }
}
