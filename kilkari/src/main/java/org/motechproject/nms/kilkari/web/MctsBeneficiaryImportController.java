package org.motechproject.nms.kilkari.web;

import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.nms.csv.exception.CsvImportException;
import org.motechproject.nms.csv.service.CsvAuditService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Controller that supports import of Kilkari subscribers (mother and child records) from MCTS
 */
@Controller
public class MctsBeneficiaryImportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsBeneficiaryImportController.class);

    private AlertService alertService;
    private MctsBeneficiaryImportService mctsBeneficiaryImportService;
    private CsvAuditService csvAuditService;

    @Autowired
    public void setAlertService(AlertService alertService) {
        this.alertService = alertService;
    }

    @Autowired
    public void setMctsBeneficiaryImportService(MctsBeneficiaryImportService mctsBeneficiaryImportService) {
        this.mctsBeneficiaryImportService = mctsBeneficiaryImportService;
    }

    @Autowired
    public void setCsvAuditService(CsvAuditService csvAuditService) {
        this.csvAuditService = csvAuditService;
    }

    @RequestMapping(value = "/mother/import", method = RequestMethod.POST)
        @ResponseStatus(HttpStatus.OK)
        public void importMotherData(@RequestParam MultipartFile csvFile) {

        try {
            try (InputStream in = csvFile.getInputStream()) {
                mctsBeneficiaryImportService.importMotherData(new InputStreamReader(in));
                csvAuditService.auditSuccess(csvFile.getName(), "/kilkari/mother/import");
            }
        } catch (CsvImportException e) {
            logError(csvFile.getName(), "/kilkari/mother/import", e);
            throw e;
        } catch (Exception e) {
            logError(csvFile.getName(), "/kilkari/mother/import", e);
            throw new CsvImportException("An error occurred during CSV import", e);
        }
    }

    @RequestMapping(value = "/child/import", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void importChildData(@RequestParam MultipartFile csvFile) {

        try {
            try (InputStream in = csvFile.getInputStream()) {
                mctsBeneficiaryImportService.importChildData(new InputStreamReader(in));
                csvAuditService.auditSuccess(csvFile.getName(), "/kilkari/child/import");
            }
        } catch (CsvImportException e) {
            logError(csvFile.getName(), "/kilkari/child/import", e);
            throw e;
        } catch (Exception e) {
            logError(csvFile.getName(), "/kilkari/child/import", e);
            throw new CsvImportException("An error occurred during CSV import", e);
        }
    }

    private void logError(String fileName, String endpoint, Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        csvAuditService.auditFailure(fileName, endpoint, exception.getMessage());
        alertService.create("mcts_import_error", "MCTS data import error", exception.getMessage(),
                AlertType.CRITICAL, AlertStatus.NEW, 0, null);
    }


}
