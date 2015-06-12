package org.motechproject.nms.flw.service.impl;

import org.apache.commons.lang.StringUtils;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.service.FrontLineWorkerImportService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.StateDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.prefs.CsvPreference;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service("frontLineWorkerImportService")
public class FrontLineWorkerImportServiceImpl implements FrontLineWorkerImportService {

    public static final String ID = "ID";
    public static final String CONTACT_NO = "Contact_No";
    public static final String NAME = "Name";
    public static final String DISTRICT_ID = "District_ID";

    private FrontLineWorkerService frontLineWorkerService;
    private StateDataService stateDataService;

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
                frontLineWorkerService.add(processInstance(record, state));
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(createErrorMessage(e.getConstraintViolations(), csvImporter.getRowNumber()), e);
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


    private FrontLineWorker processInstance(Map<String, Object> record, State state) {
        String mctsFlwId = (String) record.get(ID);
        Long contactNumber = (Long) record.get(CONTACT_NO);
        String name = (String) record.get(NAME);
        Long districtId = (Long) record.get(DISTRICT_ID);
        District district = getDistrictById(state, districtId);

        FrontLineWorker instance = new FrontLineWorker(contactNumber);
        instance.setMctsFlwId(mctsFlwId);
        instance.setName(name);
        instance.setDistrict(district);
        if (null != district) {
            instance.setLanguage(district.getLanguage());
        }
        return instance;
    }

    private District getDistrictById(State state, Long districtId) {
        if (null == districtId) {
            return null;
        } else {
            for (District district : state.getDistricts()) {
                if (Objects.equals(district.getCode(), districtId)) {
                    return district;
                }
            }
            throw new CsvImportDataException("District does not exists");
        }
    }

    private Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(ID, new GetString());
        mapping.put(CONTACT_NO, new GetLong());
        mapping.put(NAME, new GetString());
        mapping.put(DISTRICT_ID, new Optional(new GetLong()));
        return mapping;
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
}
