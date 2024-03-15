package org.motechproject.nms.imi.web.contract;

public class WhatsAppFileNotificationRequest {
    private String fileName;
    private FileInfo cdrSummary;

    public WhatsAppFileNotificationRequest() {
    }

    public WhatsAppFileNotificationRequest(String fileName, FileInfo cdrSummary) {
        this.fileName = fileName;
        this.cdrSummary = cdrSummary;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileInfo getCdrSummary() {
        return cdrSummary;
    }

    public void setCdrSummary(FileInfo cdrSummary) {
        this.cdrSummary = cdrSummary;
    }

    @Override
    public String toString() {
        return "WhatsAppSMSFileNotificationRequest{" +
                "fileName='" + fileName + '\'' +
                ", cdrSummary=" + cdrSummary +
                '}';
    }
}
