package org.motechproject.nms.testing.tracking.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackField;

@Entity
@TrackClass
@InstanceLifecycleListeners
public class TrackedEntity {

    @Field
    private Long id;

    @Field
    private Integer integer;

    @Field
    private String string;

    @Field
    private TrackedEntity instance;

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

    @TrackField
    public void setString(String string) {
        this.string = string;
    }

    public TrackedEntity getInstance() {
        return instance;
    }

    @TrackField
    public void setInstance(TrackedEntity instance) {
        this.instance = instance;
    }
}
