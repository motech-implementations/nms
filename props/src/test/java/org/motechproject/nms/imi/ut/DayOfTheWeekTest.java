package org.motechproject.nms.imi.ut;

import org.junit.Test;
import org.motechproject.nms.props.domain.DayOfTheWeek;

import static junit.framework.Assert.assertEquals;

public class DayOfTheWeekTest {
    @Test
    public void testNextDay() {
        assertEquals(DayOfTheWeek.TUESDAY, DayOfTheWeek.MONDAY.nextDay());
        assertEquals(DayOfTheWeek.WEDNESDAY, DayOfTheWeek.TUESDAY.nextDay());
        assertEquals(DayOfTheWeek.THURSDAY, DayOfTheWeek.WEDNESDAY.nextDay());
        assertEquals(DayOfTheWeek.FRIDAY, DayOfTheWeek.THURSDAY.nextDay());
        assertEquals(DayOfTheWeek.SATURDAY, DayOfTheWeek.FRIDAY.nextDay());
        assertEquals(DayOfTheWeek.SUNDAY, DayOfTheWeek.SATURDAY.nextDay());
        assertEquals(DayOfTheWeek.MONDAY, DayOfTheWeek.SUNDAY.nextDay());
    }

    @Test
    public void testValue() {
        assertEquals(DayOfTheWeek.MONDAY.getValue(), 1);
    }

    @Test
    public void testFromInt() {
        assertEquals(DayOfTheWeek.fromInt(2), DayOfTheWeek.TUESDAY);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromIntFailure() {
        DayOfTheWeek.fromInt(0);
    }
}
