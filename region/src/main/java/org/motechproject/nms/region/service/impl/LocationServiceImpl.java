package org.motechproject.nms.region.service.impl;

import org.apache.commons.io.IOUtils;
import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.region.domain.LocationEnum;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.LocationFinder;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.TalukaDataService;
import org.motechproject.nms.region.repository.VillageDataService;
import org.motechproject.nms.region.repository.HealthBlockDataService;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.repository.HealthSubFacilityDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.HealthBlockService;
import org.motechproject.nms.region.service.HealthFacilityService;
import org.motechproject.nms.region.service.HealthSubFacilityService;
import org.motechproject.nms.region.service.LocationService;
import org.motechproject.nms.region.service.StateService;
import org.motechproject.nms.region.service.TalukaService;
import org.motechproject.nms.region.service.VillageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.prefs.CsvPreference;

import javax.jdo.Query;
import javax.validation.ConstraintViolationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.List;

import static org.motechproject.nms.region.utils.LocationConstants.CODE_SQL_STRING;
import static org.motechproject.nms.region.utils.LocationConstants.DISTRICT_ID;
import static org.motechproject.nms.region.utils.LocationConstants.DISTRICT_NAME;
import static org.motechproject.nms.region.utils.LocationConstants.HEALTHBLOCK_ID;
import static org.motechproject.nms.region.utils.LocationConstants.HEALTHBLOCK_NAME;
import static org.motechproject.nms.region.utils.LocationConstants.HEALTHFACILITY_ID;
import static org.motechproject.nms.region.utils.LocationConstants.HEALTHFACILITY_NAME;
import static org.motechproject.nms.region.utils.LocationConstants.HEALTHSUBFACILITY_ID;
import static org.motechproject.nms.region.utils.LocationConstants.HEALTHSUBFACILITY_NAME;
import static org.motechproject.nms.region.utils.LocationConstants.INVALID;
import static org.motechproject.nms.region.utils.LocationConstants.LOCATION_PART_SIZE;
import static org.motechproject.nms.region.utils.LocationConstants.NON_CENSUS_VILLAGE;
import static org.motechproject.nms.region.utils.LocationConstants.OR_SQL_STRING;
import static org.motechproject.nms.region.utils.LocationConstants.PHC_ID;
import static org.motechproject.nms.region.utils.LocationConstants.PHC_NAME;
import static org.motechproject.nms.region.utils.LocationConstants.STATE_ID;
import static org.motechproject.nms.region.utils.LocationConstants.SUBCENTRE_ID;
import static org.motechproject.nms.region.utils.LocationConstants.SUBCENTRE_NAME;
import static org.motechproject.nms.region.utils.LocationConstants.TALUKA_ID;
import static org.motechproject.nms.region.utils.LocationConstants.TALUKA_NAME;
import static org.motechproject.nms.region.utils.LocationConstants.VILLAGE_ID;
import static org.motechproject.nms.region.utils.LocationConstants.VILLAGE_NAME;


/**
 * Location service impl to get location objects
 */
