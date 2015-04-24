package org.motechproject.nms.outbounddialer.service;

/**
 *
 */
public interface OutboundDialerService {

    void handleNewCdrFile();

    void handleFileProcessedStatusNotification();
}
