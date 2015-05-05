package org.motechproject.nms.outbounddialer.web.contract;

/**
 * POST request data for 4.2.6
 * /outbound-dialer/cdrFileNotification
 *
 * IVR shall invoke this NMS API to notify IVR platform when the CDR files are ready for processing.
 */
public class CdrFileNotificationRequest {
    private String fileName;
    private FileInfo cdrSummary;
    private FileInfo cdrDetail;

    public CdrFileNotificationRequest() { }

    public CdrFileNotificationRequest(String fileName, FileInfo cdrSummary, FileInfo cdrDetail) {
        this.fileName = fileName;     // The target file associated with the CDRs
        this.cdrSummary = cdrSummary; // Contains one-line summary info for each request from NMS system
        this.cdrDetail = cdrDetail;   // Contains one record for each call attempt
    }

    public String getFileName() { return fileName; }

    public void setFileName(String fileName) { this.fileName = fileName; }

    public FileInfo getCdrSummary() { return cdrSummary; }

    public void setCdrSummary(FileInfo cdrSummary) { this.cdrSummary = cdrSummary; }

    public FileInfo getCdrDetail() { return cdrDetail; }

    public void setCdrDetail(FileInfo cdrDetail) { this.cdrDetail = cdrDetail; }

    @Override
    public String toString() {
        return "CdrFileNotificationRequest{" +
                "fileName='" + fileName + '\'' +
                ", cdrSummary=" + cdrSummary +
                ", cdrDetail=" + cdrDetail +
                '}';
    }
}
