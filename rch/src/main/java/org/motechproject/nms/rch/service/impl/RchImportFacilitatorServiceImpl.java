package org.motechproject.nms.rch.service.impl;

import org.joda.time.LocalDate;
import org.motechproject.nms.rch.domain.RchImportFacilitator;
import org.motechproject.nms.rch.domain.RchUserType;
import org.motechproject.nms.rch.exception.RchFileManipulationException;
import org.motechproject.nms.rch.repository.RchImportFacilitatorDataService;
import org.motechproject.nms.rch.service.RchImportFacilitatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("rchImportFacilitatorService")
public class RchImportFacilitatorServiceImpl implements RchImportFacilitatorService {

    private RchImportFacilitatorDataService rchImportFacilitatorDataService;


    @Autowired
    public RchImportFacilitatorServiceImpl(RchImportFacilitatorDataService rchImportFacilitatorDataService) {
        this.rchImportFacilitatorDataService = rchImportFacilitatorDataService;
    }

    @Override
    public void createImportFileAudit(RchImportFacilitator rchImportFacilitator) throws RchFileManipulationException  {
        if (rchImportFacilitator.getFileName() == null) {
            throw new RchFileManipulationException("Invalid file name");
        }
        rchImportFacilitatorDataService.create(rchImportFacilitator);
    }

    @Override
    public List<RchImportFacilitator> findByImportDateAndRchUserType(LocalDate importDate, RchUserType rchUserType) {
        return rchImportFacilitatorDataService.getByImportDateAndUsertype(importDate, rchUserType);
    }
}

