package org.motechproject.nms.flw.controller;

import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.nms.csv.exception.CsvImportException;
import org.motechproject.nms.flw.service.FrontLineWorkerImportService;
import org.motechproject.nms.flw.service.FrontLineWorkerUpdateImportService;
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

@Controller
public class FrontLineWorkerImportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrontLineWorkerImportController.class);

    private AlertService alertService;

    private FrontLineWorkerImportService frontLineWorkerImportService;

    private FrontLineWorkerUpdateImportService flwUpdateImportService;

    @RequestMapping(value = "/update/language", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void updateFrontLineWorkersLanguage(@RequestParam MultipartFile csvFile) {
        try {
            try (InputStream in = csvFile.getInputStream()) {
                flwUpdateImportService.importLanguageData(new InputStreamReader(in));
            }
        } catch (CsvImportException e) {
            logError(e, "front_line_workers_language_import_error", "Front line workers language import error");
            throw e;
        } catch (Exception e) {
            logError(e, "front_line_workers_language_import_error", "Front line workers language import error");
            throw new CsvImportException("An error occurred during CSV import", e);
        }
    }

    @RequestMapping(value = "/update/msisdn", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void updateFrontLineWorkersMSISDN(@RequestParam MultipartFile csvFile) {
        try {
            try (InputStream in = csvFile.getInputStream()) {
                flwUpdateImportService.importMSISDNData(new InputStreamReader(in));
            }
        } catch (CsvImportException e) {
            logError(e, "front_line_workers_msisdn_import_error", "Front line workers msisdn import error");
            throw e;
        } catch (Exception e) {
            logError(e, "front_line_workers_msisdn_import_error", "Front line workers msisdn import error");
            throw new CsvImportException("An error occurred during CSV import", e);
        }
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void importFrontLineWorkers(@RequestParam MultipartFile csvFile) {
        try {
            try (InputStream in = csvFile.getInputStream()) {
                frontLineWorkerImportService.importData(new InputStreamReader(in));
            }
        } catch (CsvImportException e) {
            logError(e, "front_line_workers_import_error", "Front line workers import error");
            throw e;
        } catch (Exception e) {
            logError(e, "front_line_workers_import_error", "Front line workers import error");
            throw new CsvImportException("An error occurred during CSV import", e);
        }
    }

    private void logError(Exception exception, String entityId, String name) {
        LOGGER.error(exception.getMessage(), exception);
        alertService.create(entityId, name, exception.getMessage(), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
    }

    @Autowired
    public void setAlertService(AlertService alertService) {
        this.alertService = alertService;
    }

    @Autowired
    public void setFrontLineWorkerImportService(FrontLineWorkerImportService frontLineWorkerImportService) {
        this.frontLineWorkerImportService = frontLineWorkerImportService;
    }

    @Autowired
    public void setFlwUpdateImportService(FrontLineWorkerUpdateImportService flwUpdateImportService) {
        this.flwUpdateImportService = flwUpdateImportService;
    }
}
