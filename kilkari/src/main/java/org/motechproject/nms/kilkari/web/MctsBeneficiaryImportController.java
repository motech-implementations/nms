package org.motechproject.nms.kilkari.web;

import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.csv.exception.CsvImportException;
import org.motechproject.nms.csv.service.CsvAuditService;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportReaderService;
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
    private CsvAuditService csvAuditService;
    private MctsBeneficiaryImportReaderService mctsBeneficiaryImportReaderService;


    @Autowired
    public void setAlertService(AlertService alertService) {
        this.alertService = alertService;
    }

    @Autowired
    public void setMctsBeneficiaryImportReaderService(MctsBeneficiaryImportReaderService mctsBeneficiaryImportReaderService) {
        this.mctsBeneficiaryImportReaderService = mctsBeneficiaryImportReaderService;
    }

    @Autowired
    public void setCsvAuditService(CsvAuditService csvAuditService) {
        this.csvAuditService = csvAuditService;
    }




    @RequestMapping(value = "/mother/import", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void importMotherData(@RequestParam MultipartFile csvFile) {

        LOGGER.debug("importMotherData() BEGIN");
        Timer timer = new Timer("mom", "moms");
        int count = 0;
        try {
            try (InputStream in = csvFile.getInputStream()) {
                count = mctsBeneficiaryImportReaderService.importMotherData(new InputStreamReader(in), SubscriptionOrigin.MCTS_IMPORT);
                csvAuditService.auditSuccess(csvFile.getOriginalFilename(), "/kilkari/mother/import");
            }
        } catch (CsvImportException e) {
            logError(csvFile.getOriginalFilename(), "/kilkari/mother/import", e);
            throw e;
        } catch (Exception e) {
            logError(csvFile.getOriginalFilename(), "/kilkari/mother/import", e);
            throw new CsvImportException("An error occurred during CSV import", e);
        }
        LOGGER.debug("importMotherData() END ({})", count > 0 ? timer.frequency(count) : timer.time());
    }


    @RequestMapping(value = "/child/import", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void importChildData(@RequestParam MultipartFile csvFile) {
        LOGGER.debug("importChildData() BEGIN");
        Timer timer = new Timer("kid", "kids");
        int count = 0;
        try {
            try (InputStream in = csvFile.getInputStream()) {
                count = mctsBeneficiaryImportReaderService.importChildData(new InputStreamReader(in), SubscriptionOrigin.MCTS_IMPORT);
                csvAuditService.auditSuccess(csvFile.getOriginalFilename(), "/kilkari/child/import");
            }
        } catch (CsvImportException e) {
            logError(csvFile.getOriginalFilename(), "/kilkari/child/import", e);
            throw e;
        } catch (Exception e) {
            logError(csvFile.getOriginalFilename(), "/kilkari/child/import", e);
            throw new CsvImportException("An error occurred during CSV import", e);
        }
        LOGGER.debug("importChildData() END ({})", count > 0 ? timer.frequency(count) : timer.time());
    }

    private void logError(String fileName, String endpoint, Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        csvAuditService.auditFailure(fileName, endpoint, exception.getMessage());
        alertService.create("mcts_import_error", "MCTS data import error", exception.getMessage(),
                AlertType.CRITICAL, AlertStatus.NEW, 0, null);
    }


}
