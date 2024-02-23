package org.motechproject.nms.imi.service.contract;

/**
 * Request body
 *
 * 4.3.2 CDRFileProcessedStatus Notification API
 * NMS shall invoke the notification API of IVR platform to notify the receipt of the CDR files.
 * http://<IVROBDAPI:port>/obdmanager/notifytargetfile
 */
public class WhatsAppCdrFileProcessedNotification {
    private Integer whatsAppCdrFileProcessingStatus;
    private String fileName;
    private String failureReason;

    public WhatsAppCdrFileProcessedNotification(Integer whatsAppCdrFileProcessingStatus, String fileName, String failureReason) {
        this.whatsAppCdrFileProcessingStatus = whatsAppCdrFileProcessingStatus;
        this.fileName = fileName;
        this.failureReason = failureReason;
    }

    public Integer getWhatsAppCdrFileProcessingStatus() {
        return whatsAppCdrFileProcessingStatus;
    }

    public void setWhatsAppCdrFileProcessingStatus(Integer whatsAppCdrFileProcessingStatus) {
        this.whatsAppCdrFileProcessingStatus = whatsAppCdrFileProcessingStatus;
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
        return "WhatsAppCdrFileProcessedNotification{" +
                "whatsAppCdrFileProcessingStatus=" + whatsAppCdrFileProcessingStatus +
                ", fileName='" + fileName + '\'' +
                ", failureReason='" + failureReason + '\'' +
                '}';
    }
}
