package org.motechproject.nms.props.ut;

import org.junit.Test;
import org.motechproject.nms.props.domain.RequestId;

import static junit.framework.Assert.assertEquals;

public class RequestIdUnitTest {

    @Test
    public void testFromString() {
        RequestId requestId = RequestId.fromString("20150515153503:626de970-f770-11e4-a322-1697f925ec7b");

        assertEquals("626de970-f770-11e4-a322-1697f925ec7b", requestId.getSubscriptionId());
        assertEquals("20150515153503", requestId.getTimestamp());
    }


    @Test
    public void testToString() {
        RequestId requestId = new RequestId("c2f29976-f770-11e4-a322-1697f925ec7b", "20150515153503");

        assertEquals("20150515153503:c2f29976-f770-11e4-a322-1697f925ec7b", requestId.toString());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFromString() {
        RequestId requestId = RequestId.fromString("xxx");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCtorInvalidSubscriptionId() {
        RequestId requestId = new RequestId("xxx", "20150515153503");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testCtorInvalidTimestamp() {
        RequestId requestId = new RequestId("c2f29976-f770-11e4-a322-1697f925ec7b", "xxx");
    }
}
