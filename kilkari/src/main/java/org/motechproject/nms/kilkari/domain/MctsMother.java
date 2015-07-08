package org.motechproject.nms.kilkari.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

@Entity(tableName = "nms_mcts_mothers")
public class MctsMother extends MctsBeneficiary {

    @Field
    private DateTime dateOfBirth; // this field is needed to calculate mother's age for some Kilkari reports

    public MctsMother(String beneficiaryId) {
        super(beneficiaryId);
    }

    public DateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(DateTime dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
