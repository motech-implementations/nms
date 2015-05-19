package org.motechproject.nms.imi.service;

import org.motechproject.event.MotechEvent;
import org.motechproject.nms.imi.service.contract.VerifyResults;
import org.motechproject.nms.imi.web.contract.FileInfo;

import java.io.File;
import java.util.List;

/**
 *
 */
public interface CdrFileService {

    /**
     * Internal method used to verify the given call detail record file (checksum, recordCount) and aggregate the
     * detail records into a memory map of CallSummaryDetailDto objects collecting errors on the way
     *
     * NOTE: only exposed here for ITs
     *
     * @param file          the actual file to process
     * @param fileInfo      file information provided about the file (ie: expected checksum & recordCount)
     * @param verifyOnly    if true, will not create summary memory map (ie: will only verify the file)
     * @return              a VerifyResult object containing the map (or an empty one) and a list of errors (if any)
     */
    VerifyResults aggregateDetailFile(File file, FileInfo fileInfo, boolean verifyOnly);


    /**
     * Verify file exists, verify checksum & record count match. Then sends event to proceed to CDR processing
     * phase 2
     */
    VerifyResults verifyDetailFile(FileInfo fileInfo);


    /**
     * Aggregates multiple detail records provided my IMI into one summary record for each call in a given day.
     * Then sends a MOTECH PROCESS_SUMMARY_RECORD event for each summary record such that the summary record
     * process is distributed among all MOTECH nodes.
     *
     * NOTE: only exposed here for ITs. Normally called by the MOTECH event system (it's a @MotechListener)
     */
    List<String> processDetailFile(MotechEvent event);
}
