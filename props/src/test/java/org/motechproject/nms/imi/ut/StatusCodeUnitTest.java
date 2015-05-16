package org.motechproject.nms.imi.ut;


import org.junit.Test;
import org.motechproject.nms.props.domain.StatusCode;

import static junit.framework.Assert.assertEquals;

public class StatusCodeUnitTest {
    @Test
    public void testValue() {
        assertEquals(StatusCode.OBD_SUCCESS_CALL_CONNECTED.getValue(), 1001);
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
