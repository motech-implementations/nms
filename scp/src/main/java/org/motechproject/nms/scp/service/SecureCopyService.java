package org.motechproject.nms.scp.service;

import org.motechproject.nms.scp.service.exception.SecureCopyException;

/**
 *
 */
public interface SecureCopyService {
    void copyFrom(String user, String host, String identityFile, String remoteSource, String localDestination)
            throws SecureCopyException;
    void copyTo(String user, String host, String identityFile, String localSource,
                String remoteDestination) throws SecureCopyException;
}
