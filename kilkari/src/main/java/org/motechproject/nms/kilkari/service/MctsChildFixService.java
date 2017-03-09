package org.motechproject.nms.kilkari.service;

import org.motechproject.nms.kilkari.domain.MctsChild;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

/**
 * Service interface for updating mother for every child from the one time MCTS dump
 */
public interface MctsChildFixService {

    void updateMotherChild(Reader reader) throws IOException;

    void updateSubscriber(Map<String, Object> record, List<MctsChild> childRecords);

    }
