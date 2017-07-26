package org.motechproject.nms.rch.service;

import org.motechproject.nms.rch.domain.RchImportFacilitator;
import org.motechproject.nms.rch.exception.RchFileManipulationException;

/**
 * Created by vishnu on 24/7/17.
 */
public interface RchImportFacilitatorService {

    void createImportFileAudit(RchImportFacilitator rchImportFacilitator) throws RchFileManipulationException;
}
