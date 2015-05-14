package org.motechproject.nms.kilkari.osgi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Kilkari module integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        SubscriptionServiceBundleIT.class, CallDetailRecordServiceBundleIT.class
})
public class IntegrationTests {
}
