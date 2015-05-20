package org.motechproject.nms.region.controller;

import org.motechproject.nms.region.exception.CsvImportException;
import org.motechproject.nms.region.service.LocationDataImportService;
import org.motechproject.nms.region.service.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LocationDataImportController {

    private LoggingService loggingService;

    private LocationDataImportService districtImportService;
    private LocationDataImportService talukaImportService;
    private LocationDataImportService nonCensusVillageImportService;
    private LocationDataImportService censusVillageImportService;
    private LocationDataImportService healthBlockImportService;
    private LocationDataImportService healthFacilityImportService;
    private LocationDataImportService healthSubFacilityImportService;

    private Map<String, LocationDataImportService> locationDataImportServiceMapping;

    @RequestMapping(value = "/data/import/{location}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void importLocationData(@RequestParam MultipartFile csvFile, @PathVariable String location) {
        try {
            try(InputStream in = csvFile.getInputStream()) {
                LocationDataImportService importService = getLocationDataImportServiceMapping().get(location);
                if (null != importService) {
                    importService.importData(new InputStreamReader(in));
                } else {
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

    @ExceptionHandler(CsvImportException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleCsvImportException() {
    }

    @Autowired
    public void setLoggingService(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Autowired
    @Qualifier("districtImportService")
    public void setDistrictImportService(LocationDataImportService districtImportService) {
        this.districtImportService = districtImportService;
    }

    @Autowired
    @Qualifier("talukaImportService")
    public void setTalukaImportService(LocationDataImportService talukaImportService) {
        this.talukaImportService = talukaImportService;
    }

    @Autowired
    @Qualifier("nonCensusVillageImportService")
    public void setNonCensusVillageImportService(LocationDataImportService nonCensusVillageImportService) {
        this.nonCensusVillageImportService = nonCensusVillageImportService;
    }

    @Autowired
    @Qualifier("censusVillageImportService")
    public void setCensusVillageImportService(LocationDataImportService censusVillageImportService) {
        this.censusVillageImportService = censusVillageImportService;
    }

    @Autowired
    @Qualifier("healthBlockImportService")
    public void setHealthBlockImportService(LocationDataImportService healthBlockImportService) {
        this.healthBlockImportService = healthBlockImportService;
    }

    @Autowired
    @Qualifier("healthFacilityImportService")
    public void setHealthFacilityImportService(LocationDataImportService healthFacilityImportService) {
        this.healthFacilityImportService = healthFacilityImportService;
    }

    @Autowired
    @Qualifier("healthSubFacilityImportService")
    public void setHealthSubFacilityImportService(LocationDataImportService healthSubFacilityImportService) {
        this.healthSubFacilityImportService = healthSubFacilityImportService;
    }

    private Map<String, LocationDataImportService> getLocationDataImportServiceMapping() {
        if (null == locationDataImportServiceMapping) {
            locationDataImportServiceMapping = new HashMap<>();
            locationDataImportServiceMapping.put("district", districtImportService);
            locationDataImportServiceMapping.put("taluka", talukaImportService);
            locationDataImportServiceMapping.put("nonCensusVillage", nonCensusVillageImportService);
            locationDataImportServiceMapping.put("censusVillage", censusVillageImportService);
            locationDataImportServiceMapping.put("healthBlock", healthBlockImportService);
            locationDataImportServiceMapping.put("healthFacility", healthFacilityImportService);
            locationDataImportServiceMapping.put("healthSubFacility", healthSubFacilityImportService);
        }
        return locationDataImportServiceMapping;
    }
}
