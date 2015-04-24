package org.motechproject.nms.outbounddialer.web.contract;

/**
 * POST request data for 4.2.6: http://<motech:port>/motech­platform­server/module/obd/cdrFileNotification
 */
public class CdrFileNotificationRequest {
    private String fileName;
    private CdrInformation cdrSummary;
    private CdrInformation cdrDetail;

    public CdrFileNotificationRequest() {
    }

    public CdrFileNotificationRequest(String fileName, CdrInformation cdrSummary, CdrInformation cdrDetail) {
        this.fileName = fileName; // The target file associated with the CDRs
        this.cdrSummary = cdrSummary; // Contains one-line summary info for each request from NMS system
        this.cdrDetail = cdrDetail; // Contains one record for each call attempt
    }

    public String getFileName() { return fileName; }

    public void setFileName(String fileName) { this.fileName = fileName; }

    public CdrInformation getCdrSummary() { return cdrSummary; }

    public void setCdrSummary(CdrInformation cdrSummary) { this.cdrSummary = cdrSummary; }

    public CdrInformation getCdrDetail() { return cdrDetail; }

    public void setCdrDetail(CdrInformation cdrDetail) { this.cdrDetail = cdrDetail; }

    class CdrInformation {
        private String cdrFile;
        private String checksum;
        private int recordsCount;

        public CdrInformation() {
        }

        public CdrInformation(String cdrFile, String checksum, int recordsCount) {
            this.cdrFile = cdrFile;
            this.checksum = checksum;
            this.recordsCount = recordsCount;
        }

        public String getCdrFile() { return cdrFile; }

        public void setCdrFile(String cdrFile) { this.cdrFile = cdrFile;  }

        public String getChecksum() { return checksum; }

        public void setChecksum(String checksum) { this.checksum = checksum; }

        public int getRecordsCount() { return recordsCount; }

        public void setRecordsCount(int recordsCount) { this.recordsCount = recordsCount; }
    }

}
