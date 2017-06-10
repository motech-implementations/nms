package org.motechproject.nms.rch.service;

import org.joda.time.LocalDate;
import org.motechproject.event.MotechEvent;

import java.net.URL;
import java.util.List;

/**
 * Service responsible for importing data from RCH web service
 */
public interface RchWsImportService {
    /**
     * Ops hook to restart RCH import for the day
     */
    void startRchImport();

    /**
     * Executes the import from the MCTS service.
     * @param stateIds ids of states for which data should get imported
     * @param referenceDate the date for which we are importing
     * @param endpoint the url of the web service endpoint, null will mean using the default one from WSDL
     */
    void importFromRch(List<Long> stateIds, LocalDate referenceDate, URL endpoint);

    // TEST HOOK ONLY. Do not call directly in production.
    void importRchMothersData(MotechEvent motechEvent);
    void importRchChildrenData(MotechEvent motechEvent);
}
