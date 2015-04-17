package org.motechproject.nms.api.osgi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * API bundle integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        UserControllerBundleIT.class,
        KilkariControllerBundleIT.class
})
public class IntegrationTests {
}
