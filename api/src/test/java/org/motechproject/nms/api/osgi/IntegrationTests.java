package org.motechproject.nms.api.osgi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * API bundle integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        LanguageControllerBundleIT.class,
        UserControllerBundleIT.class,
        CallDetailsControllerBundleIT.class,
        KilkariControllerBundleIT.class,
        MobileAcademyControllerBundleIT.class
})
public class IntegrationTests {
}
