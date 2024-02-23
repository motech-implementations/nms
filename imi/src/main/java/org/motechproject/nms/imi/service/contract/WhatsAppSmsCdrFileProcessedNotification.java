package org.motechproject.nms.imi.service.contract;

public class WhatsAppSmsCdrFileProcessedNotification {
    private Integer cdrFileProcessingStatus;
    private String fileName;
    private String failureReason;

    public WhatsAppSmsCdrFileProcessedNotification(Integer cdrFileProcessingStatus, String fileName, String failureReason) {
        this.cdrFileProcessingStatus = cdrFileProcessingStatus;
        this.fileName = fileName;
        this.failureReason = failureReason;
    }

    public Integer getCdrFileProcessingStatus() {
        return cdrFileProcessingStatus;
    }

    public void setCdrFileProcessingStatus(Integer cdrFileProcessingStatus) {
        this.cdrFileProcessingStatus = cdrFileProcessingStatus;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    @Override
    public String toString() {
        return "WhatsAppSmsCdrFileProcessedNotification{" +
                "cdrFileProcessingStatus=" + cdrFileProcessingStatus +
                ", fileName='" + fileName + '\'' +
                ", failureReason='" + failureReason + '\'' +
                '}';
    }
}
