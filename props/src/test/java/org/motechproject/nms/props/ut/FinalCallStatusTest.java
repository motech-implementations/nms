package org.motechproject.nms.props.ut;

import org.junit.Test;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.StatusCode;

import static junit.framework.Assert.assertEquals;

public class FinalCallStatusTest {

    @Test
    public void testValue() {
        assertEquals(1, FinalCallStatus.SUCCESS.getValue());
    }

    @Test
    public void testFromInt() {
        assertEquals(FinalCallStatus.FAILED, FinalCallStatus.fromInt(2));
    }

    @Test
    public void testFromStatusCode() {
        assertEquals(FinalCallStatus.SUCCESS, FinalCallStatus.fromStatusCode(
                StatusCode.OBD_SUCCESS_CALL_CONNECTED));
        assertEquals(FinalCallStatus.REJECTED, FinalCallStatus.fromStatusCode(StatusCode.OBD_DNIS_IN_DND));
        assertEquals(FinalCallStatus.FAILED, FinalCallStatus.fromStatusCode(StatusCode.OBD_FAILED_NOANSWER));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromIntFailure() {
        FinalCallStatus.fromInt(0);
    }
}
