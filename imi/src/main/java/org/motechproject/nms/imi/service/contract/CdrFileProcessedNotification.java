package org.motechproject.nms.imi.service.contract;

/**
 * Request body
 *
 * 4.3.2 CDRFileProcessedStatus Notification API
 * NMS shall invoke the notification API of IVR platform to notify the receipt of the CDR files.
 * http://<IVROBDAPI:port>/obdmanager/notifytargetfile
 */
public class CdrFileProcessedNotification {
    private Integer cdrFileProcessingStatus;
    private String fileName;
    private String failureReason;

    public CdrFileProcessedNotification(Integer cdrFileProcessingStatus, String fileName, String failureReason) {
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
        return "CdrFileProcessedNotification{" +
                "cdrFileProcessingStatus=" + cdrFileProcessingStatus +
                ", fileName='" + fileName + '\'' +
                ", failureReason='" + failureReason + '\'' +
                '}';
    }
}
