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

    boolean importMotherRecord(Map<String, Object> record);

    boolean importChildRecord(Map<String, Object> record);
}
