package org.motechproject.nms.kilkari.service;

import java.io.IOException;
import java.io.Reader;

/**
 * Service interface for updating MCTS-imported Kilkari subscribers
 */
public interface MctsBeneficiaryUpdateService {

    void updateBeneficiaryData(Reader reader) throws IOException;

}
