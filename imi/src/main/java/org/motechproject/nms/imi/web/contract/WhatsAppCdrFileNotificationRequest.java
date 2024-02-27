package org.motechproject.nms.imi.web.contract;

/**
 * POST request data for 4.2.6
 * /imi/cdrFileNotification
 *
 * IVR shall invoke this NMS API to notify IVR platform when the CDR files are ready for processing.
 */
public class WhatsAppCdrFileNotificationRequest {
    private String targetFileName;
    private FileInfoWhatsApp whatsappResSummary;

    public WhatsAppCdrFileNotificationRequest() { }

    public WhatsAppCdrFileNotificationRequest(String targetFileName, FileInfoWhatsApp whatsappResSummary) {
        this.targetFileName = targetFileName;
        this.whatsappResSummary = whatsappResSummary;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    public FileInfoWhatsApp getWhatsappResSummary() {
        return whatsappResSummary;
    }

    public void setWhatsappResSummary(FileInfoWhatsApp whatsappResSummary) {
        this.whatsappResSummary = whatsappResSummary;
    }

    @Override
    public String toString() {
        return "WhatsAppCdrFileNotificationRequest{" +
                "targetFileName='" + targetFileName + '\'' +
                ", whatsappResSummary=" + whatsappResSummary +
                '}';
    }
}
