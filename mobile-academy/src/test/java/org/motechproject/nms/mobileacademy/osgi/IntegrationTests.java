package org.motechproject.nms.mobileacademy.osgi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * HelloWorld bundle integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        MobileAcademyServiceBundleIT.class,
})
public class IntegrationTests {
}
