package org.motechproject.nms.outbounddialer.service;

import org.motechproject.nms.outbounddialer.web.contract.CdrFileNotificationRequest;
import org.motechproject.nms.outbounddialer.web.contract.FileProcessedStatusRequest;

/**
 *
 */
public interface OutboundDialerService {

    void generateTargetFile();

    void processCdrFile(CdrFileNotificationRequest request);

    void handleFileProcessedStatusNotification(FileProcessedStatusRequest request);
}
