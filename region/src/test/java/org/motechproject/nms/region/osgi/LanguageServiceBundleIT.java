package org.motechproject.nms.region.osgi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.region.language.domain.Language;
import org.motechproject.nms.region.language.domain.LanguageLocation;
import org.motechproject.nms.region.language.repository.LanguageDataService;
import org.motechproject.nms.region.language.repository.LanguageLocationDataService;
import org.motechproject.nms.region.language.service.LanguageService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Verify that LanguageService is present & functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class LanguageServiceBundleIT extends BasePaxIT {

    @Inject
    private LanguageService languageService;
    @Inject
    private LanguageDataService languageDataService;
    @Inject
    private LanguageLocationDataService languageLocationDataService;

    private void setupData() {
        languageLocationDataService.deleteAll();
        languageDataService.deleteAll();

        Language la = languageDataService.create(new Language("ladhaki", "10"));
        Language ur = languageDataService.create(new Language("urdu", "11"));
        Language hi = languageDataService.create(new Language("hindi", "12"));
        Language ta = languageDataService.create(new Language("tamil", "13"));

        languageLocationDataService.create(new LanguageLocation("foo", la));
        languageLocationDataService.create(new LanguageLocation("foo", ur));
        languageLocationDataService.create(new LanguageLocation("foo", hi));
        languageLocationDataService.create(new LanguageLocation("bar", ta));
    }

    @Test
    public void testServicePresent() throws Exception {
        assertNotNull(languageService);
    }

    @Test
    public void testServiceFunctional() throws Exception {
        setupData();
        List<Language> languages = languageService.getCircleLanguages("foo");

        Set<String> languageCodes = new HashSet<>();
        for (Language l : languages) {
            languageCodes.add(l.getCode());
        }

        assertEquals(languageCodes, new HashSet<String>(Arrays.asList("10", "11", "12")));

    }
}
