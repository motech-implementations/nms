package org.motechproject.nms.flw.service.impl;

import org.apache.commons.lang.StringUtils;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.flw.domain.FrontLineWorker;

import org.motechproject.nms.flw.exception.FlwImportException;
import org.motechproject.nms.flw.service.FrontLineWorkerImportService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.utils.FlwConstants;
import org.motechproject.nms.props.service.LogHelper;
import org.motechproject.nms.region.domain.State;
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

import static org.motechproject.nms.flw.utils.FlwMapper.createFlw;
import static org.motechproject.nms.flw.utils.FlwMapper.updateFlw;

@Service("frontLineWorkerImportService")
public class FrontLineWorkerImportServiceImpl implements FrontLineWorkerImportService {

    private FrontLineWorkerService frontLineWorkerService;
    private StateDataService stateDataService;
    private LocationService locationService;

    /*
        Expected file format:
        * any number of empty lines
        * first non blank line to contain state name in the following format:  State Name : ACTUAL STATE_ID NAME
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
                importFrontLineWorker(record, state);
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(createErrorMessage(e.getConstraintViolations(), csvImporter.getRowNumber()), e);
        } catch (InvalidLocationException | FlwImportException | JDODataStoreException e) {
            throw new CsvImportDataException(createErrorMessage(e.getMessage(), csvImporter.getRowNumber()), e);
        }
    }

    @Override
    @Transactional
    public void importFrontLineWorker(Map<String, Object> record, State state) throws InvalidLocationException {
        FrontLineWorker flw = flwFromRecord(record, state);

        record.put(FlwConstants.STATE_ID, state.getCode());
        Map<String, Object> location = locationService.getLocations(record);

        if (flw == null) {
            frontLineWorkerService.add(createFlw(record, location));
        } else {
            frontLineWorkerService.update(updateFlw(flw, record, location));
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

        String mctsFlwId = (String) record.get(FlwConstants.ID);
        Long msisdn = (Long) record.get(FlwConstants.CONTACT_NO);

        if (mctsFlwId != null) {
            flw = frontLineWorkerService.getByMctsFlwIdAndState(mctsFlwId, state);
        }

        if (flw == null && msisdn != null) {
            flw = frontLineWorkerService.getByContactNumber(msisdn);

            // If we loaded the flw by msisdn but the flw we found has a different mcts id
            // then the data needs to be hand corrected since we don't know if the msisdn has changed or
            // if the mcts id has changed.
            if (flw != null && mctsFlwId != null && flw.getMctsFlwId() != null && !mctsFlwId.equals(flw.getMctsFlwId())) {
                throw new CsvImportDataException(String.format("Existing FLW with same MSISDN (%s) but " +
                                        "different MCTS ID (%s != %s)", LogHelper.obscure(msisdn), mctsFlwId, flw.getMctsFlwId()));
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

    private Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(FlwConstants.ID, new GetString());
        mapping.put(FlwConstants.CONTACT_NO, new GetLong());
        mapping.put(FlwConstants.NAME, new GetString());

        mapping.put(FlwConstants.STATE_ID, new Optional(new GetLong()));

        mapping.put(FlwConstants.DISTRICT_ID, new Optional(new GetLong()));
        mapping.put(FlwConstants.DISTRICT_NAME, new Optional(new GetString()));

        mapping.put(FlwConstants.TALUKA_ID, new Optional(new GetString()));
        mapping.put(FlwConstants.TALUKA_NAME, new Optional(new GetString()));

        mapping.put(FlwConstants.HEALTH_BLOCK_ID, new Optional(new GetLong()));
        mapping.put(FlwConstants.HEALTH_BLOCK_NAME, new Optional(new GetString()));

        mapping.put(FlwConstants.PHC_ID, new Optional(new GetLong()));
        mapping.put(FlwConstants.PHC_NAME, new Optional(new GetString()));

        mapping.put(FlwConstants.SUB_CENTRE_ID, new Optional(new GetLong()));
        mapping.put(FlwConstants.SUB_CENTRE_NAME, new Optional(new GetString()));

        mapping.put(FlwConstants.CENSUS_VILLAGE_ID, new Optional(new GetLong()));
        mapping.put(FlwConstants.NON_CENSUS_VILLAGE_ID, new Optional(new GetLong()));
        mapping.put(FlwConstants.VILLAGE_NAME, new Optional(new GetString()));

        mapping.put(FlwConstants.TYPE, new Optional(new GetString()));

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
