package org.motechproject.nms.imi.service;

import org.motechproject.event.MotechEvent;
import org.motechproject.nms.imi.web.contract.FileInfo;

import java.io.File;
import java.util.List;

/**
 *
 */
public interface CdrFileService {

    enum Action {
        PASS1, // checksum, record count, valid csv
        PASS2, // PASS1 + sort order, entities (subscription, circle, etc...) exist
        PASS3  // PASS1 + aggregate CDRS into CSR and send for distributed processing
    }


    /**
     * Internal method used to verify the given call detail record file entities or send aggregated detail
     * records for processing as CallSummaryRecordDto in MOTECH events
     *
     * NOTE: only exposed here for ITs
     *
     * @param file          the actual file to process
     * @param fileInfo      file information provided about the file (ie: expected checksum & recordCount)
     * @return              a list of errors (if any)
     */
    List<String> iterateDetailFile(File file, FileInfo fileInfo, Action action);


    /**
     * Verify file exists, verify checksum & record count match. Then sends event to proceed to CDR processing
     * phase 2
     */
    List<String> verifyDetailFileChecksumAndCount(FileInfo fileInfo);


    /**
     * Aggregates multiple detail records provided my IMI into one summary record for each call in a given day.
     * Then sends a MOTECH PROCESS_SUMMARY_RECORD event for each summary record such that the summary record
     * process is distributed among all MOTECH nodes.
     *
     * NOTE: only exposed here for ITs. Normally called by the MOTECH event system (it's a @MotechListener)
     */
    List<String> processDetailFile(MotechEvent event);
}
