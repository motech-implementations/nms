package org.motechproject.nms.outbounddialer.web.contract;

import org.motechproject.nms.outbounddialer.domain.FileProcessedStatus;

/**
 * POST request data for 4.2.7: http://<motech:port>/motech­platform­server/module/obd/obdFileProcessedStatusNotification
 */
public class FileProcessedStatusRequest {
    private FileProcessedStatus fileProcessedStatus;
    private String fileName;

    public FileProcessedStatusRequest() {
    }

    public FileProcessedStatusRequest(FileProcessedStatus fileProcessedStatus, String fileName) {
        this.fileProcessedStatus = fileProcessedStatus;
        this.fileName = fileName;
    }

    public FileProcessedStatus getFileProcessedStatus() { return fileProcessedStatus; }

    public void setFileProcessedStatus(FileProcessedStatus fileProcessedStatus) {
        this.fileProcessedStatus = fileProcessedStatus;
    }

    public String getFileName() { return fileName; }

    public void setFileName(String fileName) { this.fileName = fileName; }

}
