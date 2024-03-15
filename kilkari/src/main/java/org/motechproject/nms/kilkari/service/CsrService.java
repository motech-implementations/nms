package org.motechproject.nms.kilkari.service;

import org.motechproject.event.MotechEvent;

public interface CsrService {

    void processCallSummaryRecord(MotechEvent event);

    void processWhatsAppSMSCsr(MotechEvent event);

    void processWhatsAppCsr(MotechEvent event);

}
