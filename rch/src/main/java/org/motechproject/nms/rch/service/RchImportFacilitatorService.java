package org.motechproject.nms.rch.service;

import org.joda.time.LocalDate;
import org.motechproject.nms.rch.domain.RchImportFacilitator;
import org.motechproject.nms.rch.domain.RchUserType;
import org.motechproject.nms.rch.exception.RchFileManipulationException;

import java.util.List;


public interface RchImportFacilitatorService {

    void createImportFileAudit(RchImportFacilitator rchImportFacilitator) throws RchFileManipulationException;

    List<RchImportFacilitator> findByImportDateAndRchUserType(LocalDate importDate, RchUserType rchUserType);
}
