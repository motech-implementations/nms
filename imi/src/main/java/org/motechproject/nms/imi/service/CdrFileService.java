package org.motechproject.nms.imi.service;

import org.motechproject.nms.imi.service.contract.CdrParseResult;
import org.motechproject.nms.imi.web.contract.CdrFileNotificationRequest;

/**
 *
 */
public interface CdrFileService {

    /**
     * The controller's cdrFileNotification http endpoint was invoked by the IVR system, start processing the
     * files provided in CdrFileNotificationRequest
     *
     * @param request
     */
    CdrParseResult processCdrFile(CdrFileNotificationRequest request);
}
