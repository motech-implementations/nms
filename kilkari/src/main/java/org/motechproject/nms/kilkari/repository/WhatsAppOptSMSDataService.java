package org.motechproject.nms.kilkari.repository;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.kilkari.domain.WhatsAppOptSMS;

import java.util.Date;
import java.util.List;

public interface WhatsAppOptSMSDataService extends MotechDataService<WhatsAppOptSMS> {
    @Lookup
    WhatsAppOptSMS findByRequestId(@LookupField(name = "requestId") String requestId);

}
