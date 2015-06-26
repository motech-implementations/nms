package org.motechproject.nms.api.ut;


import org.junit.Test;
import org.motechproject.nms.api.web.contract.LogHelper;

import static org.junit.Assert.assertEquals;

public class LogHelperUnitTest {

    @Test
    public void verifySafeToString() {
        assertEquals("null", LogHelper.obscure(null));
        assertEquals("0", LogHelper.obscure(0L));
        assertEquals("*234", LogHelper.obscure(1234L));
        assertEquals("*******890", LogHelper.obscure(1234567890L));
        assertEquals("**********123", LogHelper.obscure(1234567890123L));
    }
}
