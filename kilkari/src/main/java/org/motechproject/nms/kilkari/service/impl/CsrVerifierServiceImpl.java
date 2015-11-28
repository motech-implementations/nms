package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.exception.InvalidCallRecordDataException;
import org.motechproject.nms.kilkari.service.CsrVerifierService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.service.CircleService;
import org.motechproject.nms.region.service.LanguageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Loads database domain values in memory on initialization and uses it to quickly verify CSRDTOs
 */
@Service("csrVerifierService")
public class CsrVerifierServiceImpl implements CsrVerifierService {

    public static final String LANGUAGE_CACHE_EVICT_MESSAGE = "nms.region.cache.evict.language";
    public static final String READ_DOMAIN_DATA = "nms.kk.csr_verifier.read_domain_data";

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrVerifierServiceImpl.class);
    private CircleService circleService;
    private LanguageService languageService;

    private Set<String> circles;
    private Set<String> languages;

    @Autowired
    public CsrVerifierServiceImpl(CircleService circleService, LanguageService languageService) {
        this.circleService = circleService;
        this.languageService = languageService;
    }

    private synchronized void readDomainData() {
        if (circles == null || languages == null) {
            Set<String> circleSet = new HashSet<>();
            for (Circle circle : circleService.getAll()) {
                circleSet.add(circle.getName());
            }
            LOGGER.info("Loaded {} circles from database.", circleSet.size());

            Set<String> languageSet = new HashSet<>();
            for (Language language : languageService.getAll()) {
                languageSet.add(language.getCode());
            }
            LOGGER.info("Loaded {} languages from database.", languageSet.size());

            circles = circleSet;
            languages = languageSet;
        }
    }

    private void verifyCircle(String circleName) {
        readDomainData();

        if (circleName == null) {
            throw new InvalidCallRecordDataException("Missing circleName");
        }
        if (!circles.contains(circleName)) {
            throw new InvalidCallRecordDataException(String.format("Invalid circleName: %s", circleName));
        }
    }

    private void verifyLanguage(String languageCode) {
        readDomainData();

        if (languageCode == null) {
            throw new InvalidCallRecordDataException("Missing languageCode");
        }
        if (!languages.contains(languageCode)) {
            throw new InvalidCallRecordDataException(String.format("Invalid languageCode: %s", languageCode));
        }
    }

    private void verifyContentFile(String contentFileName) {
        if (contentFileName == null || contentFileName.isEmpty()) {
            throw new InvalidCallRecordDataException("Missing contentFileName");
        }
    }

    private void verifyWeekId(String weekId) {
        if (weekId == null || weekId.isEmpty()) {
            throw new InvalidCallRecordDataException("Missing weekId");
        }
    }

    public void cacheEvict() {
        circles = null;
        languages = null;
    }

    @MotechListener(subjects = { LANGUAGE_CACHE_EVICT_MESSAGE })
    public void cacheEvict(MotechEvent event) {
        cacheEvict();
    }

    @MotechListener(subjects = { READ_DOMAIN_DATA })
    public void readDomainData(MotechEvent event) {
        LOGGER.info("readDomainData");
        readDomainData();
    }

    public void verify(CallSummaryRecordDto csrDto) {
        verifyCircle(csrDto.getCircleName());
        verifyLanguage(csrDto.getLanguageCode());
        verifyContentFile(csrDto.getContentFileName());
        verifyWeekId(csrDto.getWeekId());
    }
}
