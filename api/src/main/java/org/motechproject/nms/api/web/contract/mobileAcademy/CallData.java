package org.motechproject.nms.api.web.contract.mobileAcademy;

/**
 * Call data object to track individual call data files played during a call
 */
public class CallData {

    private String type;

    private String contentName;

    private String contentFile;

    private int startTime;

    private int endTime;

    private boolean completionFlag;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContentName() {
        return contentName;
    }

    public void setContentName(String contentName) {
        this.contentName = contentName;
    }

    public String getContentFile() {
        return contentFile;
    }

    public void setContentFile(String contentFile) {
        this.contentFile = contentFile;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public boolean isCompletionFlag() {
        return completionFlag;
    }

    public void setCompletionFlag(boolean completionFlag) {
        this.completionFlag = completionFlag;
    }
}
