package org.motechproject.nms.outbounddialer.service.impl;

import org.motechproject.nms.outbounddialer.service.CdrFileService;
import org.motechproject.nms.outbounddialer.web.contract.CdrFileNotificationRequest;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


/**
 * Implementation of the {@link CdrFileService} interface.
 */
@Service("cdrFileService")
public class CdrFileServiceImpl implements CdrFileService {

    private static final String CDR_FILE_DIRECTORY = "outbound-dialer.cdr_file_directory";

    private SettingsFacade settingsFacade;
    private static final Logger LOGGER = LoggerFactory.getLogger(CdrFileServiceImpl.class);


    @Autowired
    public CdrFileServiceImpl(@Qualifier("outboundDialerSettings") SettingsFacade settingsFacade) {
        this.settingsFacade = settingsFacade;
    }


    @Override
    public void processCdrFile(CdrFileNotificationRequest request) {
        final String cdrFileLocation = settingsFacade.getProperty(CDR_FILE_DIRECTORY);
        LOGGER.debug("Processing CDR file {} located in {}", "???", cdrFileLocation);

        //todo:...
    }
}
