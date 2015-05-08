package org.motechproject.nms.imi.service;

import org.motechproject.nms.imi.domain.CallDetailRecord;

public interface ReschedulerService {
    String RESCHEDULE_CALL = "nms.imi.reschedule_call";

    void sendRescheduleMessage(CallDetailRecord cdr);
}
