package org.motechproject.nms.kilkari.service;

import org.motechproject.event.MotechEvent;

public interface CsrService {

    /**
     * Deletes all call summary records older than the given number of days
     *
     * @param retentionInDays Number of days of summary records to retain in the database
     *
     */
    void deleteOldCallSummaryRecords(final int retentionInDays);

    // IT only

    void processCallSummaryRecord(MotechEvent event);
}
