package org.motechproject.nms.csv.service.impl;

import org.motechproject.nms.csv.domain.CsvAuditRecord;
import org.motechproject.nms.csv.repository.CsvAuditRecordDataService;
import org.motechproject.nms.csv.service.CsvAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("csvAuditService")
public class CsvAuditServiceImpl implements CsvAuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvAuditServiceImpl.class);
    public static final String SUCCESS = "Success";
    public static final String FAILURE = "Failure: ";

    private CsvAuditRecordDataService csvAuditRecordDataService;

    @Autowired
    public void setCsvAuditRecordDataService(CsvAuditRecordDataService csvAuditRecordDataService) {
        this.csvAuditRecordDataService = csvAuditRecordDataService;
    }

    private void audit(String file, String endpoint, String outcome) {
        String truncatedOutcome;

        if (outcome.length() > CsvAuditRecord.MAX_OUTCOME_LENGTH) {
            truncatedOutcome = outcome.substring(0, CsvAuditRecord.MAX_OUTCOME_LENGTH);
            LOGGER.warn("The provided outcome field was more than {} characters and was truncated, original " +
                            "outcome: {}", CsvAuditRecord.MAX_OUTCOME_LENGTH - FAILURE.length(), outcome);
        } else {
            truncatedOutcome = outcome;
        }

        csvAuditRecordDataService.create(new CsvAuditRecord(file, endpoint, truncatedOutcome));
    }

    public void auditSuccess(String file, String endpoint) {
        audit(file, endpoint, SUCCESS);
    }

    public void auditFailure(String file, String endpoint, String failure) {
        audit(file, endpoint, FAILURE + failure);
    }
}
