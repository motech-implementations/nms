package org.motechproject.nms.mcts.service;

import org.joda.time.LocalDate;
import org.motechproject.nms.mcts.contract.AnmAshaDataSet;
import org.motechproject.nms.mcts.contract.ChildrenDataSet;
import org.motechproject.nms.mcts.contract.MothersDataSet;
import org.motechproject.nms.mcts.domain.MctsUserType;

import java.io.IOException;
import java.net.URL;

public interface MctsWebServiceFacade {

    MothersDataSet getMothersData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    ChildrenDataSet getChildrenData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    AnmAshaDataSet getAnmAshaData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    void locationUpdateInTableFromCsv(Long stateId, MctsUserType mctsUserType) throws IOException;
}
