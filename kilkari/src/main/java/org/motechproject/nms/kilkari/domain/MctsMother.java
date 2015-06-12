package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Entity;

@Entity(tableName = "nms_mcts_mothers")
public class MctsMother extends MctsBeneficiary {

    public MctsMother(String beneficiaryId) {
        super(beneficiaryId);
    }
}
