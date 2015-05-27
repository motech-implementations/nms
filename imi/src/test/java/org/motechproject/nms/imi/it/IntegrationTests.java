package org.motechproject.nms.imi.it;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ImiController_CDR_BundleIT.class,
        ImiController_OBD_BundleIT.class,
        TargetFileServiceBundleIT.class,
        CdrFileServiceBundleIT.class,
        CsrValidatorServiceBundleIT.class,
})
public class IntegrationTests {
}
