package org.motechproject.nms.kilkari.domain;


import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;


@Entity(tableName = "nms_mcts_children")
public class MctsChild extends MctsBeneficiary {

    @Field
    private String motherId;

    public MctsChild(String beneficiaryId, String name, String motherId) {
        super(beneficiaryId, name);
        this.motherId = motherId;
    }

    public String getMotherId() {
        return motherId;
    }

    public void setMotherId(String motherId) {
        this.motherId = motherId;
    }
}
