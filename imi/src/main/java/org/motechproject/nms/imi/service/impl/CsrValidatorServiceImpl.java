package org.motechproject.nms.imi.service.impl;

import org.motechproject.nms.imi.exception.InvalidCsrException;
import org.motechproject.nms.imi.service.CsrValidatorService;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.LanguageLocation;
import org.motechproject.nms.region.service.CircleService;
import org.motechproject.nms.region.service.LanguageLocationService;
import org.motechproject.server.config.SettingsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("csrValidatorService")
public class CsrValidatorServiceImpl implements CsrValidatorService {

    private static final String MAX_CDR_ERROR_COUNT = "imi.max_cdr_error_count";
    private static final int MAX_CDR_ERROR_COUNT_DEFAULT = 100;

    private Map<String, Set<String>> weekIds;
    private Map<String, Set<String>> contentFileNames;
    private Set<String> languageLocationCodes;
    private Set<String> circles;

    private SettingsFacade settingsFacade;
    private SubscriptionService subscriptionService;
    private LanguageLocationService languageLocationService;
    private CircleService circleService;


    @Autowired
    public CsrValidatorServiceImpl(@Qualifier("imiSettings") SettingsFacade settingsFacade,
                                   SubscriptionService subscriptionService,
                                   LanguageLocationService languageLocationService,
                                   CircleService circleService) {
        this.settingsFacade = settingsFacade;
        this.subscriptionService = subscriptionService;
        this.languageLocationService = languageLocationService;
        this.circleService = circleService;
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
    }


    private void loadLanguageLocationCodes() {
        languageLocationCodes = new HashSet<String>();
        for (LanguageLocation languageLocation : languageLocationService.getAll()) {
            languageLocationCodes.add(languageLocation.getCode());
        }
    }


    private void loadCircles() {
        circles = new HashSet<String>();
        for (Circle circle : circleService.getAll()) {
            circles.add(circle.getName());
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
        if (languageLocationCodes == null) { loadLanguageLocationCodes(); }
        if (languageLocationCodes.contains(code)) {
            return;
        }
        throw new InvalidCsrException(String.format("Invalid language location code: %s", code));
    }


    //todo: IT
    @Override
    public void validateSummaryRecord(CallSummaryRecordDto record) {
        Subscription sub = subscriptionService.getSubscription(record.getRequestId().getSubscriptionId());
        if (sub == null) {
            throw new InvalidCsrException(String.format("Subscription %s does not exist in the database",
                    record.getRequestId().getSubscriptionId()));
        }

        String pack = sub.getSubscriptionPack().getName();
        validateWeekId(pack, record.getWeekId());
        validateContentFileName(pack, record.getContentFileName());
        validateCircle(record.getCircle());
        validateLanguageLocationCode(record.getLanguageLocationCode());
    }


    private int getMaxErrorCount() {
        try {
            return Integer.parseInt(settingsFacade.getProperty(MAX_CDR_ERROR_COUNT));
        } catch (NumberFormatException e) {
            return MAX_CDR_ERROR_COUNT_DEFAULT;
        }
    }


    //todo: IT
    @Override
    public List<String> validateSummaryRecords(Map<String, CallSummaryRecordDto> records) {
        int maxErrorCount = getMaxErrorCount();
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        Iterator it = records.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            try {
                validateSummaryRecord((CallSummaryRecordDto) entry.getValue());
            } catch (InvalidCsrException e) {
                errors.add(e.getMessage());

            }
            if (errorCount >= maxErrorCount) {
                errors.add(String.format("The maximum number of allowed errors of %d has been reached, " +
                        "discarding all remaining errors.", maxErrorCount));
                break;
            }
        }

        return errors;
    }
}
