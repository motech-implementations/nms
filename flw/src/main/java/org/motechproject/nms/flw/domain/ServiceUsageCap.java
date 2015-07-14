package org.motechproject.nms.flw.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.tracking.annotation.TrackClass;
import org.motechproject.nms.tracking.annotation.TrackField;

import javax.jdo.annotations.Unique;

/**
 * Domain class responsible for holding the capping values for MA and MK
 *
 * National capping value will be stored using a 'null' state.
 */
@Entity(tableName = "nms_service_usage_caps")
@Unique(name = "UNIQUE_STATE_SERVICE_COMPOSITE_IDX", members = { "state", "service" })
@TrackClass
@InstanceLifecycleListeners
public class ServiceUsageCap {

    @Field
    private Service service;

    @Field
    private State state;

    @Field
    private int maxUsageInPulses;

    public ServiceUsageCap(State state, Service service, int maxUsageInPulses) {
        this.service = service;
        this.state = state;
        this.maxUsageInPulses = maxUsageInPulses;
    }

    public Service getService() {
        return service;
    }

    @TrackField
    public void setService(Service service) {
        this.service = service;
    }

    public State getState() {
        return state;
    }

    @TrackField
    public void setState(State state) {
        this.state = state;
    }

    public int getMaxUsageInPulses() {
        return maxUsageInPulses;
    }

    @TrackField
    public void setMaxUsageInPulses(int maxUsageInPulses) {
        this.maxUsageInPulses = maxUsageInPulses;
    }
}
