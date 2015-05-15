package org.motechproject.nms.kilkari.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.kilkari.domain.CallDetailRecord;

import java.util.List;

public interface CallDetailRecordDataService extends MotechDataService<CallDetailRecord> {

    @Lookup
    List<CallDetailRecord> findByMsisdn(@LookupField(name = "msisdn") Long msisdn);

}
