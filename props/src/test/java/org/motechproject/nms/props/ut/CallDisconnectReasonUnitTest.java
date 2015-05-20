package org.motechproject.nms.props.ut;

import org.junit.Test;
import org.motechproject.nms.props.domain.CallDisconnectReason;

import static junit.framework.Assert.assertEquals;

public class CallDisconnectReasonUnitTest {
    @Test
    public void testValue() {
        assertEquals(CallDisconnectReason.ERROR_IN_THE_API.getValue(), 5);
    }

    @Test
    public void testFromInt() {
        assertEquals(CallDisconnectReason.fromInt(5), CallDisconnectReason.ERROR_IN_THE_API);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromIntFailure() {
        CallDisconnectReason.fromInt(7);
    }
}
