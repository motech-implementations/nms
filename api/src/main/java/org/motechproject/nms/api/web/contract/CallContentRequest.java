package org.motechproject.nms.api.web.contract;

public class CallContentRequest {
    private String type;
    private String mkCardNumber;
    private String contentName;
    private String contentFileName;
    private Long startTime;
    private Long endTime;
    private Boolean completionFlag;
    private Boolean correctAnswerEntered;

    public CallContentRequest() { }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMkCardNumber() {
        return mkCardNumber;
    }

    public void setMkCardNumber(String mkCardNumber) {
        this.mkCardNumber = mkCardNumber;
    }

    public String getContentName() {
        return contentName;
    }

    public void setContentName(String contentName) {
        this.contentName = contentName;
    }

    public String getContentFileName() {
        return contentFileName;
    }

    public void setContentFileName(String contentFileName) {
        this.contentFileName = contentFileName;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Boolean getCompletionFlag() {
        return completionFlag;
    }

    public void setCompletionFlag(Boolean completionFlag) {
        this.completionFlag = completionFlag;
    }

    public Boolean isCompletionFlag() {
        return completionFlag;
    }

    public Boolean isCorrectAnswerEntered() {
        return correctAnswerEntered;
    }

    public void setCorrectAnswerEntered(Boolean correctAnswerEntered) {
        this.correctAnswerEntered = correctAnswerEntered;
    }
}
