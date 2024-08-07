package org.motechproject.nms.region.controller;

import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.nms.csv.exception.CsvImportException;
import org.motechproject.nms.csv.service.CsvAuditService;
import org.motechproject.nms.region.csv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private StateImportService stateImportService;
    private DistrictImportService districtImportService;
    private TalukaImportService talukaImportService;
    private NonCensusVillageImportService nonCensusVillageImportService;
    private CensusVillageImportService censusVillageImportService;
    private HealthBlockImportService healthBlockImportService;
    private HealthFacilityImportService healthFacilityImportService;
    private HealthSubFacilityImportService healthSubFacilityImportService;
    private CsvAuditService csvAuditService;

    private Map<String, LocationDataImportService> locationDataImportServiceMapping;

    @Autowired
    LanguageLocationImportService languageLocationImportService;

    @RequestMapping(value = "/data/import/circle", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> createCircle(@RequestParam("name") String name, @RequestParam("languageName") String languageName, @RequestParam("key") String key) {

        String returnStatement;
        if(key.equals("324tgyhdfwieagf2yge32dh2h")){
            languageLocationImportService.createCircleRecord(name,languageName);
            returnStatement="circle created/update with name "+name+" and its default language will be "+languageName;
            return new ResponseEntity<>(returnStatement, HttpStatus.OK);
        }
        else {
            returnStatement="Not Authorized";
            return new ResponseEntity<>(returnStatement,HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/data/import/state", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> createState(@RequestParam("name") String name, @RequestParam("code") Long code, @RequestParam("key") String key) {

        String returnStatement;
        if(key.equals("324tgyhdfwieagf2yge32dh2h")){
            languageLocationImportService.createStateRecord(name,code);

            returnStatement="State created/update with name "+name+" and code "+code;
            return new ResponseEntity<>(returnStatement, HttpStatus.OK);
        }
        else {
            returnStatement="Not Authorized";
            return new ResponseEntity<>(returnStatement,HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/data/import/{location}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void importLocationData(@RequestParam MultipartFile csvFile, @PathVariable String location) {
        String endpoint = String.format("region/data/import/%s", location);
        try {
            try (InputStream in = csvFile.getInputStream()) {
                LocationDataImportService importService = getLocationDataImportServiceMapping().get(location);
                if (null != importService) {
                    //TODO HARITHA commenting for now -- this webservice might have to be removed ! no longer used
                    //importService.importData(new InputStreamReader(in));
                    csvAuditService.auditSuccess(csvFile.getOriginalFilename(), endpoint);
                } else {
                    String error = String.format("Location type '%s' not supported", location);
                    csvAuditService.auditFailure(csvFile.getOriginalFilename(), endpoint, error);
                    throw new IllegalArgumentException(error);
                }
            }
        } catch (CsvImportException e) {
            logError(csvFile.getOriginalFilename(), endpoint, location, e);
            throw e;
        } catch (Exception e) {
            logError(csvFile.getOriginalFilename(), endpoint, location, e);
            throw new CsvImportException("An error occurred during CSV import", e);
        }
    }

    private void logError(String fileName, String endpoint, String location, Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        csvAuditService.auditFailure(fileName, endpoint, exception.getMessage());
        alertService.create("location_data_import_error",
                String.format("Location data import error: %s", location),
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
    public void setStateImportService(StateImportService stateImportService) {
        this.stateImportService = stateImportService;
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

    @Autowired
    public void setCsvAuditService(CsvAuditService csvAuditService) {
        this.csvAuditService = csvAuditService;
    }

    private Map<String, LocationDataImportService> getLocationDataImportServiceMapping() {
        if (null == locationDataImportServiceMapping) {
            locationDataImportServiceMapping = new HashMap<>();
            locationDataImportServiceMapping.put("state", stateImportService);
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
