package org.motechproject.nms.rejectionhandler.service.impl;

import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.mcts.contract.AnmAshaRecord;
import org.motechproject.nms.mcts.contract.ChildRecord;
import org.motechproject.nms.mcts.contract.MotherRecord;
import org.motechproject.nms.rch.contract.RchAnmAshaRecord;
import org.motechproject.nms.rch.contract.RchChildRecord;
import org.motechproject.nms.rch.contract.RchMotherRecord;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.rejectionhandler.service.ActionFinderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by beehyv on 25/7/17.
 */
@Service("actionFinderService")
public class ActionFinderServiceImpl implements ActionFinderService {

    private static String create = "CREATE";
    private static String update = "UPDATE";

    @Autowired
    private FrontLineWorkerService frontLineWorkerService;

    @Autowired
    private MctsMotherDataService mctsMotherDataService;

    @Autowired
    private MctsChildDataService mctsChildDataService;

    @Autowired
    private StateDataService stateDataService;

    @Override
    public String flwActionFinder(AnmAshaRecord record) {
        if(frontLineWorkerService.getByMctsFlwIdAndState(record.getId().toString(),stateDataService.findByCode(record.getStateId()))==null){
            return create;
        } else {
            return update;
        }
    }

    @Override
    public String RchFlwActionFinder(RchAnmAshaRecord record) {
        if(frontLineWorkerService.getByMctsFlwIdAndState(record.getGfId().toString(),stateDataService.findByCode(record.getStateId()))==null){
            return create;
        } else {
            return update;
        }
    }

    @Override
    public String MotherActionFinder(MotherRecord record) {
        if(mctsMotherDataService.findByBeneficiaryId(record.getIdNo())==null){
            return create;
        } else {
            return update;
        }
    }

    @Override
    public String RchMotherActionFinder(RchMotherRecord record) {
        if(mctsMotherDataService.findByRchId(record.getRegistrationNo())==null){
            return create;
        } else {
            return update;
        }
    }

    @Override
    public String ChildActionFinder(ChildRecord record) {
        if(mctsChildDataService.findByBeneficiaryId(record.getIdNo())==null){
            return create;
        } else{
            return update;
        }
    }

    @Override
    public String RchChildActionFinder(RchChildRecord record) {
        if(mctsChildDataService.findByRchId(record.getRegistrationNo())==null){
            return create;
        } else {
            return update;
        }
    }
}
