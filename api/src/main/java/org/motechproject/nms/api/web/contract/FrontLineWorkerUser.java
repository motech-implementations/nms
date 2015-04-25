package org.motechproject.nms.api.web.contract;

/**
 *
 */
public class FrontLineWorkerUser extends ResponseUser {
    private long currentUsageInPulses;
    private long endOfUsagePromptCounter;
    private boolean welcomePromptFlag;
    private int maxAllowedUsageInPulses;
    private int maxAllowedEndOfUsagePrompt;

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

    public int getMaxAllowedUsageInPulses() {
        return maxAllowedUsageInPulses;
    }

    public void setMaxAllowedUsageInPulses(int maxAllowedUsageInPulses) {
        this.maxAllowedUsageInPulses = maxAllowedUsageInPulses;
    }

    public int getMaxAllowedEndOfUsagePrompt() {
        return maxAllowedEndOfUsagePrompt;
    }

    public void setMaxAllowedEndOfUsagePrompt(int maxAllowedEndOfUsagePrompt) {
        this.maxAllowedEndOfUsagePrompt = maxAllowedEndOfUsagePrompt;
    }
}
