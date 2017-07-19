package org.motechproject.nms.rejectionhandler.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.rejectionhandler.domain.FlwImportRejection;

/**
 * Created by vishnu on 14/7/17.
 */
public interface FlwImportRejectionDataService extends MotechDataService<FlwImportRejection> {

    @Lookup
    FlwImportRejection findByFlwIdAndStateId(@LookupField(name = "flwId") Long flwId,
                                   @LookupField(name = "stateId") Long stateId);


}
