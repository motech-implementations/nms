package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;

/**
 * Loads database domain values in memory and uses it to quickly verify CSRDTOs
 */
public interface CsrVerifierService {
    void verify(CallSummaryRecordDto csrDto);
}
