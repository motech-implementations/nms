package org.motechproject.nms.flw.service.impl;

import org.apache.commons.lang.StringUtils;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.service.FrontLineWorkerImportService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.prefs.CsvPreference;

import javax.jdo.JDODataStoreException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service("frontLineWorkerImportService")
public class FrontLineWorkerImportServiceImpl implements FrontLineWorkerImportService {

    public static final String ID = "ID";
    public static final String CONTACT_NO = "Contact_No";
    public static final String NAME = "Name";
    private static final String STATE = "StateID";
    public static final String DISTRICT_ID = "District_ID";
    private static final String TALUKA = "Taluka_ID";
    private static final String HEALTH_BLOCK = "HealthBlock_ID";
    private static final String PHC = "PHC_ID";
    private static final String SUBCENTRE = "SubCentre_ID";
    private static final String CENSUS_VILLAGE = "Village_ID";
    private static final String NON_CENSUS_VILAGE = "SVID";


    private FrontLineWorkerService frontLineWorkerService;
    private StateDataService stateDataService;
    private LocationService locationService;

    /*
        Expected file format:
        * any number of empty lines
        * first non blank line to contain state name in the following format:  State Name : ACTUAL STATE NAME
        * any number of additional header lines
        * one empty line
        * CSV data (tab-separated)
     */
    @Override
    @Transactional
    public void importData(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);

        State state = importHeader(bufferedReader);

        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(getProcessorMapping())
                .setPreferences(CsvPreference.TAB_PREFERENCE)
                .createAndOpen(bufferedReader);
        try {
            Map<String, Object> record;
            while (null != (record = csvImporter.read())) {

                FrontLineWorker flw = flwFromRecord(record, state);

                record.put(STATE, state.getCode());
                Map<String, Object> location = locationService.getLocations(record);

                if (flw == null) {
                    frontLineWorkerService.add(processInstance(record, location));
                } else {
                    frontLineWorkerService.update(processInstance(flw, record, location));
                }
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(createErrorMessage(e.getConstraintViolations(), csvImporter.getRowNumber()), e);
        } catch (InvalidLocationException | CsvImportDataException | JDODataStoreException e) {
            throw new CsvImportDataException(createErrorMessage(e.getMessage(), csvImporter.getRowNumber()), e);
        }
    }

    private State importHeader(BufferedReader bufferedReader) throws IOException {
        String line = readLineWhileBlank(bufferedReader);
        // expect state name in the first line
        if (line.matches("^State Name : .*$")) {
            String stateName = line.substring(line.indexOf(':') + 1).trim();
            State state = stateDataService.findByName(stateName);
            verify(null != state, "State does not exists");
            readLineWhileNotBlank(bufferedReader);
            return state;
        } else {
            throw new IllegalArgumentException("Invalid file format");
        }
    }

    private FrontLineWorker flwFromRecord(Map<String, Object> record, State state) {
        FrontLineWorker flw = null;

        String mctsFlwId = (String) record.get(ID);
        Long msisdn = (Long) record.get(CONTACT_NO);

        if (mctsFlwId != null) {
            flw = frontLineWorkerService.getByMctsFlwIdAndState(mctsFlwId, state);
        }

        if (flw == null && msisdn != null) {
            flw = frontLineWorkerService.getByContactNumber(msisdn);

            // If we loaded the flw by msisdn but the flw we found has a different mcts id
            // then the data needs to be hand corrected since we don't know if the msisdn has changed or
            // if the mcts id has changed.
            if (flw != null && mctsFlwId != null && !mctsFlwId.equals(flw.getMctsFlwId())) {
                throw new CsvImportDataException(String.format("Existing FLW with same MSISDN (%s) but " +
                                        "different MCTS ID (%s != %s)", msisdn, mctsFlwId, flw.getMctsFlwId()));
            }
        }

        return flw;
    }

    private String readLineWhileBlank(BufferedReader bufferedReader) throws IOException {
        String line;
        do {
            line = bufferedReader.readLine();
        } while (null != line && StringUtils.isBlank(line));
        return line;
    }

