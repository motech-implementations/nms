package org.motechproject.nms.kilkari.service;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * Service interface for importing Kilkari subscribers from MCTS
 */
public interface MctsBeneficiaryImportService {

    int importMotherData(Reader reader) throws IOException;

    int importChildData(Reader reader) throws IOException;

    void importMotherRecord(Map<String, Object> record);

    void importChildRecord(Map<String, Object> record);
}
