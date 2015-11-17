package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.exception.CsrConversionException;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.CsrVerifierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads database domain values in memory on initialization and uses it to quickly verify CSRDTOs
 */
@Service("csrVerifierService")
public class CsrVerifierServiceImpl implements CsrVerifierService {


    // key: weekId, Value:contentFileName
    private Map<String, String> messages;

    // This will be be cached for the duration of the module's life
    @Autowired
    public CsrVerifierServiceImpl(SubscriptionPackDataService subscriptionPackDataService) {
        readDomainData(subscriptionPackDataService);
    }

    private void readDomainData(SubscriptionPackDataService subscriptionPackDataService) {
        messages = new HashMap<>();
        for (SubscriptionPack pack : subscriptionPackDataService.retrieveAll()) {
            for (SubscriptionPackMessage message : pack.getMessages()) {
                messages.put(message.getWeekId(), message.getMessageFileName());
            }
        }
    }

    private void verifyPackMessage(String weekId, String contentFileName) {
        if (weekId == null) {
            throw new CsrConversionException("Missing weekId");
        }
        if (contentFileName == null) {
            throw new CsrConversionException("Missing contentFileName");
        }
        String validContentFileName = messages.get(weekId);
        if (validContentFileName == null) {
            throw new CsrConversionException(String.format("Invalid weekId: %s", weekId));
        }
        if (!validContentFileName.equals(contentFileName)) {
            throw new CsrConversionException(String.format("Invalid messageContentFileName %s for weekId %s, the " +
                    "valid messageContentFileName is %s", contentFileName, weekId, validContentFileName));
        }
    }

    public void verify(CallSummaryRecordDto csrDto) {
        verifyPackMessage(csrDto.getWeekId(), csrDto.getContentFileName());
    }
}
