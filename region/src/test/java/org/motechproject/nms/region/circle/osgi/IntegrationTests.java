package org.motechproject.nms.region.circle.osgi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.motechproject.nms.region.circle.osgi.CircleServiceBundleIT;

/**
 * HelloWorld bundle integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        CircleServiceBundleIT.class
})
public class IntegrationTests {
}
