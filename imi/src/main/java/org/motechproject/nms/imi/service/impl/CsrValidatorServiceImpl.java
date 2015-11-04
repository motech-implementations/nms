package org.motechproject.nms.imi.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.metrics.service.Timer;
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
    private Map<String, String> subscriptions;

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


    public void evictCache() {
        weekIds = null;
        contentFileNames = null;
        languageCodes = null;
        circles = null;
        subscriptions = null;
    }


    private void loadPacks() {
        LOGGER.debug("loading Packs");
        weekIds = new HashMap<>();
        contentFileNames = new HashMap<>();
        Timer timer = new Timer();
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
        LOGGER.debug("Loaded {} packs in {}", weekIds.size(), timer.time());
        if (weekIds.size() == 0) {
            LOGGER.debug("No subscription packs in the database!");
            weekIds = null;
            contentFileNames = null;
        }
    }


    private void loadLanguageLocationCodes() {
        LOGGER.debug("loading LanguageLocationCodes");
        languageCodes = new HashSet<String>();
        Timer timer = new Timer();
        for (Language language : languageService.getAll()) {
            languageCodes.add(language.getCode());
        }
        LOGGER.debug("Loaded {} LanguageLocationCodes in {}", languageCodes.size(), timer.time());
        if (languageCodes.size() == 0) {
            LOGGER.debug("No languages in the database!");
            languageCodes = null;
        }
    }


    private void loadCircles() {
        LOGGER.debug("loading Circles");
        circles = new HashSet<String>();
        Timer timer = new Timer();
        for (Circle circle : circleService.getAll()) {
            circles.add(circle.getName());
        }
        LOGGER.debug("Loaded {} circles in {}", circles.size(), timer.time());
        if (circles.size() == 0) {
            LOGGER.debug("No circles in the database!");
            circles = null;
        }
    }


    private void loadSubscriptions() {
        LOGGER.debug("loading Subscriptions");
        subscriptions = new HashMap<String, String>();
        Timer timer = new Timer();
        for (Subscription subscription : subscriptionService.retrieveAll()) {
            subscriptions.put(subscription.getSubscriptionId(), subscription.getSubscriptionPack().getName());
        }
        LOGGER.debug("Loaded {} subscriptions in {}", subscriptions.size(), timer.time());
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


    /**
     * Makes sure the given subscription exists in the database
     * @param id - the subscription id
     * @return the pack name for the given subscription - a bit funky to return a pack name, but optimized for speed
     */
    private String validateSubscription(String id) {
        if (subscriptions == null) {
            loadSubscriptions();
        }
        if (subscriptions.containsKey(id)) {
            return subscriptions.get(id);
        }

        String error = String.format("Subscription %s does not exist in the database", id);
        LOGGER.warn(error);
        String externalID = "Subscription" + (StringUtils.isBlank(id) ? "" : " " + id);
        alertService.create(externalID, "CSR Validation", error, AlertType.HIGH, AlertStatus.NEW, 0, null);

        return null;
    }


    //todo: IT
    @Override
    public void validateSummaryRecordDto(CallSummaryRecordDto record) {
        String pack = validateSubscription(record.getRequestId().getSubscriptionId());
        if (pack == null) {
            // This is a messed up subscription, we logged & alerted it, let's just not validate anything more
            return;
        }
        validateWeekId(pack, record.getWeekId());
        validateContentFileName(pack, record.getContentFileName());
        validateCircle(record.getCircle());
        validateLanguageLocationCode(record.getLanguageLocationCode());
    }


    //todo: IT
    @Override
    public void validateSummaryRecord(CallSummaryRecord record) {
        RequestId requestId = RequestId.fromString(record.getRequestId());
        String pack = validateSubscription(requestId.getSubscriptionId());
        if (pack == null) {
            // This is a messed up subscription, we logged & alerted it, let's just not validate anything more
            return;
        }
        validateWeekId(pack, record.getWeekId());
        validateContentFileName(pack, record.getContentFileName());
        validateCircle(record.getCircle());
        validateLanguageLocationCode(record.getLanguageLocationCode());
    }
}
