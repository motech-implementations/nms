package org.motechproject.nms.imi.service;

import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;

import java.util.List;
import java.util.Map;

public interface CsrValidatorService {
    /**
     * Validates the provided call summary records, collecting errors on the go
     *
     * NOTE: directly used in IT only
     *
     * @param records a list of CallSummaryRecords
     * @return a list of errors or an empty list
     */
    List<String> validateSummaryRecords(Map<String, CallSummaryRecordDto> records);


    /**
     * Validates a call summary record. Throws InvalidCallSummaryRecord exception if something goes wrong.

     * NOTE: directly used in IT only
     *
     * @param record
     */
    void validateSummaryRecord(CallSummaryRecordDto record);
}
