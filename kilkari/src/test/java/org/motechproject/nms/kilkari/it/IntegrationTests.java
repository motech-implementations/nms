package org.motechproject.nms.kilkari.it;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Kilkari module integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        SubscriptionServiceBundleIT.class, CsrServiceBundleIT.class
})
public class IntegrationTests {
}
