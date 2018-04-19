package org.motechproject.nms.kilkari.service;

import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.region.domain.LocationFinder;
import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * Service interface for importing Kilkari subscribers from MCTS/RCH
 */
public interface MctsBeneficiaryImportService {

    int importMotherData(Reader reader, SubscriptionOrigin origin) throws IOException;

    boolean importMotherRecord(Map<String, Object> record, SubscriptionOrigin origin);

    ChildImportRejection importChildRecord(Map<String, Object> record, SubscriptionOrigin origin);

    ChildImportRejection importChildRecordCSV(Map<String, Object> record, SubscriptionOrigin origin, LocationFinder locationFinder);

    boolean validateReferenceDate(DateTime referenceDate, SubscriptionPackType packType, Long msisdn, String beneficiaryId, SubscriptionOrigin importOrigin);

    void createOrUpdateRchRejections(Map<String, Object> rejectedRecords, Map<String, Object> rejectionStatus);

    void createOrUpdateMctsRejections(Map<String, Object> rejectedRecords, Map<String, Object> rejectionStatus);

    }
