package org.motechproject.nms.mcts.service;

import org.joda.time.LocalDate;

import java.net.URL;
import java.util.List;

/**
 * Service responsible for importing data from the MCTS web service.
 */
public interface MctsWsImportService {

    /**
     * Executes the import from the MCTS service.
     * @param stateIds ids of states for which data should get imported
     * @param referenceDate the date for which we are importing
     * @param endpoint the url of the web service endpoint, null will mean using the default one from WSDL
     */
    void importFromMcts(List<Long> stateIds, LocalDate referenceDate, URL endpoint);
}
