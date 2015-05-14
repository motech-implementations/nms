package org.motechproject.nms.props.ut;

import org.junit.Test;
import org.motechproject.nms.props.domain.RequestId;

import static junit.framework.Assert.assertEquals;

public class RequestIdUnitTest {

    @Test
    public void testFromString() {
        RequestId requestId = RequestId.fromString("626de970-f770-11e4-a322-1697f925ec7b:filename");

        assertEquals("626de970-f770-11e4-a322-1697f925ec7b", requestId.getSubscriptionId());
        assertEquals("filename", requestId.getFileName());
    }

    @Test
    public void testToString() {
        RequestId requestId = new RequestId("c2f29976-f770-11e4-a322-1697f925ec7b", "filename");

        assertEquals("c2f29976-f770-11e4-a322-1697f925ec7b:filename", requestId.toString());
    }
}
