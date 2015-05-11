package org.motechproject.nms.imi.unit;

import org.junit.Test;
import org.motechproject.nms.imi.domain.CallDetailRecord;
import org.motechproject.nms.props.domain.CallStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CallDetailRecordTest {

    @Test(expected=IllegalStateException.class)
    public void testTooFewFields() {
        CallDetailRecord cdr = CallDetailRecord.fromLine("a,b");
        assertNotNull(cdr);
    }

    @Test(expected=IllegalStateException.class)
    public void testTooManyFields() {
        CallDetailRecord cdr = CallDetailRecord.fromLine("a,b,c,d,e,f,g,h,i,j,k,l,m,o");
        assertNotNull(cdr);
    }

    @Test(expected=NumberFormatException.class)
    public void testInvalidFields() {
        CallDetailRecord cdr = CallDetailRecord.fromLine("a,b,c,d,e,f,g,h,i,j,k,l,m");
        assertNotNull(cdr);
    }

    @Test
    public void testValidFields() {
        CallDetailRecord cdr = CallDetailRecord.fromLine("a,b,c,d,5,f,g,h,i,j,1,12,13");
        assertNotNull(cdr);
    }

    @Test
    public void testFromString() {
        CallDetailRecord expectedCdr = new CallDetailRecord("a", "b", "c", "d", 5, "f", "g", "h", "i", "j",
                CallStatus.SUCCESS, 12, 13);
        CallDetailRecord cdr = CallDetailRecord.fromLine("a,b,c,d,5,f,g,h,i,j,1,12,13");
        assertEquals(expectedCdr, cdr);
    }

    @Test
    public void testToString() {
        CallDetailRecord cdr = new CallDetailRecord("a", "b", "c", "d", 5, "f", "g", "h", "i", "j",
                CallStatus.SUCCESS, 12, 13);
        assertEquals("a,b,c,d,5,f,g,h,i,j,1,12,13", cdr.toLine());
    }
}
