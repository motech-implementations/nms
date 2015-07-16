package org.motechproject.nms.imi.service;

import org.motechproject.event.MotechEvent;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.imi.web.contract.FileInfo;

import java.io.File;
import java.util.List;

/**
 *
 */
public interface CdrFileService {

    enum Action {
        PASS1, // record count, valid csv, checksum
        PASS2, // record count, valid csv + sort order, entities (subscription, circle, etc...) exist
        PASS3  // record count, valid csv + aggregate CDRS into CSR and send for distributed processing
    }


    /**
     * Verifies the checksum & record count provided in fileInfo match the checksum & record count of file
     * also verifies all csv rows are valid.
     *
     * @param fileInfo      file information provided about the file (ie: expected checksum & recordCount)
     *
     */
    List<String> verifyChecksumAndCountAndCsv(FileInfo fileInfo, Boolean isDetailFile);


    /**
     * Send aggregated detail records for processing as CallSummaryRecordDto in MOTECH events
     *
     * NOTE: only exposed here for ITs
     *
     * @param file      file to process
     * @return          a list of errors (failure) or an empty list (success)
     */
    List<String> sendAggregatedRecords(File file);


    /**
     * Send summary records for processing as CallSummaryRecordDto in MOTECH events
     *
     * NOTE: only exposed here for ITs
     *
     * @param file      file to process
     * @return          a list of errors (failure) or an empty list (success)
     */
    List<String> sendSummaryRecords(File file);


    /**
     * Verify file exists, verify checksum & record count match. Then sends event to proceed to CDR processing
     * phase 2
     */
    void verifyDetailFileChecksumAndCount(CdrFileNotificationRequest request);


    /**
     * Aggregates multiple detail records provided my IMI into one summary record for each call in a given day.
     * Then sends a MOTECH PROCESS_SUMMARY_RECORD event for each summary record such that the summary record
     * process is distributed among all MOTECH nodes.
     *
     * NOTE: only exposed here for ITs. Normally called by the MOTECH event system (it's a @MotechListener)
     */
    List<String> processDetailFile(MotechEvent event);
}
