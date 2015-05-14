package org.motechproject.nms.flw.osgi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * HelloWorld bundle integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ServiceUsageServiceBundleIT.class,
        ServiceUsageCapServiceBundleIT.class,
        WhiteListServiceBundleIT.class,
        FrontLineWorkerServiceBundleIT.class
})
public class FrontLineWorkerIntegrationTests {
}
