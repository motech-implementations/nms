package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * Service interface for importing Kilkari subscribers from MCTS/RCH
 */
public interface MctsBeneficiaryImportService {

    int importMotherData(Reader reader, SubscriptionOrigin origin) throws IOException;

    int importChildData(Reader reader, SubscriptionOrigin origin) throws IOException;

    boolean importMotherRecord(Map<String, Object> record, SubscriptionOrigin origin);

    boolean importChildRecord(Map<String, Object> record, SubscriptionOrigin origin);
}
