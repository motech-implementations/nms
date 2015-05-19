package org.motechproject.nms.region.service.impl;

import org.motechproject.nms.region.service.LoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("loggingService")
public class LoggingServiceImpl implements LoggingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingServiceImpl.class);

    @Override
    public void logError(String message) {
        //TODO: use alert
        LOGGER.error(message);
    }
}
