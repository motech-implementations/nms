package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.MotherImportRejection;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by beehyv on 17/7/17.
 */
public interface MotherRejectionService {

    MotherImportRejection findByMotherId(String idNo, String registrationNo);

    void createOrUpdateMother(MotherImportRejection motherImportRejection);

    Map<String, Object> findMotherRejectionByRchId(Set<String> rchIds);

    Map<String, Object> findMotherRejectionByMctsId(Set<String> mctsIds);

    Long mctsBulkInsert(final List<MotherImportRejection> createObjects);
    Long mctsBulkUpdate(final List<MotherImportRejection> updateObjects);


    Long rchBulkInsert(final List<MotherImportRejection> createObjects);
    Long rchBulkUpdate(final List<MotherImportRejection> updateObjects);
}
