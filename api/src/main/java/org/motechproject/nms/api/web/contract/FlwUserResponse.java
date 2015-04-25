package org.motechproject.nms.api.web.contract;

/**
 * Response body
 *
 * 2.2.1 Get User Details API
 * IVR shall invoke this API when to retrieve details specific to the user identified by callingNumber.
 * In case user specific details are not available in the database, the API will attempt to load system
 * defaults based on the operator and circle provided.
 * /api/mobileacademy/user?callingNumber=9999999900&operator=A&circle=AP&callId=123456789012345
 *
 * 3.2.1 Get User Details API
 * IVR shall invoke this API when to retrieve details specific to the user identified by callingNumber.
 * In case user specific details are not available in the database, the API will attempt to load system
 * defaults based on the operator and circle provided.
 * /api/mobilekunji/user?callingNumber=9999999900&operator=A&circle=AP&callId=234000011111111
 *
 */
public class FlwUserResponse extends UserResponse {
    private long currentUsageInPulses;
    private long endOfUsagePromptCounter;
    private boolean welcomePromptFlag;
    private int maxAllowedUsageInPulses;
    private int maxAllowedEndOfUsagePrompt;

    public FlwUserResponse() {
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
