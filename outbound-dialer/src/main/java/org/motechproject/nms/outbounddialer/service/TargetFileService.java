package org.motechproject.nms.outbounddialer.service;

import org.motechproject.nms.outbounddialer.web.contract.FileProcessedStatusRequest;

/**
 * Creating the targetFile: a csv file containing all the phone numbers to be called by the IVR system
 */
public interface TargetFileService {
    /**
     * Probably only to be called by an IT. This service's constructor sets the repeating nms.obd.generate_target_file
     * MOTECH event which triggers the daily generation of the targetFile.
     */
    void generateTargetFile();


    /**
     * The IVR system invoked the obdFileProcessedStatusNotification http endpoint signalling the completion of the
     * processing of the targetFile we generated
     *
     * @param request
     */
    void handleFileProcessedStatusNotification(FileProcessedStatusRequest request);

}
