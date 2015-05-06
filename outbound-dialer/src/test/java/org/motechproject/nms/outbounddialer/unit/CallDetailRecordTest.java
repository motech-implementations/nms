package org.motechproject.nms.outbounddialer.unit;

import org.junit.Test;
import org.motechproject.nms.outbounddialer.domain.CallDetailRecord;

import static org.junit.Assert.assertNotNull;

public class CallDetailRecordTest {

    @Test(expected=IllegalStateException.class)
    public void testTooFewFields() {
        CallDetailRecord cdr =
            CallDetailRecord.fromLine("a,b");
        assertNotNull(cdr);
    }

    @Test(expected=IllegalStateException.class)
    public void testTooManyFields() {
        CallDetailRecord cdr =
                CallDetailRecord.fromLine("a,b,c,d,e,f,g,h,i,j,k,l,m,o");
        assertNotNull(cdr);
    }

    @Test(expected=NumberFormatException.class)
    public void testInvalidFields() {
        CallDetailRecord cdr =
                CallDetailRecord.fromLine("a,b,c,d,e,f,g,h,i,j,k,l,m");
        assertNotNull(cdr);
    }

    @Test
    public void testValidFields() {
        CallDetailRecord cdr =
                CallDetailRecord.fromLine("a,b,c,d,5,f,g,h,i,j,11,12,13");
        assertNotNull(cdr);
    }
}
