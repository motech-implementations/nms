package org.motechproject.nms.mcts.service;

import org.joda.time.LocalDate;
import org.motechproject.nms.kilkari.contract.ChildRecord;
import org.motechproject.nms.kilkari.contract.MotherRecord;
import org.motechproject.nms.mcts.contract.AnmAshaDataSet;
import org.motechproject.nms.mcts.contract.ChildrenDataSet;
import org.motechproject.nms.mcts.contract.MothersDataSet;
import org.motechproject.nms.mcts.domain.MctsUserType;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public interface MctsWebServiceFacade {

    MothersDataSet getMothersData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    ChildrenDataSet getChildrenData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    AnmAshaDataSet getAnmAshaData(LocalDate from, LocalDate to, URL endpoint, Long stateId);

    void locationUpdateInTableFromCsv(Long stateId, MctsUserType mctsUserType) throws IOException;

    String getBeneficiaryLocationUpdateDirectory();

    void toMapLocMother(Map<String, Object> map, MotherRecord motherRecord);

    void toMapLocChild(Map<String, Object> map, ChildRecord childRecord);
}
