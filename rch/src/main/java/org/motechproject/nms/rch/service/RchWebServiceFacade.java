package org.motechproject.nms.rch.service;

import org.joda.time.LocalDate;
import org.motechproject.event.MotechEvent;
import org.motechproject.nms.rch.domain.RchUserType;
import org.motechproject.nms.rch.exception.RchFileManipulationException;

import java.io.IOException;
import java.net.URL;

/**
 * Created by beehyvsc on 1/6/17.
 */
public interface RchWebServiceFacade {
    boolean getMothersData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    boolean getDistrictData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    boolean getTalukasData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    boolean getVillagesData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    boolean getHealthBlockData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    boolean getTalukaHealthBlockData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    boolean getHealthFacilityData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    boolean getHealthSubFacilityData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    boolean getVillageHealthSubFacilityData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    boolean getChildrenData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    boolean getAnmAshaData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    void readAshaResponseFromFile(MotechEvent event) throws RchFileManipulationException;

    void readMotherResponseFromFile(MotechEvent event) throws RchFileManipulationException;

    void readDistrictResponseFromFile(MotechEvent event) throws RchFileManipulationException;

    void readTalukaResponseFromFile(MotechEvent event) throws RchFileManipulationException;

    void readVillageResponseFromFile(MotechEvent event) throws RchFileManipulationException;

    void readChildResponseFromFile(MotechEvent event) throws RchFileManipulationException;

    void readHealthBlockResponseFromFile(MotechEvent event) throws RchFileManipulationException;

    void readTalukaHealthBlockResponseFromFile(MotechEvent event) throws RchFileManipulationException;

    void readHealthFacilityResponseFromFile(MotechEvent event) throws RchFileManipulationException;

    void readHealthSubFacilityResponseFromFile(MotechEvent event) throws RchFileManipulationException;

    void readVillageHealthSubFacilityResponseFromFile(MotechEvent event) throws RchFileManipulationException;

    void locationUpdateInTable(Long stateId, RchUserType rchUserType);

    void locationUpdateInTableFromCsv(Long stateId, RchUserType rchUserType) throws IOException;

    String getBeneficiaryLocationUpdateDirectory();

    String getLocationFilesDirectory();
}
