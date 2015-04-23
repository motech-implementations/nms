package org.motechproject.nms.location.osgi;

import org.junit.Before;
import org.motechproject.nms.language.domain.CircleLanguage;
import org.motechproject.nms.language.domain.Language;
import org.motechproject.nms.language.repository.CircleLanguageDataService;
import org.motechproject.nms.language.repository.LanguageDataService;
import org.motechproject.nms.language.service.LanguageService;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import static org.junit.Assert.assertTrue;

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
    private CircleLanguageDataService circleLanguageDataService;

    private void setupData() {
        languageDataService.deleteAll();
        Language la = languageDataService.create(new Language("ladhaki", 10));
        Language ur = languageDataService.create(new Language("urdu", 11));
        Language hi = languageDataService.create(new Language("hindi", 12));
        Language ta = languageDataService.create(new Language("tamil", 13));

        circleLanguageDataService.deleteAll();
        circleLanguageDataService.create(new CircleLanguage("foo", la));
        circleLanguageDataService.create(new CircleLanguage("foo", ur));
        circleLanguageDataService.create(new CircleLanguage("foo", hi));
        circleLanguageDataService.create(new CircleLanguage("bar", ta));
    }

    @Test
    public void testServicePresent() throws Exception {
        assertNotNull(languageService);
    }

    @Test
    public void testServiceFunctional() throws Exception {
        setupData();
        List<Language> languages = languageService.getCircleLanguages("foo");

        Set<Integer> languageCodes = new HashSet<>();
        for (Language l : languages) {
            languageCodes.add(l.getCode());
        }

        assertEquals(languageCodes, new HashSet<Integer>(Arrays.asList(10, 11, 12)));

    }
}
