package org.motechproject.nms.flw.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.location.domain.State;

import javax.jdo.annotations.Unique;

/**
 * Domain class responsible for holding the capping values for MA and MK
 *
 * National capping value will be stored using a 'null' state.
 */
@Entity
@Unique(name = "UNIQUE_STATE_SERVICE_COMPOSITE_IDX", members = { "state", "service" })
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

    public void setService(Service service) {
        this.service = service;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getMaxUsageInPulses() {
        return maxUsageInPulses;
    }

    public void setMaxUsageInPulses(int maxUsageInPulses) {
        this.maxUsageInPulses = maxUsageInPulses;
    }
}
