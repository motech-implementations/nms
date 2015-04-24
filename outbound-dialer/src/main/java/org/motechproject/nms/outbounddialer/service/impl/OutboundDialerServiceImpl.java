package org.motechproject.nms.outbounddialer.service.impl;

import org.motechproject.nms.outbounddialer.service.OutboundDialerService;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link OutboundDialerService} interface.
 */
@Service("outboundDialerService")
public class OutboundDialerServiceImpl implements OutboundDialerService {

    @Override
    public void handleNewCdrFile() {
        //TODO: download the files from the specified locations and validate their checksums

        //TODO: post a message to begin processing the files

    }

    @Override
    public void handleFileProcessedStatusNotification() {

    }


}
