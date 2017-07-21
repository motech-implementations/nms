package org.motechproject.nms.rch.service.impl;

import org.motechproject.nms.rch.domain.RchImportFacilitator;
import org.motechproject.nms.rch.exception.RchFileManipulationException;
import org.motechproject.nms.rch.repository.RchImportFacilitatorDataService;
import org.motechproject.nms.rch.service.RchImportFacilitatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by beehyvsc on 21/7/17.
 */
@Service("rchImportFacilitatorService")
public class RchImportFacilitatorServiceImpl implements RchImportFacilitatorService  {
    @Autowired
    private RchImportFacilitatorDataService rchImportFacilitatorDataService;

    @Override
    public void createImportFileAudit(RchImportFacilitator rchImportFacilitator) throws RchFileManipulationException {
        if (rchImportFacilitator.getFileName() == null) {
            throw new RchFileManipulationException("Invalid file name");
        }
        rchImportFacilitatorDataService.create(rchImportFacilitator);
    }
}
