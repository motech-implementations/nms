package org.motechproject.nms.language.osgi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.location.service.LocationService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

/**
 * Verify that LanguageService is present & functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class StateServiceBundleIT extends BasePaxIT {

    @Inject
    private LocationService locationService;

    private void setupData() {
    }

    @Test
    public void testServicePresent() throws Exception {
        assertNotNull(locationService);
    }

}
