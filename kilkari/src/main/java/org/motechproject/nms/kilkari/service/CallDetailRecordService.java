package org.motechproject.nms.kilkari.service;

import org.motechproject.event.MotechEvent;

/**
 * Service interface for processing CallDetailRecords from the IVR provider
 */
public interface CallDetailRecordService {

    /**
     *
     * @param event
     */
    void processCallDetailRecord(MotechEvent event);

}
