package org.motechproject.nms.testing.tracking.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.mds.domain.MdsEntity;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackField;

@Entity
//TODO UPGRADE
@TrackClass
@InstanceLifecycleListeners
public class TrackedSimple extends MdsEntity {

    @Field
    private Long id;

    @Field
    private Integer integer;

    @Field
    @TrackField
    private String string;

    @Field
    @TrackField
    private TrackedSimple instance;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getInteger() {
        return integer;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public TrackedSimple getInstance() {
        return instance;
    }

    public void setInstance(TrackedSimple instance) {
        this.instance = instance;
    }
}
