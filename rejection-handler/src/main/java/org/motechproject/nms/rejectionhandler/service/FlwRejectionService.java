package org.motechproject.nms.rejectionhandler.service;

import org.motechproject.nms.rejectionhandler.domain.FlwImportRejection;

/**
 * Created by vishnu on 15/7/17.
 */
public interface FlwRejectionService {

    FlwImportRejection findByFlwIdAndStateId(Long flwId, Long stateId);

    void createUpdate(FlwImportRejection flwImportRejection);

}
