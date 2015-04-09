package org.motechproject.nms.language.osgi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * HelloWorld bundle integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        LanguageServiceBundleIT.class
})
public class IntegrationTests {
}
