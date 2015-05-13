package org.motechproject.nms.mobileacademy.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.mobileacademy.domain.CompletionRecord;

/**
 * data interface to create and update completion record for course
 */
public interface CompletionRecordDataService extends MotechDataService<CompletionRecord> {

    @Lookup
    CompletionRecord findRecordByCallingNumber(@LookupField(name = "callingNumber") Long callingNumber);
}
