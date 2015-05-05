package org.motechproject.nms.region.osgi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * HelloWorld bundle integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    LanguageServiceBundleIT.class,
    LocationServiceBundleIT.class,
    CircleServiceBundleIT.class
})
public class IntegrationTests {
}
