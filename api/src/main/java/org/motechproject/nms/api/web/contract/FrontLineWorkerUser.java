package org.motechproject.nms.api.web.contract;

/**
 *
 */
public class FrontLineWorkerUser extends ResponseUser {
    private long currentUsageInPulses;
    private long endOfUsagePromptCounter;
    private boolean welcomePromptFlag;
    private long maxAllowedUsageInPulses;
    private long maxAllowedEndOfUsagePrompt;

    public FrontLineWorkerUser() {
        super();
    }

    public long getCurrentUsageInPulses() {
        return currentUsageInPulses;
    }

    public void setCurrentUsageInPulses(long currentUsageInPulses) {
        this.currentUsageInPulses = currentUsageInPulses;
    }

    public long getEndOfUsagePromptCounter() {
        return endOfUsagePromptCounter;
    }

    public void setEndOfUsagePromptCounter(long endOfUsagePromptCounter) {
        this.endOfUsagePromptCounter = endOfUsagePromptCounter;
    }

    public boolean isWelcomePromptFlag() {
        return welcomePromptFlag;
    }

    public void setWelcomePromptFlag(boolean welcomePromptFlag) {
        this.welcomePromptFlag = welcomePromptFlag;
    }

    public long getMaxAllowedUsageInPulses() {
        return maxAllowedUsageInPulses;
    }

    public void setMaxAllowedUsageInPulses(long maxAllowedUsageInPulses) {
        this.maxAllowedUsageInPulses = maxAllowedUsageInPulses;
    }

    public long getMaxAllowedEndOfUsagePrompt() {
        return maxAllowedEndOfUsagePrompt;
    }

    public void setMaxAllowedEndOfUsagePrompt(long maxAllowedEndOfUsagePrompt) {
        this.maxAllowedEndOfUsagePrompt = maxAllowedEndOfUsagePrompt;
    }
}
