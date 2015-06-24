package org.motechproject.nms.region.csv.impl;

import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetBoolean;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.csv.LanguageLocationImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service("languageLocationImportService")
public class LanguageLocationImportServiceImpl implements LanguageLocationImportService {

    public static final Logger LOGGER = LoggerFactory.getLogger(LanguageLocationImportServiceImpl.class);

    public static final String LANGUAGE_CODE = "languagelocation code";
    public static final String LANGUAGE_NAME = "Language";
    public static final String CIRCLE = "Circle";
    public static final String STATE = "State";
    public static final String DISTRICT = "District";
    public static final String DEFAULT_FOR_CIRCLE = "Default Language for Circle (Y/N)";

    private LanguageDataService languageDataService;
    private CircleDataService circleDataService;
    private StateDataService stateDataService;
    private DistrictDataService districtDataService;
    private DistrictService districtService;

    @Override
    @Transactional
    public void importData(Reader reader) throws IOException {
        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(getProcessorMapping())
                .createAndOpen(reader);
        Map<String, Object> record;
        while (null != (record = csvImporter.read())) {
            try {
                importRecord(record);
            } catch (ConstraintViolationException e) {
                throw new CsvImportDataException(String.format("CSV import error, constraints violated: %s",
                        ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
            }
        }
    }

    private void importRecord(Map<String, Object> record) {
        String languageCode = (String) record.get(LANGUAGE_CODE);
        String languageName = (String) record.get(LANGUAGE_NAME);
        State state = (State) record.get(STATE);
        if (state == null) {
            throw new CsvImportDataException("State must be provided");
        }

        District district = null;
        if (record.get(DISTRICT) != null) {
            district = districtService.findByStateAndName(state, (String) record.get(DISTRICT));
            if (district == null) {
                throw new CsvImportDataException(String.format("District %s doesn't exist in state %s",
                        (String) record.get(DISTRICT), state.getName()));
            }
        }
        Circle circle = (Circle) record.get(CIRCLE);
        Boolean defaultForCircle = (Boolean) record.get(DEFAULT_FOR_CIRCLE);

        if (state != null && district != null) {
            importRecordForStateAndDistrict(languageCode, languageName, circle, defaultForCircle, state, district);
        } else if (state != null) {
            importRecordForState(languageCode, languageName, circle, defaultForCircle, state);
        } else if (district != null) {
            importRecordForDistrict(languageCode, languageName, circle, defaultForCircle, district);
        }
    }

    private void importRecordForStateAndDistrict(String languageCode, String languageName, Circle circle,
                                                 Boolean defaultForCircle, State state, District district) {
        verify(Objects.equals(district.getState().getCode(), state.getCode()),
                "District's state does not match the state supplied in the CSV record");
        verifyStateInCircle(circle, state);
        verifyDistrictLanguage(district);
        addDistrictToLanguageLocation(languageCode, languageName, circle, defaultForCircle, Collections.singletonList(district));
    }

    private void importRecordForState(String languageCode, String languageName, Circle circle,
                                      Boolean defaultForCircle, State state) {
        verifyStateInCircle(circle, state);
        for (District stateDistrict : state.getDistricts()) {
            verifyDistrictLanguage(stateDistrict);
        }
        addDistrictToLanguageLocation(languageCode, languageName, circle, defaultForCircle, state.getDistricts());
    }

    private void importRecordForDistrict(String languageCode, String languageName, Circle circle,
                                         Boolean defaultForCircle, District district) {
        LOGGER.warn("State not specified for the '{}' district", district.getName());
        verifyStateInCircle(circle, district.getState());
        verifyDistrictLanguage(district);
        addDistrictToLanguageLocation(languageCode, languageName, circle, defaultForCircle, Collections.singletonList(district));
    }

    private void addDistrictToLanguageLocation(String languageCode, String languageName, Circle circle, Boolean defaultForCircle, Collection<District> districts) {
        Language language = languageDataService.findByCode(languageCode);
        if (language == null) {
            language = languageDataService.create(new Language(languageCode, languageName));
        }

        verify(Objects.equals(language.getName(), languageName),
                "Language name provided '%s' does not match record in database",
                languageName);

        for (District district: districts) {
            district.setLanguage(language);
            districtDataService.update(district);
        }

        if (defaultForCircle) {
            if (circle.getDefaultLanguage() != null) {
                verify(Objects.equals(circle.getDefaultLanguage(), language),
                        "Existing default language for circle '%s' does not match language provided in CSV record",
                        circle.getDefaultLanguage().getCode());
            } else {
                circle.setDefaultLanguage(language);
                circleDataService.update(circle);
            }
        }
    }

    private void verifyStateInCircle(Circle circle, State state) {
        verify(circle.getStates().contains(state), "State is not contained in the supplied circle");
    }

    private void verifyDistrictLanguage(District district) {
        verify(null == district.getLanguage(), "Language location for the '%s' district already specified", district.getName());
    }

    private Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(LANGUAGE_CODE, new GetString());
        mapping.put(LANGUAGE_NAME, new GetString());
        mapping.put(CIRCLE, new GetInstanceByString<Circle>() {
            @Override
            public Circle retrieve(String value) {
                Circle circle = circleDataService.findByName(value);
                verify(null != circle, "Circle does not exist");
                return circle;
            }
        });
        mapping.put(STATE, new Optional(new GetInstanceByString<State>() {
            @Override
            public State retrieve(String value) {
                State state = stateDataService.findByName(value);
                verify(null != state, "State does not exist");
                return state;
            }
        }));
        mapping.put(DISTRICT, new Optional(new GetString()));
        mapping.put(DEFAULT_FOR_CIRCLE, new GetBoolean());
        return mapping;
    }

    private void verify(boolean condition, String message, String... args) {
        if (!condition) {
            throw new CsvImportDataException(String.format(message, args));
        }
    }

    @Autowired
    public void setLanguageDataService(LanguageDataService languageDataService) {
        this.languageDataService = languageDataService;
    }

    @Autowired
    public void setCircleDataService(CircleDataService circleDataService) {
        this.circleDataService = circleDataService;
    }

    @Autowired
    public void setStateDataService(StateDataService stateDataService) {
        this.stateDataService = stateDataService;
    }

    @Autowired
    public void setDistrictDataService(DistrictDataService districtDataService) {
        this.districtDataService = districtDataService;
    }

    @Autowired
    public void setDistrictService(DistrictService districtService) {
        this.districtService = districtService;
    }
}
