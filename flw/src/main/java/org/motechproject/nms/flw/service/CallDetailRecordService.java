package org.motechproject.nms.flw.service;

import org.motechproject.nms.flw.domain.CallDetailRecord;

public interface CallDetailRecordService {
    void add(CallDetailRecord callDetailRecord);

    CallDetailRecord getByCallingNumber(long callingNumber);

    void update(CallDetailRecord record);

    void delete(CallDetailRecord record);
}
