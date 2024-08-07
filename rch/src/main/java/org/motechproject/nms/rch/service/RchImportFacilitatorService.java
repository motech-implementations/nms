package org.motechproject.nms.rch.service;

import org.joda.time.LocalDate;
import org.motechproject.nms.rch.domain.RchImportFacilitator;
import org.motechproject.nms.rch.domain.RchUserType;
import org.motechproject.nms.rch.exception.RchFileManipulationException;

import java.util.List;


public interface RchImportFacilitatorService {

    void createImportFileAudit(RchImportFacilitator rchImportFacilitator) throws RchFileManipulationException;

    List<RchImportFacilitator> findByImportDateAndRchUserType(LocalDate importDate, RchUserType rchUserType);

    List<RchImportFacilitator> findByImportDateStateIdAndRchUserType(Long stateId, LocalDate importDate, RchUserType rchUserType);

    List<RchImportFacilitator> findByStateIdAndRchUserType(Long stateId, RchUserType rchUserType);

    void updateRchImportFacilatorAudit(LocalDate importDate, RchUserType rchUserType, Long stateId);
}
