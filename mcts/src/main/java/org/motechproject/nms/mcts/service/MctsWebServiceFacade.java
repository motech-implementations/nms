package org.motechproject.nms.mcts.service;

import org.joda.time.LocalDate;
import org.motechproject.nms.mcts.contract.AnmAshaDataSet;
import org.motechproject.nms.mcts.contract.ChildrenDataSet;
import org.motechproject.nms.mcts.contract.MothersDataSet;

import java.net.URL;

public interface MctsWebServiceFacade {

    MothersDataSet getMothersData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    ChildrenDataSet getChildrenData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    AnmAshaDataSet getAnmAshaData(LocalDate from, LocalDate to, URL endpoint, Long stateId);
}
