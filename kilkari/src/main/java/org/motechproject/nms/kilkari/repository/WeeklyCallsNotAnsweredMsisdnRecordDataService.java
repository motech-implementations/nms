package org.motechproject.nms.kilkari.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.kilkari.domain.WeeklyCallsNotAnsweredMsisdnRecord;

public interface WeeklyCallsNotAnsweredMsisdnRecordDataService extends MotechDataService<WeeklyCallsNotAnsweredMsisdnRecord> {

    @Lookup
    WeeklyCallsNotAnsweredMsisdnRecord findByNumber(@LookupField(name = "callingNumber") Long callingNumber);
}
