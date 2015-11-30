package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.exception.InvalidCallRecordDataException;
import org.motechproject.nms.kilkari.service.CsrVerifierService;
import org.motechproject.nms.region.service.CircleService;
import org.motechproject.nms.region.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Loads database domain values in memory on initialization and uses it to quickly verify CSRDTOs
 */
@Service("csrVerifierService")
public class CsrVerifierServiceImpl implements CsrVerifierService {

    private static final String CSR_VERIFIER_CACHE_EVICT_MESSAGE = "nms.kk.cache.evict.csv_verifier";
    private static final String CIRCLE_CACHE_EVICT_MESSAGE = "nms.region.cache.evict.language";
    private static final String LANGUAGE_CACHE_EVICT_MESSAGE = "nms.region.cache.evict.language";

    private CircleService circleService;
    private LanguageService languageService;
    private EventRelay eventRelay;

    @Autowired
    public CsrVerifierServiceImpl(CircleService circleService, LanguageService languageService, EventRelay eventRelay) {
        this.circleService = circleService;
        this.languageService = languageService;
        this.eventRelay = eventRelay;
    }

    private void verifyCircle(String circleName) {
        if (circleName == null) {
            throw new InvalidCallRecordDataException("Missing circleName");
        }
        if (circleService.getByName(circleName) == null) {
            throw new InvalidCallRecordDataException(String.format("Invalid circleName: %s", circleName));
        }
    }

    private void verifyLanguage(String languageCode) {
        if (languageCode == null) {
            throw new InvalidCallRecordDataException("Missing languageCode");
        }
        if (languageService.getForCode(languageCode) == null) {
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
        eventRelay.broadcastEventMessage(new MotechEvent(CIRCLE_CACHE_EVICT_MESSAGE));
        eventRelay.broadcastEventMessage(new MotechEvent(LANGUAGE_CACHE_EVICT_MESSAGE));
    }

    @MotechListener(subjects = { CSR_VERIFIER_CACHE_EVICT_MESSAGE })
    public void cacheEvict(MotechEvent event) {
        cacheEvict();
    }

    public void verify(CallSummaryRecordDto csrDto) {
        verifyCircle(csrDto.getCircleName());
        verifyLanguage(csrDto.getLanguageCode());
        verifyContentFile(csrDto.getContentFileName());
        verifyWeekId(csrDto.getWeekId());
    }
}
