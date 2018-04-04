package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by beehyv on 20/7/17.
 */
public interface ChildRejectionService {

//    ChildImportRejection findByChildId(String idNo, String registrationNo);

    boolean createOrUpdateChild(ChildImportRejection childImportRejection);

    /**
     * Get the list of ChildImportRejection due for a message on the specified day. Used by the Kilkari outbound dialer to
     * create the list of message recipients for a given day.
     * @param rchIds Set of rch registration ids to get the child rejects

     * @return The list of subscriptions due for a message
     */
    Map<String, Object> findChildRejectionByRchId(final Set<String> rchIds);

    Long bulkInsert(final List<ChildImportRejection> createObjects);
    Long bulkUpdate(final List<ChildImportRejection> updateObjects);
}
