package org.motechproject.nms.imi.it;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ImiControllerBundleIT.class,
        TargetFileServiceBundleIT.class,
        CdrFileServiceBundleIT.class,
})
public class IntegrationTests {
}
