package org.motechproject.nms.imi.service;

import org.motechproject.nms.imi.service.contract.ParseResults;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;

public interface CsrValidatorService {
    /**
     * Validates the provided call summary records, collecting errors on the go
     *
     * NOTE: directly used in IT only
     *
     * @param results the provided list of CSRs (read) and errors (write)
     * @return true if no errors, false otherwise
     */
    boolean validateCallSummaryRecords(ParseResults results);


    /**
     * Validates a call summary record. Throws InvalidCallSummaryRecord exception if something goes wrong.

     * NOTE: directly used in IT only
     *
     * @param record
     */
    void validateSummaryRecord(CallSummaryRecordDto record);
}
