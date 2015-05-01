package org.motechproject.nms.outbounddialer.web.contract;

/**
 * POST request data for 4.2.6: http://<motech:port>/motech­platform­server/module/obd/cdrFileNotification
 */
public class CdrFileNotificationRequest {
    private String fileName;
    private CdrFileNotificationRequestFileInfo cdrSummary;
    private CdrFileNotificationRequestFileInfo cdrDetail;

    public CdrFileNotificationRequest() { }

    public CdrFileNotificationRequest(String fileName, CdrFileNotificationRequestFileInfo cdrSummary,
        CdrFileNotificationRequestFileInfo cdrDetail) {
        this.fileName = fileName; // The target file associated with the CDRs
        this.cdrSummary = cdrSummary; // Contains one-line summary info for each request from NMS system
        this.cdrDetail = cdrDetail; // Contains one record for each call attempt
    }

    public String getFileName() { return fileName; }

    public void setFileName(String fileName) { this.fileName = fileName; }

    public CdrFileNotificationRequestFileInfo getCdrSummary() { return cdrSummary; }

    public void setCdrSummary(CdrFileNotificationRequestFileInfo cdrSummary) { this.cdrSummary = cdrSummary; }

    public CdrFileNotificationRequestFileInfo getCdrDetail() { return cdrDetail; }

    public void setCdrDetail(CdrFileNotificationRequestFileInfo cdrDetail) { this.cdrDetail = cdrDetail; }

}
