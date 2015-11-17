package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.exception.InvalidCsrDataException;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.CsrVerifierService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.service.CircleService;
import org.motechproject.nms.region.service.LanguageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Loads database domain values in memory on initialization and uses it to quickly verify CSRDTOs
 */
@Service("csrVerifierService")
public class CsrVerifierServiceImpl implements CsrVerifierService {

    public static final String LANGUAGE_CACHE_EVICT_MESSAGE = "nms.region.cache.evict.language";

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrVerifierServiceImpl.class);
    private SubscriptionPackDataService subscriptionPackDataService;
    private CircleService circleService;
    private LanguageService languageService;

    // key: weekId, Value:contentFileName
    private Map<String, String> messages;
    private Set<String> circles;
    private Set<String> languages;

    // This will be be cached for the duration of the module's life
    @Autowired
    public CsrVerifierServiceImpl(SubscriptionPackDataService subscriptionPackDataService,
                                  CircleService circleService, LanguageService languageService) {
        this.subscriptionPackDataService = subscriptionPackDataService;
        this.circleService = circleService;
        this.languageService = languageService;
    }

    private void readDomainData() {

        messages = new HashMap<>();
        for (SubscriptionPack pack : subscriptionPackDataService.retrieveAll()) {
            for (SubscriptionPackMessage message : pack.getMessages()) {
                messages.put(message.getWeekId(), message.getMessageFileName());
            }
        }
        LOGGER.info("Loaded {} message weekId/fileName pairs from database.", messages.size());

        circles = new HashSet<>();
        for (Circle circle : circleService.getAll()) {
            circles.add(circle.getName());
        }
        LOGGER.info("Loaded {} circles from database.", circles.size());

        languages = new HashSet<>();
        for (Language language : languageService.getAll()) {
            languages.add(language.getCode());
        }
        LOGGER.info("Loaded {} languages from database.", languages.size());
    }

    private void verifyPackMessage(String weekId, String contentFileName) {
        if (messages == null) {
            readDomainData();
        }
        if (weekId == null) {
            throw new InvalidCsrDataException("Missing weekId");
        }
        if (contentFileName == null) {
            throw new InvalidCsrDataException("Missing contentFileName");
        }
        String validContentFileName = messages.get(weekId);
        if (validContentFileName == null) {
            throw new InvalidCsrDataException(String.format("Invalid weekId: %s", weekId));
        }
        if (!validContentFileName.equals(contentFileName)) {
            throw new InvalidCsrDataException(String.format("Invalid messageContentFileName %s for weekId %s, the " +
                    "valid messageContentFileName is %s", contentFileName, weekId, validContentFileName));
        }
    }

    private void verifyCircle(String circleName) {
        if (circles == null) {
            readDomainData();
        }
        if (circleName == null) {
            throw new InvalidCsrDataException("Missing circleName");
        }
        if (!circles.contains(circleName)) {
            throw new InvalidCsrDataException(String.format("Invalid circleName: %s", circleName));
        }
    }

    private void verifyLanguage(String languageCode) {
        if (languages == null) {
            readDomainData();
        }
        if (languageCode == null) {
            throw new InvalidCsrDataException("Missing languageCode");
        }
        if (!languages.contains(languageCode)) {
            throw new InvalidCsrDataException(String.format("Invalid languageCode: %s", languageCode));
        }
    }

    public void cacheEvict() {
        messages = null;
    }

    @MotechListener(subjects = { LANGUAGE_CACHE_EVICT_MESSAGE })
    public void cacheEvict(MotechEvent event) {
        cacheEvict();
    }

    public void verify(CallSummaryRecordDto csrDto) {
        verifyPackMessage(csrDto.getWeekId(), csrDto.getContentFileName());
        verifyCircle(csrDto.getCircleName());
        verifyLanguage(csrDto.getLanguageCode());
    }
}
