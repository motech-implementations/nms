package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.contract.ChildRecord;
import org.motechproject.nms.kilkari.contract.MotherRecord;
import org.motechproject.nms.kilkari.contract.RchMotherRecord;
import org.motechproject.nms.kilkari.service.ActionFinderService;
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
    private MctsMotherDataService mctsMotherDataService;

    @Autowired
    private MctsChildDataService mctsChildDataService;

    @Override
    public String motherActionFinder(MotherRecord record) {
        if (record.getIdNo() == null || record.getIdNo().isEmpty() || "".equals(record.getIdNo())) {
            return create;
        }
        if (mctsMotherDataService.findByBeneficiaryId(record.getIdNo()) == null) {
            return create;
        } else {
            return update;
        }
    }

    @Override
    public String rchMotherActionFinder(RchMotherRecord record) {
        if (record.getRegistrationNo() == null || record.getRegistrationNo().isEmpty() || "".equals(record.getRegistrationNo())) {
           return create;
        }
        if (mctsMotherDataService.findByRchId(record.getRegistrationNo()) == null) {
            return create;
        } else {
            return update;
        }
    }

    @Override
    public String childActionFinder(ChildRecord record) {
        if (record.getIdNo() == null || record.getIdNo().isEmpty() || "".equals(record.getIdNo())) {
            return create;
        }
        if (mctsChildDataService.findByBeneficiaryId(record.getIdNo()) == null) {
            return create;
        } else {
            return update;
        }
    }
}
