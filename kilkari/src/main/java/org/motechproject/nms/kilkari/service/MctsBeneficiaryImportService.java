package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.BeneficiaryImportOrigin;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * Service interface for importing Kilkari subscribers from MCTS
 */
public interface MctsBeneficiaryImportService {

    int importMotherData(Reader reader, BeneficiaryImportOrigin beneficiaryImportOrigin) throws IOException;

    int importChildData(Reader reader, BeneficiaryImportOrigin beneficiaryImportOrigin) throws IOException;

    boolean importMotherRecord(Map<String, Object> record, BeneficiaryImportOrigin beneficiaryImportOrigin);

    boolean importChildRecord(Map<String, Object> record, BeneficiaryImportOrigin beneficiaryImportOrigin);
}
