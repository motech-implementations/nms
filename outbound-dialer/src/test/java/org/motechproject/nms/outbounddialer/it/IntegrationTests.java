package org.motechproject.nms.outbounddialer.it;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Outbound Dialer bundle integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        OutboundDialerControllerBundleIT.class,
        OutboundDialerServiceBundleIT.class,
})
public class IntegrationTests {
}
