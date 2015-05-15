package org.motechproject.nms.region.osgi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * HelloWorld bundle integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    LocationServiceBundleIT.class,
    CircleServiceBundleIT.class,
    NationalDefaultLanguageLocationBundleIT.class
})
public class IntegrationTests {
}
