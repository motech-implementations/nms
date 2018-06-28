package org.motechproject.nms.api.web.service;

import org.motechproject.nms.rch.domain.RchUserType;
import java.io.IOException;


/**
 * Created by beehyv on 19/6/18.
 */
public interface BeneficiaryUpdateService {

    void rchBulkUpdate(Long stateId, RchUserType rchUserType, String origin) throws IOException;

}
