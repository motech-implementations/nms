package org.motechproject.nms.imi.ut;

import org.junit.Test;
import org.motechproject.nms.imi.service.RequestId;

import static junit.framework.Assert.assertEquals;

public class RequestIdTest {

    @Test
    public void testFromString() {
        RequestId requestId = RequestId.fromString(
                "626de970-f770-11e4-a322-1697f925ec7b:626dee34-f770-11e4-a322-1697f925ec7b");

        assertEquals("626de970-f770-11e4-a322-1697f925ec7b", requestId.getFileIdentifier());
        assertEquals("626dee34-f770-11e4-a322-1697f925ec7b", requestId.getSubscriptionId());
    }

    @Test
    public void testToString() {
        RequestId requestId = new RequestId("c2f29976-f770-11e4-a322-1697f925ec7b",
                "c2f29bec-f770-11e4-a322-1697f925ec7b");

        assertEquals("c2f29976-f770-11e4-a322-1697f925ec7b:c2f29bec-f770-11e4-a322-1697f925ec7b",
                requestId.toString());
    }
}
