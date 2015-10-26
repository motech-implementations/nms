package org.motechproject.nms.mcts.service;

import org.motechproject.event.MotechEvent;

/**
 *
 */
public interface MctsImportService {

    /**
     *
     * @param event
     */
    void handleImportEvent(MotechEvent event);
}
