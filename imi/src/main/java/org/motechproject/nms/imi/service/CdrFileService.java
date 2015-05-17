package org.motechproject.nms.imi.service;

import org.motechproject.nms.imi.service.contract.ParseResults;
import org.motechproject.nms.imi.web.contract.FileInfo;

/**
 *
 */
public interface CdrFileService {

    ParseResults parseDetailFile(FileInfo fileInfo);
    void processDetailFile(FileInfo fileInfo);
}
