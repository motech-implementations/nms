package org.motechproject.nms.mcts.service;

import org.joda.time.LocalDate;
import org.motechproject.event.MotechEvent;

import java.net.URL;
import java.util.List;

/**
 * Service responsible for importing data from the MCTS web service.
 */
public interface MctsWsImportService {

    /**
     * Ops hook to restart Mcts import for the day
     */
    void startMctsImport();

    /**
     * Executes the import from the MCTS service.
     * @param stateIds ids of states for which data should get imported
     * @param referenceDate the date for which we are importing
     * @param endpoint the url of the web service endpoint, null will mean using the default one from WSDL
     */
    void importFromMcts(List<Long> stateIds, LocalDate referenceDate, URL endpoint);

    // TEST HOOK ONLY. Do not call directly in production. AAAAAHHHHH
    void importMothersData(MotechEvent motechEvent);
    void importChildrenData(MotechEvent motechEvent);
    void importAnmAshaData(MotechEvent motechEvent);

}
