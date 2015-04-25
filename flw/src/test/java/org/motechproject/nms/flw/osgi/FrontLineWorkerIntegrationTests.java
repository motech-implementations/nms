package org.motechproject.nms.flw.osgi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * HelloWorld bundle integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        FrontLineWorkerServiceBundleIT.class,
        ServiceUsageServiceBundleIT.class,
        ServiceUsageCapServiceBundleIT.class,
        WhiteListServiceBundleIT.class
})
public class FrontLineWorkerIntegrationTests {
}
