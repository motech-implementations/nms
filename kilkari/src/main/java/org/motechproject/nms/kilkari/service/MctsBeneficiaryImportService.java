package org.motechproject.nms.kilkari.service;

import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.region.domain.LocationFinder;
import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;
import org.motechproject.nms.rejectionhandler.domain.MotherImportRejection;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.List;
import java.util.Map;

/**
 * Service interface for importing Kilkari subscribers from MCTS/RCH
 */
public interface MctsBeneficiaryImportService {

    MotherImportRejection importMotherRecord(Map<String, Object> record, SubscriptionOrigin origin, LocationFinder locationFinder);

    ChildImportRejection importChildRecord(Map<String, Object> record, SubscriptionOrigin origin, LocationFinder locationFinder);

    List<List<Map<String, Object>>> splitRecords(List<Map<String, Object>> recordList, String contactNumber);

    boolean validateReferenceDate(DateTime referenceDate, SubscriptionPackType packType, Long msisdn, String beneficiaryId, SubscriptionOrigin importOrigin);

    void createOrUpdateRchChildRejections(Map<String, Object> rejectedRecords, Map<String, Object> rejectionStatus);

    void createOrUpdateMctsChildRejections(Map<String, Object> rejectedRecords, Map<String, Object> rejectionStatus);

    @Transactional
    void createOrUpdateRchMotherRejections(Map<String, Object> rejectedRecords, Map<String, Object> rejectionStatus);

    @Transactional
    void createOrUpdateMctsMotherRejections(Map<String, Object> rejectedRecords, Map<String, Object> rejectionStatus);

    Map<String, CellProcessor> getMotherProcessorMapping();

    Map<String, CellProcessor> getRchMotherProcessorMapping();
}
