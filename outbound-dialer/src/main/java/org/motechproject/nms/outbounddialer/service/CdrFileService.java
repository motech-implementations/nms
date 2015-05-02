package org.motechproject.nms.outbounddialer.service;

import org.motechproject.nms.outbounddialer.web.contract.CdrFileNotificationRequest;

/**
 *
 */
public interface CdrFileService {

    /**
     * The controller's cdrFileNotification http endpoint was invoked by the IVR system, start processing the provided
     * CDR files
     *
     * @param request
     */
    void processCdrFile(CdrFileNotificationRequest request);
}
