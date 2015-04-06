package org.motechproject.nms.kilkari.osgi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * HelloWorld bundle integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        HelloWorldServiceBundleIT.class,
        HelloWorldWebBundleIT.class,
        HelloWorldRecordServiceBundleIT.class
})
public class HelloWorldIntegrationTests {
}
