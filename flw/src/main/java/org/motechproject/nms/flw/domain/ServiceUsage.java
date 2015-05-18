package org.motechproject.nms.flw.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.props.domain.Service;

@Entity(tableName = "nms_service_usage")
public class ServiceUsage {

    @Field
    private FrontLineWorker frontLineWorker;

    @Field
    private Service service;

    @Field
    private int usageInPulses;

    @Field
    private int endOfUsage;

    @Field
    private int welcomePrompt;

    @Field
    private DateTime timestamp;


    public ServiceUsage(FrontLineWorker frontLineWorker, Service service, int usageInPulses, int endOfUsage,
                        int welcomePrompt, DateTime timestamp) {
        this.frontLineWorker = frontLineWorker;
        this.service = service;
        this.usageInPulses = usageInPulses;
        this.endOfUsage = endOfUsage;
        this.welcomePrompt = welcomePrompt;
        this.timestamp = timestamp;
    }

    public FrontLineWorker getFrontLineWorker() {
        return frontLineWorker;
    }

    public void setFrontLineWorker(FrontLineWorker frontLineWorker) {
        this.frontLineWorker = frontLineWorker;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public int getUsageInPulses() {
        return usageInPulses;
    }

    public void setUsageInPulses(int usageInPulses) {
        this.usageInPulses = usageInPulses;
    }

    public int getEndOfUsage() {
        return endOfUsage;
    }

    public void setEndOfUsage(int endOfUsage) {
        this.endOfUsage = endOfUsage;
    }

    public int getWelcomePrompt() {
        return welcomePrompt;
    }

    public void setWelcomePrompt(int welcomePrompt) {
        this.welcomePrompt = welcomePrompt;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }
}
