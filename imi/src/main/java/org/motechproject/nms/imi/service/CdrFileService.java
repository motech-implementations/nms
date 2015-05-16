package org.motechproject.nms.imi.service;

import org.motechproject.nms.imi.service.contract.ProcessResult;
import org.motechproject.nms.imi.web.contract.FileInfo;

/**
 *
 */
public interface CdrFileService {

    /**
     * The controller's cdrFileNotification http endpoint was invoked by the IVR system, start processing the
     * CDR Detail file provided in CdrFileNotificationRequest.cdrDetail, we ignore the CDR Summary file
     *
     * @param fileInfo
     */
    ProcessResult processDetailFile(FileInfo fileInfo);
}
