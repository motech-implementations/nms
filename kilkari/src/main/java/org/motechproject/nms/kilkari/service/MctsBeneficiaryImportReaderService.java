package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;

import java.io.IOException;
import java.io.Reader;

public interface MctsBeneficiaryImportReaderService {

    int importChildData(Reader reader, SubscriptionOrigin origin) throws IOException;
}
