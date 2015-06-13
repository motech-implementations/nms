package org.motechproject.nms.kilkari.service;

import java.io.IOException;
import java.io.Reader;

/**
 * Service interface for updating MCTS-imported Kilkari subscribers
 */
public interface MctsBeneficiaryUpdateService {

    void updateMsisdn(Reader reader) throws IOException;

    void updateReferenceDate(Reader reader) throws IOException;

    void updateAddress(Reader reader) throws IOException;

}
