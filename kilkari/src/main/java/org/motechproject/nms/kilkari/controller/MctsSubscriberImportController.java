package org.motechproject.nms.kilkari.controller;

import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.nms.kilkari.service.MctsSubscriberImportService;
import org.motechproject.nms.kilkari.exception.CsvImportException;
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
public class MctsSubscriberImportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsSubscriberImportController.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    private MctsSubscriberImportService mctsSubscriberImportService;

    @RequestMapping(value = "/mother/import", method = RequestMethod.POST)
        @ResponseStatus(HttpStatus.OK)
        public void importMotherData(@RequestParam MultipartFile csvFile) {

        try {
            try (InputStream in = csvFile.getInputStream()) {
                mctsSubscriberImportService.importMotherData(new InputStreamReader(in));
            }
        } catch (CsvImportException e) {
            logError(e);
            throw e;
        } catch (Exception e) {
            logError(e);
            throw new CsvImportException("An error occurred during CSV import", e);
        }
    }

    @RequestMapping(value = "/child/import", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void importChildData(@RequestParam MultipartFile csvFile) {

        try {
            try (InputStream in = csvFile.getInputStream()) {
                mctsSubscriberImportService.importChildData(new InputStreamReader(in));
            }
        } catch (CsvImportException e) {
            logError(e);
            throw e;
        } catch (Exception e) {
            logError(e);
            throw new CsvImportException("An error occurred during CSV import", e);
        }
    }

    private void logError(Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        alertService.create("mcts_import_error", "MCTS data import error", exception.getMessage(), AlertType.CRITICAL,
                AlertStatus.NEW, 0, null);
    }

}
