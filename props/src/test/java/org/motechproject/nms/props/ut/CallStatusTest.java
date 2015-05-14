package org.motechproject.nms.props.ut;

import org.junit.Test;
import org.motechproject.nms.props.domain.CallStatus;

import static junit.framework.Assert.assertEquals;

public class CallStatusTest {

    @Test
    public void testValue() {
        assertEquals(CallStatus.SUCCESS.getValue(), 1);
    }

    @Test
    public void testFromInt() {
        assertEquals(CallStatus.fromInt(2), CallStatus.FAILED);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromIntFailure() {
        CallStatus.fromInt(0);
    }
}
