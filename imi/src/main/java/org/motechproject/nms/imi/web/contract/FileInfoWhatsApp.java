package org.motechproject.nms.imi.web.contract;

import java.io.Serializable;

public class FileInfoWhatsApp implements Serializable {

    private static final long serialVersionUID = 8968492271799113244L;
    private String wpResFile;
    private String checksum;
    private int recordsCount;

    public FileInfoWhatsApp() {
    }

    public FileInfoWhatsApp(String wpResFile, String checksum, int recordsCount) {
        this.wpResFile = wpResFile;
        this.checksum = checksum;
        this.recordsCount = recordsCount;
    }

    public String getWpResFile() {
        return wpResFile;
    }

    public void setWpResFile(String wpResFile) {
        this.wpResFile = wpResFile;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public int getRecordsCount() {
        return recordsCount;
    }

    public void setRecordsCount(int recordsCount) {
        this.recordsCount = recordsCount;
    }
}
