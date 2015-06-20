package org.motechproject.nms.kilkari.ut;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.motechproject.nms.kilkari.service.impl.PhoneNumberHelper;

import static org.junit.Assert.assertEquals;

public class PhoneNumberHelperUnitTest {


    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void verifyTruncateLongNumber() {
        assertEquals(0L, PhoneNumberHelper.truncateLongNumber(0L));
        assertEquals(1L, PhoneNumberHelper.truncateLongNumber(1L));
        assertEquals(1234567890L, PhoneNumberHelper.truncateLongNumber(1234567890L));
        assertEquals(9999999999L, PhoneNumberHelper.truncateLongNumber(9999999999L));
        assertEquals(0L, PhoneNumberHelper.truncateLongNumber(10000000000L));
        assertEquals(555L, PhoneNumberHelper.truncateLongNumber(1230000000555L));

        exception.expect(IllegalArgumentException.class);
        PhoneNumberHelper.truncateLongNumber(-123L);
    }
}
