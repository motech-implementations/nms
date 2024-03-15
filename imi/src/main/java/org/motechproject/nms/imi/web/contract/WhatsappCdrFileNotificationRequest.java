package org.motechproject.nms.imi.web.contract;

public class WhatsappCdrFileNotificationRequest {
    private String fileName;
    private FileInfo cdrSummary;

    public WhatsappCdrFileNotificationRequest(String fileName, FileInfo cdrSummary) {
        this.fileName = fileName;
        this.cdrSummary = cdrSummary;
    }

    public WhatsappCdrFileNotificationRequest() {
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
    public String toString(){
        return "CdrFileNotificationRequest{" +
                "fileName='" + fileName + '\''+
                ", cdrDetail=" + cdrSummary +
                '}';
    }
}
