package org.motechproject.nms.kilkari.domain;

import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;

import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;

/**
 * A beneficiary (mother or child) sourced from MCTS.
 */
public abstract class MctsBeneficiary {

    // 18-digit IDs are used for most states but not all, so a strict length constraint cannot be set for this column
    @Field
    @Unique
    @NotNull
    String beneficiaryId;

    @Field
    String name;

    @Field
    State state;

    @Field
    District district;

    @Field
    Taluka taluka;

    @Field
    HealthBlock healthBlock;

    // TODO: PHC field
}
