package org.motechproject.nms.imi.service.impl;

import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.nms.imi.domain.CallSummaryRecord;
import org.motechproject.nms.imi.exception.InvalidCsrException;
import org.motechproject.nms.imi.service.CsrValidatorService;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.RequestId;
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

@Service("csrValidatorService")
public class CsrValidatorServiceImpl implements CsrValidatorService {

    private Map<String, Set<String>> weekIds;
    private Map<String, Set<String>> contentFileNames;
    private Set<String> languageCodes;
    private Set<String> circles;

    private SubscriptionService subscriptionService;
    private LanguageService languageService;
    private CircleService circleService;
    private AlertService alertService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrValidatorServiceImpl.class);


    @Autowired
    public CsrValidatorServiceImpl(SubscriptionService subscriptionService,
                                   LanguageService languageService,
                                   CircleService circleService,
                                   AlertService alertService) {
        this.subscriptionService = subscriptionService;
        this.languageService = languageService;
        this.circleService = circleService;
        this.alertService = alertService;
    }


    private void loadPacks() {
        weekIds = new HashMap<>();
        contentFileNames = new HashMap<>();
        for (SubscriptionPack pack : subscriptionService.getSubscriptionPacks()) {
            Set<String> packWeekIds = new HashSet<String>();
            Set<String> packContentFileNames = new HashSet<String>();
            for (SubscriptionPackMessage message :pack.getMessages()) {
                packWeekIds.add(message.getWeekId());
                packContentFileNames.add(message.getMessageFileName());
            }
            weekIds.put(pack.getName(), packWeekIds);
            contentFileNames.put(pack.getName(), packContentFileNames);
        }
        if (weekIds.size() == 0) {
            LOGGER.debug("No subscription packs in the database!");
            weekIds = null;
            contentFileNames = null;
        }
    }


    private void loadLanguageLocationCodes() {
        languageCodes = new HashSet<String>();
        for (Language language : languageService.getAll()) {
            languageCodes.add(language.getCode());
        }
        if (languageCodes.size() == 0) {
            LOGGER.debug("No languages in the database!");
            languageCodes = null;
        }
    }


    private void loadCircles() {
        circles = new HashSet<String>();
        for (Circle circle : circleService.getAll()) {
            circles.add(circle.getName());
        }
        if (circles.size() == 0) {
            LOGGER.debug("No circles in the database!");
            circles = null;
        }
    }


    private void validateWeekId(String pack, String week) {
        if (weekIds == null) { loadPacks(); }
        if (weekIds.containsKey(pack)) {
            if (weekIds.get(pack).contains(week)) {
                return;
            }
            throw new InvalidCsrException(String.format("Invalid weekId for %s pack: %s", pack, week));
        }
        throw new IllegalStateException(String.format("Unexpected pack name: %s", pack));
    }


    private void validateContentFileName(String pack, String fileName) {
        if (contentFileNames == null) { loadPacks(); }
        if (contentFileNames.containsKey(pack)) {
            if (contentFileNames.get(pack).contains(fileName)) {
                return;
            }
            throw new InvalidCsrException(String.format("Invalid contentFileName for %s pack: %s", pack, fileName));
        }
        throw new IllegalStateException(String.format("Unexpected pack name: %s", pack));
    }


    private void validateCircle(String name) {
        if (circles == null) { loadCircles(); }
        if (circles.contains(name)) {
            return;
        }
        throw new InvalidCsrException(String.format("Invalid circle: %s", name));
    }


    private void validateLanguageLocationCode(String code) {
        if (languageCodes == null) { loadLanguageLocationCodes(); }
        if (languageCodes.contains(code)) {
            return;
        }
        throw new InvalidCsrException(String.format("Invalid language location code: %s", code));
    }


    private Subscription validateSubscription(String id) {
        Subscription sub = subscriptionService.getSubscription(id);
        if (sub == null) {
            String error = String.format("Subscription %s does not exist in the database", id);
            LOGGER.warn(error);
            alertService.create(String.format("Subscription"), "CSR Validation", error, AlertType.HIGH, AlertStatus.NEW, 0, null);
        }
        return sub;
    }


    //todo: IT
    @Override
    public void validateSummaryRecord(CallSummaryRecordDto record) {
        Subscription sub = validateSubscription(record.getRequestId().getSubscriptionId());
        if (sub == null) {
            // This is a messed up subscription, we logged & alerted it, let's just not validate anything more
            return;
        }
        String pack = sub.getSubscriptionPack().getName();
        validateWeekId(pack, record.getWeekId());
        validateContentFileName(pack, record.getContentFileName());
        validateCircle(record.getCircle());
        validateLanguageLocationCode(record.getLanguageLocationCode());
    }


    //todo: IT
    @Override
    public void validateSummaryRecord(CallSummaryRecord record) {
        RequestId requestId = RequestId.fromString(record.getRequestId());
        Subscription sub = validateSubscription(requestId.getSubscriptionId());
        if (sub == null) {
            // This is a messed up subscription, we logged & alerted it, let's just not validate anything more
            return;
        }
        String pack = sub.getSubscriptionPack().getName();
        validateWeekId(pack, record.getWeekId());
        validateContentFileName(pack, record.getContentFileName());
        validateCircle(record.getCircle());
        validateLanguageLocationCode(record.getLanguageLocationCode());
    }
}
