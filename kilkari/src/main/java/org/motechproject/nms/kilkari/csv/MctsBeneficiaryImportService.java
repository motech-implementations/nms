package org.motechproject.nms.kilkari.csv;

import java.io.IOException;
import java.io.Reader;

/**
 * Service interface for importing Kilkari subscribers from MCTS
 */
public interface MctsBeneficiaryImportService {

    void importMotherData(Reader reader) throws IOException;

    void importChildData(Reader reader) throws IOException;
}
