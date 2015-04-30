package org.motechproject.nms.outbounddialer.it;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Outbound Dialer bundle integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        OutboundDialerControllerBundleIT.class,
        TargetFileServiceBundleIT.class,
        CdrFileServiceBundleIT.class,
})
public class IntegrationTests {
}
