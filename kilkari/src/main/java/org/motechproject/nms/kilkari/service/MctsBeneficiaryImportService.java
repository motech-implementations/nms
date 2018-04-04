package org.motechproject.nms.kilkari.service;

import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

/**
 * Service interface for importing Kilkari subscribers from MCTS/RCH
 */
public interface MctsBeneficiaryImportService {

    int importMotherData(Reader reader, SubscriptionOrigin origin) throws IOException;

    boolean importMotherRecord(Map<String, Object> record, SubscriptionOrigin origin);

    List<Map<String, Object>> importChildRecord(Map<String, Object> record, SubscriptionOrigin origin, List<Map<String, Object>> rejectedRecords);

    boolean validateReferenceDate(DateTime referenceDate, SubscriptionPackType packType, Long msisdn, String beneficiaryId, SubscriptionOrigin importOrigin);

    void createOrUpdateRejections(Map<String, Object> rejectedRecords, Map<String, Object> rejectionStatus);

    }
