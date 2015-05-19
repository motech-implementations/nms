package org.motechproject.nms.region.controller;

import org.motechproject.nms.region.exception.CsvImportException;
import org.motechproject.nms.region.service.DistrictImportService;
import org.motechproject.nms.region.service.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;

@Controller
public class LocationDataImportController {

    private DistrictImportService districtImportService;
    private LoggingService loggingService;

    @RequestMapping(value = "/data/import/{location}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void importLocationData(@RequestParam MultipartFile csvFile, @PathVariable String location) {
        try {
            try(InputStream in = csvFile.getInputStream()) {
                switch (location) {
                    case "district":
                        districtImportService.importData(new InputStreamReader(in));
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("Location type '%s' not supported", location));
                }

            }
        } catch (CsvImportException e) {
            loggingService.logError(e.getMessage());
            throw e;
        } catch (Exception e) {
            loggingService.logError(e.getMessage());
            throw new CsvImportException("An error occurred during CSV import", e);
        }
    }

    @Autowired
    public void setDistrictImportService(DistrictImportService districtImportService) {
        this.districtImportService = districtImportService;
    }

    @Autowired
    public void setLoggingService(LoggingService loggingService) {
        this.loggingService = loggingService;
    }
}
