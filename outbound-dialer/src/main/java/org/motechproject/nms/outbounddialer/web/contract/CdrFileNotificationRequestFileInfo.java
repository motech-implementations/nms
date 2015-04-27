package org.motechproject.nms.outbounddialer.web.contract;

public class CdrFileNotificationRequestFileInfo {
    private String cdrFile;
    private String checksum;
    private int recordsCount;

    public CdrFileNotificationRequestFileInfo() {
    }

    public CdrFileNotificationRequestFileInfo(String cdrFile, String checksum, int recordsCount) {
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
