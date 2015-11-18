package org.motechproject.nms.kilkari.service.impl;

import org.apache.commons.lang.StringUtils;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.exception.InvalidCallRecordDataException;
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

    // key: pack name, value:hash of pack messages: key:weekId, value:contentFileName
    private Map<String, Map<String, String>> packs;
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

        int messageCount = 0;
        packs = new HashMap<>();
        for (SubscriptionPack pack : subscriptionPackDataService.retrieveAll()) {
            Map<String, String> messages = new HashMap<>();
            for (SubscriptionPackMessage message : pack.getMessages()) {
                messages.put(message.getWeekId(), message.getMessageFileName());
                messageCount++;
            }
            packs.put(pack.getName(), messages);
        }
        LOGGER.info("Loaded {} message weekId/fileName pairs from {} packs from database.", messageCount, packs.size());

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
        if (packs == null) {
            readDomainData();
        }
        if (weekId == null) {
            throw new InvalidCallRecordDataException("Missing weekId");
        }
        if (contentFileName == null) {
            throw new InvalidCallRecordDataException("Missing contentFileName");
        }
        Set<String> validContentFileNames = new HashSet<>();
        for (Map<String, String> messages : packs.values()) {
            if (messages.containsKey(weekId)) {
                validContentFileNames.add(messages.get(weekId));
            }
        }
        if (validContentFileNames.size() == 0) {
            throw new InvalidCallRecordDataException(String.format("Invalid weekId: %s", weekId));
        }
        if (!validContentFileNames.contains(contentFileName)) {
            throw new InvalidCallRecordDataException(String.format("Invalid messageContentFileName %s for weekId %s, " +
                    " valid messageContentFileNames are %s", contentFileName, weekId,
                    StringUtils.join(validContentFileNames, ", ")));
        }
    }

    private void verifyCircle(String circleName) {
        if (circles == null) {
            readDomainData();
        }
        if (circleName == null) {
            throw new InvalidCallRecordDataException("Missing circleName");
        }
        if (!circles.contains(circleName)) {
            throw new InvalidCallRecordDataException(String.format("Invalid circleName: %s", circleName));
        }
    }

    private void verifyLanguage(String languageCode) {
        if (languages == null) {
            readDomainData();
        }
        if (languageCode == null) {
            throw new InvalidCallRecordDataException("Missing languageCode");
        }
        if (!languages.contains(languageCode)) {
            throw new InvalidCallRecordDataException(String.format("Invalid languageCode: %s", languageCode));
        }
    }

    public void cacheEvict() {
        packs = null;
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
