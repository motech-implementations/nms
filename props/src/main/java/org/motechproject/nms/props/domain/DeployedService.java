package org.motechproject.nms.props.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackFields;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;

@Entity(tableName = "nms_deployed_services")
@Unique(name = "UNIQUE_STATE_SERVICE_COMPOSITE_IDX", members = { "state", "service" })
@TrackClass
@TrackFields
@InstanceLifecycleListeners
public class DeployedService {
    @Field
    @NotNull
    @Column(allowsNull = "false")
    private State state;

    @Field
    @NotNull
    @Column(allowsNull = "false")
    private Service service;

    public DeployedService(State state, Service service) {
        this.state = state;
        this.service = service;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
