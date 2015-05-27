package org.motechproject.nms.testing.it;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.motechproject.nms.testing.it.kilkari.CsrServiceBundleIT;
import org.motechproject.nms.testing.it.kilkari.SubscriberServiceBundleIT;
import org.motechproject.nms.testing.it.kilkari.SubscriptionServiceBundleIT;

/**
 * Testing module integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SubscriptionServiceBundleIT.class,
    SubscriberServiceBundleIT.class,
    CsrServiceBundleIT.class
})
public class KilkariIntegrationTests {
}
