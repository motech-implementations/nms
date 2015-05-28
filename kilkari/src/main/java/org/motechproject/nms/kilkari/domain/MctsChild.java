package org.motechproject.nms.kilkari.domain;


import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

import javax.jdo.annotations.Column;
import javax.validation.constraints.NotNull;

@Entity(tableName = "nms_mcts_children")
public class MctsChild extends MctsBeneficiary {

    @Field
    String motherId;

    @Field
    @NotNull
    @Column(allowsNull = "false")
    DateTime dateOfBirth;

}