@Service("locationService")
public class LocationServiceImpl implements LocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationServiceImpl.class);

    private StateService stateService;

    private StateDataService stateDataService;

    private DistrictService districtService;

    private TalukaService talukaService;

    private VillageService villageService;

    private HealthBlockService healthBlockService;

    private HealthFacilityService healthFacilityService;

    private HealthSubFacilityService healthSubFacilityService;

    private DistrictDataService districtDataService;

    private TalukaDataService talukaDataService;

    private VillageDataService villageDataService;

    private HealthBlockDataService healthBlockDataService;

    private HealthFacilityDataService healthFacilityDataService;

    private HealthSubFacilityDataService healthSubFacilityDataService;

    @Autowired
    public LocationServiceImpl(StateService stateService, StateDataService stateDataService, DistrictService districtService,
                               TalukaService talukaService, VillageService villageService,
                               HealthBlockService healthBlockService, HealthFacilityService healthFacilityService,
                               HealthSubFacilityService healthSubFacilityService, DistrictDataService districtDataService, TalukaDataService talukaDataService, VillageDataService villageDataService, HealthBlockDataService healthBlockDataService, HealthFacilityDataService healthFacilityDataService, HealthSubFacilityDataService healthSubFacilityDataService) {
        this.stateService = stateService;
        this.stateDataService = stateDataService;
        this.districtService = districtService;
        this.talukaService = talukaService;
        this.villageService = villageService;
        this.healthBlockService = healthBlockService;
        this.healthFacilityService = healthFacilityService;
        this.healthSubFacilityService = healthSubFacilityService;
        this.districtDataService = districtDataService;
        this.talukaDataService = talukaDataService;
        this.villageDataService = villageDataService;
        this.healthBlockDataService = healthBlockDataService;
        this.healthFacilityDataService = healthFacilityDataService;
        this.healthSubFacilityDataService = healthSubFacilityDataService;
    }


    private boolean isValidID(final Map<String, Object> map, final String key) {
        Object obj = map.get(key);
        if (obj == null || obj.toString().isEmpty() || "NULL".equalsIgnoreCase(obj.toString())) {
            return false;
        }

        if (obj.getClass().equals(Long.class)) {
            return (Long) obj > 0L;
        }

        return !"0".equals(obj);
    }

    public Map<String, Object> getLocations(Map<String, Object> map) throws InvalidLocationException {
       return getLocations(map, false);
    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    @SuppressWarnings("PMD")
    public Map<String, Object> getLocations(Map<String, Object> map, boolean createIfNotExists) throws InvalidLocationException {

        Map<String, Object> locations = new HashMap<>();

        LOGGER.info("map {}", isValidID(map, STATE_ID));

        // set state
        if (!isValidID(map, STATE_ID)) {
            return locations;
        }
        State state = stateDataService.findByCode((Long) map.get(STATE_ID));
        LOGGER.info("state {}", state);
        if (state == null) { // we are here because stateId wasn't null but fetch returned no data
            throw new InvalidLocationException(String.format(INVALID, STATE_ID, map.get(STATE_ID)));
        }
        locations.put(STATE_ID, state);


        // set district
        if (!isValidID(map, DISTRICT_ID)) {
            return locations;
        }
        District district = districtService.findByStateAndCode(state, (Long) map.get(DISTRICT_ID));
        if (district == null) {
            throw new InvalidLocationException(String.format(INVALID, DISTRICT_ID, map.get(DISTRICT_ID)));
        }
        locations.put(DISTRICT_ID, district);


        // set and/or create taluka
        if (!isValidID(map, TALUKA_ID)) {
            return locations;
        }
        Taluka taluka = talukaService.findByDistrictAndCode(district, (String) map.get(TALUKA_ID));
        if (taluka == null && createIfNotExists) {
            taluka = new Taluka();
            taluka.setCode((String) map.get(TALUKA_ID));
            taluka.setName((String) map.get(TALUKA_NAME));
            taluka.setDistrict(district);
            district.getTalukas().add(taluka);
            LOGGER.debug(String.format("Created %s in %s with id %d", taluka, district, taluka.getId()));
        }
        locations.put(TALUKA_ID, taluka);


        // set and/or create village
        Long svid = map.get(NON_CENSUS_VILLAGE) == null ? 0 : (Long) map.get(NON_CENSUS_VILLAGE);
        Long vcode = map.get(VILLAGE_ID) == null ? 0 : (Long) map.get(VILLAGE_ID);
        Village village = new Village();
        if (vcode != 0 || svid != 0) {
            village = villageService.findByTalukaAndVcodeAndSvid(taluka, vcode, svid);
            if (village == null && createIfNotExists) {
                village = new Village();
                village.setSvid(svid);
                village.setVcode(vcode);
                village.setTaluka(taluka);
                village.setName((String) map.get(VILLAGE_NAME));
                taluka.getVillages().add(village);
                LOGGER.debug(String.format("Created %s in %s with id %d", village, taluka, village.getId()));
            }
            locations.put(VILLAGE_ID + NON_CENSUS_VILLAGE, village);
        }


        // set and/or create health block
        if (!isValidID(map, HEALTHBLOCK_ID)) {
            return locations;
        }
        HealthBlock healthBlock = healthBlockService.findByTalukaAndCode(taluka, (Long) map.get(HEALTHBLOCK_ID));
        if (healthBlock == null && createIfNotExists) {
            healthBlock = new HealthBlock();
            healthBlock.addTaluka(taluka);
            healthBlock.setDistrict(district);
            healthBlock.setCode((Long) map.get(HEALTHBLOCK_ID));
            healthBlock.setName((String) map.get(HEALTHBLOCK_NAME));
            taluka.addHealthBlock(healthBlock);
            district.getHealthBlocks().add(healthBlock);
            LOGGER.debug(String.format("Created %s in %s with id %d", healthBlock, taluka, healthBlock.getId()));
        }
        locations.put(HEALTHBLOCK_ID, healthBlock);


        // set and/or create health facility
        if (!isValidID(map, PHC_ID)) {
            return locations;
        }
        HealthFacility healthFacility = healthFacilityService.findByHealthBlockAndCode(healthBlock, (Long) map.get(PHC_ID));
        if (healthFacility == null && createIfNotExists) {
            healthFacility = new HealthFacility();
            healthFacility.setHealthBlock(healthBlock);
            healthFacility.setCode((Long) map.get(PHC_ID));
            healthFacility.setName((String) map.get(PHC_NAME));
            healthBlock.getHealthFacilities().add(healthFacility);
            LOGGER.debug(String.format("Created %s in %s with id %d", healthFacility, healthBlock, healthFacility.getId()));
        }
        locations.put(PHC_ID, healthFacility);


        // set and/or create health sub-facility
        if (!isValidID(map, SUBCENTRE_ID)) {
            return locations;
        }
        HealthSubFacility healthSubFacility = healthSubFacilityService.findByHealthFacilityAndCode(healthFacility, (Long) map.get(SUBCENTRE_ID));
        if (healthSubFacility == null && createIfNotExists) {
            healthSubFacility = new HealthSubFacility();
            healthSubFacility.addVillage(village);
            healthSubFacility.setHealthFacility(healthFacility);
            healthSubFacility.setCode((Long) map.get(SUBCENTRE_ID));
            healthSubFacility.setName((String) map.get(SUBCENTRE_NAME));
            healthFacility.getHealthSubFacilities().add(healthSubFacility);
            village.addHealthSubFacility(healthSubFacility);
            LOGGER.debug(String.format("Created %s in %s with id %d", healthSubFacility, healthFacility, healthSubFacility.getId()));
        }
        locations.put(SUBCENTRE_ID, healthSubFacility);

        return locations;
    }

    @Override
    public Taluka updateTaluka(Map<String, Object> flw, Boolean createIfNotExists) {
        State state = stateDataService.findByCode((Long) flw.get(STATE_ID));
        District district = districtService.findByStateAndCode(state, (Long) flw.get(DISTRICT_ID));
            // set and/or create taluka
        if (!isValidID(flw, TALUKA_ID)) {
            return null;
        }
        Taluka taluka = talukaService.findByDistrictAndCode(district, (String) flw.get(TALUKA_ID));
        if (taluka == null && createIfNotExists) {
            taluka = new Taluka();
            taluka.setCode((String) flw.get(TALUKA_ID));
            taluka.setName((String) flw.get(TALUKA_NAME));
            taluka.setDistrict(district);
            LOGGER.debug(String.format("taluka: %s", taluka.toString()));
            district.getTalukas().add(taluka);
            LOGGER.debug(String.format("Created %s in %s with id %d", taluka, district, taluka.getId()));
        }
        return taluka;
    }

    @Override
    public HealthBlock updateBlock(Map<String, Object> flw, Taluka taluka, Boolean createIfNotExists) {

        // set and/or create health block
        if (!isValidID(flw, HEALTHBLOCK_ID)) {
            return null;
        }
        HealthBlock healthBlock = healthBlockService.findByTalukaAndCode(taluka, (Long) flw.get(HEALTHBLOCK_ID));
        if (healthBlock == null && createIfNotExists) {
            healthBlock = new HealthBlock();
            healthBlock.addTaluka(taluka);
            healthBlock.setDistrict(taluka.getDistrict());
            healthBlock.setCode((Long) flw.get(HEALTHBLOCK_ID));
            healthBlock.setName((String) flw.get(HEALTHBLOCK_NAME));
            taluka.addHealthBlock(healthBlock);
            LOGGER.debug(String.format("Created %s in %s with id %d", healthBlock, taluka, healthBlock.getId()));
        }
        return healthBlock;
    }

    @Override
    public HealthFacility updateFacility(Map<String, Object> flw, HealthBlock healthBlock, Boolean createIfNotExists) {

        // set and/or create health facility
        if (!isValidID(flw, PHC_ID)) {
            return null;
        }
        HealthFacility healthFacility = healthFacilityService.findByHealthBlockAndCode(healthBlock, (Long) flw.get(PHC_ID));
        if (healthFacility == null && createIfNotExists) {
            healthFacility = new HealthFacility();
            healthFacility.setHealthBlock(healthBlock);
            healthFacility.setCode((Long) flw.get(PHC_ID));
            healthFacility.setName((String) flw.get(PHC_NAME));
            healthBlock.getHealthFacilities().add(healthFacility);
            LOGGER.debug(String.format("Created %s in %s with id %d", healthFacility, healthBlock, healthFacility.getId()));
        }
        return healthFacility;
    }

    @Override
    public HealthSubFacility updateSubFacility(Map<String, Object> flw, HealthFacility healthFacility, Boolean createIfNotExists) {

        // set and/or create health sub-facility
        if (!isValidID(flw, SUBCENTRE_ID)) {
            return null;
        }
        HealthSubFacility healthSubFacility = healthSubFacilityService.findByHealthFacilityAndCode(healthFacility, (Long) flw.get(SUBCENTRE_ID));
        if (healthSubFacility == null && createIfNotExists) {
            healthSubFacility = new HealthSubFacility();
            healthSubFacility.setHealthFacility(healthFacility);
            healthSubFacility.setCode((Long) flw.get(SUBCENTRE_ID));
            healthSubFacility.setName((String) flw.get(SUBCENTRE_NAME));
            healthFacility.getHealthSubFacilities().add(healthSubFacility);
            LOGGER.debug(String.format("Created %s in %s with id %d", healthSubFacility, healthFacility, healthSubFacility.getId()));
        }
        return healthSubFacility;
    }

    @Override
    public Village updateVillage(Map<String, Object> flw, Taluka taluka, Boolean createIfNotExists) {

        // set and/or create village
        Long svid = flw.get(NON_CENSUS_VILLAGE) == null ? 0 : (Long) flw.get(NON_CENSUS_VILLAGE);
        Long vcode = flw.get(VILLAGE_ID) == null ? 0 : (Long) flw.get(VILLAGE_ID);
         if (vcode != 0 || svid != 0) {
             Village village = villageService.findByTalukaAndVcodeAndSvid(taluka, vcode, svid);
             if (village == null && createIfNotExists) {
                 village = new Village();
                 village.setSvid(svid);
                 village.setVcode(vcode);
                 village.setTaluka(taluka);
                 village.setName((String) flw.get(VILLAGE_NAME));
                 taluka.getVillages().add(village);
                 LOGGER.debug(String.format("Created %s in %s with id %d", village, taluka, village.getId()));
             }
             return village;
         }
        return null;
    }


    @Override
    public State getState(Long stateId) {

        return stateDataService.findByCode(stateId);
    }

    @Override
    public District getDistrict(Long stateId, Long districtId) {

        State state = getState(stateId);

        if (state != null) {
            return districtService.findByStateAndCode(state, districtId);
        }

        return null;
    }

    @Override
    public Taluka getTaluka(Long stateId, Long districtId, String talukaId) {

        District district = getDistrict(stateId, districtId);

        if (district != null) {
            return talukaService.findByDistrictAndCode(district, talukaId);
        }

        return null;
    }

    @Override
    public Village getVillage(Long stateId, Long districtId, String talukaId, Long vCode, Long svid) {

        Taluka taluka = getTaluka(stateId, districtId, talukaId);

        if (taluka != null) {
            return villageService.findByTalukaAndVcodeAndSvid(taluka, vCode, svid);
        }

        return null;
    }

    @Override
    public Village getCensusVillage(Long stateId, Long districtId, String talukaId, Long vCode) {

        return getVillage(stateId, districtId, talukaId, vCode, 0L);
    }

    @Override
    public Village getNonCensusVillage(Long stateId, Long districtId, String talukaId, Long svid) {

        return getVillage(stateId, districtId, talukaId, 0L, svid);
    }

    @Override
    public HealthBlock getHealthBlock(Long stateId, Long districtId, String talukaId, Long healthBlockId) {

        Taluka taluka = getTaluka(stateId, districtId, talukaId);

        if (taluka != null) {

            return healthBlockService.findByTalukaAndCode(taluka, healthBlockId);
        }

        return null;
    }

    @Override
    public HealthFacility getHealthFacility(Long stateId, Long districtId, String talukaId, Long healthBlockId,
                                            Long healthFacilityId) {

        HealthBlock healthBlock = getHealthBlock(stateId, districtId, talukaId, healthBlockId);

        if (healthBlock != null) {

            return healthFacilityService.findByHealthBlockAndCode(healthBlock, healthFacilityId);
        }

        return null;
    }

    @Override
    public HealthSubFacility getHealthSubFacility(Long stateId, Long districtId, String talukaId,
                                                  Long healthBlockId, Long healthFacilityId,
                                                  Long healthSubFacilityId) {

        HealthFacility healthFacility = getHealthFacility(stateId, districtId, talukaId, healthBlockId,
                healthFacilityId);

        if (healthFacility != null) {

            return healthSubFacilityService.findByHealthFacilityAndCode(healthFacility, healthSubFacilityId);
        }

        return null;
    }

    @Override //NO CHECKSTYLE Cyclomatic Complexity
    public LocationFinder updateLocations(List<Map<String, Object>> recordList) { //NOPMD NcssMethodCount
        int count = 0;

        Map<String, State> stateHashMap = new HashMap<>();
        Map<String, District> districtHashMap = new HashMap<>();
        Map<String, Taluka> talukaHashMap = new HashMap<>();
        Map<String, Village> villageHashMap = new HashMap<>();
        Map<String, HealthBlock> healthBlockHashMap = new HashMap<>();
        Map<String, HealthFacility> healthFacilityHashMap = new HashMap<>();
        Map<String, HealthSubFacility> healthSubFacilityHashMap = new HashMap<>();
        LocationFinder locationFinder = new LocationFinder();
        try {

            for(Map<String, Object> record : recordList) {
                count++;
                StringBuffer mapKey = new StringBuffer(record.get(STATE_ID).toString());
                if (isValidID(record, STATE_ID)) {
                    stateHashMap.put(mapKey.toString(), null);
                    mapKey.append("_");
                    mapKey.append(record.get(DISTRICT_ID).toString());
                    if (isValidID(record, DISTRICT_ID)) {
                        districtHashMap.put(mapKey.toString(), null);

                        if (isValidID(record, TALUKA_ID)) {
                            Taluka taluka = new Taluka();
                            taluka.setCode((String) record.get(TALUKA_ID));
                            taluka.setName((String) record.get(TALUKA_NAME));
                            mapKey.append("_");
                            mapKey.append(Long.parseLong(record.get(TALUKA_ID).toString()));
                            talukaHashMap.put(mapKey.toString(), taluka);

                            Long svid = record.get(NON_CENSUS_VILLAGE) == null ? 0 : (Long) record.get(NON_CENSUS_VILLAGE);
                            Long vcode = record.get(VILLAGE_ID) == null ? 0 : (Long) record.get(VILLAGE_ID);
                            if (vcode != 0 || svid != 0) {
                                Village village = new Village();
                                village.setSvid(svid);
                                village.setVcode(vcode);
                                village.setName((String) record.get(VILLAGE_NAME));
                                villageHashMap.put(mapKey.toString() + "_" + vcode.toString() + "_" +
                                        svid.toString(), village);
                            }

                            mapKey = new StringBuffer(record.get(STATE_ID).toString() + "_" +
                                    record.get(DISTRICT_ID).toString());

                            if (isValidID(record, HEALTHBLOCK_ID)) {
                                HealthBlock healthBlock = new HealthBlock();
                                healthBlock.setCode((Long) record.get(HEALTHBLOCK_ID));
                                healthBlock.setName((String) record.get(HEALTHBLOCK_NAME));
                                mapKey.append("_");
                                mapKey.append((Long) record.get(HEALTHBLOCK_ID));
                                healthBlockHashMap.put(mapKey.toString(), healthBlock);

                                if (isValidID(record, PHC_ID)) {
                                    HealthFacility healthFacility = new HealthFacility();
                                    healthFacility.setCode((Long) record.get(PHC_ID));
                                    healthFacility.setName((String) record.get(PHC_NAME));
                                    mapKey.append("_");
                                    mapKey.append((Long) record.get(PHC_ID));
                                    healthFacilityHashMap.put(mapKey.toString(), healthFacility);

                                    if (isValidID(record, SUBCENTRE_ID)) {
                                        HealthSubFacility healthSubFacility = new HealthSubFacility();
                                        healthSubFacility.setCode((Long) record.get(SUBCENTRE_ID));
                                        healthSubFacility.setName((String) record.get(SUBCENTRE_NAME));
                                        mapKey.append("_");
                                        mapKey.append((Long) record.get(SUBCENTRE_ID));
                                        healthSubFacilityHashMap.put(mapKey.toString(), healthSubFacility);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(String.format("Locations import error, constraints violated: %s",
                    ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
        }
        if (!stateHashMap.isEmpty()) {
            fillStates(stateHashMap);
            locationFinder.setStateHashMap(stateHashMap);

            if (!districtHashMap.isEmpty()) {
                fillDistricts(districtHashMap, stateHashMap);
                locationFinder.setDistrictHashMap(districtHashMap);

                if (!talukaHashMap.isEmpty()) {
                    fillTalukas(talukaHashMap, districtHashMap);
                    locationFinder.setTalukaHashMap(talukaHashMap);

                    if (!villageHashMap.isEmpty()) {
                        fillVillages(villageHashMap, talukaHashMap);
                        locationFinder.setVillageHashMap(villageHashMap);
                    }
                    if (!healthBlockHashMap.isEmpty()) {
                        fillHealthBlocks(healthBlockHashMap, districtHashMap);
                        locationFinder.setHealthBlockHashMap(healthBlockHashMap);

                        if (!healthFacilityHashMap.isEmpty()) {
                            fillHealthFacilities(healthFacilityHashMap, healthBlockHashMap);
                            locationFinder.setHealthFacilityHashMap(healthFacilityHashMap);

                            if (!healthSubFacilityHashMap.isEmpty()) {
                                fillHealthSubFacilities(healthSubFacilityHashMap, healthFacilityHashMap);
                                locationFinder.setHealthSubFacilityHashMap(healthSubFacilityHashMap);
                            }
                        }
                    }
                }
            }
        }
        LOGGER.debug("Locations processed. Records Processed : {}", count);

        return locationFinder;

    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    public void createLocations(Long stateID, LocationEnum locationType, String fileLocation) throws IOException {
        MultipartFile rchImportFile = findByStateId(stateID, locationType.toString(), fileLocation);

        try {
            InputStream in = rchImportFile.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            Map<String, CellProcessor> cellProcessorMapper = null;
            List<Map<String, Object>> recordList;
            switch (locationType) {
                case DISTRICT : cellProcessorMapper = getDistrictMapping(); break;
                case TALUKA : cellProcessorMapper = getTalukaMapping(); break;
                case VILLAGE : cellProcessorMapper = getVillageMapping(); break;
                case HEALTHBLOCK : cellProcessorMapper = getHealthBlockMapping(); break;
                case TALUKAHEALTHBLOCK : cellProcessorMapper = getTalukaHealthBlockMapping(); break;
                case HEALTHFACILITY : cellProcessorMapper = getHealthFacilityMapping(); break;
                case HEALTHSUBFACILITY : cellProcessorMapper = getHealthSubFacilityMapping(); break;
                case VILLAGEHEALTHSUBFACILTY : cellProcessorMapper = getVillageHealthSubFacilityMapping();
            }

            recordList = readCsv(bufferedReader, cellProcessorMapper);

            int count = 0;
            int partNumber = 0;
            Long totalUpdatedRecords = 0L;
            while (count < recordList.size()) {
                List<Map<String, Object>> recordListPart = new ArrayList<>();
                while (recordListPart.size() < LOCATION_PART_SIZE && count < recordList.size()) {
                    recordListPart.add(recordList.get(count));
                    count++;
                }
                partNumber++;
                totalUpdatedRecords += createLocationPart(recordListPart, locationType, rchImportFile.getOriginalFilename(), partNumber);
                recordListPart.clear();
            }
            LOGGER.debug("File {} processed. {} records updated", rchImportFile.getOriginalFilename(), totalUpdatedRecords);
        } catch(NullPointerException e) {
            LOGGER.error("{} File Error", locationType, e);
        }
    }

    public Long createLocationPart(List<Map<String, Object>> recordList, LocationEnum locationType, String rchImportFileName, int partNumber) { //NOPMD NcssMethodCount

        Map<String, State> stateHashMap;
        Map<String, District> districtHashMap;
        Map<String, Taluka> talukaHashMap;
        Map<String, Village> villageHashMap;
        Map<String, HealthBlock> healthBlockHashMap;
        Map<String, HealthFacility> healthFacilityHashMap;
        Map<String, HealthSubFacility> healthSubFacilityHashMap;

        Long updatedRecords = 0L;
        switch (locationType) {
            case DISTRICT :
                stateHashMap = stateService.fillStateIds(recordList);
                updatedRecords = districtService.createUpdateDistricts(recordList, stateHashMap);
                break;

            case TALUKA:
                stateHashMap = stateService.fillStateIds(recordList);
                districtHashMap = districtService.fillDistrictIds(recordList, stateHashMap);
                updatedRecords = talukaService.createUpdateTalukas(recordList, districtHashMap);
                break;

            case VILLAGE:
                stateHashMap = stateService.fillStateIds(recordList);
                districtHashMap = districtService.fillDistrictIds(recordList, stateHashMap);
                talukaHashMap = talukaService.fillTalukaIds(recordList, districtHashMap);
                updatedRecords = villageService.createUpdateVillages(recordList, talukaHashMap);
                break;

            case HEALTHBLOCK:
                stateHashMap = stateService.fillStateIds(recordList);
                districtHashMap = districtService.fillDistrictIds(recordList, stateHashMap);
                talukaHashMap = talukaService.fillTalukaIds(recordList, districtHashMap);
                updatedRecords = healthBlockService.createUpdateHealthBlocks(recordList, districtHashMap, talukaHashMap);
                break;

            case TALUKAHEALTHBLOCK:
                stateHashMap = stateService.fillStateIds(recordList);
                districtHashMap = districtService.fillDistrictIds(recordList, stateHashMap);
                talukaHashMap = talukaService.fillTalukaIds(recordList, districtHashMap);
                healthBlockHashMap = healthBlockService.fillHealthBlockIds(recordList, districtHashMap);
                updatedRecords = healthBlockService.createUpdateTalukaHealthBlock(recordList, healthBlockHashMap, talukaHashMap);
                break;

            case HEALTHFACILITY:
                stateHashMap = stateService.fillStateIds(recordList);
                districtHashMap = districtService.fillDistrictIds(recordList, stateHashMap);
                talukaHashMap = talukaService.fillTalukaIds(recordList, districtHashMap);
                healthBlockHashMap = healthBlockService.fillHealthBlockIds(recordList, districtHashMap);
                updatedRecords = healthFacilityService.createUpdateHealthFacilities(recordList, talukaHashMap, healthBlockHashMap);
                break;

            case HEALTHSUBFACILITY:
                stateHashMap = stateService.fillStateIds(recordList);
                districtHashMap = districtService.fillDistrictIds(recordList, stateHashMap);
                talukaHashMap = talukaService.fillTalukaIds(recordList, districtHashMap);
                healthBlockHashMap = healthBlockService.fillHealthBlockIds(recordList, districtHashMap);
                healthFacilityHashMap = healthFacilityService.fillHealthFacilityIds(recordList, healthBlockHashMap);
                updatedRecords = healthSubFacilityService.createUpdateHealthSubFacilities(recordList, talukaHashMap, healthFacilityHashMap);
                break;

            case VILLAGEHEALTHSUBFACILTY:
                stateHashMap = stateService.fillStateIds(recordList);
                districtHashMap = districtService.fillDistrictIds(recordList, stateHashMap);
                talukaHashMap = talukaService.fillTalukaIds(recordList, districtHashMap);
                healthBlockHashMap = healthBlockService.fillHealthBlockIds(recordList, districtHashMap);
                healthFacilityHashMap = healthFacilityService.fillHealthFacilityIds(recordList, healthBlockHashMap);
                healthSubFacilityHashMap = healthSubFacilityService.fillHealthSubFacilityIds(recordList, healthFacilityHashMap);
                villageHashMap = villageService.fillVillageIds(recordList, talukaHashMap);
                updatedRecords = healthSubFacilityService.createUpdateVillageHealthSubFacility(recordList, healthSubFacilityHashMap, villageHashMap);
                break;

        }
        LOGGER.debug("File {}, Part {} processed. {} records updated", rchImportFileName, partNumber, updatedRecords);
        return updatedRecords;

    }


    private Map<String, CellProcessor> getDistrictMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        mapping.put(STATE_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(DISTRICT_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(DISTRICT_NAME, new org.supercsv.cellprocessor.Optional(new GetString()));

        return mapping;
    }

    private Map<String, CellProcessor> getTalukaMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        mapping.put(STATE_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(TALUKA_ID, new org.supercsv.cellprocessor.Optional(new GetString()));
        mapping.put(TALUKA_NAME, new org.supercsv.cellprocessor.Optional(new GetString()));
        mapping.put(DISTRICT_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));

        return mapping;
    }

    private Map<String, CellProcessor> getVillageMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        mapping.put(STATE_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(DISTRICT_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(TALUKA_ID, new org.supercsv.cellprocessor.Optional(new GetString()));
        mapping.put(NON_CENSUS_VILLAGE, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(VILLAGE_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(VILLAGE_NAME, new org.supercsv.cellprocessor.Optional(new GetString()));

        return mapping;
    }

    private Map<String, CellProcessor> getHealthBlockMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        mapping.put(STATE_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(HEALTHBLOCK_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(HEALTHBLOCK_NAME, new org.supercsv.cellprocessor.Optional(new GetString()));
        mapping.put(DISTRICT_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(TALUKA_ID, new org.supercsv.cellprocessor.Optional(new GetString()));

        return mapping;
    }

    private Map<String, CellProcessor> getTalukaHealthBlockMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        mapping.put(STATE_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(HEALTHBLOCK_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(TALUKA_NAME, new org.supercsv.cellprocessor.Optional(new GetString()));
        mapping.put(TALUKA_ID, new org.supercsv.cellprocessor.Optional(new GetString()));

        return mapping;
    }

    private Map<String, CellProcessor> getHealthFacilityMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        mapping.put(STATE_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(HEALTHFACILITY_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(HEALTHFACILITY_NAME, new org.supercsv.cellprocessor.Optional(new GetString()));
        mapping.put(TALUKA_ID, new org.supercsv.cellprocessor.Optional(new GetString()));
        mapping.put(DISTRICT_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(HEALTHBLOCK_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));

        return mapping;
    }

    private Map<String, CellProcessor> getHealthSubFacilityMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        mapping.put(STATE_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(HEALTHSUBFACILITY_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(HEALTHSUBFACILITY_NAME, new org.supercsv.cellprocessor.Optional(new GetString()));
        mapping.put(TALUKA_ID, new org.supercsv.cellprocessor.Optional(new GetString()));
        mapping.put(DISTRICT_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(HEALTHFACILITY_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));

        return mapping;
    }

    private Map<String, CellProcessor> getVillageHealthSubFacilityMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        mapping.put(STATE_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(HEALTHSUBFACILITY_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(VILLAGE_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(DISTRICT_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));

        return mapping;
    }


    private MultipartFile findByStateId(Long stateId, String locationType, String fileLocation) throws IOException {

        MultipartFile csvFilesByStateIdAndRchUserType = null;
        File file = new File(fileLocation);

        File[] files = file.listFiles();
        if (files != null) {
            for(File f: files){
                String[] fileNameSplitter =  f.getName().split("_");
                if(Objects.equals(fileNameSplitter[1], stateId.toString()) && fileNameSplitter[0].equalsIgnoreCase(locationType)){
                    try {
                        FileInputStream input = new FileInputStream(f);
                        csvFilesByStateIdAndRchUserType = new MockMultipartFile("file",
                                f.getName(), "text/plain", IOUtils.toByteArray(input));
                    } catch (IOException e) {
                        LOGGER.debug("IO Exception", e);
                    }
                }
            }
        }
        return csvFilesByStateIdAndRchUserType;
    }


    private List<Map<String, Object>> readCsv(BufferedReader bufferedReader, Map<String, CellProcessor> cellProcessorMapper) throws IOException {
        int count = 0;

        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(cellProcessorMapper)
                .setPreferences(CsvPreference.TAB_PREFERENCE)
                .createAndOpen(bufferedReader);

        List<Map<String, Object>> recordList = new ArrayList<>();
        Map<String, Object> record;
        while (null != (record = csvImporter.read())) {
            recordList.add(record);
            count++;
        }
        LOGGER.debug("{} records added to object", count);
        return recordList;
    }


    /**
     * Fills the stateHashMap with State objects from database
     * @param stateHashMap contains (stateCode, State) with dummy State objects
     */
    private void fillStates(Map<String, State> stateHashMap) {
        Timer queryTimer = new Timer();
        final Set<String> stateKeys = stateHashMap.keySet();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<State>> queryExecution = new SqlQueryExecution<List<State>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT * from nms_states where";
                int count = stateKeys.size();
                for (String stateString : stateKeys) {
                    count--;
                    query += " code = " + stateString;
                    if (count > 0) {
                        query += OR_SQL_STRING;
                    }
                }

                LOGGER.debug("STATE Query: {}", query);
                return query;
            }

            @Override
            public List<State> execute(Query query) {
                query.setClass(State.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<State> states;
                if (fqr.isEmpty()) {
                    return null;
                }
                states = (List<State>) fqr;
                return states;
            }
        };

        List<State> states = stateDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("STATE Query time: {}", queryTimer.time());
        for (State state : states) {
            stateHashMap.put(state.getCode().toString(), state);
        }
    }

    /**
     * Fills districtHashMap with District objects from database
     * @param districtHashMap contains (stateCode_districtCode, District) with dummy District objects
     * @param stateHashMap contains (stateCode, State) with original State objects from database
     */
    private void fillDistricts(Map<String, District> districtHashMap, final Map<String, State> stateHashMap) {
        Timer queryTimer = new Timer();
        final Set<String> districtKeys = districtHashMap.keySet();
        Map<Long, String> stateIdMap = new HashMap<>();
        for (String stateKey : stateHashMap.keySet()) {
            stateIdMap.put(stateHashMap.get(stateKey).getId(), stateKey);
        }


        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<District>> queryExecution = new SqlQueryExecution<List<District>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT * from nms_districts where";
                int count = districtKeys.size();
                for (String districtString : districtKeys) {
                    count--;
                    String[] ids = districtString.split("_");
                    Long stateId = stateHashMap.get(ids[0]).getId();
                    query += CODE_SQL_STRING + ids[1] + " and state_id_oid = " + stateId + ")";
                    if (count > 0) {
                        query += OR_SQL_STRING;
                    }
                }

                LOGGER.debug("DISTRICT Query: {}", query);
                return query;
            }

            @Override
            public List<District> execute(Query query) {
                query.setClass(District.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<District> districts;
                if (fqr.isEmpty()) {
                    return null;
                }
                districts = (List<District>) fqr;
                return districts;
            }
        };

        List<District> districts = districtDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("DISTRICT Query time: {}", queryTimer.time());
        for (District district : districts) {
            String stateKey = stateIdMap.get(district.getState().getId());
            districtHashMap.put(stateKey + "_" + district.getCode(), district);
        }


    }


    /**
     * Fills talukaHashMap with Taluka objects from the database
     * @param talukaHashMap contains (stateCode_districtCode_talukaCode, Taluka) with dummy Taluka objects
     * @param districtHashMap contains (stateCode_districtCode, District) with original District objects from database
     */
    private void fillTalukas(Map<String, Taluka> talukaHashMap, final Map<String, District> districtHashMap) {
        Timer queryTimer = new Timer();
        final Set<String> talukaKeys = talukaHashMap.keySet();
        Map<Long, String> districtIdMap = new HashMap<>();
        for (String districtKey : districtHashMap.keySet()) {
            districtIdMap.put(districtHashMap.get(districtKey).getId(), districtKey);
        }

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<Taluka>> queryExecution = new SqlQueryExecution<List<Taluka>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT * from nms_talukas where";
                int count = talukaKeys.size();
                for (String talukaString : talukaKeys) {
                    count--;
                    String[] ids = talukaString.split("_");
                    Long districtId = districtHashMap.get(ids[0] + "_" + ids[1]).getId();
                    query += CODE_SQL_STRING + ids[2] + " and district_id_oid = " + districtId + ")";
                    if (count > 0) {
                        query += OR_SQL_STRING;
                    }
                }

                LOGGER.debug("TALUKA Query: {}", query);
                return query;
            }

            @Override
            public List<Taluka> execute(Query query) {
                query.setClass(Taluka.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<Taluka> talukas;
                if (fqr.isEmpty()) {
                    return null;
                }
                talukas = (List<Taluka>) fqr;
                return talukas;
            }
        };

        List<Taluka> talukas = talukaDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("TALUKA Query time: {}", queryTimer.time());
        if(talukas != null && !talukas.isEmpty()) {
            for (Taluka taluka : talukas) {
                String districtKey = districtIdMap.get(taluka.getDistrict().getId());
                talukaHashMap.put(districtKey + "_" + Long.parseLong(taluka.getCode()), taluka);
            }
        }
    }


    /**
     * Fills villageHashMap with Village objects from database
     * @param villageHashMap contains (stateCode_districtCode_talukaCode_villageCode_Svid, Village) with dummy Village objects
     * @param talukaHashMap contains (stateCode_districtCode_talukaCode, Taluka) with original Taluka objects from database
     */
    private void fillVillages(Map<String, Village> villageHashMap, final Map<String, Taluka> talukaHashMap) {
        Timer queryTimer = new Timer();
        final Set<String> villageKeys = villageHashMap.keySet();
        Map<Long, String> talukaIdMap = new HashMap<>();
        for (String districtKey : talukaHashMap.keySet()) {
            talukaIdMap.put(talukaHashMap.get(districtKey).getId(), districtKey);
        }

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<Village>> queryExecution = new SqlQueryExecution<List<Village>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT * from nms_villages where";
                int count = villageKeys.size();
                for (String villageString : villageKeys) {
                    count--;
                    String[] ids = villageString.split("_");
                    Long talukaId = talukaHashMap.get(ids[0] + "_" + ids[1] + "_" + ids[2]).getId();
                    query += " (vcode = " + ids[3] + " and svid = " + ids[4] + " and taluka_id_oid = " + talukaId + ")";
                    if (count > 0) {
                        query += OR_SQL_STRING;
                    }
                }

                LOGGER.debug("VILLAGE Query: {}", query);
                return query;
            }

            @Override
            public List<Village> execute(Query query) {
                query.setClass(Village.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<Village> villages;
                if (fqr.isEmpty()) {
                    return null;
                }
                villages = (List<Village>) fqr;
                return villages;
            }
        };

        List<Village> villages = villageDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("VILLAGE Query time: {}", queryTimer.time());
        if(villages != null && !villages.isEmpty()) {
            for (Village village : villages) {
                String talukaKey = talukaIdMap.get(village.getTaluka().getId());
                villageHashMap.put(talukaKey + "_" + village.getVcode() + "_" + village.getSvid(), village);
            }
        }
    }

    /**
     * Fills healthBlockHashMap with HealthBlock objects from database
     * @param healthBlockHashMap contains (stateCode_districtCode_healthBlockCode, HealthBlock) with dummy HealthBlock objects
     * @param districtHashMap contains (stateCode_districtCode, District) with original District objects from database
     */
    private void fillHealthBlocks(Map<String, HealthBlock> healthBlockHashMap, final Map<String, District> districtHashMap) {
        Timer queryTimer = new Timer();
        final Set<String> healthBlockKeys = healthBlockHashMap.keySet();
        Map<Long, String> districtIdMap = new HashMap<>();
        for (String districtKey : districtHashMap.keySet()) {
            districtIdMap.put(districtHashMap.get(districtKey).getId(), districtKey);
        }

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<HealthBlock>> queryExecution = new SqlQueryExecution<List<HealthBlock>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT * from nms_health_blocks where";
                int count = healthBlockKeys.size();
                for (String healthBlockString : healthBlockKeys) {
                    count--;
                    String[] ids = healthBlockString.split("_");
                    Long districtId = districtHashMap.get(ids[0] + "_" + ids[1]).getId();
                    query += "(code = " + ids[2] +  " and district_id_OID = " + districtId + ")";
                    if (count > 0) {
                        query += OR_SQL_STRING;
                    }
                }

                LOGGER.debug("HEALTHBLOCK Query: {}", query);
                return query;
            }

            @Override
            public List<HealthBlock> execute(Query query) {
                query.setClass(HealthBlock.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<HealthBlock> healthBlocks;
                if (fqr.isEmpty()) {
                    return null;
                }
                healthBlocks = (List<HealthBlock>) fqr;
                return healthBlocks;
            }
        };

        List<HealthBlock> healthBlocks = healthBlockDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("HEALTHBLOCK Query time: {}", queryTimer.time());
        if(healthBlocks != null && !healthBlocks.isEmpty()) {
            for (HealthBlock healthBlock : healthBlocks) {
                    String districtKey = districtIdMap.get(healthBlock.getDistrict().getId());
                    healthBlockHashMap.put(districtKey + "_" + healthBlock.getCode(), healthBlock);
            }
        }
    }


    /**
     * Fills healthFacilityHashMap with HealthFacility objects from the database
     * @param healthFacilityHashMap contains (stateCode_districtCode_healthBlockCode_healthFacilityCode, HealthFacility)
     *                              with dummy HealthFacility objects
     * @param healthBlockHashMap contains (stateCode_districtCode_healthBlockCode, HealthBlock)
     *                           with original HealthBlock objects from database
     */
    private void fillHealthFacilities(Map<String, HealthFacility> healthFacilityHashMap, final Map<String, HealthBlock> healthBlockHashMap) {
        Timer queryTimer = new Timer();
        final Set<String> healthFacilityKeys = healthFacilityHashMap.keySet();
        Map<Long, String> healthBlockIdMap = new HashMap<>();
        for (String healthBlockKey : healthBlockHashMap.keySet()) {
            healthBlockIdMap.put(healthBlockHashMap.get(healthBlockKey).getId(), healthBlockKey);
        }

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<HealthFacility>> queryExecution = new SqlQueryExecution<List<HealthFacility>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT * from nms_health_facilities where";
                int count = healthFacilityKeys.size();
                for (String healthFacilityString : healthFacilityKeys) {
                    count--;
                    String[] ids = healthFacilityString.split("_");
                    Long healthBlockId = healthBlockHashMap.get(ids[0] + "_" + ids[1] + "_" + ids[2]).getId();
                    query += CODE_SQL_STRING + ids[3] +  " and healthBlock_id_oid = " + healthBlockId + ")";
                    if (count > 0) {
                        query += OR_SQL_STRING;
                    }
                }

                LOGGER.debug("HEALTHFACILITY Query: {}", query);
                return query;
            }

            @Override
            public List<HealthFacility> execute(Query query) {
                query.setClass(HealthFacility.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<HealthFacility> healthFacilities;
                if (fqr.isEmpty()) {
                    return null;
                }
                healthFacilities = (List<HealthFacility>) fqr;
                return healthFacilities;
            }
        };

        List<HealthFacility> healthFacilities = healthFacilityDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("HEALTHFACILITY Query time: {}", queryTimer.time());
        if(healthFacilities != null && !healthFacilities.isEmpty()) {
            for (HealthFacility healthFacility : healthFacilities) {
                String healthBlockKey = healthBlockIdMap.get(healthFacility.getHealthBlock().getId());
                healthFacilityHashMap.put(healthBlockKey + "_" + healthFacility.getCode(), healthFacility);
            }
        }
    }


    /**
     * Fills healthSubFacilityHashMap with HealthSubFacility objects from the database
     * @param healthSubFacilityHashMap contains (stateCode_districtCode_healthBlockCode_healthFacilityCode_healthSubFacilityCode, HealthSubFacility)
     *                              with dummy HealthSubFacility objects
     * @param healthFacilityHashMap contains (stateCode_districtCode_healthBlockCode_healthFacilityCode, HealthFacility)
     *                           with original HealthFacility objects from database
     */
    private void fillHealthSubFacilities(Map<String, HealthSubFacility> healthSubFacilityHashMap, final Map<String, HealthFacility> healthFacilityHashMap) {
        Timer queryTimer = new Timer();
        final Set<String> healthSubFacilityKeys = healthSubFacilityHashMap.keySet();
        Map<Long, String> healthFacilityIdMap = new HashMap<>();
        for (String healthFacilityKey : healthFacilityHashMap.keySet()) {
            healthFacilityIdMap.put(healthFacilityHashMap.get(healthFacilityKey).getId(), healthFacilityKey);
        }

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<HealthSubFacility>> queryExecution = new SqlQueryExecution<List<HealthSubFacility>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT * from nms_health_sub_facilities where";
                int count = healthSubFacilityKeys.size();
                for (String healthFacilityString : healthSubFacilityKeys) {
                    count--;
                    String[] ids = healthFacilityString.split("_");
                    Long healthFacilityId = healthFacilityHashMap.get(ids[0] + "_" + ids[1] + "_" + ids[2] + "_" + ids[3]).getId();
                    query += CODE_SQL_STRING + ids[4] +  " and healthFacility_id_oid = " + healthFacilityId + ")";
                    if (count > 0) {
                        query += OR_SQL_STRING;
                    }
                }

                LOGGER.debug("HEALTHSUBFACILITY Query: {}", query);
                return query;
            }

            @Override
            public List<HealthSubFacility> execute(Query query) {
                query.setClass(HealthSubFacility.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<HealthSubFacility> healthSubFacilities;
                if (fqr.isEmpty()) {
                    return null;
                }
                healthSubFacilities = (List<HealthSubFacility>) fqr;
                return healthSubFacilities;
            }
        };

        List<HealthSubFacility> healthSubFacilities = healthSubFacilityDataService.executeSQLQuery(queryExecution);
        LOGGER.debug("HEALTHSUBFACILITY Query time: {}", queryTimer.time());
        if(healthSubFacilities != null && !healthSubFacilities.isEmpty()) {
            for (HealthSubFacility healthSubFacility : healthSubFacilities) {
                String healthFacilityKey = healthFacilityIdMap.get(healthSubFacility.getHealthFacility().getId());
                healthSubFacilityHashMap.put(healthFacilityKey + "_" + healthSubFacility.getCode(), healthSubFacility);
            }
        }
    }

}
