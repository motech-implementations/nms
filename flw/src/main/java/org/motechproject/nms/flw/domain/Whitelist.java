package org.motechproject.nms.flw.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.region.location.domain.State;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;

@Entity
public class Whitelist {
    @Field
    @NotNull
    @Unique
    @Column(allowsNull = "false")
    private State state;

}
