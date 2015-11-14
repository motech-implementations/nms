package org.motechproject.nms.kilkari.service;

import org.motechproject.event.MotechEvent;

public interface CsrService {

    void deleteOldCallSummaryRecords(final int retentionInDays);

    // IT only

    void processCallSummaryRecord(MotechEvent event);
    void buildMessageDurationCache();
}
