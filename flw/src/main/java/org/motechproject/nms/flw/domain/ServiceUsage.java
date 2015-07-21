package org.motechproject.nms.flw.domain;

import org.motechproject.nms.props.domain.Service;

public class ServiceUsage {

    private FrontLineWorker frontLineWorker;

    private Service service;

    private int usageInPulses;

    private int endOfUsage;

    private boolean welcomePrompt;


    public ServiceUsage(FrontLineWorker frontLineWorker, Service service, int usageInPulses, int endOfUsage,
                        boolean welcomePrompt) {
        this.frontLineWorker = frontLineWorker;
        this.service = service;
        this.usageInPulses = usageInPulses;
        this.endOfUsage = endOfUsage;
        this.welcomePrompt = welcomePrompt;
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

    public boolean getWelcomePrompt() {
        return welcomePrompt;
    }

    public void setWelcomePrompt(boolean welcomePrompt) {
        this.welcomePrompt = welcomePrompt;
    }
}
