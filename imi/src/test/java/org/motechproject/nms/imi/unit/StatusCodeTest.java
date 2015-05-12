package org.motechproject.nms.imi.unit;

import org.joda.time.DateTime;
import org.junit.Test;
import org.motechproject.nms.imi.domain.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.Assert.assertEquals;

public class StatusCodeTest {

    @Test
    public void testValue() {
        assertEquals(StatusCode.OBD_FAILED_NOATTEMPT.getValue(), 2000);
    }

    @Test
    public void testFromInt() {
        assertEquals(StatusCode.fromInt(2002), StatusCode.OBD_FAILED_NOANSWER);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromIntFailure() {
        StatusCode.fromInt(0);
    }

    @Test
    public void testDayOfWeek() {
        Logger logger = LoggerFactory.getLogger(StatusCodeTest.class);
        DateTime today = DateTime.now();
        for (int i=0 ; i<=7 ; i++) {
            logger.debug("today.plusDays({}).getDayOfWeek() = {}", i, today.plusDays(i).getDayOfWeek());
        }
    }
}