    private String readLineWhileNotBlank(BufferedReader bufferedReader) throws IOException {
        String line;
        do {
            line = bufferedReader.readLine();
        } while (null != line && StringUtils.isNotBlank(line));
        return line;
    }


    private FrontLineWorker processInstance(Map<String, Object> record, Map<String, Object> location)
            throws InvalidLocationException {
        Long contactNumber = (Long) record.get(CONTACT_NO);

        FrontLineWorker flw = new FrontLineWorker(contactNumber);
        flw.setStatus(FrontLineWorkerStatus.INACTIVE);

        return processInstance(flw, record, location);
    }

    private FrontLineWorker processInstance(FrontLineWorker flw, Map<String, Object> record,
                                            Map<String, Object> location) throws InvalidLocationException {

        String mctsFlwId = (String) record.get(ID);
        Long contactNumber = (Long) record.get(CONTACT_NO);
        String name = (String) record.get(NAME);

        if (contactNumber != null) {
            flw.setContactNumber(contactNumber);
        }

        if (mctsFlwId != null) {
            flw.setMctsFlwId(mctsFlwId);
        }

        if (name != null) {
            flw.setName(name);
        }

        setFrontLineWorkerLocation(flw, location);

        if (flw.getLanguage() == null) {
            flw.setLanguage(flw.getDistrict().getLanguage());
        }

        return flw;
    }

    private void setFrontLineWorkerLocation(FrontLineWorker flw, Map<String, Object> locations) throws InvalidLocationException {
        if (locations.get(STATE) == null && locations.get(DISTRICT_ID) == null) {
            throw new InvalidLocationException("Missing mandatory state and district fields");
        }

        if (locations.get(STATE) == null) {
            throw new InvalidLocationException("Missing mandatory state field");
        }

        if (locations.get(DISTRICT_ID) == null) {
            throw new InvalidLocationException("Missing mandatory district field");
        }

        flw.setState((State) locations.get(STATE));
        flw.setDistrict((District) locations.get(DISTRICT_ID));
        flw.setTaluka((Taluka) locations.get(TALUKA));
        flw.setHealthBlock((HealthBlock) locations.get(HEALTH_BLOCK));
        flw.setHealthFacility((HealthFacility) locations.get(PHC));
        flw.setHealthSubFacility((HealthSubFacility) locations.get(SUBCENTRE));
        flw.setVillage((Village) locations.get(CENSUS_VILLAGE + NON_CENSUS_VILAGE));
    }

    private Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(ID, new GetString());
        mapping.put(CONTACT_NO, new GetLong());
        mapping.put(NAME, new GetString());
        mapping.put(DISTRICT_ID, new Optional(new GetLong()));
        mapping.put(TALUKA, new Optional(new GetString()));
        mapping.put(HEALTH_BLOCK, new Optional(new GetLong()));
        mapping.put(PHC, new Optional(new GetLong()));
        mapping.put(SUBCENTRE, new Optional(new GetLong()));
        mapping.put(CENSUS_VILLAGE, new Optional(new GetLong()));
        mapping.put(NON_CENSUS_VILAGE, new Optional(new GetLong()));

        return mapping;
    }

    private String createErrorMessage(String message, int rowNumber) {
        return String.format("CSV instance error [row: %d]: %s", rowNumber, message);
    }

    private String createErrorMessage(Set<ConstraintViolation<?>> violations, int rowNumber) {
        return String.format("CSV instance error [row: %d]: validation failed for instance of type %s, violations: %s",
                rowNumber, FrontLineWorker.class.getName(), ConstraintViolationUtils.toString(violations));
    }

    private void verify(boolean condition, String message) {
        if (!condition) {
            throw new CsvImportDataException(message);
        }
    }

    @Autowired
    public void setFrontLineWorkerService(FrontLineWorkerService frontLineWorkerService) {
        this.frontLineWorkerService = frontLineWorkerService;
    }

    @Autowired
    public void setStateDataService(StateDataService stateDataService) {
        this.stateDataService = stateDataService;
    }

    @Autowired
    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }
}
