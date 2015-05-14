package org.motechproject.nms.props.ut;

import org.junit.Test;
import org.motechproject.nms.kilkari.domain.StatusCode;

import static junit.framework.Assert.assertEquals;

public class StatusCodeUnitTest {

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
}
