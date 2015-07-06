package org.motechproject.nms.csv.service;

public interface CsvAuditService {

    void auditSuccess(String file, String endpoint);
    void auditFailure(String file, String endpoint, String failure);
}
