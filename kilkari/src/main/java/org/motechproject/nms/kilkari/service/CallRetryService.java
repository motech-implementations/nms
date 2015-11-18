package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.CallRetry;

import java.util.List;

public interface CallRetryService {
    /**
     * Get the list of all CallRetry records
     *
     * @param offset The row number at which to start returning results
     * @param max The maximum number of rows to return
     * @return The list of CallRetry records
     */
    List<CallRetry> retrieveAll(long offset, int max);


    /**
     * Deletes all call retry records older than the given number of days
     *
     * @param retentionInDays Number of days of call retry records to retain in the database
     *
     */
    void deleteOldRetryRecords(final int retentionInDays);
}
