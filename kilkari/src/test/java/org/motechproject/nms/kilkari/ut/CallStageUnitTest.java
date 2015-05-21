package org.motechproject.nms.kilkari.ut;

import org.junit.Test;
import org.motechproject.nms.kilkari.domain.CallStage;

import static org.junit.Assert.assertEquals;

public class CallStageUnitTest {

    @Test
    public void verifyNextStage() {
        assertEquals(CallStage.RETRY_2, CallStage.RETRY_1.nextStage());
        assertEquals(CallStage.RETRY_LAST, CallStage.RETRY_2.nextStage());
    }

    @Test(expected = IllegalStateException.class)
    public void verifyInvalidNextStage() {
        CallStage illegal = CallStage.RETRY_LAST.nextStage();
    }
}
