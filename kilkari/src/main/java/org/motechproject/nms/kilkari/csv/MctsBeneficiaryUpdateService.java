package org.motechproject.nms.kilkari.csv;

import java.io.IOException;
import java.io.Reader;

/**
 * Service interface for updating MCTS-imported Kilkari subscribers
 */
public interface MctsBeneficiaryUpdateService {

    void updateBeneficiary(Reader reader) throws IOException;

}
