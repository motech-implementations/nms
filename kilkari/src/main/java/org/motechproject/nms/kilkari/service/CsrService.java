package org.motechproject.nms.kilkari.service;

import org.motechproject.event.MotechEvent;

public interface CsrService {

    // IT only

    void processCallDetailRecord(MotechEvent event);
    void buildMessageDurationCache();
}
