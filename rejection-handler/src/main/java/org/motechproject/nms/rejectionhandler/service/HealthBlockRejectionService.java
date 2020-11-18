package org.motechproject.nms.rejectionhandler.service;

public interface HealthBlockRejectionService {
    Long saveRejectedHealthBlockInBulk(String rejectedHealthBlockValues);
}