package org.motechproject.nms.kilkari.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.kilkari.domain.BlockedMsisdnRecord;

public interface BlockedMsisdnRecordDataService extends MotechDataService<BlockedMsisdnRecord> {

    @Lookup
    BlockedMsisdnRecord findByNumber(@LookupField(name = "callingNumber") Long callingNumber);
}
