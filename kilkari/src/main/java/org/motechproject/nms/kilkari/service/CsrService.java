package org.motechproject.nms.kilkari.service;

import org.motechproject.event.MotechEvent;

public interface CsrService {

    // IT only

    void processCallSummaryRecord(MotechEvent event);
    void buildMessageDurationCache();
}
