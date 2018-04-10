package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by beehyv on 20/7/17.
 */
public interface ChildRejectionService {

    boolean createOrUpdateChild(ChildImportRejection childImportRejection);

    Map<String, Object> findChildRejectionByRchId(final Set<String> rchIds);

    Long bulkInsert(final List<ChildImportRejection> createObjects);
    Long bulkUpdate(final List<ChildImportRejection> updateObjects);
}
