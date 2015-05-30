package org.motechproject.nms.region.controller;

import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.nms.region.exception.CsvImportException;
import org.motechproject.nms.region.service.CensusVillageImportService;
import org.motechproject.nms.region.service.DistrictImportService;
import org.motechproject.nms.region.service.HealthBlockImportService;
import org.motechproject.nms.region.service.HealthFacilityImportService;
import org.motechproject.nms.region.service.HealthSubFacilityImportService;
import org.motechproject.nms.region.service.LocationDataImportService;
import org.motechproject.nms.region.service.NonCensusVillageImportService;
import org.motechproject.nms.region.service.TalukaImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationDataImportController.class);

    private AlertService alertService;

    private DistrictImportService districtImportService;
    private TalukaImportService talukaImportService;
    private NonCensusVillageImportService nonCensusVillageImportService;
    private CensusVillageImportService censusVillageImportService;
    private HealthBlockImportService healthBlockImportService;
    private HealthFacilityImportService healthFacilityImportService;
    private HealthSubFacilityImportService healthSubFacilityImportService;

    private Map<String, LocationDataImportService> locationDataImportServiceMapping;

    @RequestMapping(value = "/data/import/{location}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void importLocationData(@RequestParam MultipartFile csvFile, @PathVariable String location) {
        try {
            try (InputStream in = csvFile.getInputStream()) {
                LocationDataImportService importService = getLocationDataImportServiceMapping().get(location);
                if (null != importService) {
                    importService.importData(new InputStreamReader(in));
                } else {
                    throw new IllegalArgumentException(String.format("Location type '%s' not supported", location));
                }
            }
        } catch (CsvImportException e) {
            logError(location, e);
            throw e;
        } catch (Exception e) {
            logError(location, e);
            throw new CsvImportException("An error occurred during CSV import", e);
        }
    }

    private void logError(String location, Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        alertService.create("location_data_import_error", String.format("Location data import error: %s", location),
                exception.getMessage(), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
    }

    @ExceptionHandler(CsvImportException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleCsvImportException() {
    }

    @Autowired
    public void setAlertService(AlertService alertService) {
        this.alertService = alertService;
    }

    @Autowired
    public void setDistrictImportService(DistrictImportService districtImportService) {
        this.districtImportService = districtImportService;
    }

    @Autowired
    public void setTalukaImportService(TalukaImportService talukaImportService) {
        this.talukaImportService = talukaImportService;
    }

    @Autowired
    public void setNonCensusVillageImportService(NonCensusVillageImportService nonCensusVillageImportService) {
        this.nonCensusVillageImportService = nonCensusVillageImportService;
    }

    @Autowired
    public void setCensusVillageImportService(CensusVillageImportService censusVillageImportService) {
        this.censusVillageImportService = censusVillageImportService;
    }

    @Autowired
    public void setHealthBlockImportService(HealthBlockImportService healthBlockImportService) {
        this.healthBlockImportService = healthBlockImportService;
    }

    @Autowired
    public void setHealthFacilityImportService(HealthFacilityImportService healthFacilityImportService) {
        this.healthFacilityImportService = healthFacilityImportService;
    }

    @Autowired
    public void setHealthSubFacilityImportService(HealthSubFacilityImportService healthSubFacilityImportService) {
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
