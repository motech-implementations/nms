package org.motechproject.nms.imi.service;

import org.motechproject.nms.imi.service.contract.ParseResults;
import org.motechproject.nms.imi.web.contract.FileInfo;

/**
 *
 */
public interface CdrFileService {

    /**
     * Reads the call detail records file provided by IMI and creates an in-memory CallSummaryRecord map of aggregated
     * CDRs (there may be multiple tries to call a recipient in a single day) and a potential list of errors
     *
     * NOTE: this method is only directly called by ITs. It's called by dispatchSummaryRecords in production.
     *
     * @param fileInfo
     * @return a list of summary records and a (hopefully empty) list of errors
     */


    /**
     *
     * @param fileInfo
     * @return
     */
    ParseResults processDetailFile(FileInfo fileInfo);


    /**
     * Aggregates multiple detail records provided my IMI into one summary record for each call in a given day.
     * Then sends a MOTECH PROCESS_SUMMARY_RECORD event for each summary record such that the summary record process
     * is distributed among all MOTECH nodes.
     *
     * @param fileInfo
     */
    void dispatchSummaryRecords(FileInfo fileInfo);
}
